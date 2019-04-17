package framework.network;

import framework.transport.ControlFlag;
import framework.transport.RaspAddress;
import framework.transport.RaspPacket;

import java.io.IOException;
import java.net.*;

public class RaspClient extends RaspReceiver {

    /**
     * The client's run() method is inherited from the abstract class RaspReceiver.
     * Methods that differ between server and client are overridden here and called in run.
     * @param port The port the client's socket should use.
     */
    public RaspClient(int port) {
        super(port);
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    protected void establishConnection() {
        try {
            socket.setBroadcast(true);
            // Server listens on port 8001.
            RaspAddress broadcastAddress = new RaspAddress(InetAddress.getLocalHost(), 8001);
            addConnection(broadcastAddress.getAddress(), broadcastAddress.getPort());
            // Tell the RaspSocket to send a SYN.
            System.out.println(this.knownConnections.get(broadcastAddress));
            this.knownConnections.get(broadcastAddress).open();

        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while establishing connection to server. " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void relayToHandler(RaspPacket raspPacket, RaspAddress packetOrigin) throws InterruptedException {
        if (raspPacket != null) {
            if (knownConnections.containsKey(packetOrigin) && raspPacket.getHeader().getFlag() != ControlFlag.SYNACK) {
                // Call responsible RaspSocket (should always be the server for clients).
                knownConnections.get(packetOrigin).handlePacket(raspPacket);
            } else if (raspPacket.getHeader().getFlag() == ControlFlag.SYNACK) {
                try {
                    // Replace the broadcast address with a direct connection.
                    RaspAddress broadcastAddress = new RaspAddress(InetAddress.getLocalHost(), 8001);
                    this.knownConnections.get(broadcastAddress).setAddress(packetOrigin);
                    this.knownConnections.get(packetOrigin).setIsConnected(true);
                    socket.connect(packetOrigin.getAddress(), packetOrigin.getPort());
                    this.knownConnections.get(packetOrigin).handlePacket(raspPacket);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                // From now on, only accept messages from this server.
                socket.connect(packetOrigin.getAddress(), packetOrigin.getPort());
            } else if (raspPacket.getHeader().getFlag() == ControlFlag.FIN) {
                // If the server were to ever send a FIN, the client should close.
                try {
                    this.stopMe();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
