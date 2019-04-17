package framework.transport;

import framework.InvalidChecksumException;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RaspPacket extends NoAckRaspPacket {

    /**
     * Construct a RaspPacket.
     */
    public RaspPacket(byte[] payload, RaspHeader newHeader) {
        super(payload, newHeader);
    }

    public byte[] serialize() {
        // Content = header + payload.
        byte[] content = new byte[RaspHeader.getLength() + this.payload.length];
        byte[] headerFields = this.getHeader().serialize();
        System.arraycopy(headerFields, 0, content, 0, RaspHeader.getLength());
        System.arraycopy(this.payload, 0, content, RaspHeader.getLength(), this.payload.length);
        return content;
    }

    private byte[] createChecksum() {
        return this.getHeader().createChecksum(this.getPayload());
    }

    public static RaspPacket deserialize(DatagramPacket request) throws InvalidChecksumException {
        byte[] payload = Arrays.copyOfRange(request.getData(), RaspHeader.getLength(), request.getLength());
        ByteBuffer bufferedRaspHeader = ByteBuffer.wrap(request.getData(), 0,
                request.getLength() - payload.length);
        System.out.println("Creating new header");
        RaspHeader header = new RaspHeader(bufferedRaspHeader);
        RaspPacket raspPacket = new RaspPacket(payload, header);
        System.out.println("Recalculating checksum");
        byte[] expectedChecksum = raspPacket.createChecksum();
        System.out.println(header.flag + " received: " + Arrays.toString(header.getChecksum()) + " expected: " + Arrays.toString(expectedChecksum));
        if (Arrays.equals(expectedChecksum, header.getChecksum())) {
            return raspPacket;
        } else {
            throw new InvalidChecksumException();
        }
    }

    public RaspHeader getHeader() {
        return (RaspHeader) this.header;
    }
}
