package framework.rasphandling;

import java.util.Arrays;
import java.util.Objects;

public class NoAckRaspPacket {

    protected byte[] payload;
    protected RaspHeader header;

    /**
     * Construct a NoAckRaspPacket for sending.
     */
    public NoAckRaspPacket(byte[] payload, int seqNumber, ControlFlag controlFlag) {
        this.payload = payload;
        this.header = new RaspHeader(seqNumber, payload, controlFlag);
    }

    public NoAckRaspPacket(byte[] payload, RaspHeader header) {
        this.payload = payload;
        this.header = header;
    }


    public RaspPacket toRaspPacket(int ackNr) {
        return new RaspPacket(this.payload, this.header, ackNr);
    }

    protected byte[] createChecksum() {
        return this.getHeader().createChecksum(this.getPayload());
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public RaspHeader getHeader() {
        return this.header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoAckRaspPacket that = (NoAckRaspPacket) o;
        return Arrays.equals(payload, that.payload) &&
                header.equals(that.header);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(header);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
