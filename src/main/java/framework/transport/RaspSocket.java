package framework.transport;

import framework.network.RaspSender;
import framework.slidingwindow.ReceiveWindow;
import framework.slidingwindow.SendWindow;
import javafx.util.Pair;

import java.nio.ByteBuffer;

import static framework.transport.ControlFlag.DATA;
import static framework.transport.ControlFlag.SYN;

public class RaspSocket {


    // The address this handler needs to handle communication with.
    private RaspAddress address;
    private final RaspSender raspSender;
    private final int maxPacketSize;


    // ackNr = seq nr. of next expected ACK (other side's seq nr).
    private int seqNr = 0;
    private int ackNr = 0;

    // Tracking of timeout.
    //TODO: timeout = 10x average RTT?
    private long lastTimeReceived = System.currentTimeMillis();

    private ReceiveWindow receiveWindow;
    private SendWindow sendWindow;
    private final Object readLock;
    private final Object writeLock;
    private ByteBuffer leftoverBytes;
    private boolean isConnected = false;

    public RaspSocket(RaspSender sender, RaspAddress address, int maxRaspPacketSize) {
        this.address = address;
        this.raspSender = sender;
        this.maxPacketSize = maxRaspPacketSize;

        this.receiveWindow = new ReceiveWindow(10);
        this.sendWindow = new SendWindow(10);

        this.readLock = new Object();
        this.writeLock = new Object();
    }

    /**
     * Write a byte array to the socket. As data is written, the control flag should be DATA.
     * @param bytes This should be the AppInformation as byte[].
     */
    public void write(byte[] bytes) throws InterruptedException {
        write(bytes, bytes.length);
    }


    public void write(byte[] bytes, int length) throws InterruptedException {
        ByteBuffer fullBuffer = ByteBuffer.wrap(bytes, 0, length);
        write(fullBuffer);
    }

    private void write(ByteBuffer buffer) throws InterruptedException {
        synchronized (writeLock) {
            // empty?
            if (!buffer.hasRemaining()) {
                return;
            }

            // longer than max?
            int lenToRead = Math.min(buffer.remaining(), maxPacketSize);
            byte[] packetArray = new byte[lenToRead];
            buffer.get(packetArray);
            DATA.sendWithFlag(this, packetArray);

            // send the rest.
            write(buffer);
        }
    }

    /**
     * Retrieves the next NoAckRaspPacket in the receiveWindow and removes the header.
     * @return the packet's payload
     */
    public byte[] read(int nBytes) throws InterruptedException {
        synchronized (readLock) {
            return read(ByteBuffer.allocate(nBytes));
        }
    }

    private byte[] read(ByteBuffer readBuffer) throws InterruptedException {
        if (!readBuffer.hasRemaining()) {
            return readBuffer.array();

        } else if (leftoverBytes.hasRemaining()) {
            // If there are remaining bytes from previous read or not fully read packet, read these first.
            int bytesToFill = Math.min(leftoverBytes.remaining(), readBuffer.limit());
            byte[] bytes = new byte[bytesToFill];
            leftoverBytes.get(bytes, 0, bytesToFill);
            readBuffer.put(bytes);

        } else {
            // If there were no remaining bytes, get the next packet (blocks until a packet is available).
            byte[] payload = receiveWindow.getNext();
            int bytesToFill = Math.min(payload.length, readBuffer.limit());
            readBuffer.put(payload, 0, bytesToFill);

            // Fill a byte buffer with the remaining bytes from the payload if it is not fully read.
            int leftovers = payload.length - bytesToFill;
            leftoverBytes = ByteBuffer.allocate(leftovers);
            leftoverBytes.put(payload, bytesToFill, leftovers);
        }

        return read(readBuffer);
    }

    /**
     * Resets the offset of the sending window to resend packets that have not been acknowledged before timeout.
     */
    public void resend() {
        sendWindow.resetOffset();
        while (this.sendWindow.hasNext()) {
            send(this.sendWindow.getNext());
        }
    }

    public void handlePacket(RaspPacket raspPacket) throws InterruptedException {
        // Reset the last time a packet was received to current time.
        setLastTimeReceived(System.currentTimeMillis());

        raspPacket.getHeader().getFlag().respondToFlag(this, raspPacket);
    }

    protected synchronized void offer(NoAckRaspPacket packet) throws InterruptedException {
        if (packet.getHeader().getSeqNr() != this.seqNr) {
            throw new RuntimeException("Wrong sequence number. I dun goofed.");
        }
        sendWindow.offer(packet);
        increaseSeqNr();
        send(packet);
    }

    private void send(NoAckRaspPacket packet) {
        raspSender.getSendQueue().add(new Pair<>(this.address, packet));
    }

    public void increaseSeqNr() {
        this.seqNr++;
    }

    /**
     * Set the ack number (last seq nr. received).
     * Ignore the ack number if higher seq nr. has already arrived.
     */
    public void setAckNr(NoAckRaspPacket packet) {
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

    public SendWindow getSendWindow() {
        return this.sendWindow;
    }

    public ReceiveWindow getReceiveWindow() {
        return this.receiveWindow;
    }

    public RaspAddress getAddress() {
        return address;
    }

    public void setAddress(RaspAddress address) {
        this.address = address;
    }

    public void open() throws InterruptedException {
        NoAckRaspPacket synPacket = new NoAckRaspPacket(new byte[0], seqNr, SYN);
        offer(synPacket);
    }

    public void setConnectedToTrue() {
        this.isConnected = true;
    }
}
