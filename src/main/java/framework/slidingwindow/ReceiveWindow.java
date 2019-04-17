package framework.slidingwindow;

import framework.transport.NoAckRaspPacket;

public class ReceiveWindow extends Window {

    public ReceiveWindow(int windowSize) {
        super(windowSize);
    }

    public synchronized byte[] getNext() throws InterruptedException {
        while(true) {
            offerNotifier.take();
            if (hasNext()) {
                return pop();
            }
        }
    }

    public synchronized int getCurrentAck() {
        int highestSeq = lowestSeq - 1;
        if (highestSeq == -1) {
            // If sequence numbers just wrapped around, highestSeq is actually 2^32!
            highestSeq = Integer.MAX_VALUE;
        }

        for (NoAckRaspPacket packet : window) {
            if (packet == null) {
                break;
            } else {
                highestSeq = packet.getHeader().getSeqNr();
            }
        }
        return highestSeq;
    }

    protected byte[] pop() {
        NoAckRaspPacket packet = getByIndex(0);
        setByIndex(0, null);
        lowestSeq++;
        offset = offset + 1 % 5;
        return packet.getPayload();
    }

    protected NoAckRaspPacket getByIndex(int i) {
        if (i >= size) {
            throw new IndexOutOfBoundsException("Attempt at getting index outside of receive window bounds.");
        }

        return window[getInternalIndex(i)];
    }
}
