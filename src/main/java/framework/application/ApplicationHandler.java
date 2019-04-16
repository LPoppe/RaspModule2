package framework.application;


import java.util.HashMap;

public class ApplicationHandler {

    private HashMap<FileUpload, Integer> uploaders = new HashMap<>();
    private HashMap<FileDownload, Integer> downloaders = new HashMap<>();

    public ApplicationHandler() {
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
