package distsys.rr;

import java.net.*;


class MulticastPacket {
    private InetAddress addr;
    private int port;
    private String msg;
    private byte[] buff;

    public MulticastPacket(InetAddress address, int port, byte[] buff) {
        this.buff = buff;
        this.msg = new String(buff);
        this.addr = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return this.addr;
    }

    public String getString() {
        return this.msg;
    }

    public byte[] getBytes() {
        return this.buff;
    }

    public int getPort() {
        return this.port;
    }
}
