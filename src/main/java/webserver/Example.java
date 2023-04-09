package webserver;

import webserver.handlers.web.auth.AuthenticationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Set;

import static webserver.handlers.ServerHandler.runServer;

public class Example {


    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {


        final AuthenticationHandler authenticationHandler = new AuthenticationHandler(
                AuthenticationHandler.MOCKED_AUTH,
                AuthenticationHandler.MOCKED_PASSWORD_ENCRYPTOR
        );
        runServer(args, Set.of("webserver.example.MyEndpoints", "webserver.example.CommandManager"), authenticationHandler);
    }

}
