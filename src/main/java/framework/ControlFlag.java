package framework;

public enum ControlFlag {
    // Options for the flag set in the packet's header. Only one flag is used per packet.

    SYN(0) {
        @Override
        public void respondToFlag(boolean isServer) {
            // Only the server should ever receive a SYN.
            if (!isServer) {
            } else {
            }
            // Retrieve address & port. Add to known addresses.
            // SeqNr ++
            // Send a SYNACK.
        }
    }, ACK(1) {
        @Override
        public void respondToFlag(boolean isServer) {
            // SeqNr/AckNr ++
            // Send new packet if you can + other rules based on sliding window once that is working.
        }
    }, SYNACK(2) {
        @Override
        public void respondToFlag(boolean isServer) {
            // SeqNr ++
            // Only the client should ever receive a SYNACK.
            // Send ACK, with data if you can.
        }
    }, DATA(3) {
        @Override
        public void respondToFlag(boolean isServer) {
            // SeqNr/AckNr ++
            // ACK, with data if you can.
        }
    }, ACKDATA(4) {
        @Override
        public void respondToFlag(boolean isServer) {
            // Send payload onwards to application.
            // SeqNr/AckNr ++
            // Send ACK, with data if you can.
        }
    }, FIN(5) {
        @Override
        public void respondToFlag(boolean isServer) {
            // If client, close connection. Send close onwards to close application.
            // If server, remove client from known addresses. Keep running for potential other clients.
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
     * A method that defines what response should follow based on the ControlFlag in a received DatagramPacket.
     * @param isServer The response may differ between server and client, so this parameter is required to
     *                 enable different behaviour.
     */
    public abstract void respondToFlag(boolean isServer);
}
