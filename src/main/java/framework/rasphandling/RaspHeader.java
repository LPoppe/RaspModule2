package framework.rasphandling;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class RaspHeader {

    private int seqNr;
    private int ackNr;
    private ControlFlag flag;
    private int payloadLength;
    private byte[] checksum;

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
     * Creates a new header for a RaspPacket.
     */
    public RaspHeader(int seqNumber, byte[] payload, ControlFlag controlFlag) {
        this.seqNr = seqNumber;
        this.payloadLength = payload.length;
        this.flag = controlFlag;
    }

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

    public byte[] getHeader(int ackNr, byte[] payload) {
        // Fill in missing fields.
        this.setAckNr(ackNr);
        this.checksum = this.createChecksum(payload);

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

    public static int getLength() {
        // Assumes CHECKSUM is the last field in the header!
        return HeaderField.CHECKSUM.getFieldLoc() + HeaderField.CHECKSUM.getFieldLength();
    }

    public int getSeqNr() {
        return this.seqNr;
    }

    public int getAckNr() {
        return this.ackNr;
    }

    // The AckNr is only set upon retrieving the data for sending to avoid creating deadlocks.
    public void setAckNr(int ackNr) {
        this.ackNr = ackNr;
    }

    public ControlFlag getFlag() {
        return this.flag;
    }

    public int getPayloadLength() {
        return this.payloadLength;
    }

    public byte[] getChecksum() {
        return this.checksum;
    }

}