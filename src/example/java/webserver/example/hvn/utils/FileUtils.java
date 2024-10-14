package webserver.example.hvn.utils;

import tools.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    private FileUtils(){}

    public static void deleteIfExist(String path) {
        File file = Path.of(path).toFile();
        if (file.exists()) {
            if (!file.delete()) {
                LogUtils.error("cant delete file " + path);
            }
        }
    }

    public static byte[] readBinaryFile(String path) {
        try {
            LogUtils.info("Reading path " + path);
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            LogUtils.error("cant read file", e);
            return "cant read file".getBytes();
        }
    }
}
