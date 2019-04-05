package deskfileclient;


import framework.FileSenderThread;
import framework.Packet;

import java.net.*;
import java.util.Scanner;

// TODO: You should be able to prove that the file you download from the server
//  is exactly the same as the one on the server, and the other way around (data integrity).
public class FileClient {

    public static void main(String[] args) throws SocketException {

        if (args.length != 2){
            System.out.println("Wrong arguments: expecting <hostname> <port number>");
            return;
        }

        Scanner inputScanner = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();
        String hostname = args[0];

        int port = Integer.parseInt(args[1]);

        // Thread is active while the user has not typed "quit".
        boolean active = true;

        while (active) {
            String userInput = inputScanner.nextLine();

            switch (userInput) {
                case "quit":
                    active = false;
                    break;
                case "send":
                    // Allow the user to send a file to the server.
                    //new FileSenderThread("filename").start();
                    break;
                case "get":
                    //Handle request to receive file from server.
                    break;
            }
        }



        try {
            InetAddress hostAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            System.err.println("Host unknown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //TODO: Your client should be able to discover the server on a local network without knowing its IP address.
    public void findServer() {

    }

}
