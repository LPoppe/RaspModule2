package framework.application;

public enum AppCommand {
    QUIT(0),
    SEND(1),
    GET(2),
    GETLIST(3),
    PAUSE(4),
    RESUME(5),
    SENDFILE(6),
    SENDLIST(7),
    ERROR(8);

    private int command;

    public int getCommand() {
        return command;
    }
    AppCommand(int command) {
        this.command = command;
    }
}
