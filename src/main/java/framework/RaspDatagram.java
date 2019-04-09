package framework;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

public class RaspDatagram {

    public static final int MAX_PACKET_SIZE = 1024;
    private static int HEADER_SIZE = 10;
    /**
     * @param payload the data to be sent.
     * @param destination the address to send it to.
     * @param destPort the destination port.
     * @param controlFlag the flag set for the packet created.
     */
    public static DatagramPacket createPacket(byte[] payload, InetAddress destination,
                                              int destPort, int seqNumber, ControlFlag controlFlag) {

        byte[] content = createContent(seqNumber, payload, controlFlag);
        return new DatagramPacket(content, content.length, destination, destPort);
    }

    /** Creates a byte array of the header and payload that are contained in the UDP Datagram.
     * @param seqNumber the sequence number.
     *  @param payload the data sent in the packet.
     * @param controlFlag the packet's flag.
     */
    private static byte[] createContent(int seqNumber, byte[] payload, ControlFlag controlFlag) {

        byte[] header = new byte[HEADER_SIZE];

        // Sequence number
        header[0] = (byte) ((seqNumber & 0xff000000) >> 24);
        header[1] = (byte) ((seqNumber & 0xff0000) >> 16);
        header[2] = (byte) ((seqNumber & 0xff00) >> 8);
        header[3] = (byte) (seqNumber & 0xff);

        // Control flag
        header[4] = (byte) controlFlag.getFlag();

        // Checksum
        CRC32 checksum = new CRC32();
        checksum.reset();
        checksum.update(payload);
        ByteBuffer checkBuf = ByteBuffer.allocate(8);
        byte[] sum = checkBuf.putLong(checksum.getValue()).array();
        System.out.println(Arrays.toString(sum));
        header[5] = sum[4];
        header[6] = sum[5];
        header[7] = sum[6];
        header[8] = sum[7];
//        System.out.println(String.format("Should be: %s %s %s %s", header[5], header[6], header[7], header[8]));
        // Payload length
        header[9] = (byte) payload.length;

        // Content = header + payload
        byte[] content = new byte[header.length + payload.length];
        System.arraycopy(header,0, content,0, header.length);
        System.arraycopy(payload,0, content, header.length, payload.length);
        return content;
    }

    private long createChecksum(byte[] packet) {
        CRC32 checksum = new CRC32();
        checksum.update(packet);
        return checksum.getValue();
    }

    public static boolean testChecksum(DatagramPacket input) {
        byte[] data = new byte[input.getLength()];
        System.arraycopy(input.getData(), input.getOffset(), data, 0, input.getLength());

        byte[] payload = new byte[data.length - HEADER_SIZE];
        System.arraycopy(data, HEADER_SIZE, payload, 0, data.length - HEADER_SIZE);

        byte[] receivedCheckBytes = new byte[4];
        System.arraycopy(data, 5, receivedCheckBytes, 0, 4);
        System.out.println("Received: " + Arrays.toString(receivedCheckBytes));
        ByteBuffer receivedChecksum = ByteBuffer.wrap(receivedCheckBytes);

        CRC32 checksum = new CRC32();
        checksum.update(payload);
        long expected = checksum.getValue();
        long actual = (long) receivedChecksum.getInt() & 0xffffffffL;
        return expected == actual;
    }

    /** Extract the payload from the packet.
     * @param input the full packet including headers
     * @return only the payload part of the byte [].
     */
    public static byte[] getPayload(DatagramPacket input) {
        return null;
    }
}
