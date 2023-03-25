package tools.security.asymetric;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;

public abstract class AsymmetricKeyHandler<T extends Key> {

    public static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    public static KeyPair createPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        keyPairGen.initialize(KEY_SIZE);
        return keyPairGen.generateKeyPair();
    }

    public static BufferedInputStream toBufferedInputStream(byte[] bytes) {
        return new BufferedInputStream(new ByteArrayInputStream(bytes));
    }

    byte[] byteProcessor(int cipherMode, Key key, byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        cipher.init(cipherMode, key);
        return cipher.doFinal(input);
    }

    void fileProcessor(int cipherMode, Key key, File inputFile, File outputFile) {
        try (FileInputStream inputStream = new FileInputStream(inputFile); FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = byteProcessor(cipherMode, key, inputBytes);
            outputStream.write(outputBytes);
        } catch (IOException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public abstract BufferedInputStream recursiveProcessor(LinkedList<T> keys, BufferedInputStream inputStream) throws Exception;


    public abstract void process(File i, T key, File o);

    public abstract void save(String path, T key) throws IOException;

    public abstract T load(String path, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException;

}
