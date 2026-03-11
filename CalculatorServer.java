import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Server class that creates and registers the Calculator service.
 * Handles RMI registry setup and service binding.
 */
public class CalculatorServer {
    
    /**
     * Main method to start the Calculator server.
     * Creates the RMI registry, instantiates the calculator service,
     * and binds it to the registry for client access.
     * @param args command line arguments:
     *             args[0] - port number (default: 1099)
     *             args[1] - service name (default: "Calculator")
     *             args[2] - per-client stacks mode (default: false)
     */
    public static void main(String[] args) {
        int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 1099;
        String name = (args.length >= 2) ? args[1] : "Calculator";
        boolean perClientStacks = (args.length >= 3) && Boolean.parseBoolean(args[2]);

        try {
            try {
                LocateRegistry.createRegistry(port);
                System.out.println("RMI registry created on port " + port);
            } catch (Exception e) {
                System.out.println("RMI registry may already be running on port " + port + ": " + e);
            }

            Calculator impl = new CalculatorImplementation(perClientStacks);
            String url = "//localhost:" + port + "/" + name;
            Naming.rebind(url, impl);
            System.out.println("Calculator server bound at " + url + " (perClientStacks=" + perClientStacks + ")");
            System.out.println("Press Ctrl+C to stop.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}