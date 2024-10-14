package webserver.example.hvn.persistence;

import tools.LogUtils;
import webserver.ServerProperties;
import webserver.example.hvn.utils.MetadataUtils;
import webserver.example.hvn.web.LocalFilesEndpoints;
import webserver.example.hvn.web.models.FileMetadata;
import webserver.example.hvn.web.models.SimpleFileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HvnDataAccessImpl implements HvnDataAccess {



    private static Map<String, LocalFilesEndpoints.FileIndexedForManifest> cache = new HashMap<>();


    private static int MAX_RESULT = 10;

    @Override
    public LocalFilesEndpoints.FileIndexedForManifest getFileManifest(String key) {
        return cache.get(key);
    }

    public Map<String, LocalFilesEndpoints.FileIndexedForManifest> scanAndGetAllKeyToFilesManifest() throws IOException {
        cache = Files.find(Path.of(ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow()), 5, (path, attrib) -> path.getFileName().toString().endsWith("mp4"))
                //cache = Files.list(Path.of(ServerProperties.KEY_STATIC_FILES_BASE_DIRECTORY.getValue().orElseThrow()))
                .filter(path -> path.toFile().isFile())
                .map(path -> path.toAbsolutePath().toString())
                .filter(path -> path.endsWith(".mp4")) // TODO PFr improve filter
                .map(absolutePath -> new LocalFilesEndpoints.FileIndexedForManifest(MetadataUtils.toSHA1(absolutePath), absolutePath, List.of()))
                .map(MetadataUtils::createMetadataFolder)
                .collect(Collectors.toMap(LocalFilesEndpoints.FileIndexedForManifest::key, Function.identity()));
        return cache;
    }

    @Override
    public List<SimpleFileInfo> search(int page) {
        final List<SimpleFileInfo> result = new ArrayList<>();
        int minIndexExpected = page * MAX_RESULT;
        int maxIndexExpected = (page + 1) * MAX_RESULT;
        int fileIndex = 0;
        for (LocalFilesEndpoints.FileIndexedForManifest fileIndexedForManifest : cache.values()) {
            fileIndex++;
            if (minIndexExpected <= fileIndex && fileIndex <= maxIndexExpected) {
                final Path fileLocation = Path.of(fileIndexedForManifest.absolutePath());
                final Path metadataFolder = getMetadataFolder(fileLocation, fileIndexedForManifest.key());
                final String image1RelativePath = toRelativePath(metadataFolder, "image1.jpg");
                final String image2RelativePath = toRelativePath(metadataFolder, "image2.jpg");
                final FileMetadata metadata = new FileMetadata(
                        image1RelativePath,
                        image2RelativePath,
                        List.of()
                );
                result.add(new SimpleFileInfo(fileIndexedForManifest.key(), fileLocation.getFileName().toString(), metadata));
            }
        }
        return result;
    }

    private Path getMetadataFolder(Path pathFile, String key) {
        final Path directory = pathFile.getParent();
        return directory.resolve(key);
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
}
