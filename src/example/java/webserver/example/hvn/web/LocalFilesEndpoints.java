package webserver.example.hvn.web;

import tools.LogUtils;
import webserver.ServerProperties;
import webserver.annotations.Endpoint;
import webserver.example.hvn.web.models.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalFilesEndpoints {

    public static Map<String, FileIndexedForManifest> cache = new HashMap<>();
    private static int MAX_RESULT = 10;

    public static final String PATH_ROOT_SCAN = "/home/pierre/Download/";

    @Endpoint(method = "POST", path = "/search")
    public SearchFileResponse Search(Map<String, Object> headers, SearchTextRequest searchTextRequest) {
        int fileIndex = 0;
        // TODO PFr replace with database
        final List<SimpleFileInfo> result = new ArrayList<>();
        int minIndexExpected = searchTextRequest.getPage() * MAX_RESULT;
        int maxIndexExpected = (searchTextRequest.getPage() + 1) * MAX_RESULT;
        for (FileIndexedForManifest fileIndexedForManifest : cache.values()) {
            fileIndex++;
            if (minIndexExpected <= fileIndex && fileIndex <= maxIndexExpected) {
                final Path fileLocation = Path.of(fileIndexedForManifest.absolutePath());
                final Path metadataFolder = getMetadataFolder(fileLocation, fileIndexedForManifest.key);
                final String image1RelativePath = toRelativePath(metadataFolder, "image1.jpg");
                final String image2RelativePath = toRelativePath(metadataFolder, "image2.jpg");
                final FileMetadata metadata = new FileMetadata(
                        image1RelativePath,
                        image2RelativePath
                );
                result.add(new SimpleFileInfo(fileIndexedForManifest.key(), fileLocation.getFileName().toString(), metadata));
            }
        }
        return new SearchFileResponse(
                new Pagination(minIndexExpected, maxIndexExpected, searchTextRequest.getPage(), cache.size() % MAX_RESULT),
                result);
    }

    @Endpoint(method = "POST", path = "/video/info")
    public VideoInfoResponse GetVideoInfo(Map<String, Object> headers, VideoInfoRequest videoInfoRequest) {
        final String absolutePath = cache.get(videoInfoRequest.getKey()).absolutePath();
        final Path filePath = Path.of(absolutePath);
        return new VideoInfoResponse(toRelativePath(filePath.getParent(), filePath.getFileName().toString()));
    }

    private String toRelativePath(Path metadataFolder, String fileName) {
        final Path filePath = metadataFolder.resolve(fileName);
        final File file = filePath.toFile();
        final String absolutePath = file.getAbsolutePath().replaceAll("\\\\", "/");
        if (!file.exists()) {
            LogUtils.error(absolutePath + " Doesn't exist.");
        }

        final String localRelativeRootPathWithoutDot = ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue()
                .map(path -> path.startsWith(".") ? path.substring(1) : path)
                .orElseThrow();

        final int indexPath = absolutePath.lastIndexOf(localRelativeRootPathWithoutDot);
        final String relativeWebFilePath = absolutePath.substring(indexPath + localRelativeRootPathWithoutDot.length());

        final String basePath = ServerProperties.KEY_STATIC_FILES_ENDPOINT_RELATIVE_PATH.getValue().orElseThrow();
        return (basePath + relativeWebFilePath);
    }

    public record FileIndexedForManifest(String key, String absolutePath) {
    }

    @Endpoint(method = "GET", path = "/scan")
    public void Scan(Map<String, Object> headers) {
        try {

            cache = Files.find(Path.of(ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow()), 5, (path, attrib) -> path.getFileName().toString().endsWith("mp4"))
            //cache = Files.list(Path.of(ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow()))
                    .filter(path -> path.toFile().isFile())
                    .map(path -> path.toAbsolutePath().toString())
                    .filter(path -> path.endsWith(".mp4")) // TODO PFr improve filter
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

    private Path getMetadataFolder(Path pathFile, String key) {
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
