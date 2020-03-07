import distsys.rr.*;

public class ServerDemo {
    public static void main(String []args) {
        try {
            RRServer srv = new RRServer(10000, "226.1.1.1");
            srv.registerService(44);
            srv.waitUntilDone();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
