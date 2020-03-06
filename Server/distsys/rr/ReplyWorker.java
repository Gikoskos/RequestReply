package distsys.rr;

import java.io.IOException;
import java.net.*;

class ReplyWorker implements Runnable {
    private static int cnt = 0;
    private final int id;
    private final DatagramSocket sock;

    public ReplyWorker() throws Exception {
        sock = new DatagramSocket();
        sock.setSoTimeout(GlobalLimits.REPLY_TIMEOUT * 1000);
        id = cnt++;
    }

    public void run() {
        System.out.println("ReplyWorker " + id + " started");
        byte[] buffAck = new byte[GlobalLimits.DGRAM_LEN];

        while (true) {
            try {
                RRServer.availableReply.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            ProtocolPacket packet = PacketBuffer.getPacket(RequestState.REPLIED);
            byte[] reply = packet.getReplyBuffer();

            DatagramPacket packetReply = new DatagramPacket(reply, reply.length, packet.getAddress(), packet.getPort());

            for (int i = 0; i < GlobalLimits.SEND_REPLY_TRIES; i++) {
                try {
                    this.sock.send(packetReply);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                DatagramPacket recvAck = new DatagramPacket(buffAck, buffAck.length);

                try {
                    this.sock.receive(recvAck);
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                break;
            }

            PacketBuffer.remove(packet.getPacketId());
        }
    }
}

