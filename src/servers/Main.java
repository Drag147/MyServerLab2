package servers;

public class Main {

    public static void main(String[] args) {

        try {
            Server server = new Server(8080);

            server.startServer();

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
