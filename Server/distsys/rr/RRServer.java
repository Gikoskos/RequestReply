package distsys.rr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class RRServer {
    private final RequestListener rListener;
    private final ExecutorService requestPool;
    private final ExecutorService replyPool;

    public RRServer(final int multicastPort, final String multicastAddress) throws Exception {
        System.out.println("On RRServer!");

        this.rListener = new RequestListener(multicastPort, multicastAddress);

        requestPool = Executors.newFixedThreadPool(GlobalLimits.POOL_SIZE);
        replyPool = Executors.newFixedThreadPool(GlobalLimits.POOL_SIZE);

        for (int i = 0; i < GlobalLimits.POOL_SIZE; i++) {
            requestPool.execute(new RequestWorker());
            replyPool.execute(new ReplyWorker());
        }

        rListener.start();
    }

    public void waitUntilDone() {
        try {
            this.rListener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int registerService(int svcid) {
        return ServiceHandler.register(svcid);
    }

    public int unregisterService(int svcid) {
        return ServiceHandler.unregister(svcid);
    }

    public RequestData getRequest(int svcid) {
        RequestData request = null;

        try {
            request = ServiceHandler.takeServiceRequest(svcid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }

    public void sendReply(int reqid, byte[] buf) {
        try {
            Queues.replyQueue.put(new ReplyData(reqid, buf));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
