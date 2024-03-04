import java.util.ArrayList;

public class Manager {
    public static void main(String[] args) {
        ArrayList<Thread> agents = new ArrayList<>();

        agents.add(startApiGateway());
        agents.add(startFileAgent());
        agents.add(startPostAgent());
        agents.add(startLoginAgent());
        agents.add(startRegistrationAgent());

        for (Thread agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Thread startLoginAgent() {
        Agent agent = new Agent(new LoginService());
        agent.start();
        System.out.println("LoginAgent started.");
        return agent;
    }

    private static Thread startRegistrationAgent() {
        Agent agent = new Agent(new RegistrationService());
        agent.start();
        System.out.println("RegistrationAgent started.");
        return agent;
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
        Agent agent = new Agent(new FileService());
        agent.start();
        System.out.println("FileAgent started.");
        return agent;
    }

    private static Thread startPostAgent() {
        Agent agent = new Agent(new PostService());
        agent.start();
        System.out.println("PostAgent started.");
        return agent;
    }
}
