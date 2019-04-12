package deskfileclient;

import java.io.IOException;
import java.util.Scanner;

public class ClientController {

    public ClientController() {
//        try (Scanner in = new Scanner(System.in)) {
//
//        } catch (IOException e) {
//
//        }
        Scanner inputScanner = new Scanner(System.in);
            String userInput = inputScanner.nextLine();
            boolean active = true;

//            AppCommand command = AppCommand.getCommand(userInput);
//            command.execute();

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
                case "list":
                    listFileNames();
                    break;
                case "pause":
                    //upload or download?
                    break;
                case "resume":
                    //upload or download?
                    break;
            }
        }

    //TODO: You should be able to pause and resume downloads at any time. Add cancel?
    /** Controlling the download of a specific file **/

    //PREVENT TWO FILES WITH THE SAME NAME

    public void startDownload(String fileName) {
    }

    public void pauseDownload(String fileName) {
    }

    public void resumeDownload(String fileName) {
    }


    //TODO: Likewise, client should be able to upload to server. Add cancel?
    /** Controlling the download of a specific file **/

    public void startUpload(String fileName) {
    }

    public void pauseUpload(String fileName) {
    }

    public void resumeUpload(String fileName) {
    }

    //TODO: The client should be able to ask for and list all available files on the Raspberry Pi.
    /** Ask for all available files on the Raspberry Pi. */
    public void getFileNames() {
    }

    /** List all available files on the Raspberry Pi. */
    public void listFileNames() {
    }

    //TODO: Your client should be able to show statistics about download speeds, packet loss, retransmissions, etc.
    /** Regarding statistics. To use StatisticsHandler **/
    public void getStatSummary() {
    }

    public void getStatAtTime(int time) {
    }
}
