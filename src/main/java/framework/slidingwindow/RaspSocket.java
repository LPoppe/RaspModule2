package framework.slidingwindow;

import framework.rasphandling.ControlFlag;
import framework.rasphandling.RaspConnectionHandler;
import framework.rasphandling.RaspPacket;

import java.nio.ByteBuffer;

public class RaspSocket {

    private ReceiveWindow receiveWindow;
    private SendWindow sendWindow;
    private RaspConnectionHandler handler;
    private final Object readLock;
    private final Object writeLock;
    private ByteBuffer leftoverBytes;

    public RaspSocket(RaspConnectionHandler handler) {
        this.receiveWindow = new ReceiveWindow(10);
        this.sendWindow = new SendWindow(10);
        this.handler = handler;
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
            int lenToRead = Math.min(buffer.remaining(), handler.getMaxPacketSize());
            byte[] packetArray = new byte[lenToRead];
            buffer.get(packetArray);
            RaspPacket packet = new RaspPacket(packetArray, handler.getSeqNr(), ControlFlag.DATA);
            handler.increaseSeqNr();
            this.sendWindow.offer(packet);

            // send the rest.
            write(buffer);
        }
    }

    /**
     * Retrieves the next RaspPacket in the receiveWindow and removes the header.
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
            RaspPacket packet = receiveWindow.getNext();
            byte[] payload = packet.getPayload();
            int payloadLength = packet.getPayload().length;
            int bytesToFill = Math.min(payloadLength, readBuffer.limit());
            readBuffer.put(payload, 0, bytesToFill);

            // Fill a byte buffer with the remaining bytes from the payload if it is not fully read.
            int leftovers = payloadLength - bytesToFill;
            leftoverBytes = ByteBuffer.allocate(leftovers);
            leftoverBytes.put(payload, bytesToFill, leftovers);
        }

        return read(readBuffer);
    }

    public void resend() {
        sendWindow.resetOffset();
    }

    public SendWindow getSendWindow() {
        return this.sendWindow;
    }

    public ReceiveWindow getReceiveWindow() {
        return this.receiveWindow;
    }
}
