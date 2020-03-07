package distsys.rr;

import java.util.concurrent.ArrayBlockingQueue;

class Queues {
    public final static ArrayBlockingQueue<ReplyData> replyQueue
        = new ArrayBlockingQueue<ReplyData>(GlobalLimits.BUFFER_SIZE, true);
    public final static ArrayBlockingQueue<MulticastPacket> multicastPacketQueue
        = new ArrayBlockingQueue<MulticastPacket>(GlobalLimits.BUFFER_SIZE, true);
}