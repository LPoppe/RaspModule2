package framework.application;


import framework.network.RaspClient;
import framework.network.RaspServer;
import framework.transport.RaspSocket;

import java.io.IOException;
import java.util.HashMap;

public class ApplicationHandler extends Thread {

    private final RaspSocket raspSocket;
    private final RaspUI raspUI;
    private HashMap<FileUpload, Integer> uploaders = new HashMap<>();
    private HashMap<FileDownload, Integer> downloaders = new HashMap<>();

    private boolean running = true;

    public ApplicationHandler(RaspSocket socket) {
        this.raspSocket = socket;
        if (raspSocket.getRaspReceiver() instanceof RaspClient) {
            this.raspUI = new RaspUI();
        } else {
            this.raspUI = null;
        }
    }

    public void run() {

        try {
            createUploader();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void stopMe() throws InterruptedException {
        // Instruct own loop to stop.
        this.running = false;
        this.join();
    }

    private void createUploader() throws InterruptedException {
        if (raspSocket.getRaspReceiver() instanceof RaspServer) {
            System.out.println("???");
            String filename = "src/main/resources/actualfatcat.jpg";
            FileUpload upload = new FileUpload(this, filename);
            sendToRaspSocket(upload.getNextFileSegment());
//        raspSocket.close();
        }
    }

    private void createDownloader() {

    }

    private void sendToRaspSocket(byte[] data) throws InterruptedException {
        raspSocket.write(data);
    }

    private void getFromRaspSocket() {

    }

    public HashMap<FileUpload, Integer> getUploaders() {
        return uploaders;
    }
    public HashMap<FileDownload, Integer> getDownloaders() {
        return downloaders;
    }

}
