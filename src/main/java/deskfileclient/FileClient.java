package deskfileclient;

import framework.ShippingAndReceiving;

// TODO: You should be able to prove that the file you download from the server
//  is exactly the same as the one on the server, and the other way around (data integrity).
public class FileClient {

    public static void main(String[] args) {
        //If a file is requested, start a thread to send it.
        new ShippingAndReceiving(false, 8888).start();
    }
}
