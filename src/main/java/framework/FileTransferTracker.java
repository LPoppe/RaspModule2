package framework;

public class FileTransferTracker {
    private int fileSize;
    private int dataSent;
    private int dataLeft;

    private boolean isPaused;
    private boolean isDone;

    private int startTime;
    private int timeSpent;
    // Is this a good idea?
    private int expectedFinish;

    public FileTransferTracker(int timeAtStart) {
        this.isPaused = false;
        this.isDone = false;
    }

    public void updateTracker(int timeAtUpdate) {

    }
}
