package framework;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

public class ShippingAndReceiving extends Thread {
    private static final int TIMEOUT = 50;
    private DatagramSocket socket;
    private int seqNr = 0;
    private boolean isServer;
    private boolean reachedTimeOut = false;
    private boolean receivedFIN = false;

    private InetAddress destAddress;
    private int destPort;

    // For server, map clients that have sent SYN, so we know who is allowed to send other requests.
    private HashMap<InetAddress, Integer> knownClients;

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
                try {
                    // Receive buffer set to the maximum size a packet may have.
                    byte[] receiveBuffer = new byte[RaspDatagram.MAX_PACKET_SIZE];
                    DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(request);

                    // Deal with received message if the checksum and sequence number are correct, otherwise discard.
                    if (request.getData() != null && packetOK(request)) {
                        // e.g. ControlFlag.ACK.respondToFlag(isServer);
                        System.out.println("Received: " + Arrays.toString(request.getData()));
                    }
                } catch (SocketTimeoutException e){
                    // TODO: If I could use a non-blocking channel or something, that might be nicer.
                    //  But this'll do for now.
                }

                // Then send, if necessary.
                byte[] data = new byte[1];
                data[0] = (byte) 101;
                DatagramPacket nextPacket = RaspDatagram.createPacket(data, destAddress, destPort, seqNr, ControlFlag.DATA);
                System.out.println("Sending: " + Arrays.toString(nextPacket.getData()) + " to " + nextPacket.getAddress() + " on port " + nextPacket.getPort());
                socket.send(nextPacket);

            } catch (UnknownHostException e) {
                System.err.println("Host unknown: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Once finished, close the socket.
        socket.close();
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
                        DatagramPacket hello = RaspDatagram.createPacket(broadcastBuffer, InetAddress.getLocalHost(), 8001, seqNr, ControlFlag.SYN);
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
    private static boolean packetOK(DatagramPacket packet) {
        return true;
    }

}
