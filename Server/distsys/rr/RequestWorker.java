package distsys.rr;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

class RequestWorker implements Runnable {
    private static int cnt = 0;
    private final int id;
    private final DatagramSocket sock;

    public RequestWorker() throws Exception {
        sock = new DatagramSocket(new InetSocketAddress(0));
        sock.setSoTimeout(GlobalLimits.SOCKET_TIMEOUT);
        id = cnt++;

    }

    public void run() {
        Dbg.cyan("RequestWorker " + id + " started on " + this.sock.getLocalPort());
        byte[] msgACK = "RR_SERVER_ACK".getBytes();
        byte[] buffReceive = new byte[GlobalLimits.DGRAM_BIG_LEN];
        DatagramPacket dgram = new DatagramPacket(msgACK, msgACK.length);
        ProtocolPacket arrived;
        MulticastPacket mpack;
        int networkId, svcid;

        while (true) {
            try {
                mpack = Queues.multicastPacketQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            
            networkId = mpack.getNetworkid();
            svcid = mpack.getSvcid();

            if (ServiceHandler.exists(svcid)) {
                PacketBuffer.wLock();


                try {
                    arrived = new ProtocolPacket(svcid, networkId, mpack.getAddress(), mpack.getPort());
                } catch (Exception e) {
                    e.printStackTrace();
                    PacketBuffer.wUnlock();
                    continue;
                }

                if (PacketBuffer.insert(arrived) == GlobalLimits.INVALID_PACKET_ID) {
                    Dbg.cyan("RequestWorker " + id + " discarded duplicate " + networkId);
                    PacketBuffer.wUnlock();
                    continue;
                }

                PacketBuffer.wUnlock();
            } else {
                Dbg.cyan("RequestWorker " + id + " discarded packet " + networkId + " because service " + svcid + " isn't registered");
                continue;
            }

            Dbg.cyan("RequestWorker " + id + " assigned job " + arrived.getNetworkId());


            Dbg.cyan("RequestWorker " + id + " running on " + this.sock.getLocalPort());
            dgram.setAddress(arrived.getAddress());
            dgram.setPort(arrived.getPort());
            Dbg.cyan("RequestWorker " + id + " got port " + dgram.getPort());

            int i;
            for (i = 0; i < GlobalLimits.GET_REQUEST_TRIES; i++) {

                try {
                    this.sock.send(dgram);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                Dbg.cyan("RequestWorker " + id + " sent ACK!");

                dgram.setData(buffReceive);
                dgram.setLength(buffReceive.length);

                try {
                    this.sock.receive(dgram);
                } catch (SocketTimeoutException e) {
                    Dbg.cyan("RequestWorker " + id + " request timed out!");
                    dgram.setAddress(arrived.getAddress());
                    dgram.setPort(arrived.getPort());
                    dgram.setData(msgACK);
                    dgram.setLength(msgACK.length);    
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                break;
            }

            PacketBuffer.wLock();
            if (i == GlobalLimits.GET_REQUEST_TRIES) {
                //@TODO: clear buffer
                PacketBuffer.remove(arrived.getPacketId());
                Dbg.cyan("RequestWorker " + id + " maxed on timeouts for request!");
                PacketBuffer.wUnlock();
                continue;
            }
            
            PacketBuffer.wUnlock();

            Dbg.cyan("RequestWorker " + id + " received packet with data " + dgram.getLength());


            if (!arrived.deserializeRequestBuffer(Arrays.copyOfRange(buffReceive, 0, dgram.getLength()))) {
                Dbg.cyan("RequestWorker " + id + " discards invalid packet " + arrived.getNetworkId());
                continue;
            }

            arrived.setPort(dgram.getPort());
            byte[] shortACK = ("ACK_" + arrived.getNetworkId()).getBytes();

            dgram.setData(shortACK);
            dgram.setLength(shortACK.length);

            try {
                this.sock.send(dgram);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            ServiceHandler.putServiceRequest(arrived.getServiceId(),
                new RequestData(arrived.getPacketId(), arrived.getRequestBuffer()));
        }
    }
}
