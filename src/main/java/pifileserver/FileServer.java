package pifileserver;

// The server should be able to transfer several files at the same time.
// Under what circumstances can this improve or deteriorate the total transfer speed / quality?

import framework.ShippingAndReceiving;

// You should be able to prove that the file you download from the server
// is exactly the same as the one on the server, and the other way around (data integrity).
public class FileServer {

    public static void main(String[] args) {
        //If a file is requested, start a thread to send it.
        new ShippingAndReceiving(true, 8001).start();
    }
}
