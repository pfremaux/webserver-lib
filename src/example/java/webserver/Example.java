package webserver;

import tools.Singletons;
import webserver.example.endpointslist.EndpointData;
import webserver.example.endpointslist.EndpointList;
import webserver.example.endpointslist.EndpointLister;
import webserver.generators.DocumentedEndpoint;
import webserver.handlers.ConfigHandler;
import webserver.handlers.ServerHandler;
import webserver.handlers.web.auth.AccountsHandler;
import webserver.handlers.web.auth.AuthenticationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import static webserver.handlers.ServerHandler.runServer;

public class Example {

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ConfigHandler.loadConfigFile("server-config.properties");
        //System.out.println(ServerProperties.LISTENING_PORT.getValue().get());
        //System.exit(-1);
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(
                (String login, byte[] hashed) -> AccountsHandler.validateAndGetAccount(login, hashed)
                        .map(account -> new AuthenticationHandler.AuthenticationResult(login, true, null, account.roles()))
                        .orElse(new AuthenticationHandler.AuthenticationResult(null, false, "auth failed", null)),
                pass -> AccountsHandler.hash(pass.getBytes(StandardCharsets.UTF_8), AccountsHandler.SHA_256)
        );
        runServer(args, authenticationHandler, Example::customExtension, Example::customWelcomeLogs);
    }

    private static void customExtension(DocumentedEndpoint documentedEndpoint) {
        EndpointList endpointList = Singletons.get(EndpointList.class);
        if (endpointList == null) {
            endpointList = new EndpointList(new ArrayList<>());
            Singletons.register(endpointList);
        }
        endpointList.getEndpoints().add(new EndpointData(documentedEndpoint.getPath(), documentedEndpoint.getJavaMethodName(), documentedEndpoint.getResponseExample()));

    }

    private static String[] customWelcomeLogs(String baseUrl) {
        return new String[]{
                "Welcome!",
                //baseUrl + "/web/example/savetext.html"
                baseUrl + "/self-describe"
        };
    }

}
