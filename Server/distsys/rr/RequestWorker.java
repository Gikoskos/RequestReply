package distsys.rr;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

class RequestWorker implements Runnable {
    private static int cnt = 0;
    private final int id;
    private final DatagramSocket sock;

    public RequestWorker() throws Exception {
        sock = new DatagramSocket();

        id = cnt++;

    }

    public void run() {
        System.out.println("RequestWorker " + id + " started");
        byte[] msgACK = "RR_SERVER_ACK".getBytes();
        byte[] buffRequest = new byte[GlobalLimits.DGRAM_BIG_LEN];

        while (true) {
            try {
                RRServer.availableRequest.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            
            ProtocolPacket arrived = PacketBuffer.getPacket(RequestState.ARRIVED);
            
            System.out.println("RequestWorker " + id + " assigned job!");

            DatagramPacket packetACK = new DatagramPacket(msgACK, msgACK.length, arrived.getAddress(), arrived.getPort());

            try {
                this.sock.send(packetACK);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            System.out.println("RequestWorker " + id + " sent ACK!");
            DatagramPacket packetRequest = new DatagramPacket(buffRequest, buffRequest.length);

            try {
                this.sock.receive(packetRequest);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            System.out.println("RequestWorker " + id + " received packet with data " + packetRequest.getLength());

            arrived.deserializeRequestBuffer(Arrays.copyOfRange(buffRequest, 0, packetRequest.getLength()));

            if (PacketBuffer.checkDuplicates(arrived)) {
                continue;
            }

            arrived.setPort(packetRequest.getPort());
            byte[] shortACK = ("ACK_" + arrived.getNetworkId()).getBytes();

            DatagramPacket shortpacketACK = new DatagramPacket(shortACK, shortACK.length, arrived.getAddress(), arrived.getPort());

            try {
                this.sock.send(shortpacketACK);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            ServiceHandler.releaseServiceRequest(arrived.getServiceId());
        }
    }
}
