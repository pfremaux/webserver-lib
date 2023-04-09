package webserver.handlers.web.auth;

import tools.LogUtils;
import tools.SystemUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AccountsHandler {

    public static final String SHA_256 = "SHA-256";
    private static final Map<String, Account> accounts = new HashMap<>();
     final Function<byte[], byte[]> passwordEncryptor = clearPwd -> AccountsHandler.hash(clearPwd, SHA_256);
    public static void register(String login, String pwd, Set<String> roles) {
        accounts.put(login, new Account(login, pwd, roles));
    }

    public static int accountsCount() {
        return accounts.size();
    }
    public record Account(String login, String pwd, Set<String> roles) {


    }

    public static byte[] hash(byte[] text, String algorithm) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
            return digest.digest(text);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.error("Failed to instantiate MessageDigest with algorithm " + algorithm, e);
            SystemUtils.failProgrammer();
        }
        return new byte[]{};
    }
}
