package webserver.example.hvn.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class VideoUtils {
    public static final String UNIX_TMP_OUTPUT_JPG = "/tmp/output.jpg";
    public static final String WINDOWS_TMP_OUTPUT_JPG = "C:/tmp/output.jpg";

    public static byte[] extractFrameWithBlockingProcess(final String absolutePath, final double timeSeconds) throws IOException, InterruptedException {
        final String ffmpegOutputImageFile = getTmpOutputFilePath();
        FileUtils.deleteIfExist(ffmpegOutputImageFile);
        final ProcessBuilder processBuilder = new ProcessBuilder().command("ffmpeg",
                "-y", // overwrite file without confirmation
                "-ss",
                "" + timeSeconds,
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
        /*String info = new String(inputStream.readAllBytes());
        String errors = new String(errorStream.readAllBytes());*/
        // This is a blocking process. Convenient lazy way to handle a not too long process as we're not handling Futures or long polling system with client.
        while (start.isAlive()) {
            Thread.sleep(1000);
        }

        /*System.out.println(info);
        System.out.println(errors);*/
        return FileUtils.readBinaryFile(ffmpegOutputImageFile);
    }

    public static String getTmpOutputFilePath() {
        if (File.separator.equals("/")) {
            return UNIX_TMP_OUTPUT_JPG;
        }
        return WINDOWS_TMP_OUTPUT_JPG;
    }

    public static String getFrameFileName(int index) {
        return "image" + index + ".jpg";
    }
}
