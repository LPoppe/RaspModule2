package framework;

import framework.rasphandling.RaspAddress;
import framework.rasphandling.RaspConnectionHandler;
import framework.rasphandling.RaspPacket;
import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RaspSender extends Thread {
    private final HashMap<RaspAddress, RaspConnectionHandler> knownClients;
    private DatagramSocket socket;
    private LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue = new LinkedBlockingQueue<>(50);
    private boolean running = true;

    RaspSender(DatagramSocket socket, HashMap<RaspAddress, RaspConnectionHandler> knownClients) {
        this.knownClients = knownClients;
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
                // TODO: Bit hacky.
                for (RaspConnectionHandler handler : knownClients.values()) {
                    if (this.sendQueue.remainingCapacity() == 0) {
                        break;
                    } else {
                        handler.sendWindowToSendQueueNonBlocking();
                    }
                }

                Pair<RaspConnectionHandler, RaspPacket> toSend = this.sendQueue.poll(1, TimeUnit.SECONDS);
                if (toSend != null) {
                    int ackNr = toSend.getKey().getAckNr();
                    byte[] raspPacketContent = toSend.getValue().serialize(ackNr);
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
