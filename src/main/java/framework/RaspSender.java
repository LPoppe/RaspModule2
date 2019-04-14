package framework;

import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RaspSender extends Thread {
    private DatagramSocket socket;
    private LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue = new LinkedBlockingQueue<>();
    private boolean running = true;

    RaspSender(DatagramSocket socket) {
        this.socket = socket;
    }

    LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> getSendQueue() {
        return sendQueue;
    }

    @Override
    public void run() {
        super.run();

        while(this.running){
            try {
                Pair<RaspConnectionHandler, RaspPacket> toSend = this.sendQueue.poll(1, TimeUnit.SECONDS);
                if (toSend != null) {
                    byte[] raspPacketContent = toSend.getValue().serialize();
                    RaspAddress address = toSend.getKey().getAddress();
                    DatagramPacket udpPacket = new DatagramPacket(raspPacketContent, raspPacketContent.length,
                            address.getAddress(), address.getPort());
                    socket.send(udpPacket);
                }
            } catch (InterruptedException ignored) {

            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    void stopMe() throws InterruptedException {
        this.running = false;
        this.join();
    }
}
