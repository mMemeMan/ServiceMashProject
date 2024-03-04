import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;

public class PostService implements Service{
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
        int servicePort = Integer.parseInt(properties.getProperty("post.service.port"));
        try {
            serverSocket = new ServerSocket(servicePort);

            System.out.println("Post service started. Waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection: " + socket);

                Thread clientThread = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                        String request = input.readLine();
                        System.out.println("Received post request: " + request);

                        String[] requestParts = request.split("\\|");

                        if (requestParts.length > 0) {
                            String requestType = requestParts[0];

                            switch (requestType) {
                                case "WRITE_POST":
                                    handleWritePost(requestParts, output);
                                    break;
                                case "READ_POSTS":
                                    handleReadPosts(output);
                                    break;
                                default:
                                    output.println("Unknown request type");
                            }
                        }

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

    private void handleReadPosts(PrintWriter output) {
        String url = properties.getProperty("database.url");
        String usernameDB = properties.getProperty("database.username");
        String passwordDB = properties.getProperty("database.password");

        try (Connection connection = DriverManager.getConnection(url, usernameDB, passwordDB)) {
            String query = "SELECT users.username, posts.content, posts.tstamp FROM posts " +
                    "INNER JOIN users ON posts.user_id = users.id";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String content = resultSet.getString("content");
                Timestamp timestamp = resultSet.getTimestamp("tstamp");

                String response = "Username: " + username + ", Content: " + content + ", Timestamp: " + timestamp;
                output.println(response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            output.println("Failed to fetch posts from the database.");
        }
    }

    private void handleWritePost(String[] requestParts, PrintWriter output) {
        String content = requestParts[1];
        String username = requestParts[2];

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String url = properties.getProperty("database.url");
        String usernameDB = properties.getProperty("database.username");
        String passwordDB = properties.getProperty("database.password");

        try (Connection connection = DriverManager.getConnection(url, usernameDB, passwordDB)) {
            String userIdQuery = "SELECT id FROM users WHERE username=?";
            PreparedStatement userIdStatement = connection.prepareStatement(userIdQuery);
            userIdStatement.setString(1, username);

            ResultSet resultSet = userIdStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("id");

                String insertQuery = "INSERT INTO posts (user_id, content, tstamp) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, content);
                preparedStatement.setTimestamp(3, timestamp);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Post saved in the database.");
                    output.println("POST_RECEIVED");
                } else {
                    System.out.println("Failed to save post in the database.");
                    output.println("POST_FAILED");
                }
            } else {
                System.out.println("User not found.");
                output.println("USER_NOT_FOUND");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            output.println("POST_FAILED");
        }
    }


    public static void main(String[] args) {
        PostService postService = new PostService();
        postService.startService();
    }
}
