package framework;

// To keep it interesting, use UDP combined with an ARQ protocol. You are not allowed to use TCP/IP.
// Any errors or failures should be handled gracefully.
public interface BaseProtocol {

    public void transferPacket();

    public void acknowledgePacket();

}
