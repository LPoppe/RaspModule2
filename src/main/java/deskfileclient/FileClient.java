package deskfileclient;


import framework.ConnectionManager;
import framework.PacketBuilder;

import java.io.IOException;
import java.net.*;

// TODO: You should be able to prove that the file you download from the server
//  is exactly the same as the one on the server, and the other way around (data integrity).
public class FileClient {

    public static void main(String[] args) throws SocketException {

        if (args.length != 2){
            System.out.println("Wrong arguments: expecting <hostname> <port number>");
            return;
        }

        DatagramSocket socket = new DatagramSocket();
        String hostname = args[0];

        int hostPort = Integer.parseInt(args[1]);


        try {
            InetAddress hostAddress = InetAddress.getByName(hostname);

            while (true) {
//                // Sending...
                byte[] abytearray = new byte[1];
                String destfilename = null;
                PacketBuilder nextPacket = new PacketBuilder(abytearray, hostAddress, hostPort, destfilename);
                DatagramPacket nextToSend = nextPacket.createPacket();
                socket.send(nextToSend);

                // Receiving...
                byte[] buffer = new byte[400];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                Thread.sleep(10000);
            }
        } catch (UnknownHostException e) {
            System.err.println("Host unknown: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error at client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //TODO: Your client should be able to discover the server on a local network without knowing its IP address.
    public void findServer() {

    }

}
