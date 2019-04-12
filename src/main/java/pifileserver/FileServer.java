package pifileserver;

import framework.ShippingAndReceiving;

public class FileServer {

    public static void main(String[] args) throws InterruptedException {
        new ShippingAndReceiving(true, 8001).start();
    }

}
