package webserver.generators.endpoint;

import com.sun.net.httpserver.HttpExchange;
import tools.JsonMapper;
import tools.LogUtils;
import tools.Singletons;
import tools.security.SimpleSecretHandler;
import webserver.annotations.Role;
import webserver.handlers.WebHandlerUtils;
import webserver.handlers.web.BaseError;
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

public class AbstractEndpointHandler {

    private AbstractEndpointHandler() {
    }

    static void processAbstractEndpoint(Object instanceToProcess, Method declaredMethod, Role requiredRole, Optional<Parameter> bodyParameter, String method, HttpExchange mutableInputOutputObject) throws IOException {
        final Map<String, List<String>> headers = new HashMap<>(mutableInputOutputObject.getRequestHeaders());
        // VALIDATE ROLE
        final ProcessRequestStep roleValidationResult = validateRoleOfCaller(headers, requiredRole, mutableInputOutputObject);
        if (validateStepAndDecideIfStop(roleValidationResult, mutableInputOutputObject, 400)) {
            return;
        }

        if (!WebHandlerUtils.validateHttpRequest(mutableInputOutputObject, method)) {// TODO PFR il peut y avoir confusion si on a 2 endpoints avec la meme url mais pas la meme method
            // return new LambdaStep(BaseError.METHOD_NOT_ALLOWED); ?
            return;
        }

        // EXTRACT REQUEST BODY AND PROCESS
        final ProcessRequestStep processRequestStep = extractRequestBodyAndParse(mutableInputOutputObject, headers, bodyParameter, declaredMethod, instanceToProcess);
        if (validateStepAndDecideIfStop(processRequestStep, mutableInputOutputObject, 400)) {
            return;
        }

        // PROCESS RESPONSE
        final ProcessRequestStep step = processResponse(mutableInputOutputObject, processRequestStep.result());
        validateStepAndDecideIfStop(step, mutableInputOutputObject, 400);
    }

    private static ProcessRequestStep processResponse(HttpExchange mutableInputOutputObject, Object result) {
        try {
            final String responseText = result == null ? "{}" : JsonMapper.objectToJson(result).toString();
            mutableInputOutputObject.sendResponseHeaders(200, responseText.length());

            final OutputStream os = mutableInputOutputObject.getResponseBody();
            os.write(responseText.getBytes());
            os.close();

            mutableInputOutputObject.getResponseBody().close();
        } catch (IOException e) {
            return new ProcessRequestStep(e);
        }
        return ProcessRequestStep.ALL_GOOD;
    }

    private static ProcessRequestStep validateRoleOfCaller(Map<String, List<String>> headers, Role requiredRole, HttpExchange mutableInputOutputObject) {
        if (requiredRole != null) {
            try {
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
                    return new ProcessRequestStep(true);
                } else {
                    LogUtils.info(secValues.get(0));//TODO PFR remove once valid
                }
                final String token = secValues.get(0);
                final SimpleSecretHandler secretHandler = Singletons.get(SimpleSecretHandler.class);
                LogUtils.info("deciphering token...");
                final String decrypt = secretHandler.decrypt(Base64.getDecoder().decode(token));
                final TokenStructure tokenStructure = Singletons.get(TokenStructure.class);
                final Token token1 = tokenStructure.parseAndStoreTokenDeciphered(decrypt);
                final String userId = token1.get(DefaultTokenFields.USER_ID);
                LogUtils.info("userId = " + userId);
                return new ProcessRequestStep(true);
            } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                     NoSuchPaddingException | IOException e) {
                return new ProcessRequestStep(e);
            }
        }
        return ProcessRequestStep.ALL_GOOD;
    }

    private static ProcessRequestStep extractRequestBodyAndParse(HttpExchange mutableInputOutputObject, Map<String, List<String>> headers, Optional<Parameter> bodyParameter, Method declaredMethod, Object instanceToProcess) throws IOException {

        final byte[] bytes = mutableInputOutputObject.getRequestBody().readAllBytes();
        try {
            final String data = new String(bytes, StandardCharsets.UTF_8);
            if (bodyParameter.isPresent()) {
                Class<?> bodyParameterType = bodyParameter.get().getType();
                final Object b = JsonMapper.jsonToObject(new StringBuilder(data), bodyParameterType);
                if (b == null) {
                    return new ProcessRequestStep(BaseError.MISSING_BODY);
                }
                if (bodyParameterType.isAssignableFrom(ValidationTrait.class)) {
                    ValidationTrait validator = (ValidationTrait) b;
                    final Optional<ErrorReport> errorReport = validator.validate();// TODO PFR handle null?
                    if (errorReport.isPresent()) {
                        return new ProcessRequestStep(errorReport.get());
                    }
                }

                return ProcessRequestStep.ALL_GOOD.withResult(declaredMethod.invoke(instanceToProcess, headers, b));
            } else {
                return ProcessRequestStep.ALL_GOOD.withResult(declaredMethod.invoke(instanceToProcess, headers));
            }
        } catch (Exception e) {
            return new ProcessRequestStep(e);
        }
    }

    private static boolean validateStepAndDecideIfStop(ProcessRequestStep processRequestStep, HttpExchange exchange, int httpCode) {
        try {
            if (processRequestStep.errorReport() != null) {
                WebHandlerUtils.prepareErrorResponse(exchange, httpCode, processRequestStep.errorReport());
                return true;
            } else if (processRequestStep.exception() != null) {
                LogUtils.warning(processRequestStep.exception().getLocalizedMessage());
                WebHandlerUtils.buildValidResponseAndClose(exchange, processRequestStep.exception().getLocalizedMessage());
                return true;
            } else {
                return processRequestStep.leave();
            }
        } catch (IOException e) {
            LogUtils.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}
