package framework.rasphandling;

import framework.InvalidChecksumException;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RaspPacket extends NoAckRaspPacket {

    /**
     * Construct a RaspPacket (through NoAckRaspPacket.toRaspPacket()).
     */
    public RaspPacket(byte[] payload, RaspHeader header, int ackNr) {
        super(payload, header);
        header.setAckNr(ackNr);
        createChecksum();
    }

    /**
     * Construct a RaspPacket (through deserialize()).
     */
    public RaspPacket(byte[] payload, RaspHeader header) {
        super(payload, header);
    }

    public byte[] serialize() {
        // Content = header + payload.
        byte[] content = new byte[RaspHeader.getLength() + this.payload.length];
        byte[] headerFields = this.getHeader().getHeader();
        System.arraycopy(headerFields, 0, content, 0, RaspHeader.getLength());
        System.arraycopy(this.payload, 0, content, RaspHeader.getLength(), this.payload.length);
        return content;
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
}
