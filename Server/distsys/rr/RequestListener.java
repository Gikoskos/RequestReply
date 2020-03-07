package distsys.rr;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RequestListener extends Thread {
    private static final Pattern mcastHeaderPattern = Pattern.compile("^RR_CLIENT_(\\d{1,4})_(\\d{1,9})");
    private MulticastServer mcastServer;
    private Matcher mcastHeaderMatcher;

    public RequestListener(int port, String address) throws Exception {
        this.mcastServer = new MulticastServer(port, address);
    }

    private MulticastPacket getValidMulticastPacket() {
        MulticastPacket mpack = null;

        Dbg.purple("Listening for multicast packets...");

        while (mpack == null) {
            try {
                mpack = this.mcastServer.listen();
                this.mcastHeaderMatcher = mcastHeaderPattern.matcher(mpack.getString());
                
                if (this.mcastHeaderMatcher.matches()) {
                    Dbg.purple("VALID MESSAGE BY " + mpack.getAddress().getHostName() + ": " + mpack.getString());
                    mpack.setSvcid(Integer.parseInt(this.mcastHeaderMatcher.group(1)));
                    mpack.setNetworkid(Integer.parseInt(this.mcastHeaderMatcher.group(2)));
                } else {
                    Dbg.purple("INVALID MESSAGE BY " + mpack.getAddress().getHostName() + ": " + mpack.getString());
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

            Dbg.purple("RequestListener got multicast request with service id " + mpack.getSvcid() + " networkId " + mpack.getNetworkid());

            try {
                Queues.multicastPacketQueue.put(mpack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
