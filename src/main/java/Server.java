import org.zeromq.*;


public class Server implements Runnable{



    public Server(){
        run();
    }


    public static void main(String[] args) {
        new Server();
    }

    @Override
    public void run() {

        //Context nesnesi oluşturuluyor.
        ZMQ.Context context = ZMQ.context(1);

        // Pub Socketini 5000 portu üzerinde açılıyor.
        ZMQ.Socket pub = context.socket(ZMQ.PUB);
        pub.bind("tcp://localhost:5000");

        //Pull Socketini 5001 portu üzerinde bağlandık.
        ZMQ.Socket receive = context.socket(ZMQ.PULL);
        receive.bind("tcp://localhost:5001");


        // Mesaj alındığında burası çalışıyor.
        while(!Thread.currentThread().isInterrupted()){
            String message = receive.recvStr();  // Receive socketinden yani 5001 portu üzerinden mesajı aldık.
            System.out.println("Received : "+message);
            pub.send(message,0); // Tüm alıcılara mesajı gönderdik.
        }

        receive.close();
        pub.close();
        context.term();

    }
}