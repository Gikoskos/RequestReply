package distsys.rr;

import java.net.*;


class MulticastPacket {
    private InetAddress addr;
    private int port;
    private int svcid, networkid;
    private String msg;
    private byte[] buff;

    public MulticastPacket(InetAddress address, int port, byte[] buff) {
        this.buff = buff;
        this.msg = new String(buff);
        this.addr = address;
        this.port = port;
    }

    public String getString() {
        return this.msg;
    }

    public byte[] getBytes() {
        return this.buff;
    }

    public InetAddress getAddress() {
        return this.addr;
    }

    public int getPort() {
        return this.port;
    }

    public int getSvcid() {
        return svcid;
    }

    public void setSvcid(int svcid) {
        this.svcid = svcid;
    }

    public int getNetworkid() {
        return networkid;
    }

    public void setNetworkid(int networkid) {
        this.networkid = networkid;
    }
}
