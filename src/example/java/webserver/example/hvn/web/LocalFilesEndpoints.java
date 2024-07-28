package webserver.example.hvn.web;

import tools.LogUtils;
import webserver.annotations.Endpoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalFilesEndpoints {

    public static Map<String, FileIndexedForManifest> cache = new HashMap<>();

    public static final String PATH_ROOT_SCAN = "/home/pierre/Download/";

    @Endpoint(method = "POST", path = "/search")
    public void Search(Map<String, Object> headers, SearchTextRequest searchTextRequest) {

    }

    public record FileIndexedForManifest(String key, String absolutePath) {};
    @Endpoint(method = "GET", path = "/scan")
    public void Scan(Map<String, Object> headers) {
        try {
            cache = Files.list(Path.of(PATH_ROOT_SCAN))
                    .filter(path -> path.toFile().isFile())
                    .map(path -> path.toAbsolutePath().toString())
                    .map(absolutePath -> new FileIndexedForManifest(toSHA1(absolutePath), absolutePath))
                    .map(this::createMetadataFolder)
                    .collect(Collectors.toMap(FileIndexedForManifest::key, Function.identity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileIndexedForManifest createMetadataFolder(FileIndexedForManifest manifestFileDatum) {
        Path pathFile = Path.of(manifestFileDatum.absolutePath);
        Path directory = pathFile.getParent();
        Path pathFileMetadata = directory.resolve(toSHA1(manifestFileDatum.absolutePath));
        if (pathFileMetadata.toFile().exists() && pathFileMetadata.toFile().isDirectory()) {
            return manifestFileDatum;
        }
        if (!pathFileMetadata.toFile().mkdir()) {
            LogUtils.error("Can't create directory {}", pathFileMetadata);
        }
        return manifestFileDatum;
    }

    // TODO PFR beautify in another class
    public static String toSHA1(String textToHash)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(textToHash.getBytes(StandardCharsets.UTF_8));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
