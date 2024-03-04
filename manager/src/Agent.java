public class Agent extends Thread {
    Service service;

    public Agent(Service service){
        this.service = service;
    }

    @Override
    public void run() {
        service.startService();
    }
}
