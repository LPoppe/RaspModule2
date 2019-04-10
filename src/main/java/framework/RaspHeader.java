package framework;

import java.nio.ByteBuffer;
import java.util.Arrays;
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

    // Constructor for creation of packet.
    public RaspHeader(int seqNumber, int ackNumber, byte[] payload, ControlFlag controlFlag) {
        this.seqNr = seqNumber;
        this.ackNr = ackNumber;
        this.payloadLength = payload.length;
        this.flag = controlFlag;
        this.checksum = createChecksum(payload);
    }

    /**
     * Creates a header object from the data of a DatagramPacket received.
     * @param bufferedHeader the contents of the header in a ByteBuffer.
     */
    public RaspHeader(ByteBuffer bufferedHeader) {
        this.seqNr = bufferedHeader.getInt(HeaderField.SEQ_NR.getFieldLoc());
        this.ackNr = bufferedHeader.getInt(HeaderField.ACK_NR.getFieldLoc());
        this.payloadLength = bufferedHeader.getInt(HeaderField.CON_LEN.getFieldLoc());
        this.flag = ControlFlag.fromInt(bufferedHeader.get(HeaderField.FLAG.getFieldLoc()));
    }

    public byte[] getHeader() {
        ByteBuffer fieldBuf = ByteBuffer.allocate(getLength());
        fieldBuf.put(this.checksum, HeaderField.CHECKSUM.getFieldLoc(), HeaderField.CHECKSUM.getFieldLength());
        fieldBuf.putInt(HeaderField.CON_LEN.getFieldLoc(), this.payloadLength);
        fieldBuf.putInt(HeaderField.FLAG.getFieldLoc(), this.flag.getFlag());
        fieldBuf.putInt(HeaderField.ACK_NR.getFieldLoc(), this.ackNr);
        fieldBuf.putInt(HeaderField.SEQ_NR.getFieldLoc(), this.seqNr);
        return fieldBuf.array();
    }

    /**
     * The checksum is calculated over only the payload.
     * @param payload the payload the checksum needs to be calculated over.
     * @return the checksum value (a long).
     */
    private static byte[] createChecksum(byte[] payload) {
        CRC32 checksum = new CRC32();
        checksum.update(payload);
        return longToBytes(checksum.getValue());
    }

    /**
     * Compares a new calculation of the checksum over a payload with that contained within the header of the packet.
     * @param packet the received packet.
     * @return true if checksum is validated.
     */
    public static boolean testChecksum(RaspPacket packet) {
        byte[] payload = packet.getPayload();
        byte[] expected = createChecksum(payload);
        return Arrays.equals(expected, packet.getHeader().checksum);
    }

    /** Convert the long from a checksum into 4 bytes **/
    private static byte[] longToBytes(long checkValue) {
        byte[] result = new byte[4];
        result[3] = (byte) checkValue;
        result[2] = (byte) (checkValue >>> 8);
        result[1] = (byte) (checkValue >>> 16);
        result[0] = (byte) (checkValue >>> 32);
        return result;
    }

    public static int getLength() {
        return HeaderField.CHECKSUM.getFieldLoc() + HeaderField.CHECKSUM.getFieldLength();
    }

    public int getSeqNr() {
        return this.seqNr;
    }

    public int getAckNr() {
        return this.ackNr;
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