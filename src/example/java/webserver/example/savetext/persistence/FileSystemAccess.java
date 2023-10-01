package webserver.example.savetext.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSystemAccess {

    private final Path basePath;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public FileSystemAccess(String basePath) {
        this.basePath = Path.of(basePath);
    }

    public String horodateText(String text) {
        final String fileName = dateFormat.format(new Date()) + ".txt";
        try {
            Files.writeString(basePath.resolve(fileName), text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileName;
    }
}
