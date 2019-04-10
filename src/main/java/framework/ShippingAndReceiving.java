package framework;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ShippingAndReceiving extends Thread {
    private static final int TIMEOUT = 50;
    private DatagramSocket socket;
    private int seqNr = 0;
    private int ackNr = 0;

    private boolean isServer;
    private boolean reachedTimeOut = false;
    private boolean receivedFIN = false;

    // The client connects to a single server and ignores all other messages.
    private InetAddress destAddress;
    private int destPort;

    // For server, map clients that have sent SYN, so we know who is allowed to send other requests.
    private HashMap<InetAddress, Integer> knownClients = new HashMap<>();

    private final LinkedBlockingQueue sendQueue;
    private final LinkedBlockingQueue receiveQueue;

    /**
     * File sender thread that can be used by the client and server to send files.
     */
    public ShippingAndReceiving(boolean startAsServer, int portNr) {
        try {
            socket = new DatagramSocket(portNr);
            this.isServer = startAsServer;
        } catch (SocketException e) {
            System.err.println("Failed to open socket: " + e.getMessage());
            e.printStackTrace();
        }
        sendQueue = new LinkedBlockingQueue();
        receiveQueue = new LinkedBlockingQueue();
    }

    public void run() {
        // TODO: Add a time-out to the connection.
        // TODO: Allow reconnecting, if we manage.

        if (!isServer) {
            // Start a specific connection if client.
            establishConnection();
        }

        // Continue until timeout or FIN.
        while (!reachedTimeOut && !receivedFIN) {
            try {
                socket.setSoTimeout(TIMEOUT);

                // First check receiving.
                receive();
                // Then send, if necessary.
                ship();

            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Once finished, close the socket.
        socket.close();
    }

    private void receive() {
        // Receive buffer set to the maximum size a packet may have.
        byte[] receiveBuffer = new byte[RaspPacket.MAX_PACKET_SIZE];
        DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            socket.receive(request);
        } catch (SocketTimeoutException e){
            // TODO: If I could use a non-blocking channel or something, that might be nicer.
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        }

        // Deal with received message if the checksum and sequence number are correct, otherwise discard.
        RaspPacket received = new RaspPacket(request);
        if (request.getData() != null && packetOK(received)) {
            received.getHeader().getFlag().respondToFlag(isServer);
            System.out.println("Received: " + Arrays.toString(request.getData()));
        }
    }

    private void ship() {
        byte[] data = new byte[1];
        data[0] = (byte) 101;
        RaspPacket nextPacket = new RaspPacket(data, destAddress, destPort, seqNr, ackNr, ControlFlag.DATA);
        DatagramPacket fullPacket = nextPacket.createPacket();
        try {
            socket.send(fullPacket);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void establishConnection() {
        // TODO: socket.getReceiveBufferSize can be used to determine possible packet size.
        System.out.println("Waiting to establish connection");
        try {
            if (!isServer) {
                // Clients broadcast their presence upon attempting to establish connection.
                int maxRetries = 5;
                for (int i = 0; i < maxRetries; i++) {
                    try {
                        socket.setBroadcast(true);
                        String broadcastMessage = "HELLO";
                        byte[] broadcastBuffer = broadcastMessage.getBytes();
                        RaspPacket packet = new RaspPacket(broadcastBuffer, InetAddress.getLocalHost(), 8001, seqNr, ackNr, ControlFlag.SYN);
                        DatagramPacket hello = packet.createPacket();
                        socket.send(hello);

                        byte[] receiveBuffer = new byte[500];
                        DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                        socket.setSoTimeout(1000);
                        // If no response, a SocketTimeoutException is caught.
                        socket.receive(response);

                        destAddress = response.getAddress();
                        destPort = response.getPort();
                        System.out.println("Found connection.");
                        // Ignore packets from other sources.
                        socket.connect(destAddress, destPort);
                        socket.setBroadcast(false);
                        break;
                    } catch (SocketTimeoutException e) {
                        System.out.println("No response.");
                    }
                }

                // Check if properly connected. If it has timed out or otherwise failed, close.
                if (!socket.isConnected()) {
                    socket.close();
                    reachedTimeOut = true;
                }
            }
        } catch (UnknownHostException ex) {
            System.err.println("Host unknown: " + ex.getMessage());
            ex.printStackTrace();
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Determine if received packet contains the correct checksum, sequence number, and ack number.
     *  And if server, if the client is already properly connected (if it is in the HashMap of known clients).
     *  @return true if values match expected.
     */
    private boolean packetOK(RaspPacket packet) {
        return RaspHeader.testChecksum(packet);
    }

    public void setSeqNr(RaspPacket packet) {
        this.seqNr = packet.getHeader().getAckNr();
    }

    public void setAckNr(RaspPacket packet) {
        this.ackNr = packet.getHeader().getSeqNr() + 1;
    }

    public HashMap<InetAddress, Integer> getKnownClients() {
        return knownClients;
    }

    public void addClient(InetAddress address, Integer port) {
        this.knownClients.put(address, port);
    }
}
