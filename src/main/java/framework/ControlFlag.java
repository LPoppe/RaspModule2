package framework;

public enum ControlFlag {
    // Options for the flag set in the packet's header. Only one flag is used per packet.
    SYN(0) {
        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet) {
            // Only the server should ever receive a SYN. Clients should ignore it.
            // Server responds to client by sending a SYNACK to provide its address and port.
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
        }

    }, ACK(1) {
        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet) {
            // SeqNr/AckNr ++
            // We should be allowed to send data now.
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
        }
    }, SYNACK(2) {
        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet) {
            // Only the client should ever receive a SYNACK.
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
        }
    }, DATA(3) {

        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet) {
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
        }

    }, ACKDATA(4) {

        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet){
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
        }

    }, FIN(5) {
        @Override
        public void respondToFlag(RaspConnectionHandler handler, RaspPacket packet) {
        }

        @Override
        public boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException {
            return false;
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
    public abstract void respondToFlag(RaspConnectionHandler handler, RaspPacket packet);
    public abstract boolean sendWithFlag(RaspConnectionHandler handler) throws InterruptedException;
}
