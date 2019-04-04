package deskfileclient;

// Your client should be able to show statistics about download speeds, packet loss, retransmissions, etc.

public class StatisticsHandler {
    //Separate into little class later?
    private int downloadSpeed;
    private int packetsLost;
    private int percentagePacketsLost;
    private int retransmissions;
    private int dataTransferred;
    private int dataLeft;
    private int timePoint;

    /** Return printable version of statistics at a certain time point. */
    public void returnStatsAtTime(int timePoint) {

    }

    /** Option to store the statistics in a JSON file? */
    public void sendStatsToFile() {

    }
}
