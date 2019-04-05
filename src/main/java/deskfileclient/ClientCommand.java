package deskfileclient;

import java.util.Arrays;

public enum ClientCommand {
    QUIT("quit");

    private String command;

    ClientCommand(String command) {
        this.command = command;
    }
}
