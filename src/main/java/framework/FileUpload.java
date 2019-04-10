package framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUpload {
    private int fileSize;
    private int dataSent;
    private int dataLeft;

    private boolean isPaused;
    private boolean isDone;

    private int startTime;
    private int timeSpent;
    // Is this a good idea?
    private int expectedFinish;

    public FileUpload(int timeAtStart) {
        this.isPaused = false;
        this.isDone = false;
    }

    public void updateTracker(int timeAtUpdate) {

    }

    private byte[] getNextFileSegment(String fileName) {
        byte[] nextSegment = null;
        try {
            nextSegment = Files.readAllBytes(Paths.get(fileName));
        } catch (FileNotFoundException fe) {
            System.err.println("File requested not found: " + fe.getMessage());
            //TODO: Send some response notifying the user/client file does not exist. Not here. But somewhere?
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        }
        return nextSegment;
    }
}
