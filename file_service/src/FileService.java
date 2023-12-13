import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileService {
    private ServerSocket serverSocket;
    private boolean isRunning;

    public FileService() {
        isRunning = false;
    }

    public void startService() {
        try {
            serverSocket = new ServerSocket(9004);
            isRunning = true;
            System.out.println("FileService started. Waiting for connections...");

            while (isRunning) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection established: " + socket);

                Thread clientThread = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                        String request = input.readLine();
                        System.out.println("Received file upload request: " + request);

                        String[] requestParts = request.split("\\|");

                        if (requestParts.length > 0) {
                            String requestType = requestParts[0];

                            switch (requestType) {
                                case "UPLOAD_FILE":
                                    handleFileUpload(requestParts, output);
                                    break;
                                case "DOWNLOAD_FILE":
                                    handleFileDownload(requestParts, output);
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


    private void handleFileDownload(String[] requestParts, PrintWriter output) {
        if (requestParts.length > 1) {
            String fileName = requestParts[1];

            File fileToDownload = new File("file_service/files/" + fileName);
            File destination = new File(System.getProperty("user.home") + "/Downloads/" + fileName);

            try {
                FileInputStream fileInputStream = new FileInputStream(fileToDownload);
                FileOutputStream fileOutputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = fileInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                fileInputStream.close();
                fileOutputStream.close();

                output.println("FILE_DOWNLOADED");
                System.out.println("File downloaded to system Downloads folder.");
            } catch (IOException e) {
                e.printStackTrace();
                output.println("FILE_DOWNLOAD_FAILED");
            }
        } else {
            output.println("FILE_DOWNLOAD_FAILED");
        }
    }

    private void handleFileUpload(String[] requestParts, PrintWriter output) {
        if (requestParts.length > 1) {
            String filePath = requestParts[1];

            File fileToCopy = new File(filePath);
            File destination = new File("file_service/files/" + fileToCopy.getName());

            try {
                FileInputStream fileInputStream = new FileInputStream(fileToCopy);
                FileOutputStream fileOutputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = fileInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                fileInputStream.close();
                fileOutputStream.close();

                output.println("FILE_UPLOADED");
                System.out.println("File uploaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                output.println("FILE_UPLOAD_FAILED");
            }
        } else {
            output.println("FILE_UPLOAD_FAILED");
        }
    }

    public static void main(String[] args) {
        FileService fileService = new FileService();
        fileService.startService();
    }
}
