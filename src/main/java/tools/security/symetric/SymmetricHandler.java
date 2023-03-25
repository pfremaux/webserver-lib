package tools.security.symetric;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class SymmetricHandler {

    public static String DEFAULT_SYMMETRIC_ALGO = "AES";


    // The symetric key we're using expects a fixed size. Consequently you'll get InvalidKeyException: Invalid AES key length: <your password size> bytes
    public static SecretKeySpec getKey(String password, String algorithm) {
        return new SecretKeySpec(Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16), algorithm);
    }

    public static byte[] encrypt(SecretKeySpec secretKeySpec, byte[] data, String algorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        // key = fixSecret(password, length);
        Cipher instance = Cipher.getInstance(algorithm);
        instance.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return instance.doFinal(data);
    }

    public static byte[] decrypt(SecretKeySpec secretKeySpec, byte[] encryptedData, String algorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher instance = Cipher.getInstance(algorithm);
        instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return instance.doFinal(encryptedData);
    }

    public static void main(String... s) throws Exception {
        String password = "mdp";
        String data = "yaTp2Fdp@B      ";
        String algo = SymmetricHandler.DEFAULT_SYMMETRIC_ALGO;
        SecretKeySpec secretKeySpec = getKey(password, algo);
        System.out.println("encrypt " + data + "with " + password);
        byte[] encrypted = encrypt(secretKeySpec, data.getBytes(StandardCharsets.UTF_8), algo);
        byte[] base64Bytes = Base64.getEncoder().encode(encrypted);

        String encB64 = new String(base64Bytes, StandardCharsets.UTF_8);
        //System.out.println("gives : " + enc + " with size of bytes : " + encrypted.length + " and " + enc.length() + " for string");
        System.out.println("but " + encB64.getBytes(StandardCharsets.UTF_8).length + " when coming back to bytes");
        byte[] encryptedBack = Base64.getDecoder().decode(encB64.getBytes(StandardCharsets.UTF_8));
        System.out.println("Decrypted gives : " + new String(decrypt(secretKeySpec, encryptedBack, algo), StandardCharsets.UTF_8));
        //System.out.println("Decrypted gives : " + new String(decrypt(secretKeySpec, encrypted, algo), StandardCharsets.UTF_8));
    }

    public static String fillPassword(String password) {
        // from package private interface AESConstants : int[] AES_KEYSIZES = { 16, 24, 32 };
        int length = password.length();
        if (length > 32) {
            return password.substring(0, 32);
        } else if (length > 24) {
            final StringBuilder stringBuffer = new StringBuilder(32);
            stringBuffer.append(password);
            while (stringBuffer.length() < 32) {
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        } else if (length > 16) {
            final StringBuilder stringBuffer = new StringBuilder(24);
            stringBuffer.append(password);
            while (stringBuffer.length() < 24) {
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        } else {
            final StringBuilder stringBuffer = new StringBuilder(16);
            stringBuffer.append(password);
            while (stringBuffer.length() < 16) {
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }
    }
}
