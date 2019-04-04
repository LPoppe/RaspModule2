package pifileserver;

import java.util.ArrayList;
import java.util.List;

//TODO: The server should be able to return all available files on the Raspberry Pi if asked by client.
public class FileManager {
    private List<String> filesOnServer = new ArrayList<>();

    public List<String> getFileNames () {
        return filesOnServer;
    }

}
