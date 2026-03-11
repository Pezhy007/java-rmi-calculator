import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Implementation of the Calculator remote interface.
 * Provides a thread-safe stack-based calculator service that can be accessed
 * by multiple clients simultaneously. Supports both shared stack and per-client
 * stack modes.
 */
public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    private final boolean perClientStacks;
    private final Map<String, Deque<Integer>> stacks = new HashMap<>();

    /**
     * Default constructor that creates a calculator with shared stack mode.
     * @throws RemoteException if the remote object cannot be created
     */
    public CalculatorImplementation() throws RemoteException {
        this(false);
    }
    
    /**
     * Constructor that allows configuration of stack sharing mode.
     * @param perClientStacks if true, each client gets their own stack; 
     *                       if false, all clients share one stack
     * @throws RemoteException if the remote object cannot be created
     */
    public CalculatorImplementation(boolean perClientStacks) throws RemoteException {
        super();
        this.perClientStacks = perClientStacks;
        stacks.put("ALL", new ArrayDeque<>());
    }

    /**
     * Determines the current client's stack key.
     * Returns "ALL" for shared mode, or the client's host address for per-client mode.
     * @return string key identifying which stack to use
     */
    private String currentKey() {
        if (!perClientStacks) return "ALL";
        try {
            return RemoteServer.getClientHost();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Gets the appropriate stack for the current client.
     * Creates a new stack if needed for new clients in per-client mode.
     * @return the stack (Deque) for the current client to use
     */
    private Deque<Integer> getStack() {
        String key = currentKey();
        synchronized (stacks) {
            return stacks.computeIfAbsent(key, k -> new ArrayDeque<>());
        }
    }

    /**
     * Pushes an integer value onto the stack.
     * The value is added to the top of the stack in a thread-safe manner.
     * @param val the integer value to push onto the stack
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public void pushValue(int val) throws RemoteException {
        Deque<Integer> stack = getStack();
        synchronized (stack) {
            stack.push(val);
        }
    }

    /**
     * Pushes an operation onto the stack, processing all current values.
     * Pops all values from the stack, performs the specified operation on them,
     * and pushes the single result back onto the stack.
     * Supported operations: "min", "max", "gcd", "lcm" (case insensitive).
     * @param operator the operation to perform on all stack values
     * @throws RemoteException if operation is invalid or stack is empty
     */
    @Override
    public void pushOperation(String operator) throws RemoteException {
        String op = operator.toLowerCase(Locale.ROOT).trim();
        Deque<Integer> stack = getStack();

        List<Integer> values = new ArrayList<>();
        synchronized (stack) {
            while (!stack.isEmpty()) {
                values.add(stack.pop());
            }
        }
        if (values.isEmpty()) {
            throw new RemoteException("Operation '" + op + "' requested on an empty stack");
        }

        int result;
        switch (op) {
            case "min":
                result = values.stream().min(Integer::compareTo).get();
                break;
            case "max":
                result = values.stream().max(Integer::compareTo).get();
                break;
            case "gcd":
                result = fold(values, Math::abs, CalculatorImplementation::gcd);
                break;
            case "lcm":
                result = fold(values, Math::abs, CalculatorImplementation::lcm);
                break;
            default:
                throw new RemoteException("Unsupported operator: " + operator);
        }

        synchronized (stack) {
            stack.push(result);
        }
    }

    /**
     * Pops and returns the top value from the stack.
     * Removes the most recently added value from the stack.
     * @return the top value from the stack
     * @throws RemoteException if stack is empty or remote communication error occurs
     */
    @Override
    public int pop() throws RemoteException {
        Deque<Integer> stack = getStack();
        synchronized (stack) {
            if (stack.isEmpty()) {
                throw new RemoteException("Pop requested on empty stack");
            }
            return stack.pop();
        }
    }

    /**
     * Checks if the stack is empty.
     * Returns true if there are no values on the stack, false otherwise.
     * @return true if the stack is empty, false if it contains values
     * @throws RemoteException if a remote communication error occurs
     */
    @Override
    public boolean isEmpty() throws RemoteException {
        Deque<Integer> stack = getStack();
        synchronized (stack) {
            return stack.isEmpty();
        }
    }

    /**
     * Waits for the specified time before popping the top value.
     * Sleeps for the given number of milliseconds, then performs a normal pop operation.
     * If interrupted during sleep, the interrupt status is preserved.
     * @param millis milliseconds to wait before popping (negative values treated as 0)
     * @return the top value from the stack after the delay
     * @throws RemoteException if stack is empty or remote communication error occurs
     */
    @Override
    public int delayPop(int millis) throws RemoteException {
        try {
            Thread.sleep(Math.max(0, millis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return pop();
    }

    /**
     * Calculates the greatest common divisor of two integers using Euclidean algorithm.
     * Both inputs are converted to absolute values before calculation.
     * @param a first integer
     * @param b second integer
     * @return the GCD of a and b
     */
    private static int gcd(int a, int b) {
        a = Math.abs(a); b = Math.abs(b);
        if (a == 0) return b;
        if (b == 0) return a;
        while (b != 0) {
            int t = a % b;
            a = b; b = t;
        }
        return a;
    }
    
    /**
     * Calculates the least common multiple of two integers.
     * Uses the relationship LCM(a,b) = |a*b| / GCD(a,b).
     * Returns 0 if either input is 0.
     * @param a first integer
     * @param b second integer
     * @return the LCM of a and b, or 0 if either input is 0
     */
    private static int lcm(int a, int b) {
        a = Math.abs(a); b = Math.abs(b);
        if (a == 0 || b == 0) return 0;
        return Math.abs(a / gcd(a, b) * b);
    }

    /**
     * Functional interface for binary operations on integers.
     */
    @FunctionalInterface
    private interface IntBinOp { 
        int apply(int x, int y); 
    }
    
    /**
     * Functional interface for unary operations on integers.
     */
    @FunctionalInterface
    private interface IntUnary { 
        int apply(int x); 
    }

    /**
     * Applies a binary operation to a list of values using a fold/reduce pattern.
     * First applies the unary preprocessing function to each value,
     * then combines them using the binary operation from left to right.
     * @param vals list of integer values to process
     * @param pre unary function to preprocess each value
     * @param op binary operation to combine values
     * @return the final result after folding all values
     */
    private static int fold(List<Integer> vals, IntUnary pre, IntBinOp op) {
        int acc = pre.apply(vals.get(0));
        for (int i = 1; i < vals.size(); i++) {
            acc = op.apply(acc, pre.apply(vals.get(i)));
        }
        return acc;
    }
}