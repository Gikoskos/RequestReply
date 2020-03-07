package distsys.rr;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ProtocolPacket {
    @SuppressWarnings("unused")
    private final int svcid;
    private final InetAddress addr;

    private RequestState state;
    private byte[] reqbuff, repbuff;
    private int port, networkid, requestlen, packetid;

    public ProtocolPacket(int svcid, InetAddress addr, int port) throws UnsupportedEncodingException {
        this.svcid = svcid;
        this.addr = addr;
        this.port = port;
        this.state = RequestState.ARRIVED;
    }

    public void setReplyBuffer(byte[] buff) {
        this.repbuff = buff;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public RequestState getState() {
        return this.state;
    }

    public InetAddress getAddress() {
        return addr;
    }

    public int getServiceId() {
        return svcid;
    }

    public int getPort() {
        return port;
    }

    public int getNetworkId() {
        return networkid;
    }

    public int getPacketId() {
        return this.packetid;
    }

    public void setPacketId(int packetid) {
        this.packetid = packetid;
    }

    public void deserializeRequestBuffer(byte[] buff) {
        ByteBuffer buffPacket = ByteBuffer.wrap(buff);

        buffPacket.order(ByteOrder.BIG_ENDIAN);
        this.networkid = buffPacket.getInt(0);
        this.requestlen = buffPacket.getInt(4);

        this.reqbuff = new byte[this.requestlen];

        buffPacket.get(this.reqbuff);
        System.out.println("Deserialized packet (" + this.networkid + ", " + this.requestlen  + ")");
    }

    public byte[] getRequestBuffer() {
        return reqbuff;
    }

    public byte[] getReplyBuffer() {
        return repbuff;
    }

    public void serializeReplyBuffer(byte[] buff) {
        ByteBuffer buffPacket = ByteBuffer.allocate(8 + buff.length);

        buffPacket.order(ByteOrder.BIG_ENDIAN);
        buffPacket.putInt(0, this.networkid);
        buffPacket.putInt(4, buff.length);
        buffPacket.put(buff, 8, buff.length);

        this.repbuff = buffPacket.array();
    }
}
