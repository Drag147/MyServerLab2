package servers;

import java.net.*;
import java.io.*;
import java.time.LocalDate;
import java.util.Date;

public class Server {

    private final ServerSocket serverSocket;
    private Socket socketClient;

    private String typeReq;
    private String fileReq;

    private OutputStream outS;
    private InputStream inS;

    private String response;

    public Server(int port) throws IOException {

        serverSocket = new ServerSocket(port);
    }

    public void startServer() {

        while (true) {
            waiteConnection();
            readRequest();

            generateResponse();
        }
    }

    private void waiteConnection() {

        try {
            this.socketClient = this.serverSocket.accept();
        }catch (Exception ex){
            waiteConnection();
        }
    }

    private void readRequest() {
        try {
            inS = this.socketClient.getInputStream();
            this.outS = this.socketClient.getOutputStream();

            byte[] buffer = new byte[64 * 1024];
            int r = inS.read(buffer);
            String request = new String(buffer, 0, r);

            typeReq = request.substring(0, request.indexOf("/")-1);

            int indexRef = request.indexOf("/") + 1;
            int lastRef = request.indexOf("HTTP", indexRef) - 1;
            fileReq = request.substring(indexRef, lastRef);
        }
        catch (Exception ex){
            waiteConnection();
        }
    }

    private void generateResponse() {

        if(typeReq.equals("GET")) {
            getRequest();
        }
    }

    private void getRequest(){

        try {
            if (fileReq.equals("") || fileReq.equals("index.php") || fileReq.equals("index")) {
                fileReq = "index.html";
            }
//            if (fileReq.equals("favicon.ico")) {
//                typeReq = "";
//                inS.close();
//                outS.close();
//                this.socketClient.close();
//                return;
//            }

            File file = new File("html/"+fileReq);

            System.out.println(typeReq + " -> " + fileReq);

            if (!file.exists()) {
                get404();
            } else {
                get200(file);
            }

            inS.close();
            outS.close();

            if(!this.socketClient.isClosed()) {
                this.socketClient.close();
            }

            typeReq = "";
        } catch (Exception ex) {
            typeReq = "";
        }
    }

    private void get404() throws IOException {

        byte[] bufferFile = new byte[64 * 1024];

        response = "HTTP/1.1 404 Not Found\n";
        response += "Date: " + LocalDate.now() + "\n";
        response += "Content-Type: text/html\n";
        response += "Connection: close\n";
        response += "Server: Server\n";
        response += "Pragma: no-cache\n\n";

        outS.write(response.getBytes());

        FileInputStream fis = new FileInputStream("html/404.html");
        int write = 1;
        while (write > 0) {
            write = fis.read(bufferFile);
            if (write > 0) outS.write(bufferFile, 0, write);
        }

        fis.close();
    }

    private void get200(File file) throws IOException {

        byte[] bufferFile = new byte[64 * 1024];

        response = "HTTP/1.1 200 OK\n";
        response += "Last-Modified: " + new Date(file.lastModified()) + "\n";
        response += "Content-Length: " + file.length() + "\n";
        response += "Content-Type: ";

        switch (fileReq.substring(fileReq.indexOf(".")+1).toLowerCase()){
            case "html":
                response += "text/html" + "\n";
                break;
            case "css":
                response += "text/css" + "\n";
                break;
            case "ico":
                response += "image/vnd.microsoft.icon" + "\n";
                break;
            case "png":
                response += "image/png" + "\n";
                break;
            case "jpg":
                response += "image/jpeg" + "\n";
                break;
            case "jpeg":
                response += "image/jpeg" + "\n";
                break;
            case "min.js":
                response += "text/javascript" + "\n";
                break;
            case "js":
                response += "text/javascript" + "\n";
                break;
            default:
                response += response += "text/html" + "\n";

        }

        response += "Connection: close\n";
        response += "Server: Server\n\n";

        outS.write(response.getBytes());

        FileInputStream fis = new FileInputStream("html/"+fileReq);
        int write = 1;
        while (write > 0) {
            write = fis.read(bufferFile);
            if (write > 0) outS.write(bufferFile, 0, write);
        }

        fis.close();
    }
}
