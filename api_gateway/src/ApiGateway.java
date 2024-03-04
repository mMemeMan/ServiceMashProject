import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class ApiGateway {
    private ServerSocket serverSocket;
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));
        try {
            serverSocket = new ServerSocket(apiGatewayPort);
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
