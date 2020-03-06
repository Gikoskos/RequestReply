package distsys.rr;

public class RequestData {
    private final byte[] data;
    private final int reqid;

    public RequestData(int reqid, byte[] data) {
        this.reqid = reqid;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public int getRequestId() {
        return reqid;
    }
}