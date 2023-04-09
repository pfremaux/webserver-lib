package tools.security.asymetric;

import tools.LogUtils;

import java.util.logging.Logger;


import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.LinkedList;

public class PublicKeyHandler extends AsymmetricKeyHandler<PublicKey> {

    public void save(String path, PublicKey publicKey) throws IOException {
        LogUtils.debug("Saving public key in {}", path);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(x509EncodedKeySpec.getEncoded());
        }
    }

    public PublicKey load(String path, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        LogUtils.debug("Loading public key in {}", path);
        final File filePublicKey = new File(path);
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        try (FileInputStream fis = new FileInputStream(path)) {
            fis.read(encodedPublicKey);
        }

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public void process(byte[] data, PublicKey key, File f) {
        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                cipherOutputStream.write(data);
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BufferedInputStream recursiveProcessor(LinkedList<PublicKey> keys, BufferedInputStream inputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        LogUtils.debug("Doing recursive encryption, {} encryption(s) remaining.", keys.size());
        if (keys.isEmpty()) {
            return inputStream;
        }
        final PublicKey publicKey = keys.poll();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        while (inputStream.available() > 0) {
            LogUtils.debug(String.format("[encrypting] %d remaining byte(s) to process.", inputStream.available()));
            final int sizeToRead = Math.min(inputStream.available(), 245);
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher)) {
                byte[] array = new byte[sizeToRead];
                inputStream.read(array, 0, sizeToRead);
                cipherOutputStream.write(array);
            }
        }
        byteArrayOutputStream.close();
        return recursiveProcessor(keys, new BufferedInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
    }

    @Override
    public void process(File i, PublicKey key, File o) {
        fileProcessor(Cipher.ENCRYPT_MODE, key, i, o);
    }

    public byte[] process(byte[] i, PublicKey key) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return byteProcessor(Cipher.ENCRYPT_MODE, key, i);
    }
}
