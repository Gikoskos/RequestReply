import java.net.*;

public class Server {
    public static void listen() {
        try {
            InetAddress group = InetAddress.getByName("228.5.6.7");
            MulticastSocket s = new MulticastSocket(6789);
            byte[] buf = new byte[15];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);

            s.setLoopbackMode(true);
            s.joinGroup(group);

            s.receive(recv);

            System.out.println(new String(buf));
            s.leaveGroup(group);
            s.close();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        System.out.println("Server init");
        Server.listen();
    }
}

//Client.java
import java.net.*;

public class Client {
    public static void send() {
        try {
            MulticastSocket s = new MulticastSocket(6789);
            String msg = "Test testtest";
            DatagramPacket recv = new DatagramPacket(
                msg.getBytes(),
                msg.length(),
                InetAddress.getByName("228.5.6.7"),
                6789
            );


            s.send(recv);
            s.close();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        System.out.println("Client init");
        Client.send();
    }
}
