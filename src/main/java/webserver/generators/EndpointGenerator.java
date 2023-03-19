package webserver.generators;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.MdDoc;
import webserver.annotations.Endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
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
            try {
                documentedEndpoint.setBodyExample(JsonMapper.objectToJsonExample(bodyParameter.getType()).toString());
                documentedEndpoint.setResponseExample(JsonMapper.objectToJsonExample(declaredMethod.getReturnType()).toString());
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Endpoint creation failed.", e1);
            }

            final HttpHandler handler = exchange -> {
                if (!exchange.getRequestMethod().equals(method)) {
                    final String msg = "Method not allowed";
                    exchange.sendResponseHeaders(409, msg.length());
                    final OutputStream os = exchange.getResponseBody();
                    os.write(msg.getBytes());
                    os.close();
                    exchange.getResponseBody().close();
                    return;
                }
                final Map<String, List<String>> headers = new HashMap<>(exchange.getRequestHeaders());
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
