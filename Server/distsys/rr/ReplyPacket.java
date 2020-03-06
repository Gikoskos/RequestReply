/*package distsys.rr;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ReplyPacket {
    @SuppressWarnings("unused")
    private final int reqid;
    private final ProtocolPacket packet;

    public ReplyPacket(int reqid, byte[] data) throws UnsupportedEncodingException {
        ByteBuffer buffPacket = ByteBuffer.allocate(4 + 4 + data.length);

        buffPacket.order(ByteOrder.BIG_ENDIAN);
        buffPacket.putInt(reqid);

        buffPacket.putInt(data.length);
        buffPacket.put(data);

        packet = new ProtocolPacket(null, buffPacket.array());

        this.reqid = reqid;
    }

    public byte[] getPacket() {
        return this.packet.getPacket();
    }
}
*/