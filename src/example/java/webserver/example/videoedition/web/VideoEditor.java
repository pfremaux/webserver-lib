package webserver.example.videoedition.web;

import tools.LogUtils;
import webserver.annotations.Endpoint;
import webserver.example.videoedition.model.ExtractFrameRequest;
import webserver.example.videoedition.model.ExtractFrameResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class VideoEditor {
    @Endpoint(method = "POST", path = "/extract/frame")
    public ExtractFrameResponse extractFrame(Map<String, Object> headers, ExtractFrameRequest extractFrameRequest) throws IOException, InterruptedException {
        final ProcessBuilder processBuilder = new ProcessBuilder().command("ffmpeg",
                "-ss",
                "" + extractFrameRequest.getTimeSeconds(),
                "-i",
                "/home/pierre/Documents/projetServer/webserver-lib/src/main/web/video.mp4",// TODO PFr resolve path
                "-frames:v",
                "1",
                "q:v",
                "2",
                "/tmp/output.jpg");// TODO PFR il n<overwrite pas par defaut. Il faut changer de nom ou cleanup.
        Process start = processBuilder.start();
        while (start.isAlive()) {
            Thread.sleep(1000);
        }
        String info = new String(start.getInputStream().readAllBytes());
        String errors = new String(start.getErrorStream().readAllBytes());
        System.out.println(info);
        System.out.println(errors);
        return new ExtractFrameResponse(Base64.getEncoder().encodeToString(readBinaryFile("/tmp/output.jpg")));//""/home/pierre/Images/ff7.jpg")));
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
}
