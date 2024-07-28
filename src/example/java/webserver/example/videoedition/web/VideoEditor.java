package webserver.example.videoedition.web;

import tools.LogUtils;
import webserver.annotations.Endpoint;
import webserver.example.hvn.web.LocalFilesEndpoints;
import webserver.example.videoedition.model.ExtractFrameRequest;
import webserver.example.videoedition.model.ExtractFrameResponse;
import webserver.example.videoedition.model.SaveFrameRequest;
import webserver.example.videoedition.model.SaveFrameResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class VideoEditor {

    public static final String TMP_OUTPUT_JPG = "/tmp/output.jpg";

    @Endpoint(method = "POST", path = "/extract/frame")
    public ExtractFrameResponse extractFrame(Map<String, Object> headers, ExtractFrameRequest extractFrameRequest) throws IOException, InterruptedException {
        deleteIfExist(TMP_OUTPUT_JPG);
        final ProcessBuilder processBuilder = new ProcessBuilder().command("ffmpeg",
                "-ss",
                "" + extractFrameRequest.getTimeSeconds(),
                "-i",
                "/home/pierre/Documents/projetServer/webserver-lib/src/main/web/video.mp4",// TODO PFr resolve path
                "/tmp/output.jpg");
        Process start = processBuilder.start();
        while (start.isAlive()) {
            Thread.sleep(1000);
        }
        /*String info = new String(start.getInputStream().readAllBytes());
        String errors = new String(start.getErrorStream().readAllBytes());
        System.out.println(info);
        System.out.println(errors);*/
        byte[] imageBytes = readBinaryFile(TMP_OUTPUT_JPG);
        // Need to delete the file locally because ffmpeg can't override an existing file and we want to save disk space
        return new ExtractFrameResponse(Base64.getEncoder().encodeToString(imageBytes));
    }

    private void deleteIfExist(String path) {
        File file = Path.of(path).toFile();
        if (file.exists()) {
            if (!file.delete()) {
                LogUtils.error("cant delete file "+ path);
            }
        }
    }

    private byte[] readBinaryFile(String path) {
        try {
            LogUtils.info("Reading path " + path);
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            LogUtils.error("cant read file", e);
            return "cant read file".getBytes();
        }
    }

    @Endpoint(method = "POST", path = "/extract/frame/save")
    public SaveFrameResponse saveFrame(Map<String, Object> headers, SaveFrameRequest saveFrameRequest) {
        final LocalFilesEndpoints.FileIndexedForManifest fileIndexedForManifest = LocalFilesEndpoints.cache.get(saveFrameRequest.getKey());
        Path metadataFolder = getMetadataFolder(fileIndexedForManifest.absolutePath());
        try {
            Files.move(Path.of(TMP_OUTPUT_JPG), metadataFolder.resolve(saveFrameRequest.getIndex()+".jpg"));
        } catch (IOException e) {
            LogUtils.error("Can't move file.", e);
        }
        return new SaveFrameResponse("TODO");
    }

    private Path getMetadataFolder(String absolutePath) {
        Path pathFile = Path.of(absolutePath);
        Path directory = pathFile.getParent();
        return directory.resolve(LocalFilesEndpoints.toSHA1(absolutePath));
    }
}
