package framework.transport;

public enum ControlFlag {
    // Options for the flag set in the packet's header. Only one flag is used per packet.
    SYN(0) {
        @Override
        public void respondToFlag(RaspSocket raspSocket, RaspPacket packet) throws InterruptedException {
            raspSocket.setAckNr(packet);
            // Only the server should ever receive a SYN. Clients should ignore it.
            // Server responds to client by sending a SYNACK to provide its address and port.
            SYNACK.sendWithFlag(raspSocket, new byte[0]);
        }

        @Override
        public void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException {
            // Sent directly in RaspClient class in establishConnection().
        }

    }, ACK(1) {
        @Override
        public void respondToFlag(RaspSocket raspSocket, RaspPacket packet) {
            raspSocket.setAckNr(packet);
        }

        @Override
        public void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException {
            NoAckRaspPacket ackPacket = new NoAckRaspPacket(new byte[0], raspSocket.getSeqNr(), ACK);
            raspSocket.offer(ackPacket);
        }
    }, SYNACK(2) {
        @Override
        public void respondToFlag(RaspSocket raspSocket, RaspPacket packet) {
            raspSocket.setAckNr(packet);
            // Only the client should ever receive a SYNACK. This is handled directly in the receiver class.
            // After a SYNACK it is OK to send data.
        }

        @Override
        public void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException {
            NoAckRaspPacket synackPacket = new NoAckRaspPacket(new byte[0], raspSocket.getSeqNr(), SYNACK);
            raspSocket.offer(synackPacket);
        }
    }, DATA(3) {
        @Override
        public void respondToFlag(RaspSocket raspSocket, RaspPacket packet) throws InterruptedException {
            raspSocket.setAckNr(packet);
            if (!raspSocket.getSendWindow().hasNext()) {
                ACK.sendWithFlag(raspSocket, new byte[0]);
            }
        }

        @Override
        public void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException {
            NoAckRaspPacket packet = new NoAckRaspPacket(packetArray, raspSocket.getSeqNr(), ControlFlag.DATA);
            raspSocket.offer(packet);
        }
    },
        FIN(4) {
        @Override
        public void respondToFlag(RaspSocket raspSocket, RaspPacket packet) {
            // Responding to a FIN is handled by the receiver class.
            // No further communication is necessary once a FIN has been received.
        }

        @Override
        public void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException {
            NoAckRaspPacket finPacket = new NoAckRaspPacket(new byte[0], raspSocket.getSeqNr(), FIN);
            raspSocket.offer(finPacket);
        }
    };

    private final int flag;
    ControlFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public static ControlFlag fromInt(int value) {
        for (ControlFlag flag : ControlFlag.values()) {
            if (flag.getFlag() == value) {
                return flag;
            }
        }
        return null;
    }


    /**
     * A method that defines what response should follow based on the ControlFlag in a received packet,
     * based on the control flag contained in its header.
     */
    public abstract void respondToFlag(RaspSocket raspSocket, RaspPacket packet) throws InterruptedException;
    public abstract void sendWithFlag(RaspSocket raspSocket, byte[] packetArray) throws InterruptedException;
}
