package webserver.generators;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.LogUtils;
import tools.MdDoc;
import tools.security.SimpleSecretHandler;
import tools.security.Singletons;
import webserver.annotations.Endpoint;
import webserver.annotations.Role;
import webserver.handlers.HandlerUtils;
import webserver.handlers.web.auth.DefaultTokenFields;
import webserver.handlers.web.auth.Token;
import webserver.handlers.web.auth.TokenStructure;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EndpointGenerator {


    public static Map<String, HttpHandler> loadHttpHandlers(List<Object> instancesToProcess, List<Consumer<DocumentedEndpoint>> addons) {
        final Map<String, HttpHandler> handlers = new HashMap<>();
        for (Object instanceToProcess : instancesToProcess) {
            extractEndpointsFromClasses(instanceToProcess, addons, handlers);
        }
        return handlers;
    }

    private static Map<String, HttpHandler> extractEndpointsFromClasses(Object instanceToProcess, List<Consumer<DocumentedEndpoint>> addons, Map<String, HttpHandler> handlers) {
        final Class<?> class1 = instanceToProcess.getClass();
        // Look for all endpoint and process all methods annotated @Endpoint
        for (Method declaredMethod : class1.getDeclaredMethods()) {
            final Endpoint declaredAnnotation = declaredMethod.getDeclaredAnnotation(Endpoint.class);
            if (declaredAnnotation == null) {
                continue;
            }
            final Role requiredRole = declaredMethod.getDeclaredAnnotation(Role.class);
            // Gets the body instance
            final Parameter bodyParameter = Stream.of(declaredMethod.getParameters())
                    .filter((Parameter p) -> !p.getType().equals(Map.class)).findFirst().orElse(null);

            // TODO allow no body.
            if (bodyParameter == null) {
                throw new NullPointerException(
                        "body attribute is null for method '" + declaredMethod.getName() + "'. All method annotated with @Endpoint should have 2 parameters :"
                                + "(Map<String, List<String>> headers, Body body)");
            }

            final String method = declaredAnnotation.method();
            final String path = declaredAnnotation.path();

            final DocumentedEndpoint documentedEndpoint = new DocumentedEndpoint();
            documentedEndpoint.setJavaMethodName(declaredMethod.getName());
            documentedEndpoint.setHttpMethod(method);
            documentedEndpoint.setPath(path);
            documentedEndpoint.setRole(requiredRole == null ? null : requiredRole.value());
            try {
                documentedEndpoint.setBodyExample(JsonMapper.objectToJsonExample(bodyParameter.getType()).toString());
                documentedEndpoint.setResponseExample(JsonMapper.objectToJsonExample(declaredMethod.getReturnType()).toString());
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Endpoint creation failed.", e1);
            }

            final HttpHandler handler = exchange -> {
                final Map<String, List<String>> headers = new HashMap<>(exchange.getRequestHeaders());
                LogUtils.info("Entering enpoint");
                if (requiredRole != null) {
                    LogUtils.info("Role required " + requiredRole + " ; headers = " + headers);
                    List<String> secValues = headers.get("Sec");
                    if (secValues == null || secValues.size() == 0) {
                        LogUtils.info("header sec = " + requiredRole);
                        final String msg = "Missing security token";
                        exchange.sendResponseHeaders(403, msg.length());
                        final OutputStream os = exchange.getResponseBody();
                        os.write(msg.getBytes());
                        os.close();
                        exchange.getResponseBody().close();
                        return;
                    } else {
                        LogUtils.info(secValues.get(0));

                    }
                    String token = secValues.get(0);
                    SimpleSecretHandler secretHandler = Singletons.get(SimpleSecretHandler.class);
                    try {
                        LogUtils.info("deciphering token...");
                        final String decrypt = secretHandler.decrypt(Base64.getDecoder().decode(token));
                        final TokenStructure tokenStructure = Singletons.get(TokenStructure.class);
                        final Token token1 = tokenStructure.parseAndStoreTokenDeciphered(decrypt);
                        final String userId = token1.get(DefaultTokenFields.USER_ID);
                        LogUtils.info("userId = " + userId);
                    } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException |
                             NoSuchAlgorithmException | NoSuchPaddingException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }
                if (HandlerUtils.validateHttpRequest(exchange, method)) {
                    return;
                }

                final byte[] bytes = exchange.getRequestBody().readAllBytes();
                final Object result;

                try {
                    final String data = new String(bytes, StandardCharsets.UTF_8);
                    final Object b = JsonMapper.jsonToObject(new StringBuilder(data), bodyParameter.getType());

                    result = declaredMethod.invoke(instanceToProcess, headers, b);
                } catch (Throwable e) {
                    handleException(exchange, e);
                    return;
                }

                final String responseText = result == null ? "{}" : JsonMapper.objectToJson(result).toString();
                exchange.sendResponseHeaders(200, responseText.length());

                final OutputStream os = exchange.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
                exchange.getResponseBody().close();
                return;
            };
            handlers.put(path, handler);

            final Map<String, String> nameToTypeParamers = JsonMapper.objectToMapDescriptor(bodyParameter.getType());
            documentedEndpoint.setParameters(nameToTypeParamers);

            final MdDoc declaredDoc = declaredMethod.getDeclaredAnnotation(MdDoc.class);
            if (declaredDoc != null) {
                documentedEndpoint.setDescription(Objects.requireNonNullElse(declaredDoc.description(), ""));
            }
            for (Consumer<DocumentedEndpoint> addon : addons) {
                addon.accept(documentedEndpoint);
            }
        }

        return handlers;
    }

    private static void handleException(HttpExchange exchange, Throwable t) throws IOException {
        final String msg = t.getLocalizedMessage() + "\n"
                + Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        exchange.sendResponseHeaders(400, msg.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(msg.getBytes());
        os.close();
        exchange.getResponseBody().close();
    }

}
