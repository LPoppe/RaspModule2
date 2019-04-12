package framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class FileUpload {
    private boolean isPaused;
    private boolean isDone;
    private Integer fileIdentifier;
    private int segmentCounter;
    private TransferController transferControl;

    private BufferedInputStream fileStream;

    public FileUpload(TransferController controller) {
        this.isPaused = false;
        this.isDone = false;
        this.transferControl = controller;
        this.fileIdentifier = getRandomNotInUse(1, 50000);
        this.segmentCounter = 0;
    }

    public void updateTracker(int timeAtUpdate) {

    }

    public byte[] getNextFileSegment(String fileName) {
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

    public int getFileIdentifier() {
        return fileIdentifier;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    private int getRandomNotInUse(int lower, int upper) {
        if (lower >= upper) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        int newIdentifier;
        do {
            Random rand = new Random();
            newIdentifier = rand.nextInt((upper - lower) + 1) + lower;
        } while (transferControl.getUploaders().containsValue(newIdentifier)
                || transferControl.getDownloaders().containsValue(newIdentifier));

        return newIdentifier;
    }
}
