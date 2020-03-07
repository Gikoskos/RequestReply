import distsys.rr.*;
import java.nio.*;
public class ServerDemo {
    //public boolean primalityTest(int tocheck) {

    //}
    public static void main(String []args) {
        try {
            RRServer srv = new RRServer(10000, "226.1.1.1");
            srv.registerService(44);

            for (;;) {
                RequestData data = srv.getRequest(44);
                if (data != null) {
                    System.out.println("request id from app = " + data.getRequestId());
                    ByteBuffer buff = ByteBuffer.wrap(data.getData());
                    int int_data = buff.getInt();
                    System.out.println("data from app = " + int_data);

                    Thread.sleep(4000);
                    int x = 1;
                    srv.sendReply(data.getRequestId(), ByteBuffer.allocate(4).putInt(x).array());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
