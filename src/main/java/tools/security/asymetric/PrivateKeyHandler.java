package tools.security.asymetric;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.LinkedList;

public class PrivateKeyHandler extends AsymmetricKeyHandler<PrivateKey> {


    public void save(String path, PrivateKey privateKey) throws IOException {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(pkcs8EncodedKeySpec.getEncoded());
        }
    }

    public PrivateKey load(String path, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        File filePrivateKey = new File(path);
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        try (FileInputStream fis = new FileInputStream(path)) {
            fis.read(encodedPrivateKey);
        }

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        return keyFactory.generatePrivate(privateKeySpec);
    }


    public void process(byte[] data, PrivateKey key, File f) {
        try (FileInputStream inputStream = new FileInputStream(f)) {
            Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            try (CipherInputStream cipherOutputStream = new CipherInputStream(inputStream, cipher)) {
                cipherOutputStream.read(data);
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BufferedInputStream recursiveProcessor(LinkedList<PrivateKey> keys, BufferedInputStream inputStream) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if (keys.isEmpty()) {
            return inputStream;
        }
        PrivateKey privateKey = keys.poll();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int currentPos = 0;
        while (inputStream.available() > 0) {
            int sizeToRead = Math.min(inputStream.available(), 256);
            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher)) {
                byte[] array = new byte[sizeToRead];
                inputStream.read(array, 0, sizeToRead);
                cipherOutputStream.write(array);
                currentPos += sizeToRead;
            }
        }
        byteArrayOutputStream.close();
        return recursiveProcessor(keys, new BufferedInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
    }

    @Override
    public void process(File i, PrivateKey pk, File o) {
        fileProcessor(Cipher.DECRYPT_MODE, pk, i, o);
    }

    public byte[] process(byte[] i, PrivateKey key) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return byteProcessor(Cipher.DECRYPT_MODE, key, i);
    }
}
