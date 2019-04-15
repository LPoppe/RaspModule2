package framework;

import framework.rasphandling.RaspConnectionHandler;
import framework.rasphandling.RaspHeader;

import java.net.DatagramSocket;
import java.net.SocketException;

public class RaspClient {

    // DatagramSocket with timeout for receiving packets.
    private DatagramSocket socket;
    private static final int TIMEOUT = 50;

    // Thread responsible for sending created packets over shared socket.
    private final RaspSender senderThread;

    // The client only needs one handler for the server it is connected to.
    RaspConnectionHandler server;
    private boolean running = true;

    // Determine maximum packet size for RaspPacket.
    private static final int UDP_HEADER_LENGTH = 8;
    private static final int IP_HEADER_LENGTH = 20;
    private static final int MAX_RASP_PACKET_SIZE = 65535 - UDP_HEADER_LENGTH - IP_HEADER_LENGTH - RaspHeader.getLength();


    public RaspClient(int port) {
        try {
            this.socket = new DatagramSocket(port);
            socket.setSoTimeout(200);
        } catch (SocketException e) {
            System.err.println("Failed to open socket: " + e.getMessage());
            e.printStackTrace();
        }

        senderThread = new RaspSender(socket);
    }
}
