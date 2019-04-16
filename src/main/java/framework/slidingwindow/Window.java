package framework.slidingwindow;

import framework.transport.NoAckRaspPacket;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class Window {
    protected final int size;
    protected int lowestSeq = 0;
    protected int offset = 0;
    protected final NoAckRaspPacket[] window;

    // Couldn't find the appropriate lock. This works.
    private final Object lockToken = new Object();
    protected final ArrayBlockingQueue<Object> offerNotifier;

    public Window(int windowSize) {
        this.size = windowSize;
        this.window = new NoAckRaspPacket[windowSize];
        this.offerNotifier = new ArrayBlockingQueue<>(1);
    }

    protected NoAckRaspPacket getBySeqNr(int seqNr) {
        return window[seqNr - lowestSeq];
    }

    protected void setByIndex(int i, NoAckRaspPacket packet) {
        window[getInternalIndex(i)] = packet;
    }

    protected void setBySeqNr(int seqNr, NoAckRaspPacket packet) {
        setByIndex(seqNr - lowestSeq, packet);
    }

    protected int getInternalIndex(int i) {
        return offset + i % size;
    }



    public boolean hasNext() {
        return window[getInternalIndex(0)] != null;
    }

    protected boolean isSeqInWindow(int seqNr) {
        return seqNr >= lowestSeq && seqNr < lowestSeq + size;
    }

    public synchronized void offer(NoAckRaspPacket packet) throws InterruptedException {
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
