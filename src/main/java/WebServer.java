import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {

    public static void main(String[] args) {
        /*
        The server handle requests concurrently but within a limit. (8 threads)
         */
        final int PORT = 8080;
        ExecutorService pool = Executors.newFixedThreadPool(8);

        try{
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);

            while(true){

                Socket clientSocket = serverSocket.accept();

                pool.submit(() -> { handleClientRequest(clientSocket); });
            }
        }catch(IOException e){

            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (   BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = input.readLine();
            System.out.println("Received request: " + request);

            if (request != null && request.startsWith("GET")) {
                String[] requestParts = request.split(" ");
                String path = requestParts[1].substring(1); // /index.html -> index.html

                if (path.isEmpty()) {
                    path = "index.html";
                }

                System.out.println("Requested path: " + path);
                serveFile(path, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void serveFile(String fileName, PrintWriter output) throws IOException {
        String baseDir = "src/main/java/";
        File file = new File(baseDir + fileName);

        if(file.exists()){
            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: text/html");
            output.println("Content-Length: " + file.length());
            output.println();

            try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    output.write(new String(buffer, 0, bytesRead));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            output.println("HTTP/1.1 404 Not Found");
            output.println("Content-Type: text/html");
            output.println();
            output.println("<h1>404 Not Found<h1>");
        }

    }
}

