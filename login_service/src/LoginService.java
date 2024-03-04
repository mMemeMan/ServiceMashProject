import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class LoginService implements Service{
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

    public void startService() {
        int servicePort = Integer.parseInt(properties.getProperty("login.service.port"));
        try {
            serverSocket = new ServerSocket(servicePort);
            System.out.println("Login service started. Waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection established: " + socket);

                Thread clientThread = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                        String request = input.readLine();
                        System.out.println("Received login request: " + request);

                        String[] requestParts = request.split("\\|");
                        handleLogin(requestParts, output);

                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(String[] requestParts, PrintWriter output) {
        String login = requestParts[0];
        String password = requestParts[1];

        String url = properties.getProperty("database.url");
        String usernameDB = properties.getProperty("database.username");
        String passwordDB = properties.getProperty("database.password");

        try (Connection connection = DriverManager.getConnection(url, usernameDB, passwordDB)) {
            String selectQuery = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                output.println("LOGIN_SUCCESS");
            } else {
                output.println("LOGIN_FAILURE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginService loginService = new LoginService();
        loginService.startService();
    }
}
