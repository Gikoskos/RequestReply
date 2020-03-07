package distsys.rr;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


class ProtocolPacket {
    @SuppressWarnings("unused")
    private final int svcid;
    private final InetAddress addr;

    private RequestState state;
    private byte[] reqbuff, repbuff;
    private int port, networkid, requestlen, packetid;

    public ProtocolPacket(int svcid, int networkid, InetAddress addr, int port) throws UnsupportedEncodingException {
        this.svcid = svcid;
        this.networkid = networkid;
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

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public boolean deserializeRequestBuffer(byte[] buff) {
        ByteBuffer buffPacket = ByteBuffer.wrap(buff);

        //System.out.println("Serialized packet = " + bytesToHex(buff));
        buffPacket.order(ByteOrder.BIG_ENDIAN);
        this.requestlen = buffPacket.getInt(4);
        
        this.networkid = buffPacket.getInt(0);

        this.reqbuff = Arrays.copyOfRange(buff, 8, buff.length);

        Dbg.green("Deserialized packet (" + this.networkid + ", " + this.requestlen  + ", " + bytesToHex(this.reqbuff) + ")");
        return true;
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
        buffPacket.putInt(this.networkid);
        buffPacket.putInt(buff.length);
        buffPacket.put(buff);

        this.repbuff = Arrays.copyOfRange(buffPacket.array(), 0, buffPacket.array().length);
    }
}
