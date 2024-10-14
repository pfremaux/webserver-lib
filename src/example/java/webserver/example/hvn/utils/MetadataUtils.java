package webserver.example.hvn.utils;

import tools.LogUtils;
import webserver.example.hvn.web.LocalFilesEndpoints;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class MetadataUtils {
    private MetadataUtils() {}

    public static LocalFilesEndpoints.FileIndexedForManifest createMetadataFolder(LocalFilesEndpoints.FileIndexedForManifest manifestFileDatum) {
        Path pathFile = Path.of(manifestFileDatum.absolutePath());
        Path directory = pathFile.getParent();
        Path pathFileMetadata = directory.resolve(toSHA1(manifestFileDatum.absolutePath()));
        if (pathFileMetadata.toFile().exists() && pathFileMetadata.toFile().isDirectory()) {
            return manifestFileDatum;
        }
        if (!pathFileMetadata.toFile().mkdir()) {
            LogUtils.error("Can't create directory {}", pathFileMetadata);
        }
        return manifestFileDatum;
    }

    public static Path getMetadataFolder(Path pathFile, String key) {
        final Path directory = pathFile.getParent();
        return directory.resolve(key);
    }

    // TODO PFR beautify in another class
    public static String toSHA1(String textToHash) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(textToHash.getBytes(StandardCharsets.UTF_8));
            sha1 = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public static Path getMetadataFolder(LocalFilesEndpoints.FileIndexedForManifest fileManifestData) {
        final String absolutePath = fileManifestData.absolutePath();
        final Path pathFile = Path.of(absolutePath);
        final Path directory = pathFile.getParent();
        return directory.resolve(MetadataUtils.toSHA1(absolutePath));
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
