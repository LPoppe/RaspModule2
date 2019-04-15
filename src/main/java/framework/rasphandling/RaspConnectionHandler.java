package framework.rasphandling;

import framework.slidingwindow.RaspSocket;
import javafx.util.Pair;

import java.util.concurrent.LinkedBlockingQueue;


public class RaspConnectionHandler {

    // The address this handler needs to handle communication with.
    private final RaspAddress address;

    // Queues for the packets to be sent.
    private final LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue;
    private final RaspSocket raspSocket;

    // Tracking of timeout.
    //TODO: timeout = 10x average RTT?
    private long lastTimeReceived = System.currentTimeMillis();

    // Largest size a packet may have
    private final int maxPacketSize;

    // ackNr = seq nr. of next expected ACK (other side's seq nr).
    private int seqNr = 0;
    private int ackNr = 0;

    public RaspConnectionHandler(LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue, RaspAddress address, int maxRaspPacketSize) {
        this.address = address;
        this.sendQueue = sendQueue;
        this.maxPacketSize = maxRaspPacketSize;
        this.raspSocket = new RaspSocket(this);
    }

    public RaspAddress getAddress() {
        return address;
    }


    public void sendWindowToSendQueueNonBlocking() {
        this.sendQueue.offer(new Pair<> (this, this.raspSocket.getSendWindow().getNext()));
    }

    public void handlePacket(RaspPacket raspPacket) {
        // Reset the last time a packet was received to current time.
        setLastTimeReceived(System.currentTimeMillis());

        raspPacket.getHeader().getFlag().respondToFlag(this, raspPacket);
    }

    /**
     * Resets the offset of the sending window to resend packets that have not been acknowledged before timeout.
     */
    public void resetWindow() {
        this.raspSocket.resend();
    }

    public void increaseSeqNr() {
        this.seqNr++;
    }

    /**
     * Set the ack number (last seq nr. received).
     * Ignore the ack number if higher seq nr. has already arrived.
     */
    public void setAckNr(RaspPacket packet) {
        //TODO: Does not wrap around.
        int packetAck = packet.getHeader().getSeqNr();
        if (packetAck > this.ackNr) {
            this.ackNr = packetAck;
        }
    }

    public int getSeqNr() {
        return seqNr;
    }

    public int getAckNr() {
        return ackNr;
    }

    public long getLastTimeReceived() {
        return lastTimeReceived;
    }

    private void setLastTimeReceived(long lastTimeReceived) {
        this.lastTimeReceived = lastTimeReceived;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public RaspSocket getSocket() {
        return this.raspSocket;
    }
}
