package framework.transport;

import java.util.Objects;

public class NoAckRaspHeader {

    protected int seqNr;

    protected ControlFlag flag;
    protected int payloadLength;

    public NoAckRaspHeader() {
    }

    public enum HeaderField {

        // Fields of the header have their location and length assigned to them here.
        // Mind that the header length is based the location and length of CHECKSUM.
        SEQ_NR(0, 4),
        ACK_NR(4,4),
        FLAG(8, 1),
        CON_LEN(9, 4),
        CHECKSUM(13, 4);

        private final int loc;
        private final int length;

        HeaderField(int loc, int length) {
            this.loc = loc;
            this.length = length;
        }

        public int getFieldLoc() {
            return loc;
        }
        public int getFieldLength() {
            return length;
        }

    }

    /**
     * Creates a new header for a NoAckRaspPacket.
     */
    public NoAckRaspHeader(int seqNumber, byte[] payload, ControlFlag controlFlag) {
        this.seqNr = seqNumber;
        this.payloadLength = payload.length;
        this.flag = controlFlag;
    }

    // Used when creating RaspHeader from NoAckRaspHeader.
    public NoAckRaspHeader(NoAckRaspHeader oldHeader) {
        this.seqNr = oldHeader.seqNr;
        this.payloadLength = oldHeader.payloadLength;
        this.flag = oldHeader.flag;
    }

    public RaspHeader toRaspHeader(int ackNr, byte[] payload) {
        return new RaspHeader(this, ackNr, payload);
    }

    public static int getLength() {
        // Assumes CHECKSUM is the last field in the header!
        return HeaderField.CHECKSUM.getFieldLoc() + HeaderField.CHECKSUM.getFieldLength();
    }

    public int getSeqNr() {
        return this.seqNr;
    }

    public ControlFlag getFlag() {
        return this.flag;
    }

    public int getPayloadLength() {
        return this.payloadLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoAckRaspHeader that = (NoAckRaspHeader) o;
        return seqNr == that.seqNr &&
                payloadLength == that.payloadLength &&
                flag == that.flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqNr, flag, payloadLength);
    }
}
