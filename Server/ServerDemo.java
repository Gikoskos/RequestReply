import distsys.rr.*;
import java.nio.*;

public class ServerDemo {

    public static boolean isPrime(long n) {
        if (n <= 3) {
            return n > 1;
        } else if ((n % 2) == 0 || (n % 2) == 0) {
            return false;
        }

        for (long i = 5; (i * i) <= n; i++) {
            if ((n % i) == 0 || (n % (i + 2)) == 0) {
                return false;
            }

            i = i + 6;
        }

        return true;
    }

    public static void main(String []args) {
        try {
            RRServer srv = new RRServer(10000, "226.1.1.1");
            srv.registerService(44);

            for (;;) {
                RequestData data = srv.getRequest(44);
                if (data != null) {
                    System.out.println("request id from app = " + data.getRequestId());
                    ByteBuffer buff = ByteBuffer.wrap(data.getData());
                    long int_data = buff.getLong();
                    System.out.println("data from app = " + int_data);

                    //Thread.sleep(4000);
                    int x = isPrime(int_data) ? 1 : 0;
                    //int x = 12;
                    srv.sendReply(data.getRequestId(), ByteBuffer.allocate(4).putInt(x).array());
                } else {
                    System.out.println("!!!getRequest failed!!!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
