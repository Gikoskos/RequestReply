/*package distsys.rr;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class AckPacket {
    @SuppressWarnings("unused")
    private final int reqid;
    private final ProtocolPacket packet;

    public AckPacket(int reqid) throws UnsupportedEncodingException {
        ByteBuffer buffInt = ByteBuffer.allocate(4);

        buffInt.order(ByteOrder.BIG_ENDIAN);
        buffInt.putInt(reqid);
        
        packet = new ProtocolPacket("RR_SERVER_ACK_", buffInt.array());
        this.reqid = reqid;
    }

    public byte[] getPacket() {
        return this.packet.getPacket();
    }
}
*/