package webserver.example.hvn.web.videoedition.web;

import tools.LogUtils;
import webserver.annotations.Endpoint;
import webserver.example.hvn.utils.FileUtils;
import webserver.example.hvn.utils.MetadataUtils;
import webserver.example.hvn.utils.VideoUtils;
import webserver.example.hvn.web.LocalFilesEndpoints;
import webserver.example.hvn.web.videoedition.model.ExtractFrameRequest;
import webserver.example.hvn.web.videoedition.model.SaveFrameRequest;
import webserver.example.hvn.web.videoedition.model.SaveFrameResponse;
import webserver.example.hvn.web.videoedition.model.ExtractFrameResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class VideoEditor {

    public static final String UNIX_TMP_OUTPUT_JPG = "/tmp/output.jpg";
    public static final String WINDOWS_TMP_OUTPUT_JPG = "C:/tmp/output.jpg";

    @Endpoint(method = "POST", path = "/extract/frame")
    public ExtractFrameResponse extractFrame(Map<String, Object> headers, ExtractFrameRequest extractFrameRequest) throws IOException, InterruptedException {
        final byte[] imageBytes = extractFrameWithBlockingProcess(extractFrameRequest);
        // Need to delete the file locally because ffmpeg can't override an existing file and we want to save disk space
        return new ExtractFrameResponse(Base64.getEncoder().encodeToString(imageBytes));
    }

    // TODO PFr use VideoUtils.java instead
    private static byte[] extractFrameWithBlockingProcess(ExtractFrameRequest extractFrameRequest) throws IOException, InterruptedException {
        final String absolutePath = LocalFilesEndpoints.cache.get(extractFrameRequest.getVideoPath()).absolutePath();// TODO PFR remplace video path par key
        return VideoUtils.extractFrameWithBlockingProcess(absolutePath, extractFrameRequest.getTimeSeconds());
        /*final ProcessBuilder processBuilder = new ProcessBuilder().command("ffmpeg",
                "-y", // overwrite file without confirmation
                "-ss",
                "" + extractFrameRequest.getTimeSeconds(),
                "-i",
                absolutePath, //"/home/pierre/Documents/projetServer/webserver-lib/src/main/web/video.mp4",// TODO PFr resolve path
                "-vframes",
                "1",
                ffmpegOutputImageFile);
        // Setting true is important to not hang on Windows
        processBuilder.redirectErrorStream(true);

        final Process start = processBuilder.start();
        final InputStream inputStream = start.getInputStream();
        final InputStream errorStream = start.getErrorStream();

        final byte[] bufferOut = new byte[1024];
        final byte[] bufferErr = new byte[1024];

        while (inputStream.read(bufferOut) > 0 || errorStream.read(bufferErr) > 0) {
            System.out.println(new String(bufferOut));
            System.out.println(new String(bufferErr));
        }
        *//*String info = new String(inputStream.readAllBytes());
        String errors = new String(errorStream.readAllBytes());*//*
        // This is a blocking process. Convenient lazy way to handle a not too long process as we're not handling Futures or long polling system with client.
        while (start.isAlive()) {
            Thread.sleep(1000);
        }

        *//*System.out.println(info);
        System.out.println(errors);*//*
        return FileUtils.readBinaryFile(getTmpOutputFilePath());*/
    }

    private static String getTmpOutputFilePath() {
        if (File.separator.equals("/")) {
            return UNIX_TMP_OUTPUT_JPG;
        }
        return WINDOWS_TMP_OUTPUT_JPG;
    }


    @Endpoint(method = "POST", path = "/extract/frame/save")
    public SaveFrameResponse saveFrame(Map<String, Object> headers, SaveFrameRequest saveFrameRequest) {
        final LocalFilesEndpoints.FileIndexedForManifest fileIndexedForManifest = requireFileIndexed(saveFrameRequest);
        final Path metadataFolder = MetadataUtils.getMetadataFolder(fileIndexedForManifest);
        try {
            int index = saveFrameRequest.getIndex();
            Files.move(Path.of(getTmpOutputFilePath()), metadataFolder.resolve(VideoUtils.getFrameFileName(index)));
        } catch (IOException e) {
            LogUtils.error("Can't move file.", e);
        }
        return new SaveFrameResponse("TODO");
    }

    private static LocalFilesEndpoints.FileIndexedForManifest requireFileIndexed(SaveFrameRequest saveFrameRequest) {
        final LocalFilesEndpoints.FileIndexedForManifest indexedForManifest = LocalFilesEndpoints.cache.get(saveFrameRequest.getKey());
        if (indexedForManifest == null) {
            LogUtils.warning("Invalid file key, no file found. InvalidKey="+saveFrameRequest.getKey());
            throw new IllegalArgumentException();
        }
        return indexedForManifest;
    }
}
