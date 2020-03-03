import java.net.*;

public class ClientDemo {
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
        ClientDemo.send();
    }
}
 
