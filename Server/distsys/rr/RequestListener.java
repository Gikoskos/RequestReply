package distsys.rr;

import java.io.IOException;
import java.net.*;
import java.util.regex.*;

public class RequestListener extends Thread {
    private static final Pattern mcastHeaderPattern = Pattern.compile("^RR_CLIENT_(\\d{1,4})");
    private int RR_PORT = 10002;
    private final MulticastServer mcastServer;
    private Matcher mcastHeaderMatcher;

    public RequestListener(int port, String address) throws Exception {
        this.mcastServer = new MulticastServer(port, address);
    }

    private MulticastPacket getValidMulticastPacket() {
        MulticastPacket mpack = null;

        while (mpack == null) {
            try {
                mpack = this.mcastServer.listen();
                this.mcastHeaderMatcher = mcastHeaderPattern.matcher(mpack.getString());

                if (this.mcastHeaderMatcher.matches()) {
                    System.out.println("VALID MESSAGE BY " + mpack.getAddress().getHostName() + ": " + mpack.getString());
                } else {
                    System.out.println("INVALID MESSAGE BY " + mpack.getAddress().getHostName() + ": " + mpack.getString());
                    //mpack = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mpack;
    }

    private void sendACK(InetAddress addr, DatagramSocket socket) throws IOException {
        String s = "RR_SERVER_ACK";
        byte[] msg = s.getBytes();
        System.out.println("ONSEND");
        
        DatagramPacket packet = new DatagramPacket(msg, msg.length, addr, this.RR_PORT);

        socket.send(packet);
    }

    private void recvACK(InetAddress addr, DatagramSocket socket) throws IOException {
        byte[] buf = new byte[128];
        System.out.println("ONRCV");
        DatagramPacket recv = new DatagramPacket(buf, buf.length);

        socket.receive(recv);
        System.out.println(new String(recv.getData(), "US-ASCII"));
    }

    public void run() {
        while (true) {
            MulticastPacket mpack = this.getValidMulticastPacket();
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(6676);
            } catch (Exception e) {}

            this.RR_PORT = mpack.getPort();

            //this.mcastHeaderMatcher.group(1);
            try {
                this.sendACK(mpack.getAddress(), socket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                this.recvACK(mpack.getAddress(), socket);
            } catch (Exception e) { e.printStackTrace();}
        }
    }
}
