package framework;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static framework.ControlFlag.SYN;

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

    private TransferController transferControl;

    /**
     * File sender thread that can be used by the client and server to send files.
     */
    public ShippingAndReceiving(boolean startAsServer, int portNr) throws InterruptedException {
        try {
            socket = new DatagramSocket(portNr);
            this.isServer = startAsServer;
            this.transferControl = new TransferController(startAsServer, this);
        } catch (SocketException e) {
            System.err.println("Failed to open socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        this.transferControl.start();

        if (!isServer) {
            try {
                socket.setSoTimeout(200);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            // Start a specific connection if client.
            establishConnection();
        }

        // Continue until timeout or FIN.
        while (!reachedTimeOut && !receivedFIN) {
            try {
                socket.setSoTimeout(TIMEOUT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            // First check receiving.
            receive();

            // If the sending queue is not empty, send a packet.
            if (!transferControl.getSendQueue().isEmpty()) {
                ship();
            }

        }

        // Once finished, close the socket.
        socket.close();
        transferControl.close();
    }

    private void receive() {
        try {
            // Receive buffer set to the maximum size a packet may have.
            byte[] receiveBuffer = new byte[RaspPacket.MAX_PACKET_SIZE];
            DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            socket.receive(request);

            // Deal with received message if the checksum and sequence number are correct, otherwise discard.
            RaspPacket received = new RaspPacket(request);
            System.out.println("Before OK. Received: " + Arrays.toString(request.getData()));
            if (packetOK(received)) {
                System.out.println("packet OK");
                transferControl.getReceiveQueue().offer(received, 10, TimeUnit.MILLISECONDS);
            }

        } catch (SocketTimeoutException ignored){
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while trying to add to receive queue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ship() {
        // First check if the server has any clients to send messages to.
        if (isServer) {
            if (this.getKnownClients().isEmpty()) {
                return;
            }
        }

        try {
            DatagramPacket fullPacket = RaspPacket.serialize((RaspPacket) transferControl.getSendQueue().take());
            System.out.println("Sending.");
            socket.send(fullPacket);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void establishConnection() {
        // TODO: socket.getReceiveBufferSize can be used to determine possible packet size.
        System.out.println("Waiting to establish connection");
        if (!isServer) {
            try {
                // Clients broadcast their presence upon attempting to establish connection.
                int maxRetries = 5;
                for (int i = 0; i < maxRetries; i++) {
                    try {
                        socket.setBroadcast(true);
                        RaspPacket packet = new RaspPacket(transferControl.getFillData(), InetAddress.getLocalHost(), 8001, seqNr, ackNr, SYN);
                        DatagramPacket hello = packet.createPacket();
                        System.out.println("Sending SYN");
                        socket.send(hello);

                        // Receive buffer set to the maximum size a packet may have.
                        byte[] receiveBuffer = new byte[RaspPacket.MAX_PACKET_SIZE];
                        DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                        socket.receive(request);

                        // Deal with received message if the checksum and sequence number are correct, otherwise discard.
                        RaspPacket received = new RaspPacket(request);

                        System.out.println("Before OK. Received: " + Arrays.toString(request.getData()));
                        if (packetOK(received)) {
                            System.out.println("packet OK");
                            transferControl.getReceiveQueue().put(received);
                        }
                        // Once successful, escape the loop.
                        break;
                    } catch (SocketTimeoutException e) {
                        System.out.println("No response.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
    }

    /** Determine if received packet contains the correct checksum, sequence number, and ack number.
     *  And if server, if the client is already properly connected (if it is in the HashMap of known clients).
     *  @return true if values match expected.
     */
    private boolean packetOK(RaspPacket packet) {
        return RaspHeader.testChecksum(packet);
    }

    public boolean isServer() { return this.isServer; }

    /** Retrieve the mapping of the clients known to a server. */
    public HashMap<InetAddress, Integer> getKnownClients() {
        return knownClients;
    }

    /** Add a new client to the mapping of the clients known to a server, occurs after an ACK. */
    public void addClient(InetAddress address, Integer port) {
        if (!this.knownClients.containsKey(address)) {
            this.knownClients.put(address, port);
        }
    }

    /** Remove a known client from the mapping of the clients known to a server, occurs after a FIN */
    public void removeClient(InetAddress address, Integer port) {
        if (this.knownClients.containsKey(address)) {
            this.knownClients.remove(address, port);
        }
    }

    public void setSeqAndAck(RaspPacket packet) {
        this.seqNr = packet.getHeader().getAckNr();
        this.ackNr = packet.getHeader().getSeqNr() + 1;
    }

    public void setReceivedFIN() {
        this.receivedFIN = true;
    }

    public int getSeqNr() { return this.seqNr; }
    public int getAckNr() { return this.ackNr; }

    public InetAddress getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }
}
