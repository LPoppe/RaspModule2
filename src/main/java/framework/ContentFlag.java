package framework;

/**
 *  Options for the flag set in the packet's header.
 *  Currently, only one flag is used per packet.
 */
public enum ContentFlag {
    SYN(0), ACK(1), DATA(2), FIN(3);

    private final int flag;
    ContentFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
