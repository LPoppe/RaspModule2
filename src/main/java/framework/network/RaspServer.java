package framework.network;

import framework.transport.*;

public class RaspServer extends RaspReceiver {


    /**
     * The server's run() method is inherited from the abstract class RaspReceiver.
     * Methods that differ between server and client are overridden here and called in run.
     * @param port The port the server's socket should use.
     */
    public RaspServer(int port) {
        super(port);
    }

    @Override
    protected void establishConnection() {
        // With the current flow, the client initiates communication,
        // and the server handles the initial messages in relayToHandler().
        // it does not need to do anything in establishConnection().
    }

    @Override
    protected void relayToHandler(RaspPacket raspPacket, RaspAddress packetOrigin) throws InterruptedException {
        if (raspPacket != null) {
            if (knownConnections.containsKey(packetOrigin)) {
                // Call responsible RaspSocket.
                System.out.println("!!!!!!!" + 7);
                System.out.println(raspPacket.getHeader().getFlag());
                knownConnections.get(packetOrigin).handlePacket(raspPacket);
            } else if (raspPacket.getHeader().getFlag() == ControlFlag.SYN) {
                System.out.println("!!!!!!!" + 8);
                // Add client to known clients. Ignore unknown clients that do not send the proper control flag.
                addConnection(packetOrigin.getAddress(), packetOrigin.getPort()).handlePacket(raspPacket);
                System.out.println("!!!!!!!" + 9);
                this.knownConnections.get(packetOrigin).setIsConnected(true);
                System.out.println(this.knownConnections.get(packetOrigin).isConnected());
                System.out.println("!!!!!!!" + 10);
            } else if (raspPacket.getHeader().getFlag() == ControlFlag.FIN) {
                // If the client finishes the connection, it is removed from the known addresses.
                removeClient(packetOrigin);
            }
        }
    }

    /** Remove a known client from the mapping of the clients known to a server, occurs after a FIN */
    public void removeClient(RaspAddress clientAddress) {
        this.knownConnections.remove(clientAddress);
    }
}
