package framework;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TransferController extends Thread {
    private LinkedBlockingQueue<RaspPacket> sendQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<RaspPacket> receiveQueue = new LinkedBlockingQueue<>();
    private ShippingAndReceiving shipper;
    private boolean isRunning;
    private byte[] fillData = new byte[1];
    private HashMap<FileUpload, Integer> uploaders = new HashMap<>();
    private HashMap<FileDownload, Integer> downloaders = new HashMap<>();

    public TransferController(boolean isServer, ShippingAndReceiving shippingAndReceiving)throws InterruptedException {
        this.shipper = shippingAndReceiving;
        this.isRunning = true;
    }

    public void run() {
        while (isRunning) {
            if (!receiveQueue.isEmpty()) {
                System.out.println("CHECK");
                // First attempt processing an item from the receive queue.
                try {
                    RaspPacket packet = receiveQueue.take();
                    // This also calls methods to create a new packet if necessary.
                    packet.getHeader().getFlag().respondToFlag(this, this.shipper, packet);

                    // TODO: Add send without responding. While only 1 message is allowed at the time this is not
                    //  relevant for the server, but the client needs to be able to send commands!
                    //send();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void send() {
        boolean dataToBeSent = false;
        if (dataToBeSent) {

        }
//            if (sending == null) {
//                nextFlag.sendWithFlag(this, shipper, newPacket);
//            } else {
//                ControlFlag.ACKDATA.sendWithFlag(this, shipper, newPacket);
//            }
    }

    LinkedBlockingQueue getSendQueue() {
        return this.sendQueue;
    }

    LinkedBlockingQueue getReceiveQueue() {
        return this.receiveQueue;
    }

    public void close() {
        this.isRunning = false;
    }

    public byte[] getNextData() {
        return null;
    }

    public byte[] getFillData() {
        return this.fillData;
    }

    public HashMap<FileUpload, Integer> getUploaders() {
        return uploaders;
    }

    public HashMap<FileDownload, Integer> getDownloaders() {
        return downloaders;
    }
}
