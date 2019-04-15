package framework.slidingwindow;

import framework.rasphandling.RaspPacket;

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

    public synchronized RaspPacket getNext() {
        RaspPacket packet = getByIndex(sendOffset);
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

    @Override
    public void offer(RaspPacket packet) throws InterruptedException {
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
