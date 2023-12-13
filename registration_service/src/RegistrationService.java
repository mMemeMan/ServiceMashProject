import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class RegistrationService {
    private ServerSocket serverSocket;
    private boolean isRunning;

    public void startService() {
        try {
            serverSocket = new ServerSocket(9001);
            System.out.println("Registration service started. Waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection: " + socket);

                Thread clientThread = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String request = input.readLine();

                        String[] requestParts = request.split("\\|");
                        handleRegistration(requestParts);

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

    private void handleRegistration(String[] requestParts) {
            String login = requestParts[0];
            String password = requestParts[1];


        String url = "jdbc:mysql://localhost/projekt";
        String usernameDB = "root";
        String passwordDB = "";

        try (Connection connection = DriverManager.getConnection(url, usernameDB, passwordDB)) {
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User registered successfully.");
            } else {
                System.out.println("Failed to register user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RegistrationService registrationService = new RegistrationService();
        registrationService.startService();
    }
}
