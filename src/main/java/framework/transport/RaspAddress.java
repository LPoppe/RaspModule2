package framework.transport;

import java.net.InetAddress;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaspAddress that = (RaspAddress) o;
        return port == that.port &&
                address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    public InetAddress getAddress() {
        return address;
    }
}
