package webserver.example.hvn.web;

import tools.LogUtils;
import webserver.ServerProperties;
import webserver.annotations.Endpoint;
import webserver.example.hvn.persistence.HvnDataAccess;
import webserver.example.hvn.persistence.HvnDataAccessImpl;
import webserver.example.hvn.utils.VideoUtils;
import webserver.example.hvn.web.models.*;
import webserver.example.hvn.web.models.tags.GetFileTagRequest;
import webserver.example.hvn.web.models.tags.GetFileTagResponse;
import webserver.example.hvn.web.models.tags.SetFileTagRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Groups endpoints that are interacting with local files.
 */
public class LocalFilesEndpoints {
    // TODO PFR cacher le cache dans MetadataUtils.java OU HvnDataAccessImpl

    public static Map<String, FileIndexedForManifest> cache = new HashMap<>();
    private static int MAX_RESULT = 10;

    private HvnDataAccess hvnDataAccess = new HvnDataAccessImpl();

    public static final String PATH_ROOT_SCAN = "/home/pierre/Download/";

    public LocalFilesEndpoints() {
        Scan(Map.of());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                FILES
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Navigate through pages of files.
     *
     * @param headers
     * @param searchTextRequest TODO use text search
     * @return
     */
    @Endpoint(method = "POST", path = "/search")
    public SearchFileResponse Search(Map<String, Object> headers, SearchTextRequest searchTextRequest) {
        /* TODO PFR replace with below */
        final List<SimpleFileInfo> result = hvnDataAccess.search(searchTextRequest.getPage());
        int minIndexExpected = searchTextRequest.getPage() * MAX_RESULT;
        int maxIndexExpected = (searchTextRequest.getPage() + 1) * MAX_RESULT;
        /*for (FileIndexedForManifest fileIndexedForManifest : cache.values()) {
            fileIndex++;
            if (minIndexExpected <= fileIndex && fileIndex <= maxIndexExpected) {
                final Path fileLocation = Path.of(fileIndexedForManifest.absolutePath());
                final Path metadataFolder = MetadataUtils.getMetadataFolder(fileLocation, fileIndexedForManifest.key);
                final String image1RelativePath = toRelativePath(metadataFolder, "image1.jpg");
                final String image2RelativePath = toRelativePath(metadataFolder, "image2.jpg");
                final FileMetadata metadata = new FileMetadata(
                        image1RelativePath,
                        image2RelativePath,
                        List.of()
                );
                result.add(new SimpleFileInfo(fileIndexedForManifest.key(), fileLocation.getFileName().toString(), metadata));
            }
        }*/
        return new SearchFileResponse(
                new Pagination(minIndexExpected, maxIndexExpected, searchTextRequest.getPage(), cache.size() % MAX_RESULT),
                result);
    }

    /**
     * Get basic files info to initiate a streaming or display tags.
     *
     * @param headers
     * @param videoInfoRequest
     * @return
     */
    @Endpoint(method = "POST", path = "/video/info")
    public VideoInfoResponse GetVideoInfo(Map<String, Object> headers, VideoInfoRequest videoInfoRequest) {
        final FileIndexedForManifest fileIndexedForManifest = hvnDataAccess.getFileManifest(videoInfoRequest.getKey());// TODO PFR <-- key est null le client envoi n'imp
        // TODO PFR Toutes les lignes restantes dans le Utils
        final String absolutePath = fileIndexedForManifest.absolutePath();
        final Path filePath = Path.of(absolutePath);

        return new VideoInfoResponse(
                toRelativePath(filePath.getParent(), filePath.getFileName().toString()),
                fileIndexedForManifest.tags);
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
        return (/*TODO PFR semble useless: basePath + */relativeWebFilePath);
    }

    public record FileIndexedForManifest(String key, String absolutePath, List<String> tags) {
        public FileIndexedForManifest {
            Objects.requireNonNull(key);
            Objects.requireNonNull(absolutePath);
            tags = Collections.unmodifiableList(tags);
        }

        public FileIndexedForManifest withReplacedTags(List<String> tags) {
            return new FileIndexedForManifest(key, absolutePath, tags);
        }
    }

    /**
     * Scan local file system and register files.
     *
     * @param headers
     */
    @Endpoint(method = "GET", path = "/scan")
    public void Scan(Map<String, Object> headers) {
        try {
            // TODO PFR PRIORITY: lecture de video se fait avec /watch/
            final Map<String, FileIndexedForManifest> manifestMap = hvnDataAccess.scanAndGetAllKeyToFilesManifest();
            for (FileIndexedForManifest file : manifestMap.values()) {
                Path frame1 = Path.of(file.absolutePath).getParent().resolve(file.key).resolve(VideoUtils.getFrameFileName(1));
                if (!frame1.toFile().exists()) {
                    VideoUtils.extractFrameWithBlockingProcess(file.absolutePath, 30);
                    final Path source = Path.of(VideoUtils.getTmpOutputFilePath());
                    if (source.toFile().exists()) {
                        Files.move(source, frame1);
                    }
                }
            }
            cache.putAll(manifestMap);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////                TAGS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Attach tags to a file.
     *
     * @param headers
     * @param setFileTagRequest
     */
    @Endpoint(method = "POST", path = "/file/tags/set")
    public void setFileTags(Map<String, Object> headers, SetFileTagRequest setFileTagRequest) {
        FileIndexedForManifest fileIndexedForManifest = cache.get(setFileTagRequest.getKey());
        cache.put(setFileTagRequest.getKey(), fileIndexedForManifest.withReplacedTags(setFileTagRequest.getTags()));
    }

    /**
     * Get tags from a file.
     *
     * @param headers
     * @param getFileTagRequest
     * @return
     */
    @Endpoint(method = "POST", path = "/file/tags/get")
    public GetFileTagResponse getFileTags(Map<String, Object> headers, GetFileTagRequest getFileTagRequest) {
        FileIndexedForManifest fileIndexedForManifest = cache.get(getFileTagRequest.getKey());
        return new GetFileTagResponse(fileIndexedForManifest.tags);
    }

}
