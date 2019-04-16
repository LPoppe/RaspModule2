package framework.network;

import framework.rasphandling.NoAckRaspPacket;
import framework.rasphandling.ControlFlag;
import framework.rasphandling.RaspAddress;
import framework.rasphandling.RaspPacket;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;

import static framework.rasphandling.ControlFlag.SYN;

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
    protected void discoverServer() {
        try {
            socket.setBroadcast(true);
            // Server port should always be 8001.
            RaspAddress broadcastAddress = new RaspAddress(InetAddress.getLocalHost(), 8001);

            int maxRetries = 5;
            for (int i = 0; i < maxRetries; i++) {
                System.out.println("Sending discover message.");

                DatagramPacket probe = new DatagramPacket(new byte[0], 0, InetAddress.getLocalHost(), 8001);
                socket.send(probe);

                // Receive buffer set to the maximum size a packet may have.
                byte[] receiveBuffer = new byte[1000];
                DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                socket.receive(response);

                // Once successful, escape the loop.
                addConnection(response.getAddress(), response.getPort());
                // No longer accept other addresses.
                socket.connect(response.getAddress(), response.getPort());
                break;
            }

        } catch (SocketTimeoutException e) {
            System.out.println("No response.");
        } catch (UnknownHostException ex) {
            System.err.println("Host unknown: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    protected void relayToHandler(RaspPacket raspPacket, RaspAddress packetOrigin) {
        if (raspPacket != null) {
            if (knownConnections.containsKey(packetOrigin)) {
                // Call responsible RaspSocket (should always be the server for clients).
                knownConnections.get(packetOrigin).handlePacket(raspPacket);
            } else if (raspPacket.getHeader().getFlag() == ControlFlag.SYNACK) {
                // Connect to the server if it has not yet done so.
                addConnection(packetOrigin.getAddress(), packetOrigin.getPort()).handlePacket(raspPacket);
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
