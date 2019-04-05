package framework;

import pifileserver.FileServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class ConnectionManager extends Thread {

    private DatagramSocket socket = null;
    private BufferedReader in = null;

    /**
     * File sender thread that can be used by the client and server to send files.
     */
    public ConnectionManager() {
    }

    public void run() {
        byte[] receiveBuffer = new byte[400];

        while (true) {
            try (DatagramSocket socket = new DatagramSocket(1234)) {

                // receive request
                DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(request);

                // send the response to the client at "address" and "port"
                InetAddress address = request.getAddress();
                int port = request.getPort();

                byte[] data = getNextFileSegment("thefatcat.txt");
                // respond to request according to input.
                if (data != null) {
                    PacketBuilder nextToSend = new PacketBuilder(data, address, port, "thefatcat.txt");
                    DatagramPacket nextPacket = nextToSend.createPacket();
                    socket.send(nextPacket);
                } else {
                    System.out.println("Payload creation returned null.");
                }

            } catch (UnknownHostException e) {
                System.err.println("Host unknown: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private byte[] getNextFileSegment(String fileName) {
        byte[] nextSegment = null;
        try {
            nextSegment = Files.readAllBytes(Paths.get(fileName));
        } catch (FileNotFoundException fe) {
            System.err.println("File requested not found: " + fe.getMessage());
            //TODO: Send some response notifying the user/client file does not exist. Not here. But somewhere?
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        }
        return nextSegment;
    }
}
