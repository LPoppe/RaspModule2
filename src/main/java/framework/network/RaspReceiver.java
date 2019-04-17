package framework.network;

import framework.InvalidChecksumException;
import framework.transport.*;
import framework.transport.RaspSocket;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

public abstract class RaspReceiver extends Thread {
    // DatagramSocket with timeout for receiving packets.
    DatagramSocket socket;
    private static final int TIMEOUT = 500;

    // Thread responsible for sending created packets over shared socket.
    protected final RaspSender senderThread;

    // Determine maximum packet size for NoAckRaspPacket.
    private static final int UDP_HEADER_LENGTH = 8;
    private static final int IP_HEADER_LENGTH = 20;
    protected static final int MAX_RASP_PACKET_SIZE = 50 - UDP_HEADER_LENGTH - IP_HEADER_LENGTH; //- RaspHeader.getLength();
    protected static final int MAX_DATA_RECEIVED = 50;

    // The timeout of receiving upon which handlers should reset their sending window.
    protected static final int RESEND_TIMEOUT = 1000; // milliseconds

    // For server, map clients that have sent SYN, so we know who is allowed to send other requests.
    // The client will (at least currently) only ever have a single connection.
    protected HashMap<RaspAddress, RaspSocket> knownConnections = new HashMap<>();
    protected boolean running = true;

    public RaspReceiver(int port) {
        try {
            this.socket = new DatagramSocket(port);
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            System.err.println("Failed to open socket: " + e.getMessage());
            e.printStackTrace();
        }

        senderThread = new RaspSender(socket, knownConnections);
    }

    public void run() {

        establishConnection();

        while (this.running) {

            // Receive buffer set to the maximum size a packet may have.
            byte[] receiveBuffer = new byte[MAX_DATA_RECEIVED];
            DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                System.out.println("!!!!!" + 1);
                socket.receive(request);
            } catch (SocketTimeoutException e) {
                System.out.println("!!!!!" + 2);
                checkForTimeOuts();
                continue;
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("!!!!!" + 3);
            RaspAddress packetOrigin = new RaspAddress(request.getAddress(), request.getPort());

            // Deserialize. Only creates packet if checksum succeeds.
            try {
                System.out.println("!!!!!" + 4);
                RaspPacket raspPacket = RaspPacket.deserialize(request);
                System.out.println("Received: " + raspPacket.getHeader().getSeqNr() + ", " + raspPacket.getHeader().getAckNr());
                relayToHandler(raspPacket, packetOrigin);
                System.out.println("!!!!!" + 6);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            } catch (InvalidChecksumException ignored) {
            }

            // Check if any handlers need to resend packets.
            System.out.println("!!!!!" + 5);
            checkForTimeOuts();
        }
    }

    // Only used by client to establish a first connection, as the client initiates the communication.
    protected abstract void establishConnection();

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

    protected abstract void relayToHandler(RaspPacket packet, RaspAddress packetOrigin) throws InterruptedException;

    protected void checkForTimeOuts() {
        long currentTime = System.currentTimeMillis();
        for (RaspSocket raspSocket : knownConnections.values()) {
            long timeElapsed = currentTime - raspSocket.getLastTimeReceived();
            if (timeElapsed >= RESEND_TIMEOUT) {
                System.out.println("RESENDING");
                raspSocket.resend();
            }
        }
    }

    /** Add a new client to the mapping of the clients known to a server, occurs after an ACK.
     * @return a new client object*/
    protected RaspSocket addConnection(InetAddress address, Integer port) {
        RaspAddress clientAddress = new RaspAddress(address, port);

        RaspSocket connection = new RaspSocket(this, this.senderThread, clientAddress, MAX_RASP_PACKET_SIZE);
        this.knownConnections.put(clientAddress, connection);
        return connection;
    }

}
