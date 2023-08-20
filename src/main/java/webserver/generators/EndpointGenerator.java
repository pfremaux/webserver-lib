package webserver.generators;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.LogUtils;
import tools.MdDoc;
import tools.Singletons;
import tools.security.SimpleSecretHandler;
import webserver.annotations.Endpoint;
import webserver.annotations.Form;
import webserver.annotations.Role;
import webserver.handlers.WebHandlerUtils;
import webserver.handlers.web.BaseError;
import webserver.handlers.web.ErrorReport;
import webserver.handlers.web.ErrorReport;
import webserver.handlers.web.auth.DefaultTokenFields;
import webserver.handlers.web.auth.Token;
import webserver.handlers.web.auth.TokenStructure;
import webserver.validator.ValidationTrait;

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
        final StringBuilder formGeneratorBuilder = new StringBuilder();// TODO PFR work on it once I'm on JS generation
        // Look for all endpoint and process all methods annotated @Endpoint
        for (Method declaredMethod : class1.getDeclaredMethods()) {
            final Endpoint declaredAnnotation = declaredMethod.getDeclaredAnnotation(Endpoint.class);
            if (declaredAnnotation == null) {
                continue;
            }
            final Role requiredRole = declaredMethod.getDeclaredAnnotation(Role.class);
            // Gets the body instance
            final Optional<Parameter> bodyParameter = Stream.of(declaredMethod.getParameters())
                    .filter((Parameter p) -> !p.getType().equals(Map.class)).findFirst();

            final String method = declaredAnnotation.method();
            final String path = declaredAnnotation.path();
            if (handlers.containsKey(path)) {
                throw new IllegalArgumentException("Duplicate path: '%s' even if they have different HTTP method.".formatted(path));
            }

            final HttpHandler handler = mutableInputOutputObject -> {
                final Map<String, List<String>> headers = new HashMap<>(mutableInputOutputObject.getRequestHeaders());
                // VALIDATE ROLE
                if (requiredRole != null) {
                    LogUtils.info("Role required " + requiredRole + " ; headers = " + headers);
                    List<String> secValues = headers.get("Sec");
                    if (secValues == null || secValues.size() == 0) {
                        LogUtils.info("header sec = " + requiredRole);
                        final String msg = "Missing security token";
                        mutableInputOutputObject.sendResponseHeaders(403, msg.length());
                        final OutputStream os = mutableInputOutputObject.getResponseBody();
                        os.write(msg.getBytes());
                        os.close();
                        mutableInputOutputObject.getResponseBody().close();
                        return;
                    } else {
                        LogUtils.info(secValues.get(0));//TODO PFR remove once valid
                    }
                    final String token = secValues.get(0);
                    final SimpleSecretHandler secretHandler = Singletons.get(SimpleSecretHandler.class);
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

                if (!WebHandlerUtils.validateHttpRequest(mutableInputOutputObject, method)) {// TODO PFR il peut y avoir confusion si on a 2 endpoints avec la meme url mais pas la meme method
                    return;
                }

                // EXTRACT REQUEST BODY AND PROCESS
                final byte[] bytes = mutableInputOutputObject.getRequestBody().readAllBytes();
                final Object result;
                try {
                    final String data = new String(bytes, StandardCharsets.UTF_8);
                    if (bodyParameter.isPresent()) {
                        Class<?> bodyParameterType = bodyParameter.get().getType();
                        final Object b = JsonMapper.jsonToObject(new StringBuilder(data), bodyParameterType);
                        if (b == null) {
                            WebHandlerUtils.prepareErrorResponse(mutableInputOutputObject, 400, BaseError.MISSING_BODY);
                            return;
                        }
                        if (bodyParameterType.isAssignableFrom(ValidationTrait.class)) {
                            ValidationTrait validator = (ValidationTrait) b;
                            final Optional<ErrorReport> errorReport = validator.validate();// TODO PFR handle null?
                            if (errorReport.isPresent()) {
                                WebHandlerUtils.prepareErrorResponse(mutableInputOutputObject, 400, errorReport.get());
                                return;
                            }
                        }

                        result = declaredMethod.invoke(instanceToProcess, headers, b);
                    } else {
                        result = declaredMethod.invoke(instanceToProcess, headers);
                    }
                } catch (Throwable e) {
                    handleException(mutableInputOutputObject, e);
                    return;
                }

                // PROCESS RESPONSE
                final String responseText = result == null ? "{}" : JsonMapper.objectToJson(result).toString();
                mutableInputOutputObject.sendResponseHeaders(200, responseText.length());

                final OutputStream os = mutableInputOutputObject.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
                mutableInputOutputObject.getResponseBody().close();
            };
            handlers.put(path, handler);

            // PROCESS DOCUMENTATION
            // Initial documentation of this new endpoint
            final DocumentedEndpoint documentedEndpoint = new DocumentedEndpoint();
            documentedEndpoint.setJavaMethodName(declaredMethod.getName());
            documentedEndpoint.setHttpMethod(method);
            documentedEndpoint.setPath(path);
            documentedEndpoint.setRole(requiredRole == null ? null : requiredRole.value());

            try {
                if (bodyParameter.isPresent()) {
                    final Class<?> bodyParameterType = bodyParameter.get().getType();
                    documentedEndpoint.setBodyExample(JsonMapper.objectToJsonExample(bodyParameterType).toString());
                    documentedEndpoint.setBodyType(bodyParameterType);
                    final Optional<Form> formAnnotation = Optional.ofNullable(bodyParameterType.getAnnotation(Form.class));
                    documentedEndpoint.setHasForm(formAnnotation.isPresent());
                }
                documentedEndpoint.setResponseExample(JsonMapper.objectToJsonExample(declaredMethod.getReturnType()).toString());
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Endpoint creation failed.", e1);
            }


            try {
                if (bodyParameter.isPresent()) {
                    final Class<?> bodyParameterType = bodyParameter.get().getType();
                    documentedEndpoint.setBodyExample(JsonMapper.objectToJsonExample(bodyParameterType).toString());
                    final Optional<Form> formAnnotation = Optional.ofNullable(bodyParameterType.getAnnotation(Form.class));
                    if (formAnnotation.isPresent()) {
                        documentedEndpoint.setHasForm(true);
                        documentedEndpoint.setBodyType(bodyParameterType);//TODO PFR can be merged with parameter setter above
                    }
                }
                documentedEndpoint.setResponseExample(JsonMapper.objectToJsonExample(declaredMethod.getReturnType()).toString());
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Endpoint creation failed.", e1);
            }
            if (bodyParameter.isPresent()) {
                final Map<String, String> nameToTypeParameters = JsonMapper.objectToMapDescriptor(bodyParameter.get().getType());
                documentedEndpoint.setParameters(nameToTypeParameters);
            }
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

    private static boolean validateStepAndDecideIfStop(LambdaStep lambdaStep, HttpExchange exchange, int httpCode) {
        try {
            if (lambdaStep.errorReport != null) {
                WebHandlerUtils.prepareErrorResponse(exchange, httpCode, lambdaStep.errorReport);
                return true;
            } else if (lambdaStep.exception != null) {
                LogUtils.error(lambdaStep.exception.getLocalizedMessage());
                WebHandlerUtils.buildValidResponseAndClose(exchange, lambdaStep.exception.getLocalizedMessage());
                return true;
            } else {
                return lambdaStep.leave;
            }
        } catch (IOException e) {
            LogUtils.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private class LambdaStep {
        private Exception exception;
        private ErrorReport errorReport;
        private boolean leave;
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
