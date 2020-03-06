import java.net.*;

public class ClientDemo {
    public static void send() {
        try {
            MulticastSocket s = new MulticastSocket();
            String msg = "RR_CLIENT_550";
            DatagramPacket recv = new DatagramPacket(
                msg.getBytes(),
                msg.length(),
                InetAddress.getByName("226.1.1.1"),
                10000
            );


            s.send(recv);
            s.close();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        System.out.println("Client init");
        ClientDemo.send();
    }
}