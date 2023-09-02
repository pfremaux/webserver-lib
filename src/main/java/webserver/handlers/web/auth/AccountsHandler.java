package webserver.handlers.web.auth;

import tools.LogUtils;
import tools.SystemUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AccountsHandler {

    public static final String SHA_256 = "SHA-256";
    private static final Map<String, InternalAccount> accounts = new HashMap<>();

    public static void register(String login, byte[] pwd, Set<String> roles) {
        accounts.put(login, new InternalAccount(login, pwd, roles));
    }

    public static Optional<Account> validateAndGetAccount(String login, byte[] hashed) {
        final InternalAccount internalAccount = accounts.get(login);
        final byte[] valid = internalAccount.pwd;
        boolean isValid = hashed.length == valid.length;
        for (int i = 0; i < valid.length; i++) {
            isValid &= valid[i] == hashed[i];
        }
        return isValid ? Optional.of(internalAccount.toSharedAccount()) : Optional.empty();
    }

    public static int accountsCount() {
        return accounts.size();
    }

    private record InternalAccount(String login, byte[] pwd, Set<String> roles) {
        Account toSharedAccount() {
            return new Account(login, roles);
        }
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
