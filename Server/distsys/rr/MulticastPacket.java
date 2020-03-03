package distsys.rr;

import java.net.*;


class MulticastPacket {
    private InetAddress addr;
    private String msg;
    private byte[] buff;

    public MulticastPacket(InetAddress address, byte[] buff) {
        this.buff = buff;
        this.msg = new String(buff);
        this.addr = address;
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
}
