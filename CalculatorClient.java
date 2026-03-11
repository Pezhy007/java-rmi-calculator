import java.rmi.Naming;

/**
 * Test client for the Calculator RMI service.
 * Demonstrates basic functionality of all remote methods.
 */
public class CalculatorClient {
    
    /**
     * Main method that connects to the Calculator service and tests its operations.
     * Tests pushValue, pushOperation (gcd, min), pop, delayPop, and isEmpty methods.
     * @param args command line arguments:
     *             args[0] - server hostname (default: localhost)
     *             args[1] - server port (default: 1099)
     *             args[2] - service name (default: Calculator)
     * @throws Exception if connection or operation fails
     */
    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port    = (args.length >= 2) ? Integer.parseInt(args[1]) : 1099;
        String name = (args.length >= 3) ? args[2] : "Calculator";

        Calculator calc = (Calculator) Naming.lookup("//" + host + ":" + port + "/" + name);

        // Test GCD operation
        calc.pushValue(12);
        calc.pushValue(18);
        calc.pushValue(30);
        calc.pushOperation("gcd");
        System.out.println("gcd result: " + calc.pop());

        // Test MIN operation with negative numbers
        calc.pushValue(9);
        calc.pushValue(-3);
        calc.pushValue(100);
        calc.pushOperation("min");
        System.out.println("min result: " + calc.pop());

        // Test delayPop operation
        calc.pushValue(42);
        System.out.println("delayPop(500ms) result: " + calc.delayPop(500));
        
        // Test isEmpty
        System.out.println("isEmpty? " + calc.isEmpty());
    }
}