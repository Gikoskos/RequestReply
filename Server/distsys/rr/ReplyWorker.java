package distsys.rr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

class ReplyWorker implements Runnable {
    private static int cnt = 0;
    private final int id;
    private final DatagramSocket sock;

    public ReplyWorker() throws Exception {
        sock = new DatagramSocket(new InetSocketAddress(0));
        sock.setSoTimeout(GlobalLimits.SOCKET_TIMEOUT);
        id = cnt++;
    }

    public void run() {
        Dbg.yellow("ReplyWorker " + id + " started on port " + this.sock.getLocalPort());
        byte[] buffAck = new byte[GlobalLimits.DGRAM_LEN];
        ReplyData reply;

        while (true) {
            try {
                reply = Queues.replyQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            ProtocolPacket packet = PacketBuffer.getPacket(reply.getRequestId());

            packet.serializeReplyBuffer(reply.getData());

            DatagramPacket packetReply = new DatagramPacket(
                packet.getReplyBuffer(),
                packet.getReplyBuffer().length,
                packet.getAddress(),
                packet.getPort()
            );

            Dbg.yellow("ReplyWorker " + id + " key = " + reply.getRequestId());

            for (int i = 0; i < GlobalLimits.SEND_REPLY_TRIES; i++) {
                try {
                    this.sock.send(packetReply);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                DatagramPacket recvAck = new DatagramPacket(
                    buffAck,
                    buffAck.length
                );

                try {
                    this.sock.receive(recvAck);
                } catch (SocketTimeoutException e) {
                    Dbg.yellow("ReplyWorker " + id + " ACK for reply timeout");
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Dbg.yellow("ReplyWorker " + id + " got ACK for reply on " + this.sock.getLocalPort());
                break;
            }

            //remove in all cases
            PacketBuffer.wLock();
            PacketBuffer.remove(packet.getPacketId());
            PacketBuffer.wUnlock();
        }
    }
}

