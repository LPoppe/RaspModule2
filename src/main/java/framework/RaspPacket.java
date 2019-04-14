package framework;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RaspPacket {

    private byte[] payload;
    private RaspHeader header;

    /**
     * Construct a RaspPacket for sending.
     */
    public RaspPacket(byte[] payload, int seqNumber, int ackNumber, ControlFlag controlFlag) {
        this.payload = payload;
        this.header = new RaspHeader(seqNumber, ackNumber, payload, controlFlag);
    }

    /**
     * Construct a RaspPacket from received content.
     */
    public RaspPacket(byte[] payload, RaspHeader header) {
        this.payload = payload;
        this.header = header;
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
            throw new InvalidChecksumException();
        }
    }

    private byte[] createChecksum() {
        return this.getHeader().createChecksum(this.getPayload());
    }

    public byte[] serialize() {
        // Content = header + payload.
        byte[] content = new byte[RaspHeader.getLength() + this.payload.length];
        System.arraycopy(this.getHeader().getHeader(), 0, content, 0, RaspHeader.getLength());
        System.arraycopy(this.payload, 0, content, RaspHeader.getLength(), this.payload.length);
        return content;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public RaspHeader getHeader() {
        return this.header;
    }

}
