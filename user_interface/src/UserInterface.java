import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class UserInterface {
    private String login;
    private Scanner scanner;
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserInterface() {
        scanner = new Scanner(System.in);
        login = "";
    }

    public void start() {
        while (true) {
            displayMenu();
            String choice = getUserChoice();

            switch (choice) {
                case "1":
                    if (login.equals("")) {
                        register();
                    } else {
                        System.out.println("You are already logged in.");
                    }
                    break;
                case "2":
                    if (login.equals("")) {
                        login();
                    } else {
                        System.out.println("You are already logged in.");
                    }
                    break;
                case "3":
                    if (!login.equals("")) {
                        writePost();
                    } else {
                        System.out.println("Please log in to write a post.");
                    }
                    break;
                case "4":
                    if (!login.equals("")) {
                        readPosts();
                    } else {
                        System.out.println("Please log in to read posts.");
                    }
                    break;
                case "5":
                    if (!login.equals("")) {
                        uploadFile();
                    } else {
                        System.out.println("Please log in to upload a file.");
                    }
                    break;
                case "6":
                    if (!login.equals("")) {
                        downloadFile();
                    } else {
                        System.out.println("Please log in to download a file.");
                    }
                    break;
                case "7":
                        logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayMenu() {
        System.out.println();

        String chooseOption = "Choose an option";
        if (login != null) {
            if (!login.equals("")) chooseOption += "(" + login + ")";
        } else {
            chooseOption += "(Need sign in)";
        }
        chooseOption += ": ";

        System.out.println(chooseOption);
        System.out.println("1. Registration");
        System.out.println("2. Login");
        System.out.println("3. Write Post");
        System.out.println("4. Read Posts");
        System.out.println("5. Upload File");
        System.out.println("6. Download File");
        System.out.println("7. Logout");
        System.out.print("Enter your choice: ");
    }

    private String getUserChoice() {
        String choice = scanner.nextLine();
        return choice;
    }

    private void uploadFile() {
        System.out.print("Enter the file path: ");
        String filePath = scanner.nextLine();

        String request = "UPLOAD_FILE|" + filePath;

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(request);

            String response = input.readLine();
            if (response != null && response.equals("FILE_UPLOADED")) {
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("Failed to upload the file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String register() {
        System.out.println("Performing user registration...");
        System.out.print("Enter your login: ");
        String login = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

//      String[] requestParts = request.split("\\|");
        String request = "REGISTER|" + login + "|" + password;

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(request);
            System.out.println("Registration request sent to ApiGateway.");

            return request;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void login() {
        System.out.print("Enter your login: ");
        String login = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        String request = "LOGIN|" + login + "|" + password;

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(request);
            System.out.println("Login request sent to ApiGateway.");

            String response = in.readLine();
            if (response != null && response.equals("LOGIN_SUCCESS")) {
                System.out.println("Login successful.");
                this.login = login;
            } else {
                System.out.println("Login failed. Please check your credentials.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writePost() {
        System.out.println("Enter your post:");
        String postContent = scanner.nextLine();

        String request = "WRITE_POST|" + postContent + "|" + login;

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(request);
            System.out.println("Post write successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readPosts() {
        String request = "READ_POSTS";

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(request);
            System.out.println("Read posts request sent to ApiGateway.");

            String response;
            while ((response = input.readLine()) != null) {
                System.out.println(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile() {
        System.out.print("Enter the file name: ");
        String fileName = scanner.nextLine();

        String request = "DOWNLOAD_FILE|" + fileName;

        String apiGatewayIp = properties.getProperty("api.gateway.ip");
        int apiGatewayPort = Integer.parseInt(properties.getProperty("api.gateway.port"));

        try (Socket socket = new Socket(apiGatewayIp, apiGatewayPort);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(request);
            System.out.println("File download request sent to ApiGateway.");

            String response = input.readLine();
            if (response != null && response.equals("FILE_DOWNLOADED")) {
                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("Failed to download the file.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        login = "";
        System.out.println("You've logged out");
    }

    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        ui.start();
    }
}
