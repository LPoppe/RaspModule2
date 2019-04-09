package framework;

import java.net.DatagramPacket;
import java.util.zip.CRC32;

public class RaspHeader {
    //private final static int LENGTH_POS;

    // sequenceNr
    // ackNr
    // contentFlag
    // dataOffset
    // payloadLength
    // checksum (over entire packet)

    private byte[] fields;


    private int getLength() {
        return fields.length;
    }

    private byte[] getHeader() {
        //this.fields[LENGTH_POS] = getLength();
        return this.fields;
    }

    // read header

}