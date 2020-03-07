package distsys.rr;

class ReplyData {
    private int reqid;
    private byte[] data;

    public ReplyData(int reqid, byte[] data) {
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
