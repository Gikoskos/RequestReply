package distsys.rr;

import java.io.IOException;
import java.net.*;

class MulticastServer {
    private static final int DGRAM_LEN = 32;
    private final int port;
    private final String address;
    private InetAddress group;
    private MulticastSocket sock;

    public MulticastServer(int port, String address) throws UnknownHostException, IOException, SocketException {
        this.port = port;
        this.address = address;

        this.group = InetAddress.getByName(this.address);
        this.sock = new MulticastSocket(this.port);

        this.sock.setLoopbackMode(true);
        this.sock.joinGroup(this.group);
    }

    public MulticastPacket listen() throws IOException {
        byte[] buf = new byte[DGRAM_LEN];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);

        this.sock.receive(recv);

        return new MulticastPacket(recv.getAddress(), recv.getPort(), buf);
    }

    public void close() {
        try {
            this.sock.leaveGroup(this.group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sock.close();
    }
}
