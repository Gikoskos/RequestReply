package distsys.rr;

import java.io.IOException;
import java.net.*;
import java.util.regex.*;

public class RequestListener extends Thread {
    private static final Pattern mcastHeaderPattern = Pattern.compile("^RR_CLIENT_(\\d{1,4})");
    private static final int RR_PORT = 6777;
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
                    mpack = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mpack;
    }

    private void sendACK(InetAddress addr) throws IOException {
        String s = "RR_SERVER_ACK";
        byte[] msg = s.getBytes();
        DatagramSocket socket = new DatagramSocket(RR_PORT);
        DatagramPacket packet = new DatagramPacket(msg, msg.length, addr, RR_PORT);

        socket.send(packet);
    }

    public void run() {
        while (true) {
            MulticastPacket mpack = this.getValidMulticastPacket();

            this.mcastHeaderMatcher.group(1);
            try {
                this.sendACK(mpack.getAddress());
            } catch (Exception e) {}
        }
    }
}