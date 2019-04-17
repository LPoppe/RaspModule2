package framework.slidingwindow;

import framework.transport.NoAckRaspPacket;

import java.util.concurrent.ArrayBlockingQueue;

public class SendWindow extends Window {

    private int sendOffset = 0;

    // Couldn't find the appropriate lock. This works.
    private final Object lockToken = new Object();
    private ArrayBlockingQueue<Object> freeSpaceNotifier;

    public SendWindow(int windowSize) {
        super(windowSize);
        this.freeSpaceNotifier = new ArrayBlockingQueue<>(1);
    }

    public synchronized void resetOffset() {
        sendOffset = 0;
    }

    public synchronized NoAckRaspPacket getNext() {
        NoAckRaspPacket packet = getByIndex(sendOffset);
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
                this.freeSpaceNotifier.offer(lockToken);
            }
        } else {
            throw new IndexOutOfBoundsException("Invalid sequence number ACKed.");
        }
    }

    protected NoAckRaspPacket pop() {
        NoAckRaspPacket packet = getByIndex(0);
        setByIndex(0, null);
        lowestSeq++;
        offset = offset + 1 % 5;
        return packet;
    }

    protected NoAckRaspPacket getByIndex(int i) {
        if (i >= size) {
            throw new IndexOutOfBoundsException("Attempt at getting index outside of receive window bounds.");
        }
        return window[getInternalIndex(i)];
    }

    @Override
    public void offer(NoAckRaspPacket packet) throws InterruptedException {
        this.freeSpaceNotifier.poll();

        if (this.isFull()) {
            this.freeSpaceNotifier.take();
        }

        // TODO: We want to check if packet will take up the next  free spot in the SendWindow.
        //  Currently, if the logic calling offer() is wrong, we might end up replacing
        //  an existing packet or leave an empty spot.
        super.offer(packet);
    }

    private boolean isFull() {
        return getByIndex(window.length - 1) != null;
    }

}
