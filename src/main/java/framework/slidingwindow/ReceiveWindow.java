package framework.slidingwindow;

import framework.rasphandling.RaspPacket;

public class ReceiveWindow extends Window {

    private RaspPacket[] window;

    public ReceiveWindow(int windowSize) {
        super(windowSize);
        this.window = new RaspPacket[windowSize];
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

    protected RaspPacket pop() {
        RaspPacket packet = getByIndex(0);
        setByIndex(0, null);
        lowestSeq++;
        offset = offset + 1 % 5;
        return packet;
    }

    protected RaspPacket getByIndex(int i) {
        if (i >= size) {
            // TODO: ERROR MESSAGE.
            throw new IndexOutOfBoundsException("");
        }

        return window[getInternalIndex(i)];
    }
}