package framework;

import javafx.util.Pair;

import java.util.concurrent.LinkedBlockingQueue;

public class RaspConnectionHandler {

    private final RaspAddress address;
    private final LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue;
    private final int maxPacketSize;
    private int seqNr = 0;
    // Seq nr. of next expected ACK (other side's seq nr).
    private int ackNr = 0;

    public RaspConnectionHandler(LinkedBlockingQueue<Pair<RaspConnectionHandler, RaspPacket>> sendQueue, RaspAddress address, int maxRaspPacketSize) {
        this.address = address;
        this.sendQueue = sendQueue;
        this.maxPacketSize = maxRaspPacketSize;
    }

    public RaspAddress getAddress() {
        return address;
    }

    public void handlePacket(RaspPacket raspPacket) {
        raspPacket.getHeader().getFlag().respondToFlag(this, raspPacket);
    }

    public void setSeqNr(RaspPacket packet) {

    }

    public void setAckNr(RaspPacket packet) {

    }

    public int getSeqNr() {
        return seqNr;
    }

    public int getAckNr() {
        return ackNr;
    }
}
