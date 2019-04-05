package framework;

import pifileserver.FileServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;


public class FileSenderThread extends Thread {
    private FileServer main;

    private DatagramSocket socket = null;
    private BufferedReader in = null;
    private boolean dataRemaining = true;

    /**
     * File sender thread that can be used by the client and server to send files.
     * @param fileName The name of the file that needs to be sent.
     */
    public FileSenderThread(FileServer main, String fileName) throws SocketException {
        // Maybe want this to ask main for stuff.
        this.main = main;
        socket = new DatagramSocket(1234);

        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException fe) {
            System.err.println("File requested not found: " + fe.getMessage());
            //TODO: Send some response notifying the user/client file does not exist.
        }
    }

    public void run() {
        byte[] receiveBuffer = new byte[256];

        while (dataRemaining) {
            try {

                // receive request
                DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(request);

                // respond to request according to input.
                getNextFileSegment();
                //


                // send the response to the client at "address" and "port"
                InetAddress address = request.getAddress();
                int port = request.getPort();
                DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length, address, port);
                socket.send(response);


            } catch (IOException e) {
                e.printStackTrace();
                dataRemaining = false;
            }
        }
        socket.close();
    }


    private byte[] getNextFileSegment() {
        byte[] nextSegment = null;

        return nextSegment;
    }
}
