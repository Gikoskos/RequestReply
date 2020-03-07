package distsys.rr;

import java.io.IOException;
import java.util.regex.*;

public class RequestListener extends Thread {
    private static final Pattern mcastHeaderPattern = Pattern.compile("^RR_CLIENT_(\\d{1,4})");
    private MulticastServer mcastServer;
    private Matcher mcastHeaderMatcher;

    public RequestListener(int port, String address) throws Exception {
        this.mcastServer = new MulticastServer(port, address);
    }

    private MulticastPacket getValidMulticastPacket() {
        MulticastPacket mpack = null;

        System.out.println("Listening for multicast packets...");

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

    public void run() {
        while (true) {
            MulticastPacket mpack = this.getValidMulticastPacket();

            int svcid = Integer.parseInt(this.mcastHeaderMatcher.group(1));

            System.out.println("RequestListener got multicast request with service id " + svcid);
            if (ServiceHandler.exists(svcid)) {
                try {
                    PacketBuffer.insert(new ProtocolPacket(svcid, mpack.getAddress(), mpack.getPort()));
                    RRServer.availableRequest.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
