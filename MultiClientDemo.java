import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Multi-client demonstration for the Calculator RMI service.
 * Spawns multiple concurrent clients to test thread safety and
 * concurrent access to the shared calculator stack.
 */
public class MultiClientDemo {
    
    /**
     * Main method that creates and runs multiple concurrent clients.
     * Each client performs different operations to demonstrate that
     * multiple clients can safely access the calculator simultaneously.
     * @param args command line arguments:
     *             args[0] - server hostname (default: localhost)
     *             args[1] - server port (default: 1099)
     *             args[2] - service name (default: Calculator)
     * @throws Exception if any client operations fail
     */
    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port    = (args.length >= 2) ? Integer.parseInt(args[1]) : 1099;
        String name = (args.length >= 3) ? args[2] : "Calculator";

        int clients = 5;
        CountDownLatch ready = new CountDownLatch(clients);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < clients; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                try {
                    Calculator calc = (Calculator) Naming.lookup("//" + host + ":" + port + "/" + name);
                    ready.countDown();
                    start.await();

                    // Each client pushes some values
                    calc.pushValue(id + 1);
                    calc.pushValue((id + 1) * 10);

                    if (id == 0) {
                        // First client performs a max operation
                        calc.pushOperation("max");
                        int res = calc.pop();
                        System.out.println("[Client " + id + "] max result: " + res);
                    } else {
                        // Other clients test delayPop with different delays
                        calc.pushValue(id * 100);
                        int v = calc.delayPop(100 + (id * 20));
                        System.out.println("[Client " + id + "] delayPop got: " + v);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "client-" + id);
            threads.add(t);
            t.start();
        }

        // Wait for all clients to be ready, then start them simultaneously
        ready.await();
        start.countDown();

        // Wait for all clients to complete
        for (Thread t : threads) t.join();
        System.out.println("Multi-client demo complete.");
    }
}