import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    private ServerSocket serverSocket;

    public void startService() {
        try {
            serverSocket = new ServerSocket(9002);
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

        String url = "jdbc:mysql://localhost/projekt";
        String usernameDB = "root";
        String passwordDB = "";

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
