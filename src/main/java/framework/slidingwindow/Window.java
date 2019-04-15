package framework.slidingwindow;

import framework.rasphandling.RaspPacket;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class Window {
    protected final int size;
    protected int lowestSeq = 0;
    protected int offset = 0;
    protected RaspPacket[] window;

    // Couldn't find the appropriate lock. This works.
    private final Object lockToken = new Object();
    protected ArrayBlockingQueue<Object> offerNotifier;

    public Window(int windowSize) {
        this.size = windowSize;
        this.window = new RaspPacket[windowSize];

    }

    protected RaspPacket getByIndex(int i) {
        if (i >= size) {
            // TODO: ERROR MESSAGE.
            throw new IndexOutOfBoundsException("");
        }

        return window[getInternalIndex(i)];
    }

    protected RaspPacket getBySeqNr(int seqNr) {
        return window[seqNr - lowestSeq];
    }

    protected void setByIndex(int i, RaspPacket packet) {
        window[getInternalIndex(i)] = packet;
    }

    protected void setBySeqNr(int seqNr, RaspPacket packet) {
        setByIndex(seqNr - lowestSeq, packet);
    }

    protected int getInternalIndex(int i) {
        return offset + i % size;
    }

    protected RaspPacket pop() {
        RaspPacket packet = getByIndex(0);
        setByIndex(0, null);
        lowestSeq++;
        offset = offset + 1 % 5;
        return packet;
    }

    protected boolean hasNext() {
        return window[getInternalIndex(0)] != null;
    }

    protected boolean isSeqInWindow(int seqNr) {
        return seqNr >= lowestSeq && seqNr < lowestSeq + size;
    }

    public synchronized void offer(RaspPacket packet) throws InterruptedException {
        int seqNr = packet.getHeader().getSeqNr();
        if (isSeqInWindow(seqNr)) {
            // TODO: What if packet with same seq nr. but different content arrives?
            setBySeqNr(seqNr, packet);
            this.offerNotifier.offer(lockToken);
        } else {
            throw new IndexOutOfBoundsException("Invalid sequence number while adding to queue.");
        }
    }

}
