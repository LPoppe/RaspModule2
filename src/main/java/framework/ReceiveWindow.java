package framework;

public class ReceiveWindow extends Window {

    public ReceiveWindow(int windowSize) {
        super(windowSize);
    }

    public synchronized RaspPacket getNext() {
        if (hasNext()) {
            return pop();
        }
        return null;
    }

    public synchronized int getCurrentAck() {
        // TODO: If wrap-around is used, highestSeq is actually 2^32
        int highestSeq = lowestSeq - 1;
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
