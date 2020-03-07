package distsys.rr;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

class MulticastServer {
    private final int port;
    private final String address;
    private InetAddress group;
    private MulticastSocket sock;

    public MulticastServer(int port, String address) throws UnknownHostException, IOException, SocketException {
        this.port = port;
        this.address = address;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        NetworkInterface eth0Interface = null;

        for (NetworkInterface inf : Collections.list(interfaces)) {
            if (inf.getName() == "eth0") {
                eth0Interface = inf;
                break;
            }
        }

        this.group = InetAddress.getByName(this.address);
        this.sock = new MulticastSocket(this.port);

        this.sock.setLoopbackMode(true);
        this.sock.joinGroup(new InetSocketAddress(this.group, this.port), eth0Interface);
    }

    public MulticastPacket listen() throws IOException {
        byte[] buf = new byte[GlobalLimits.DGRAM_LEN];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);

        this.sock.receive(recv);

        return new MulticastPacket(recv.getAddress(), recv.getPort(), Arrays.copyOfRange(buf, 0, recv.getLength()));
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
