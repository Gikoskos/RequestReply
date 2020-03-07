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
        sock.setSoTimeout(GlobalLimits.SOCKET_TIMEOUT);
        id = cnt++;

    }

    public void run() {
        System.out.println("RequestWorker " + id + " started");
        byte[] msgACK = "RR_SERVER_ACK".getBytes();
        byte[] buffReceive = new byte[GlobalLimits.DGRAM_BIG_LEN];
        DatagramPacket dgram = new DatagramPacket(msgACK, msgACK.length);

        while (true) {
            try {
                RRServer.availableRequest.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            
            ProtocolPacket arrived = PacketBuffer.getPacket(RequestState.ARRIVED);
            
            System.out.println("RequestWorker " + id + " assigned job!");


            dgram.setAddress(arrived.getAddress());
            dgram.setPort(arrived.getPort());
            System.out.println("RequestWorker " + id + " got port " + dgram.getPort());

            int i;
            for (i = 0; i < GlobalLimits.GET_REQUEST_TRIES; i++) {

                try {
                    this.sock.send(dgram);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                System.out.println("RequestWorker " + id + " sent ACK!");

                dgram.setData(buffReceive);
                dgram.setLength(buffReceive.length);

                try {
                    this.sock.receive(dgram);
                } catch (SocketTimeoutException e) {
                    System.out.println("RequestWorker " + id + " request timed out!");
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

            if (i == GlobalLimits.GET_REQUEST_TRIES) {
                //@TODO: clear buffer
                System.out.println("RequestWorker " + id + " maxed on timeouts for request!");
                continue;
            }

            System.out.println("RequestWorker " + id + " received packet with data " + dgram.getLength());

            arrived.deserializeRequestBuffer(Arrays.copyOfRange(buffReceive, 0, dgram.getLength()));

            if (PacketBuffer.checkDuplicates(arrived)) {
                System.out.println("Found duplicate!");
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

            ServiceHandler.releaseServiceRequest(arrived.getServiceId());
        }
    }
}
