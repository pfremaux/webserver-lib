package webserver.handlers.web.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.LogUtils;
import tools.MdDoc;
import tools.Singletons;
import tools.security.SimpleSecretHandler;
import tools.security.symetric.SymmetricHandler;
import webserver.handlers.WebHandlerUtils;
import webserver.handlers.web.BaseError;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@MdDoc(description = "This HTTP handler is responsible to authenticate a user. " +
        "The programmer must instantiate it the right way to authenticate their user via the constructor.")
public class AuthenticationHandler implements HttpHandler {

    public static final BiFunction<String, byte[], AuthenticationResult> MOCKED_AUTH = (String login,
                                                                                        byte[] password) -> {
        if ("admin".equals(login) && "admin".equals(new String(password, StandardCharsets.UTF_8))) {
            return new AuthenticationResult("1", true, "", Set.of("admin"));
        }
        return new AuthenticationResult(null, false, "", Set.of());
    };

    public static final Function<String, byte[]> MOCKED_PASSWORD_ENCRYPTOR = s -> s.getBytes(StandardCharsets.UTF_8);

    private final TokenStructure tokenStructure = new TokenStructure(DefaultTokenFields.values());
    private final BiFunction<String, byte[], AuthenticationResult> authenticationValidator;
    @Deprecated // use AccountsHandler instead
    private final Function<String, byte[]> passwordEncryptor;


    public static final class AuthenticationResult { // TODO PFR move outside
        private final String userId;
        private final boolean valid;
        private final String errorDetails;
        private final Set<String> roles;

        public AuthenticationResult(String userId, boolean valid, String errorDetails, Set<String> roles) {
            this.userId = userId;
            this.valid = valid;
            this.errorDetails = errorDetails;
            this.roles = roles;
        }

        public String getUserId() {
            return userId;
        }

        public boolean getValid() {
            return valid;
        }

        public String getErrorDetails() {
            return errorDetails;
        }

        public Set<String> getRoles() {
            return roles;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (AuthenticationResult) obj;
            return Objects.equals(this.userId, that.userId) &&
                    this.valid == that.valid &&
                    Objects.equals(this.errorDetails, that.errorDetails) &&
                    Objects.equals(this.roles, that.roles);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, valid, errorDetails, roles);
        }

        @Override
        public String toString() {
            return "AuthenticationResult[" +
                    "userId=" + userId + ", " +
                    "valid=" + valid + ", " +
                    "errorDetails=" + errorDetails + ", " +
                    "roles=" + roles + ']';
        }

        }

    @MdDoc(description = "Programmers must instantiate this constructor by themselves. " +
            "This library can't figure out by itself which users exist and which roles should be assigned.")
    public AuthenticationHandler(
            @MdDoc(description = "This BiFunction should authenticate a user based on a login/password. Password is supposed to be encrypted by the other lambda: passwordEncryptor." +
                    "i.e. this BiFunction should find out the pair login/encryptedPassword in a database to return a successful authentication.")
            BiFunction<String, byte[], AuthenticationResult> authenticationValidator,
            @MdDoc(description = "This function must encrypt the password passed through the other parameter: authenticationValidator. This function will be called ")
            Function<String, byte[]> passwordEncryptor) {
        this.authenticationValidator = authenticationValidator;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final InputStream requestBody = exchange.getRequestBody();
        final byte[] allBytes = requestBody.readAllBytes();
        final String bodyData = new String(allBytes, StandardCharsets.UTF_8);
        final AuthBody jsonData;
        // Parse the request body
        try {
            jsonData = JsonMapper.jsonToObject(new StringBuilder(bodyData), AuthBody.class);
        } catch (NoSuchFieldException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LogUtils.error("Client side didn't send a valid request. Can't parse the following body request: %s. ", bodyData);
            LogUtils.error("Exception message is ", e);
            WebHandlerUtils.prepareErrorResponse(exchange, 400, BaseError.INVALID_BODY);
            return;
        }
        // No body request => error
        if (jsonData == null) {
            WebHandlerUtils.prepareErrorResponse(exchange, 400, BaseError.MISSING_BODY);
            return;
        }
        // Authenticate the caller
        final String login = jsonData.getLogin();
        final String pass = jsonData.getPass();
        final AuthenticationResult authenticationResult = authenticate(login, pass);
        if (!authenticationResult.valid) {
            WebHandlerUtils.prepareErrorResponse(exchange, 400, AuthError.BAD_CREDENTIALS);
            return;
        }

        // Create a Token for the caller
        final Token token = new Token();
        token.put(DefaultTokenFields.USER_ID, authenticationResult.userId);
        token.put(DefaultTokenFields.VERSION, "0");
        token.put(DefaultTokenFields.EXPIRATION_TIMESTAMP_MS,
                Long.toString(System.currentTimeMillis() + Duration.ofHours(1L).toMillis()));
        byte[] encrypt;
        // Encrypt the token data before we return it to the caller.
        try {
            encrypt = SymmetricHandler.encrypt(Singletons.get(SimpleSecretHandler.class).getSecretKey(),
                    tokenStructure.getFormattedTokenInClear(token).getBytes(StandardCharsets.UTF_8),
                    SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException
                 | IllegalBlockSizeException e) {
            LogUtils.error("Failed to encrypt the token: %s", token.prettyString());
            LogUtils.error("", e);
            WebHandlerUtils.prepareErrorResponse(exchange, 500, AuthError.ALGORITHM_ENCRYPTION);
            return;
        }
        // Encryption is binary, HTTP is textual => encode in base64
        final byte[] base64Bytes = Base64.getEncoder().encode(encrypt);
        final String encryptedToken = new String(base64Bytes);
        LogUtils.debug("Encrypted token: %s", encryptedToken);

        // Put the token in a json response.
        final String msg = JsonMapper.objectToJson(
                new AuthenticationResponse(encryptedToken, authenticationResult.roles)).toString();
        WebHandlerUtils.buildValidResponseAndClose(exchange, msg);
    }


    private AuthenticationResult authenticate(String login, String password) {
        return this.authenticationValidator.apply(login, passwordEncryptor.apply(password));
    }

}
