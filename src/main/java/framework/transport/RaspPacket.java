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
        RaspHeader header = new RaspHeader(bufferedRaspHeader);
        RaspPacket raspPacket = new RaspPacket(payload, header);
        byte[] expectedChecksum = raspPacket.createChecksum();
        if (Arrays.equals(expectedChecksum, header.getChecksum())) {
            return raspPacket;
        } else {
            System.out.println(header.flag + " received: " + Arrays.toString(header.getChecksum()) + " expected: " + Arrays.toString(expectedChecksum));
            System.out.println(Arrays.toString(payload));
            System.out.println(Arrays.toString(raspPacket.getPayload()));
            throw new InvalidChecksumException();
        }
    }

    public RaspHeader getHeader() {
        return (RaspHeader) this.header;
    }
}
