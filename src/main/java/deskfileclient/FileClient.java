package deskfileclient;

import framework.ShippingAndReceiving;

public class FileClient {

    public static void main(String[] args) throws InterruptedException {
        //If a file is requested, start a thread to send it.
        new ShippingAndReceiving(false, 8888).start();
    }


}
