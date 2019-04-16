package fileserver;

import framework.network.RaspServer;

public class FileServer {

    public static void main(String[] args) {
        new RaspServer(8001).start();
    }

}
