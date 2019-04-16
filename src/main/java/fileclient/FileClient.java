package fileclient;

import framework.network.RaspClient;

public class FileClient {

    public static void main(String[] args) {
        new RaspClient(8888).start();
    }

}
