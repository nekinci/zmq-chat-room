import java.util.Scanner;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQ.Poller;

public class ChatClient{
    private final static Scanner in = new Scanner(System.in);

    private static class Sender extends Thread {
        private final String name; // Gönderici ismi alınıyor.
        private final Socket send; // Sender Socket nesnesi tanımlanıyor.

        public Sender(String name, Socket send) {
            this.name = name;
            this.send = send; // Mainden gelen send nesnesi alınıyor.
            send.send(name + " has joined");
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                String messageToSend = in.nextLine();
                send.send(name + ": " + messageToSend);
            }
        }
    }

    private static class Receiver extends Thread {
        private final Poller poller; // Polling için nesne tanımlaması yapıyoruz.
        private final Socket receive;  // Alıcı soket tanımlaması yapılıyor.

        public Receiver(Poller poller, Socket receive) {
            this.poller = poller; // Main fonksiyonundan gelen polleri atadık.
            this.receive = receive;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int events = poller.poll();
                if (events > 0) {
                    String recvMessage = receive.recvStr(0);
                    System.out.println(recvMessage);
                }
            }
        }
    }

    public static void main(String[] args) {
        Context context = ZMQ.context(1);

        Socket send = context.socket(ZMQ.PUSH);
        send.connect("tcp://localhost:5001");

        Socket receive = context.socket(ZMQ.SUB);
        receive.connect("tcp://localhost:5000");
        receive.subscribe("".getBytes());

        Poller poller = context.poller();
        poller.register(receive, Poller.POLLIN);

        System.out.print("What is your name? ");
        String name = in.nextLine();

        try {
            Sender sender = new Sender(name, send);
            sender.start();

            Receiver receiver = new Receiver(poller, receive);
            receiver.start();

            sender.join();
            receiver.join();
        }
        catch (InterruptedException e) {}
        finally {
            receive.close();
            send.close();
            context.term();
        }
    }


}