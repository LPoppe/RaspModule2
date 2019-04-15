package framework.rasphandling;

import java.net.InetAddress;

public class RaspAddress {

    private final InetAddress address;
    private final int port;

    public RaspAddress(InetAddress clientAddress, int port) {
        this.address = clientAddress;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }
}
