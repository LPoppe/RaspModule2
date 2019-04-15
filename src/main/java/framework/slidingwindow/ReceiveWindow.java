package framework.slidingwindow;

import framework.rasphandling.RaspPacket;

public class ReceiveWindow extends Window {

    public ReceiveWindow(int windowSize) {
        super(windowSize);
    }

    public synchronized RaspPacket getNext() throws InterruptedException {
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

        for (RaspPacket packet : window) {
            if (packet == null) {
                break;
            } else {
                highestSeq = packet.getHeader().getSeqNr();
            }
        }
        return highestSeq;
    }

}
