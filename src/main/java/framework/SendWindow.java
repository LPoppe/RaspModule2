package framework;

public class SendWindow extends Window {

    private int sendOffset = 0;

    public SendWindow(int windowSize) {
        super(windowSize);
    }

    public synchronized void resetOffset() {
        sendOffset = 0;
    }

    public synchronized RaspPacket getNext() {
        RaspPacket packet =  getByIndex(sendOffset);
        if (packet == null) {
            return null;
        } else {
            sendOffset++;
            return packet;
        }
    }

    public synchronized void ackPacket(int seqNr) {
        if (isSeqInWindow(seqNr) && getBySeqNr(seqNr) != null) {
            int nToPop = seqNr - lowestSeq + 1;
            for (int i = 0; i < nToPop; i++) {
                pop();
            }
        } else {
            throw new IndexOutOfBoundsException("Invalid sequence number ACKed.");
        }
    }
}
