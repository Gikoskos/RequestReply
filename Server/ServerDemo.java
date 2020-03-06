import distsys.rr.*;

public class ServerDemo {

    public static void myFunc(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println("" + arr[i]);
            arr[i] = 'a';
        }
    }
    public static void main(String []args) {
        try {
            RRServer srv = new RRServer(10000, "226.1.1.1");
            srv.waitUntilDone();
        } catch (Exception e) {}

    }
}
