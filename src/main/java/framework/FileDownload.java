package framework;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileDownload {
    private int fileSize;
    private String fileName;
    private int dataSent;
    private int dataLeft;

    private boolean isPaused;
    private boolean isDone;

    private int startTime;
    private int timeSpent;

    public FileDownload() {
        this.isPaused = false;
        this.isDone = false;
    }

    public void updateTracker(int timeAtUpdate) {

    }

    private void buildFile() {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
