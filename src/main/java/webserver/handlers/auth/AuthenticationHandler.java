package webserver.handlers.auth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.LogUtils;
import tools.SystemUtils;
import tools.security.SimpleSecretHandler;
import tools.security.SyncedConfig;
import tools.security.symetric.SymmetricHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
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

	public static final Function<String, String> MOCKED_PASSWORD_ENCRYPTER =  Function.identity();
	
	public static final String MSG_FAILED_TO_LOGIN = "{\"msg\":\"failed to login\"}";// TODO PFR use json response when errors
	private final TokenStructure tokenStructure = new TokenStructure(DefaultTokenFields.values());
	private final BiFunction<String, String, AuthenticationResult> authenticationValidator;
	private final Function<String, String> passwordEncrypter;


	record AuthenticationResult(String userId, boolean valid, String errorDetails) {
	}

	// Record necessary if credentials are not saved in the DB.
	public record AccountInfo(String login, String password, List<String> roles) {
	}

	public AuthenticationHandler(
			BiFunction<String, String, AuthenticationResult> authenticationValidator,
			Function<String, String> passwordEncrypter) {
		this.authenticationValidator = authenticationValidator;
		this.passwordEncrypter = passwordEncrypter;

	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		LogUtils.info("entering auth");
		final Headers requestHeaders = exchange.getRequestHeaders();
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
		if(jsonData == null) {
			final String errorMsg = "Missing body";
			exchange.getResponseHeaders().add("Content-Type", "text/json");
			exchange.sendResponseHeaders(400, errorMsg.length());
			exchange.getResponseBody().write(errorMsg.getBytes(StandardCharsets.UTF_8));
			exchange.getResponseBody().close();
			return;
		}
		final String login = jsonData.getLogin();
		final String pass = jsonData.getPass();
		final AuthenticationResult authenticationResult = authenticate(login, pass);
		if (!authenticationResult.valid) {
			exchange.getResponseHeaders().add("Content-Type", "text/json");
			exchange.sendResponseHeaders(400, authenticationResult.errorDetails.length());
			exchange.getResponseBody().write(authenticationResult.errorDetails.getBytes(StandardCharsets.UTF_8));
			exchange.getResponseBody().close();
			return;
		}
		Token token = new Token();
		token.put(DefaultTokenFields.USER_ID, authenticationResult.userId);
		token.put(DefaultTokenFields.VERSION, "0");
		token.put(DefaultTokenFields.EXPIRATION_TIMESTAMP_MS,
				Long.toString(System.currentTimeMillis() + Duration.ofHours(1L).toMillis()));
		byte[] encrypt ;
		try {
			encrypt = SymmetricHandler.encrypt(SyncedConfig.get(SimpleSecretHandler.class).getSecretKey(),
					tokenStructure.getFormattedTokenInClear(token).getBytes(StandardCharsets.UTF_8),
					SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException e) {
			e.printStackTrace();
			exchange.getResponseHeaders().add("Content-Type", "text/json");
			exchange.sendResponseHeaders(400,e.getMessage().length());
			exchange.getResponseBody().write(e.getMessage().getBytes());
			exchange.getResponseBody().close();
			return;
		}
		final byte[] base64Bytes = Base64.getEncoder().encode(encrypt);
		final String encryptedString = new String(base64Bytes);
		System.out.println(encryptedString);


		final String msg = JsonMapper.fillWithJsonFormat(new StringBuilder(), Map.of("token", encryptedString)).toString();

		exchange.getResponseHeaders().add("Content-Type", "text/json");
		exchange.sendResponseHeaders(200, msg.length());
		exchange.getResponseBody().write(msg.getBytes(StandardCharsets.UTF_8));
		exchange.getResponseBody().close();
	}

	private AuthenticationResult authenticate(String login, String password) {

		return this.authenticationValidator.apply(login, passwordEncrypter.apply(password));

	}

}
