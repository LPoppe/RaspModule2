package framework.application;


import framework.network.RaspClient;
import framework.transport.RaspSocket;

import java.util.HashMap;

public class ApplicationHandler {

    private final RaspSocket raspSocket;
    private final RaspUI raspUI;
    private HashMap<FileUpload, Integer> uploaders = new HashMap<>();
    private HashMap<FileDownload, Integer> downloaders = new HashMap<>();

    public ApplicationHandler(RaspSocket socket) {
        this.raspSocket = socket;
        if (raspSocket.getRaspReceiver() instanceof RaspClient) {
            this.raspUI = new RaspUI();
        } else {
            this.raspUI = null;
        }
    }

    private void createUploader() {

    }

    private void createDownloader() {

    }

    public HashMap<FileUpload, Integer> getUploaders() {
        return uploaders;
    }
    public HashMap<FileDownload, Integer> getDownloaders() {
        return downloaders;
    }
}
