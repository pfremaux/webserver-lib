package tools.security;


import tools.security.asymetric.AsymmetricKeyHandler;
import tools.security.asymetric.PrivateKeyHandler;
import tools.security.asymetric.PublicKeyHandler;
import tools.security.symetric.SymmetricHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;

public class SimpleSecretHandler {

    private final Type type;
    private final SecretKeySpec secretKey;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final PrivateKeyHandler privateKeyHandler;
    private final PublicKeyHandler publicKeyHandler;


    public enum Type {SYMMETRIC, ASYMMETRIC}

    public SimpleSecretHandler(String password) {
        this.privateKeyHandler = null;
        this.publicKeyHandler = null;
        this.type = Type.SYMMETRIC;
        this.secretKey = SymmetricHandler.getKey(password, SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
        this.publicKey = null;
        this.privateKey = null;
    }

    public SimpleSecretHandler(String publicKeyPath, String privateKeyPath) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.privateKeyHandler = new PrivateKeyHandler();
        this.privateKey = this.privateKeyHandler.load(privateKeyPath, AsymmetricKeyHandler.ASYMMETRIC_ALGORITHM);
        this.publicKeyHandler = new PublicKeyHandler();
        this.publicKey = this.publicKeyHandler.load(publicKeyPath, AsymmetricKeyHandler.ASYMMETRIC_ALGORITHM);
        this.type = Type.ASYMMETRIC;
        this.secretKey = null;
    }

    public byte[] encrypt(String text) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        switch (type) {
            case SYMMETRIC:
                return SymmetricHandler.encrypt(secretKey, text.getBytes(getCharset()), SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
            case ASYMMETRIC:
                final LinkedList<PublicKey> keys = new LinkedList<>();
                keys.add(publicKey);
                final BufferedInputStream inputStream = AsymmetricKeyHandler.toBufferedInputStream(text.getBytes(StandardCharsets.UTF_8));
                final BufferedInputStream bufferedInputStream = publicKeyHandler.recursiveProcessor(keys, inputStream);
                return bufferedInputStream.readAllBytes();
            default:
                return null;
        }
    }

    private Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    public String decrypt(byte[] data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        switch (type) {
            case SYMMETRIC:
                final byte[] decrypt = SymmetricHandler.decrypt(secretKey, data, SymmetricHandler.DEFAULT_SYMMETRIC_ALGO);
                return new String(decrypt, getCharset());
            case ASYMMETRIC:
                final LinkedList<PrivateKey> keys = new LinkedList<>();
                keys.add(privateKey);
                final BufferedInputStream inputStream = AsymmetricKeyHandler.toBufferedInputStream(data);
                final BufferedInputStream bufferedInputStream = privateKeyHandler.recursiveProcessor(keys, inputStream);
                return new String(bufferedInputStream.readAllBytes(), getCharset());
            default:
                return null;
        }
    }

    public SecretKeySpec getSecretKey() {
        return secretKey;
    }
}