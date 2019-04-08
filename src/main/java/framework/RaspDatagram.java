package framework;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.zip.CRC32;

public class RaspDatagram {

    /**
     * @param payload the data to be sent.
     * @param destination the address to send it to.
     * @param destPort the destination port.
     * @param contentFlag the flag set for the packet created.
     */
    public static DatagramPacket createPacket(byte[] payload, InetAddress destination,
                                              int destPort, int seqNumber, ContentFlag contentFlag) {

        byte[] content = createContent(seqNumber, payload, contentFlag);
        return new DatagramPacket(content, content.length, destination, destPort);
    }

    /** Creates a byte array of the header and payload that are contained in the UDP Datagram.
     * @param seqNumber the sequence number.
     *  @param payload the data sent in the packet.
     * @param contentFlag the packet's flag.
     */
    private static byte[] createContent(int seqNumber, byte[] payload, ContentFlag contentFlag) {

        byte[] header = new byte[10];

        // Sequence number
        header[0] = (byte) ((seqNumber & 0xff000000) >> 24);
        header[1] = (byte) ((seqNumber & 0xff0000) >> 16);
        header[2] = (byte) ((seqNumber & 0xff00) >> 8);
        header[3] = (byte) (seqNumber & 0xff);

        // Control flag
        header[4] = (byte) contentFlag.getFlag();

        // Checksum
        CRC32 checksum = new CRC32();
        checksum.update(payload);
        header[5] = (byte) ((checksum.getValue() & 0xff000000) >> 24);
        header[6] = (byte) ((checksum.getValue() & 0xff0000) >> 16);
        header[7] = (byte) ((checksum.getValue() & 0xff00) >> 8);
        header[8] = (byte) (checksum.getValue() & 0xff);

        // Window size
        header[9] = (byte) payload.length;

        // Content = header + payload
        byte[] content = new byte[header.length + payload.length];
        System.arraycopy(header,0, content,0, header.length);
        System.arraycopy(payload,0, content, header.length, payload.length);
        return content;
    }

}
