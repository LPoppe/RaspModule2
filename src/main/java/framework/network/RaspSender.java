package framework.network;

import framework.transport.NoAckRaspPacket;
import framework.transport.RaspAddress;
import framework.transport.RaspPacket;
import framework.transport.RaspSocket;
import javafx.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RaspSender extends Thread {
    private final HashMap<RaspAddress, RaspSocket> knownClients;
    private DatagramSocket socket;
    private SetLinkedBlockingQueue<Pair<RaspAddress, NoAckRaspPacket>> sendQueue = new SetLinkedBlockingQueue<>();
        private boolean running = true;

        RaspSender(DatagramSocket socket, HashMap<RaspAddress, RaspSocket> knownClients) {
            this.knownClients = knownClients;
            this.socket = socket;
        }

        public SetLinkedBlockingQueue<Pair<RaspAddress, NoAckRaspPacket>> getSendQueue() {
            return sendQueue;
        }

        @Override
        public void run() {
        super.run();

        while(this.running){
            try {
                Pair<RaspAddress, NoAckRaspPacket> toSend = this.sendQueue.poll(1, TimeUnit.SECONDS);
                if (toSend != null) {
                    RaspAddress address = toSend.getKey();
                    RaspSocket raspSocket = knownClients.get(address);
                    RaspPacket raspPacket = toSend.getValue().toRaspPacket(raspSocket.getAckNr());
                    System.out.printf("Sending packet with: Seq: %d, Ack: %d, Checksum: %s%n", raspPacket.getHeader().getSeqNr(), raspPacket.getHeader().getAckNr(), Arrays.toString(raspPacket.getHeader().getChecksum()));
                    byte[] raspPacketContent = raspPacket.serialize();
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
