import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ApiGateway {
    private ServerSocket serverSocket;
    private boolean isRunning;

    public ApiGateway() {
        isRunning = false;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(9000);
            System.out.println("Server started. Waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection established: " + socket);

                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ApiGateway apiGateway = new ApiGateway();
        apiGateway.startServer();
    }
}
