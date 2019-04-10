package framework;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class RaspPacket {

    public static final int MAX_PACKET_SIZE = 1024;

    private InetAddress destination;
    private int destPort;
    private byte[] payload;
    private RaspHeader header;

    /**
     * Construct a RaspPacket filling the contents.
     */
    public RaspPacket(byte[] payload, InetAddress destination,
                      int destPort, int seqNumber, int ackNumber, ControlFlag controlFlag) {
        this.destination = destination;
        this.destPort = destPort;
        this.payload = payload;
        this.header = new RaspHeader(seqNumber, ackNumber, payload, controlFlag);
    }

    /**
     * Construct a RaspPacket based on a (received) DatagramPacket.
     */
    public RaspPacket(DatagramPacket packet) {
        this.destination = packet.getAddress();
        this.destPort = packet.getPort();

        ByteBuffer bufferedRaspPayload = ByteBuffer.wrap(packet.getData(),
                packet.getOffset() + RaspHeader.getLength(),
                packet.getLength() - packet.getOffset() - RaspHeader.getLength());
        this.payload = bufferedRaspPayload.array();

        ByteBuffer bufferedRaspHeader = ByteBuffer.wrap(packet.getData(),
                packet.getOffset(),
                packet.getLength() - packet.getOffset() - this.payload.length);
        this.header = new RaspHeader(bufferedRaspHeader);
    }

    /**
     * Creates a final DatagramPacket from the information in this RaspPacket.
     * @return a DatagramPacket to be sent by ShippingAndReceiving.
     */
    public DatagramPacket createPacket() {
        // Content = header + payload.
        byte[] content = new byte[RaspHeader.getLength() + this.payload.length];
        System.arraycopy(header.getHeader(),0, content,0, RaspHeader.getLength());
        System.arraycopy(this.payload,0, content, RaspHeader.getLength(), this.payload.length);
        return new DatagramPacket(content, content.length, destination, destPort);
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public RaspHeader getHeader() {
        return this.header;
    }

}
