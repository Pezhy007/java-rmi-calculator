import java.rmi.Naming;
import java.util.concurrent.*;

/**
 * Enhanced test client that provides comprehensive testing coverage
 * for all Calculator operations including edge cases and error conditions.
 * Tests both single-client and multi-client scenarios.
 */
public class EnhancedTestClient {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    
    /**
     * Main method that runs comprehensive tests on the Calculator service.
     * @param args command line arguments (same as other clients)
     * @throws Exception if connection or test setup fails
     */
    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port    = (args.length >= 2) ? Integer.parseInt(args[1]) : 1099;
        String name = (args.length >= 3) ? args[2] : "Calculator";
        
        String url = "//" + host + ":" + port + "/" + name;
        
        System.out.println("=== Enhanced Calculator Test Suite ===");
        System.out.println("Testing server at: " + url);
        System.out.println();
        
        // Run all test categories
        testBasicOperations(url);
        testMathematicalOperations(url);
        testErrorConditions(url);
        testMultiClientScenarios(url);
        
        // Print results
        System.out.println("=== Test Results ===");
        System.out.println("Tests passed: " + testsPassed + "/" + testsRun);
        System.out.println("Success rate: " + (100.0 * testsPassed / testsRun) + "%");
        if (testsPassed == testsRun) {
            System.out.println("🎉 All tests passed!");
        }
    }
    
    /**
     * Tests basic stack operations: push, pop, isEmpty.
     */
    private static void testBasicOperations(String url) throws Exception {
        System.out.println("--- Basic Operations Tests ---");
        Calculator calc = (Calculator) Naming.lookup(url);
        
        // Clear stack first
        while (!calc.isEmpty()) {
            calc.pop();
        }
        
        runTest("isEmpty on empty stack", () -> calc.isEmpty());
        
        runTest("pushValue and stack state", () -> {
            calc.pushValue(10);
            calc.pushValue(20);
            return !calc.isEmpty();
        });
        
        runTest("pop operations (LIFO)", () -> {
            int val1 = calc.pop(); // Should be 20
            int val2 = calc.pop(); // Should be 10
            return val1 == 20 && val2 == 10 && calc.isEmpty();
        });
        
        runTest("delayPop timing", () -> {
            calc.pushValue(99);
            long start = System.currentTimeMillis();
            int result = calc.delayPop(300);
            long duration = System.currentTimeMillis() - start;
            return result == 99 && duration >= 250 && duration <= 400;
        });
        
        System.out.println();
    }
    
    /**
     * Tests all mathematical operations: min, max, gcd, lcm.
     */
    private static void testMathematicalOperations(String url) throws Exception {
        System.out.println("--- Mathematical Operations Tests ---");
        Calculator calc = (Calculator) Naming.lookup(url);
        
        runTest("MIN operation", () -> {
            calc.pushValue(15);
            calc.pushValue(5);
            calc.pushValue(25);
            calc.pushOperation("min");
            int result = calc.pop();
            return result == 5 && calc.isEmpty();
        });
        
        runTest("MAX operation", () -> {
            calc.pushValue(15);
            calc.pushValue(5);
            calc.pushValue(25);
            calc.pushOperation("max");
            int result = calc.pop();
            return result == 25 && calc.isEmpty();
        });
        
        runTest("GCD operation", () -> {
            calc.pushValue(12);
            calc.pushValue(18);
            calc.pushValue(24);
            calc.pushOperation("gcd");
            int result = calc.pop();
            return result == 6 && calc.isEmpty();
        });
        
        runTest("LCM operation", () -> {
            calc.pushValue(4);
            calc.pushValue(6);
            calc.pushValue(8);
            calc.pushOperation("lcm");
            int result = calc.pop();
            return result == 24 && calc.isEmpty();
        });
        
        runTest("Operations with negative numbers", () -> {
            calc.pushValue(-10);
            calc.pushValue(-5);
            calc.pushValue(-20);
            calc.pushOperation("max");
            int result = calc.pop();
            return result == -5 && calc.isEmpty();
        });
        
        System.out.println();
    }
    
    /**
     * Tests error conditions and edge cases.
     */
    private static void testErrorConditions(String url) throws Exception {
        System.out.println("--- Error Conditions Tests ---");
        Calculator calc = (Calculator) Naming.lookup(url);
        
        // Ensure stack is empty
        while (!calc.isEmpty()) {
            calc.pop();
        }
        
        runTest("Pop from empty stack throws exception", () -> {
            try {
                calc.pop();
                return false; // Should have thrown exception
            } catch (Exception e) {
                return true; // Expected
            }
        });
        
        runTest("Operation on empty stack throws exception", () -> {
            try {
                calc.pushOperation("min");
                return false; // Should have thrown exception
            } catch (Exception e) {
                return true; // Expected
            }
        });
        
        runTest("Invalid operation throws exception", () -> {
            calc.pushValue(10);
            try {
                calc.pushOperation("invalid");
                return false; // Should have thrown exception
            } catch (Exception e) {
                // Clean up
                if (!calc.isEmpty()) calc.pop();
                return true; // Expected
            }
        });
        
        runTest("Case insensitive operations", () -> {
            calc.pushValue(10);
            calc.pushValue(20);
            calc.pushOperation("MAX"); // Uppercase
            int result = calc.pop();
            return result == 20;
        });
        
        System.out.println();
    }
    
    /**
     * Tests multi-client scenarios with 4+ concurrent clients.
     */
    private static void testMultiClientScenarios(String url) throws Exception {
        System.out.println("--- Multi-Client Tests (4+ clients) ---");
        
        runTest("Concurrent pushValue operations", () -> {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CountDownLatch latch = new CountDownLatch(4);
            
            for (int i = 0; i < 4; i++) {
                final int clientId = i;
                executor.submit(() -> {
                    try {
                        Calculator calc = (Calculator) Naming.lookup(url);
                        for (int j = 0; j < 3; j++) {
                            calc.pushValue(clientId * 10 + j);
                        }
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            return completed;
        });
        
        runTest("Concurrent mixed operations", () -> {
            ExecutorService executor = Executors.newFixedThreadPool(6);
            CountDownLatch latch = new CountDownLatch(6);
            boolean[] success = {true};
            
            // Different types of operations from different clients
            for (int i = 0; i < 6; i++) {
                final int clientId = i;
                executor.submit(() -> {
                    try {
                        Calculator calc = (Calculator) Naming.lookup(url);
                        
                        switch (clientId % 3) {
                            case 0:
                                calc.pushValue(clientId + 1);
                                break;
                            case 1:
                                if (!calc.isEmpty()) calc.pop();
                                break;
                            case 2:
                                calc.pushValue(clientId);
                                calc.delayPop(50);
                                break;
                        }
                        latch.countDown();
                    } catch (Exception e) {
                        success[0] = false;
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(15, TimeUnit.SECONDS);
            executor.shutdown();
            return completed && success[0];
        });
        
        System.out.println();
    }
    
    /**
     * Helper method to run individual tests and track results.
     */
    private static void runTest(String testName, TestCase test) {
        testsRun++;
        try {
            boolean passed = test.run();
            if (passed) {
                testsPassed++;
                System.out.println("✓ " + testName);
            } else {
                System.out.println("✗ " + testName + " - FAILED");
            }
        } catch (Exception e) {
            System.out.println("✗ " + testName + " - ERROR: " + e.getMessage());
        }
    }
    
    @FunctionalInterface
    interface TestCase {
        boolean run() throws Exception;
    }
}