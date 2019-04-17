package framework.application;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class FileUpload {
    private boolean isPaused;
    private boolean isDone;
    private Integer fileIdentifier;
    private int segmentCounter;
    private String fileName;
    private ApplicationHandler applicationHandler;
    private FileInputStream inputStream;


    public FileUpload(ApplicationHandler applicationHandler, String filename) {
        this.isPaused = false;
        this.isDone = false;
        this.applicationHandler = applicationHandler;
        this.fileName = filename;
        this.fileIdentifier = getRandomNotInUse(1, 50000);
        this.segmentCounter = 0;
    }

    public void updateTracker(int timeAtUpdate) {

    }

    public byte[] getNextFileSegment() throws InterruptedException {
        try {
            File file = new File(fileName);
            byte[] bytesArray = new byte[(int) file.length()];
            this.inputStream = new FileInputStream(file);
            return bytesArray;
        } catch (FileNotFoundException fe) {
            System.err.println("File not found: " + fe.getMessage());
        }
        return null;
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
        } while (applicationHandler.getUploaders().containsValue(newIdentifier)
                || applicationHandler.getDownloaders().containsValue(newIdentifier));

        return newIdentifier;
    }
}
