package framework;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ShippingAndReceiving extends Thread {
    private static final int TIMEOUT = 50;
    private DatagramSocket socket;
    private int seqNr = 0;
    private boolean isServer;
    private boolean reachedTimeOut = false;
    private boolean receivedFIN = false;

    private InetAddress destAddress;
    private int destPort;

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

        // Start the connection.
        establishConnection();

        //Continue until timeout or FIN.
        while (!reachedTimeOut && !receivedFIN) {
            try {
                socket.setSoTimeout(TIMEOUT);
                // First check receiving.
                byte[] receiveBuffer = new byte[500];
                DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(request);

                // Deal with received message.
                if (request.getData() != null) {
                    seqNr++;
                    System.out.println("Received: " + Arrays.toString(request.getData()));
                }

                byte[] data = new byte[1];
                data[0] = 101;
                // Then send, if necessary.
                DatagramPacket nextPacket = RaspDatagram.createPacket(data, destAddress, destPort, seqNr, ContentFlag.DATA);
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
                        System.out.println(Arrays.toString(broadcastBuffer));
                        DatagramPacket hello = new DatagramPacket(broadcastBuffer, broadcastBuffer.length, InetAddress.getLocalHost(), 8001);
                        //DatagramPacket hello = RaspDatagram.createPacket(broadcastBuffer, InetAddress.getByName("255.255.255.255"), 8001, seqNr, ContentFlag.SYN);
                        socket.send(hello);

                        byte[] receiveBuffer = new byte[500];
                        DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                        socket.setSoTimeout(1000);
                        socket.receive(response);
                        // If no response, a SocketTimeoutException is caught.
                        destAddress = response.getAddress();
                        destPort = response.getPort();
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

            } else {
                // The server listens for an incoming requests and acknowledges it. Does not use a time-out.
                // TODO: in this set-up, server only allows one connection.
                byte[] receiveBuffer = new byte[500];
                DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(request);
                socket.connect(request.getAddress(), request.getPort());

                System.out.println("Found connection.");
                String broadcastResponse = "HELLO";
                byte[] responseBuffer = broadcastResponse.getBytes();
                seqNr++;
                DatagramPacket hello = RaspDatagram.createPacket(responseBuffer, socket.getInetAddress(), socket.getPort(), seqNr, ContentFlag.ACK);
                socket.send(hello);

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

    //TODO: Allow reconnecting, if we manage.

}
