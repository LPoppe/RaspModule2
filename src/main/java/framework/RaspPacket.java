package framework;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RaspPacket {

    public static final int MAX_PACKET_SIZE = 1024;

    private InetAddress address;
    private int port;
    private byte[] payload;
    private RaspHeader header;

    /**
     * Construct a RaspPacket filling the contents.
     */
    public RaspPacket(byte[] payload, InetAddress address,
                      int port, int seqNumber, int ackNumber, ControlFlag controlFlag) {
        this.address = address;
        this.port = port;
        this.payload = payload;
        this.header = new RaspHeader(seqNumber, ackNumber, payload, controlFlag);
    }

    /**
     * Construct a RaspPacket based on a (received) DatagramPacket.
     */
    public RaspPacket(DatagramPacket packet) {
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.payload = Arrays.copyOfRange(packet.getData(), RaspHeader.getLength(), packet.getLength());
        ByteBuffer bufferedRaspHeader = ByteBuffer.wrap(packet.getData(), 0,
                packet.getLength() - this.payload.length);
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
        return new DatagramPacket(content, content.length, address, port);
    }

    public static DatagramPacket createPacketFromPacket(RaspPacket packet) {
        // Content = header + payload.
        byte[] content = new byte[RaspHeader.getLength() + packet.payload.length];
        System.arraycopy(packet.getHeader().getHeader(),0, content,0, RaspHeader.getLength());
        System.arraycopy(packet.payload,0, content, RaspHeader.getLength(), packet.payload.length);
        return new DatagramPacket(content, content.length, packet.getAddress(), packet.getPort());
    }

    public InetAddress getAddress() { return this.address;}

    public int getPort() { return this.port; }

    public byte[] getPayload() {
        return this.payload;
    }

    public RaspHeader getHeader() {
        return this.header;
    }

}
