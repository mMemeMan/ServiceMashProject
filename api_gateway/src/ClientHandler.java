import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = input.readLine();
            System.out.println("request from client: " + request);

            String[] requestParts = request.split("\\|");

            if (requestParts.length > 0) {
                String requestType = requestParts[0];
                switch (requestType) {
                    case "REGISTER":
                        handleRegistration(requestParts);
                        break;
                    case "LOGIN":
                        handleLogin(requestParts);
                        break;
                    case "WRITE_POST":
                        handleWritePost(request);
                        break;
                    case "READ_POSTS":
                        handlerReadPosts(output);
                        break;
                    case "UPLOAD_FILE":
                        handlerUploadFile(requestParts, output);
                        break;
                    case "DOWNLOAD_FILE":
                        handlerDownloadFile(requestParts, output);
                        break;
                    default:
                        System.out.println("Unknown request type");
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlerDownloadFile(String[] requestParts, PrintWriter output) {
        if (requestParts.length > 1) {
            String fileName = requestParts[1];

            String fileServiceIp = properties.getProperty("file.service.ip");
            int fileServicePort = Integer.parseInt(properties.getProperty("file.service.port"));

            try (Socket downloadFileSocket = new Socket(fileServiceIp, fileServicePort);
                 PrintWriter out = new PrintWriter(downloadFileSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(downloadFileSocket.getInputStream()))) {

                String downloadFileRequest = "DOWNLOAD_FILE|" + fileName;
                out.println(downloadFileRequest);
                System.out.println("File download request sent to FileService through ApiGateway.");

                String response = in.readLine();
                if (response != null && response.equals("FILE_DOWNLOADED")) {
                    output.println("FILE_DOWNLOADED");
                } else {
                    output.println("FILE_DOWNLOAD_FAILED");
                }
            } catch (IOException e) {
                e.printStackTrace();
                output.println("FILE_DOWNLOAD_FAILED");
            }
        } else {
            output.println("FILE_DOWNLOAD_FAILED");
        }
    }



    private void handlerUploadFile(String[] requestParts, PrintWriter output) {
        if (requestParts.length > 1) {
            String filePath = requestParts[1];

            String fileServiceIp = properties.getProperty("file.service.ip");
            int fileServicePort = Integer.parseInt(properties.getProperty("file.service.port"));

            try (Socket uploadFileSocket = new Socket(fileServiceIp, fileServicePort);
                 PrintWriter out = new PrintWriter(uploadFileSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(uploadFileSocket.getInputStream()))) {

                String uploadFileRequest = "UPLOAD_FILE|" + filePath;
                out.println(uploadFileRequest);
                System.out.println("File upload request sent to FileService through ApiGateway.");

                String response = in.readLine();
                if (response != null && response.equals("FILE_UPLOADED")) {
                    output.println("FILE_UPLOADED");
                } else {
                    output.println("FILE_UPLOAD_FAILED");
                }
            } catch (IOException e) {
                e.printStackTrace();
                output.println("FILE_UPLOAD_FAILED");
            }
        } else {
            output.println("FILE_UPLOAD_FAILED");
        }
    }

    private void handlerReadPosts(PrintWriter output) {
        String fileServiceIp = properties.getProperty("post.service.ip");
        int fileServicePort = Integer.parseInt(properties.getProperty("post.service.port"));

        try (Socket readPostsSocket = new Socket(fileServiceIp, fileServicePort);
             PrintWriter out = new PrintWriter(readPostsSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(readPostsSocket.getInputStream()))) {

            String readPostsRequest = "READ_POSTS";
            out.println(readPostsRequest);
            System.out.println("Read posts request sent to PostService through ApiGateway.");

            String response;
            while ((response = in.readLine()) != null) {
                output.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleRegistration(String[] requestParts) {
        String login = requestParts[1];
        String password = requestParts[2];

        String fileServiceIp = properties.getProperty("registration.service.ip");
        int fileServicePort = Integer.parseInt(properties.getProperty("registration.service.port"));
        try {
            Socket registrationSocket = new Socket(fileServiceIp, fileServicePort);
            PrintWriter out = new PrintWriter(registrationSocket.getOutputStream(), true);

            out.println(login + "|" + password);

            registrationSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(String[] requestParts) {
        String login = requestParts[1];
        String password = requestParts[2];

        String fileServiceIp = properties.getProperty("login.service.ip");
        int fileServicePort = Integer.parseInt(properties.getProperty("login.service.port"));

        try (Socket loginSocket = new Socket(fileServiceIp, fileServicePort);
             PrintWriter out = new PrintWriter(loginSocket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(loginSocket.getInputStream()))) {

            String loginRequest = login + "|" + password;
            out.println(loginRequest);
            System.out.println("Login request sent to LoginService.");

            String response = input.readLine();
            if (response.equals("LOGIN_SUCCESS")) {
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                toClient.println("LOGIN_SUCCESS");
            } else {
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                toClient.println("LOGIN_FAILURE");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWritePost(String request) {
        String fileServiceIp = properties.getProperty("post.service.ip");
        int fileServicePort = Integer.parseInt(properties.getProperty("post.service.port"));

        try (Socket writePostSocket = new Socket(fileServiceIp, fileServicePort);
             PrintWriter out = new PrintWriter(writePostSocket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(writePostSocket.getInputStream()))) {

            out.println(request);

            String response = input.readLine();
            PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
            toClient.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
