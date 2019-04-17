package framework.transport;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.CRC32;

public class RaspHeader extends NoAckRaspHeader{

    // The NoAckRaspHeader does not yet have an ackNr or checksum.
    private int ackNr;
    private byte[] checksum;

    /**
     * Reads the information from a received packet into a new header.
     * @param bufferedHeader the contents of the header in a ByteBuffer.
     */
    public RaspHeader(ByteBuffer bufferedHeader) {
        this.seqNr = bufferedHeader.getInt(HeaderField.SEQ_NR.getFieldLoc());
        this.ackNr = bufferedHeader.getInt(HeaderField.ACK_NR.getFieldLoc());
        this.payloadLength = bufferedHeader.getInt(HeaderField.CON_LEN.getFieldLoc());
        this.flag = ControlFlag.fromInt(bufferedHeader.get(HeaderField.FLAG.getFieldLoc()));
        this.checksum = new byte[HeaderField.CHECKSUM.getFieldLength()];
        // Loop to get checksum values back into the header.
        int location = HeaderField.CHECKSUM.getFieldLoc();
        for ( int i = 0; i < HeaderField.CHECKSUM.getFieldLength(); i++) {
            checksum[i] = bufferedHeader.get(location);
            location++;
        }
    }

    public RaspHeader(NoAckRaspHeader noAck, int ackNr, byte[] payload) {
        super(noAck);
        this.ackNr = ackNr;
        this.checksum = createChecksum(payload);
    }

    public byte[] serialize() {

        ByteBuffer fieldBuf = ByteBuffer.allocate(getLength());

        // Loop because you can't put arrays at a certain position in a byte buffer.
        int i = HeaderField.CHECKSUM.getFieldLoc();
        for (byte b : this.checksum) {
            fieldBuf.put(i, b);
            i++;
        }

        fieldBuf.putInt(HeaderField.CON_LEN.getFieldLoc(), this.payloadLength);
        fieldBuf.put(HeaderField.FLAG.getFieldLoc(), (byte) this.flag.getFlag());
        fieldBuf.putInt(HeaderField.ACK_NR.getFieldLoc(), this.ackNr);
        fieldBuf.putInt(HeaderField.SEQ_NR.getFieldLoc(), this.seqNr);
        return fieldBuf.array();
    }

    /**
     * The checksum calculated over the header fields and a payload.
     * @param payload the payload the checksum needs to be calculated over.
     * @return the checksum value (a long).
     */
    byte[] createChecksum(byte[] payload) {
        CRC32 checksum = new CRC32();
        checksum.update(this.seqNr);
        checksum.update(this.ackNr);
        checksum.update(this.flag.getFlag());
        checksum.update(this.payloadLength);
        checksum.update(payload);
        return longToBytes(checksum.getValue());
    }

    /** Convert the long from a checksum into 4 bytes **/
    private static byte[] longToBytes(long checkValue) {
        byte[] result = new byte[4];
        result[3] = (byte) checkValue;
        result[2] = (byte) (checkValue >>> 8);
        result[1] = (byte) (checkValue >>> 16);
        result[0] = (byte) (checkValue >>> 24);
        return result;
    }


    public int getAckNr() {
        return this.ackNr;
    }
    public byte[] getChecksum() {
        return this.checksum;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RaspHeader that = (RaspHeader) o;
        return ackNr == that.ackNr &&
                Arrays.equals(checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), ackNr);
        result = 31 * result + Arrays.hashCode(checksum);
        return result;
    }


}