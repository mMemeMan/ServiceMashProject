import java.util.ArrayList;

public class Manager {
    public static void main(String[] args) {
        ArrayList<Thread> agents = new ArrayList<>();

        agents.add(startApiGateway());
        agents.add(startFileAgent());
        agents.add(startPostAgent());
        agents.add(startLoginAgent());
        agents.add(startRegistrationAgent());

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Write stop to kill threads");
//        while (scanner.nextLine().equals("stop")){
//
//        }
    }

    private static Thread startLoginAgent() {
        Thread Agent = new Thread(() -> {
            LoginService loginAgent = new LoginService();
            loginAgent.startService();
        });
        Agent.start();
        System.out.println("LoginAgent started.");
        return Agent;
    }

    private static Thread startRegistrationAgent() {
        Thread Agent = new Thread(() -> {
            RegistrationService loginAgent = new RegistrationService();
            loginAgent.startService();
        });
        Agent.start();
        System.out.println("RegistrationAgent started.");
        return Agent;
    }

    private static Thread startApiGateway() {
        Thread api = new Thread(() -> {
            ApiGateway apiGateway = new ApiGateway();
            apiGateway.startServer();
        });
        api.start();
        System.out.println("ApiGateway started.");
        return api;
    }

    private static Thread startFileAgent() {
        Thread Agent = new Thread(() -> {
            FileService fileAgent = new FileService();
            fileAgent.startService();
        });
        Agent.start();
        System.out.println("FileAgent started.");
        return Agent;
    }

    private static Thread startPostAgent() {
        Thread Agent = new Thread(() -> {
            PostService postAgent = new PostService();
            postAgent.startService();
        });
        Agent.start();
        System.out.println("PostAgent started.");
        return Agent;
    }
}
