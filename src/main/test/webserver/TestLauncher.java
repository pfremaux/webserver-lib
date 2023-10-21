package webserver;


import webserver.handlers.ServerHandler;
import webserver.handlers.web.auth.AuthenticationHandler;

import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class TestLauncher {
    // TODO PFR this class would only be useful if we include github actions.
    public static void main(String[] args) throws InaccessibleObjectException, UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, InvocationTargetException {
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(
                AuthenticationHandler.MOCKED_AUTH,
                AuthenticationHandler.MOCKED_PASSWORD_ENCRYPTOR
        );
        ServerHandler.runServer(args, authenticationHandler, o -> {}, s -> new String[]{});
    }

}