package distsys.rr;

public class RRServer {
    private RequestListener rListener;

    public RRServer(final int multicastPort, final String multicastAddress) throws Exception {
        this.rListener = new RequestListener(multicastPort, multicastAddress);

        this.rListener.start();
    }

    public void waitUntilDone() {
        try {
            this.rListener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int registerService(int svcid) {
        return 0;
    }

    public int unregisterService(int svcid) {
        return 0;
    }

    public int getRequest(int svcid) {
        return 0;
    }

    public void sendReply(int reqid, byte[] buf) {

    }
}
