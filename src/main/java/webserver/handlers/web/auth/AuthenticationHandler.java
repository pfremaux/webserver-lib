package webserver.handlers.web.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.security.SimpleSecretHandler;
import tools.Singletons;
import tools.security.symetric.SymmetricHandler;
import webserver.handlers.WebHandlerUtils;
import webserver.handlers.web.BaseError;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AuthenticationHandler implements HttpHandler {

    public static final BiFunction<String, String, AuthenticationResult> MOCKED_AUTH = (String login,
                                                                                        String password) -> {
        if ("admin".equals(login) && "admin".equals(password)) {
            return new AuthenticationResult("1", true, "");
        }
        return new AuthenticationResult(null, false, "");
    };

    public static final Function<String, String> MOCKED_PASSWORD_ENCRYPTOR = Function.identity();

    private final TokenStructure tokenStructure = new TokenStructure(DefaultTokenFields.values());
    private final BiFunction<String, String, AuthenticationResult> authenticationValidator;
    @Deprecated // use AccountsHandler instead
    private final Function<String, String> passwordEncryptor;


    record AuthenticationResult(String userId, boolean valid, String errorDetails) {
    }

    public AuthenticationHandler(
            BiFunction<String, String, AuthenticationResult> authenticationValidator,
            Function<String, String> passwordEncryptor) {
        this.authenticationValidator = authenticationValidator;
        this.passwordEncryptor = passwordEncryptor;

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final InputStream requestBody = exchange.getRequestBody();
        final byte[] allBytes = requestBody.readAllBytes();
        final String bodyData = new String(allBytes, StandardCharsets.UTF_8);
        final AuthBody jsonData;
        try {
            jsonData = JsonMapper.jsonToObject(new StringBuilder(bodyData), AuthBody.class);
        } catch (NoSuchFieldException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            exchange.getResponseHeaders().add("Content-Type", "text/json");
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().write(new byte[]{});
            exchange.getResponseBody().close();
            return;
        }
        if (jsonData == null) {
            WebHandlerUtils.prepareErrorResponse(exchange, 400, BaseError.MISSING_BODY);
            return;
        }
        final String login = jsonData.getLogin();
        final String pass = jsonData.getPass();
        final AuthenticationResult authenticationResult = authenticate(login, pass);
        if (!authenticationResult.valid) {
            WebHandlerUtils.prepareErrorResponse(exchange, 400, AuthError.BAD_CREDENTIALS);
            return;
        }
        Token token = new Token();
        token.put(DefaultTokenFields.USER_ID, authenticationResult.userId);
        token.put(DefaultTokenFields.VERSION, "0");
        token.put(DefaultTokenFields.EXPIRATION_TIMESTAMP_MS,
                Long.toString(System.currentTimeMillis() + Duration.ofHours(1L).toMillis()));
        byte[] encrypt;
        try {
            encrypt = SymmetricHandler.encrypt(Singletons.get(SimpleSecretHandler.class).getSecretKey(),
                    tokenStructure.getFormattedTokenInClear(token).getBytes(StandardCharsets.UTF_8),
                    SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException
                 | IllegalBlockSizeException e) {
            e.printStackTrace();
            exchange.getResponseHeaders().add("Content-Type", "text/json");
            exchange.sendResponseHeaders(400, e.getMessage().length());
            exchange.getResponseBody().write(e.getMessage().getBytes());
            exchange.getResponseBody().close();
            return;
        }
        final byte[] base64Bytes = Base64.getEncoder().encode(encrypt);
        final String encryptedToken = new String(base64Bytes);
        System.out.println(encryptedToken);


        final String msg = JsonMapper.fillWithJsonFormat(new StringBuilder(), Map.of("token", encryptedToken)).toString();

        exchange.getResponseHeaders().add("Content-Type", "text/json");
        exchange.sendResponseHeaders(200, msg.length());
        exchange.getResponseBody().write(msg.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }


    private AuthenticationResult authenticate(String login, String password) {
        return this.authenticationValidator.apply(login, passwordEncryptor.apply(password));
    }

}
