package framework;

import java.net.SocketException;
import java.util.concurrent.TimeUnit;

public enum ControlFlag {
    // Options for the flag set in the packet's header. Only one flag is used per packet.
    SYN(0) {
        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) {
            // Only the server should ever receive a SYN. Clients should ignore it.
            if (ship.isServer()) {
                // Retrieve address & port. Add to known addresses if not already known.
                ship.getKnownClients().put(packet.getAddress(), packet.getPort());
                // SeqNr ++
                ship.setSeqAndAck(packet);
                // Send a SYNACK.
                try {
                    SYNACK.sendWithFlag(transferController, ship, packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getFillData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.SYN);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
        }

    }, ACK(1) {
        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) {
            // SeqNr/AckNr ++
            ship.setSeqAndAck(packet);
            // We should be allowed to send data now.
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getFillData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.ACK);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
        }
    }, SYNACK(2) {
        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) {
            // Only the client should ever receive a SYNACK.
            System.out.println("Test");
            if (!ship.isServer()) {
                // SeqNr ++
                ship.setSeqAndAck(packet);
                ship.setDestAddress(packet.getAddress());
                ship.setDestPort(packet.getPort());

                System.out.println("Found connection.");

                // Ignore packets from other sources.
                ship.getSocket().connect(ship.getDestAddress(), ship.getDestPort());
                try {
                    ship.getSocket().setBroadcast(false);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getFillData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.SYNACK);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
        }
    }, DATA(3) {

        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) {
            // SeqNr/AckNr ++
            ship.setSeqAndAck(packet);
            // May be ACK or ACKDATA.
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getNextData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.DATA);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
        }

    }, ACKDATA(4) {

        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet){
            // SeqNr/AckNr ++
            ship.setSeqAndAck(packet);
            // May be ACK or ACKDATA.
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getNextData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.ACKDATA);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
        }

    }, FIN(5) {
        @Override
        public void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) {
            if (ship.isServer()) {
                // If server, remove client from known addresses. Keep running for potential other clients.
                ship.removeClient(packet.getAddress(), packet.getPort());
            } else {
                // If client, close connection.
                ship.setReceivedFIN();
            }
        }

        @Override
        public boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException {
            RaspPacket nextPacket = new RaspPacket(transferController.getFillData(), packet.getAddress(), packet.getPort(), ship.getSeqNr(), ship.getAckNr(), ControlFlag.FIN);
            return transferController.getSendQueue().offer(nextPacket, 10, TimeUnit.MILLISECONDS);
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
    public abstract void respondToFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException;
    public abstract boolean sendWithFlag(TransferController transferController, ShippingAndReceiving ship, RaspPacket packet) throws InterruptedException;
}
