package distsys.rr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class RRServer {
    public static final Semaphore availableRequest = new Semaphore(GlobalLimits.BUFFER_SIZE, true);
    public static final Semaphore availableReply = new Semaphore(GlobalLimits.BUFFER_SIZE, true);

    private final RequestListener rListener;
    private final ExecutorService requestPool;
    private final ExecutorService replyPool;

    public RRServer(final int multicastPort, final String multicastAddress) throws Exception {
        availableRequest.drainPermits();
        availableReply.drainPermits();

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
        if (!ServiceHandler.waitForServiceRequest(svcid)) {
            return null;
        }

        ProtocolPacket packet = PacketBuffer.getPacket(RequestState.ARRIVED, svcid);

        if (packet == null) {
            return null;
        }

        packet.setState(RequestState.PENDING_REPLY);

        return new RequestData(packet.getPacketId(), packet.getRequestBuffer());
    }

    public void sendReply(int reqid, byte[] buf) {
        ProtocolPacket packet = PacketBuffer.getPacket(reqid);

        if (packet != null) {
            packet.setState(RequestState.REPLIED);
            packet.serializeReplyBuffer(buf);
            availableReply.release();
        }
    }
}
