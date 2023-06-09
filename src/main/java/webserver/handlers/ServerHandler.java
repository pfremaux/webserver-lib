package webserver.handlers;

import com.sun.net.httpserver.*;
import tools.*;
import tools.security.SimpleSecretHandler;
import webserver.ServerProperties;
import webserver.generators.DocumentedEndpoint;
import webserver.generators.EndpointGenerator;
import webserver.generators.JsGenerator;
import webserver.handlers.web.SelfDescribeHandler;
import webserver.handlers.web.ServeFileHandler;
import webserver.handlers.web.auth.AuthenticationHandler;
import webserver.handlers.web.auth.DefaultTokenFields;
import webserver.handlers.web.auth.TokenStructure;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MdDoc(description = "Entry point for the application that's using a webserver. You need to call the static method runServer(..).")
public class ServerHandler {
    private ServerHandler() {
    }

    @MdDoc(description = "Main method you need to call in order to start the server.")
    public static void runServer(
            @MdDoc(description = "Command line parameters passed by the runner.")
            String[] args,
            @MdDoc(description = "This HTTP handler will validate the caller's authentication.")
            AuthenticationHandler authenticationHandler
    ) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final Set<String> providedParameters = Stream.of(args).collect(Collectors.toSet());
        final CliParameterLoader cliLoader = getCliParameterLoader();
        final Map<String, String> parameters = cliLoader.load(args);
        // We need to process the config file FIRST and the command line after as some command line
        // might require some settings loaded in the config file.
        ConfigHandler.processConfigFile(providedParameters, parameters);
        ConfigHandler.processCommandLineParameters(providedParameters, parameters);

        // These parameters shouldn't be defined in ServerProperties as it's related to passwords, it's a bit sensitive.
        final char[] ksPass = Optional.ofNullable(parameters.get("--ksPass")).map(String::toCharArray).orElse(null);
        final char[] tokenPass = parameters.getOrDefault("--tokenPass", "CHANGEME").toCharArray();
        startWebServer(ksPass, tokenPass, authenticationHandler);
    }

    /**
     * Returns an instance with all supported parameters the user could pass.
     *
     * @return CliParameterLoader.
     */
    private static CliParameterLoader getCliParameterLoader() {
        return new CliParameterLoader(Map.of(), Map.of(//
                "--ksPass", "The Java Key Store password. It's required if you passed a jks path.",//
                "--tokenPass", "Password token. It's the symmetric key used to encrypted the session..",//
                ConfigHandler.GENERATE_PROPERTIES_PARAM, "Generate a default properties file", //
                ConfigHandler.CONFIG_FILE, ".properties file to customize/enable functionalities." //
        ),
                Map.of(/*fill with map when parameters are linked with each other, and you want to create pairs of required fields*/));
    }


    private static void startWebServer(char[] storePassKey, char[] tokenPass, AuthenticationHandler authenticationHandler)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            UnrecoverableKeyException, KeyManagementException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Singletons.register(new SimpleSecretHandler(tokenPass));
        Singletons.register(new TokenStructure(DefaultTokenFields.values()));

        // Instantiate each class that should contain @Endpoint
        final List<Object> instancesToProcess = new ArrayList<>();
        int counter = 0;
        String classPath;
        // Loads each classes that have @Endpoint annotation on its methods.
        // The user will have to do it manually until we add a way to scan a whole project.
        while ((classPath = System.getProperty("server.handlers." + counter + ".endpoint.class")) != null) {
            final Class<?> aClass = Class.forName(classPath);
            final Object newInstance = aClass.getConstructor().newInstance();
            instancesToProcess.add(newInstance);
            counter++;
        }

        // Generate js libs AND Generate the API documentation for each endpoint.
        // TODO PFR Generate API endpoints in a static file instead of doing it at each startup:
        //  Ideally we shouldn't have to generate it at each startup. The goal is to reduce the time to run the server.
        final List<DocumentedEndpoint> endpointsDocs = new ArrayList<>();
        final StringBuilder jsScript = new StringBuilder();
        jsScript.append(JsGenerator.asyncCallSource());
        final Map<String, HttpHandler> handlers = EndpointGenerator.loadHttpHandlers(instancesToProcess,
                List.of(endpointsDocs::add,
                        doc -> jsScript.append(JsGenerator.generateJsCall(doc)),
                        doc -> LogUtils.info("Documenting generated endpoint %s %s", doc.getHttpMethod(), doc.getPath())));

        // If the auth endpoint has been set up then register it and expose it.
        if (ServerProperties.KEY_AUTH_ENDPOINT.getValue().isPresent()) {
            LogUtils.info("Initializing auth endpoint...");
            final String relativePathEndpoint = ServerProperties.KEY_AUTH_ENDPOINT.getValue().get();
            handlers.put(relativePathEndpoint, authenticationHandler);
            final DocumentedEndpoint authEndpointDoc = new DocumentedEndpoint(null, "_auth", "POST", relativePathEndpoint, "Authenticate the caller.", "{ login:'login', pass:'pass'}", "{ token: 'token' }}", Map.of("login", "String", "pass", "String"), null);
            endpointsDocs.add(authEndpointDoc);
            jsScript.append(JsGenerator.generateJsCall(authEndpointDoc));
            jsScript.append(JsGenerator.authSource());
        }

        // If a js library has been set up then generate a js method for each endpoint to call them.
        // TODO PFR Generate js lib once, in a static file instead of generating it each time we run the server
        //  In order to reduce the time to run the server, the file should already exist. Ideally we should store it in a cache.
        if (ServerProperties.KEY_GENERATE_JS_LIB_ENDPOINT.getValue().isPresent()) {
            LogUtils.info("Initializing Javascript library...");
            final String jsSourceCode = jsScript.toString();
            handlers.put(ServerProperties.KEY_GENERATE_JS_LIB_ENDPOINT.getValue().get(), httpExchange -> {
                httpExchange.sendResponseHeaders(200, jsSourceCode.length());
                final OutputStream os = httpExchange.getResponseBody();
                os.write(jsSourceCode.getBytes());
                os.close();
            });
            endpointsDocs.add(new DocumentedEndpoint(null, null, "GET", ServerProperties.KEY_GENERATE_JS_LIB_ENDPOINT.getValue().get(), "", "", "", Map.of(), null));
        }

        // If an endpoint for shared static files, create it.
        // TODO PFR Create a cache for the most used static files
        //  Maybe something like: a limited list with files, each of them would have a last request date.
        if (ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().isPresent()) {
            LogUtils.info("Initializing static files handler...");
            handlers.put(ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().get(), new ServeFileHandler());
            endpointsDocs.add(new DocumentedEndpoint(null, "downloadAsync", "GET", ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().get() + "/*", "Return static files", "", "<requested file data>", Map.of(), null));
        }

        // If an endpoint that describes all existing endpoint has been set up, then create it.
        if (ServerProperties.KEY_SELF_DESCRIBE_ENDPOINT.getValue().isPresent()) {
            LogUtils.info("Initializing self describer endpoint...");
            handlers.put(ServerProperties.KEY_SELF_DESCRIBE_ENDPOINT.getValue().get(), new SelfDescribeHandler(endpointsDocs));
        }

        // Set up the HTTP server itself.
        final int port = ServerProperties.LISTENING_PORT.asInteger().orElse(8080);
        final int threadCount = ServerProperties.MAX_THREAD.asInteger().orElse(5);

        // Create the server for serving HTTPS or HTTP depending on the settings.
        boolean initTls = storePassKey != null;
        final HttpServer server;
        if (initTls) {
            LogUtils.info("Initializing HTTP over TLS on port %d...", port);
            server = HttpsServer.create(new InetSocketAddress(port), 0);
            final String keyStorePath = ServerProperties.KEY_STORE_PATH.getValue().orElse(null);
            initSSL((HttpsServer) server, storePassKey, keyStorePath);
        } else {
            LogUtils.info("Initializing HTTP on port %d...", port);
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }

        LogUtils.info("Create thread pool with a capacity of %d...", threadCount);
        server.setExecutor(Executors.newFixedThreadPool(threadCount));
        handlers.entrySet().stream()
                .peek(entry -> LogUtils.info("Loading handler %s...", entry.getKey()))
                .forEach(entry -> server.createContext(entry.getKey(), entry.getValue()));
        server.start();
        LogUtils.info("Accessible : %s://127.0.0.1:%d" + ServerProperties.KEY_SELF_DESCRIBE_ENDPOINT.getValue().orElse(""), initTls ? "https" : "http", port);
    }

    /*
     * In order to initiate HTTPS you need to generate a certificate. We need to
     * repeat the same password twice keytool -genkeypair -keyalg RSA -alias
     * selfsigned -keystore testkey.jks -storepass mypassword1 -keypass whatever
     * -validity 360 -keysize 2048 -deststoretype pkcs12 need to test :
     * -deststorepass:file /PATH/FILE -deststorepass:env ENV_NAME
     */
    private static void initSSL(HttpsServer httpsServer, char[] storePassKey, String keyStorePath)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {
        // Initialise the HTTPS server
        final SSLContext sslContext = SSLContext.getInstance("TLS");

        // Initialise the keystore
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        final FileInputStream fis = new FileInputStream(keyStorePath);
        ks.load(fis, storePassKey);
        // Set up the key manager factory
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, storePassKey);

        // Set up the trust manager factory
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // Private keys Public keys
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // Initialise the SSL context
                    final SSLContext c = SSLContext.getDefault();
                    final SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // Get the default parameters
                    final SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception ex) {
                    LogUtils.error("Failed to create HTTPS port", ex);
                    SystemUtils.failUser();
                }
            }
        });
    }
}
