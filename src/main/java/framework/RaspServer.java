package framework;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class RaspServer extends Thread {

    // DatagramSocket with timeout for receiving packets.
    private DatagramSocket socket;
    private static final int TIMEOUT = 50;

    // Thread responsible for sending created packets over shared socket.
    private final RaspSender senderThread;

    // Determine maximum packet size for RaspPacket.
    private static final int UDP_HEADER_LENGTH = 8;
    private static final int IP_HEADER_LENGTH = 20;
    private static final int MAX_RASP_PACKET_SIZE = 65535 - UDP_HEADER_LENGTH - IP_HEADER_LENGTH - RaspHeader.getLength();

    // For server, map clients that have sent SYN, so we know who is allowed to send other requests.
    private HashMap<RaspAddress, RaspConnectionHandler> knownClients = new HashMap<>();
    private boolean running = true;

    public RaspServer(int port) {
        try {
            this.socket = new DatagramSocket(port);
            socket.setSoTimeout(200);
        } catch (SocketException e) {
            System.err.println("Failed to open socket: " + e.getMessage());
            e.printStackTrace();
        }

        senderThread = new RaspSender(socket);
    }

    @Override
    public synchronized void start() {
        super.start();
        this.senderThread.start();
    }

    public void stopMe() throws InterruptedException{
        // Instruct own loop to stop.
        this.running = false;

        // Instruct sender thread to stop and wait for it to finish.
        this.senderThread.stopMe();

        // Wait for this thread to finish.
        this.join();
    }


    @Override
    public void run() {
        while (this.running) {

            // Receive buffer set to the maximum size a packet may have.
            byte[] receiveBuffer = new byte[MAX_RASP_PACKET_SIZE];
            DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                socket.receive(request);
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }

            RaspAddress packetOrigin = new RaspAddress(request.getAddress(), request.getPort());

            // Deserialize. Only creates packet if checksum succeeds.
            RaspPacket raspPacket = null;
            try {
                raspPacket = RaspPacket.deserialize(request);
            } catch (InvalidChecksumException e) {
                System.err.println("Invalid Checksum: " + e.getMessage());
                e.printStackTrace();
            }

            if (raspPacket != null) {
                if (knownClients.containsKey(packetOrigin)) {
                    // Call responsible RaspConnectionHandler.
                    knownClients.get(packetOrigin).handlePacket(raspPacket);

                } else if (raspPacket.getHeader().getFlag() == ControlFlag.SYN) {
                    // Add client to known clients. Ignore unknown clients that do not send the proper control flag.
                    addClient(request.getAddress(), request.getPort()).handlePacket(raspPacket);
                } else if (raspPacket.getHeader().getFlag() == ControlFlag.FIN) {
                    // If the client finishes the connection, it is removed from the known addresses.
                    removeClient(packetOrigin);
                }
            }
        }

        // Once finished, close the socket.
        socket.close();

    }

    /** Add a new client to the mapping of the clients known to a server, occurs after an ACK.
     * @return a new client object*/
    private RaspConnectionHandler addClient(InetAddress address, Integer port) {
        RaspAddress clientAddress = new RaspAddress(address, port);

        RaspConnectionHandler client = new RaspConnectionHandler(this.senderThread.getSendQueue(), clientAddress, MAX_RASP_PACKET_SIZE);
        this.knownClients.put(clientAddress, client);
        return client;
    }

    /** Remove a known client from the mapping of the clients known to a server, occurs after a FIN */
    public void removeClient(RaspAddress clientAddress) {
        this.knownClients.remove(clientAddress);
    }
}
