package webserver;

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

import static webserver.handlers.ServerHandler.runServer;

public class Example {

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(
                (String login, byte[] hashed) -> AccountsHandler.validateAndGetAccount(login, hashed)
                        .map(account -> new AuthenticationHandler.AuthenticationResult(login, true, null, account.roles()))
                        .orElse(new AuthenticationHandler.AuthenticationResult(null, false, "auth failed", null)),
                pass -> AccountsHandler.hash(pass.getBytes(StandardCharsets.UTF_8), AccountsHandler.SHA_256)
        );
        runServer(args, authenticationHandler, Example::customWelcomeLogs);
    }

    private static String[] customWelcomeLogs(String baseUrl) {
        return new String[]{
                "Welcome!",
                "┈┈┈┈┈┈▕▔╲",
                "┈┈┈┈┈┈┈▏▕",
                "┈┈┈┈┈┈┈▏▕▂▂▂",
                "▂▂▂▂▂▂╱┈▕▂▂▂▏",
                "▉▉▉▉▉┈┈┈▕▂▂▂▏",
                "▉▉▉▉▉┈┈┈▕▂▂▂▏",
                "▔▔▔▔▔▔╲▂▕▂▂▂",
                baseUrl + "/web/example/savetext.html"
        };
    }

}
