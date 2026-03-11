import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for the Calculator service.
 * Defines all remote methods that clients can invoke on the server.
 */
public interface Calculator extends Remote {
    
    /**
     * Pushes an integer value onto the stack.
     * @param val the integer value to push onto the stack
     * @throws RemoteException if a remote communication error occurs
     */
    void pushValue(int val) throws RemoteException;
    
    /**
     * Pushes an operation onto the stack, which processes all current values.
     * Pops all values from the stack, performs the specified operation, 
     * and pushes the single result back onto the stack.
     * @param operator the operation to perform: "min", "max", "gcd", or "lcm"
     * @throws RemoteException if a remote communication error occurs or invalid operator
     */
    void pushOperation(String operator) throws RemoteException;
    
    /**
     * Pops and returns the top value from the stack.
     * @return the top value from the stack
     * @throws RemoteException if a remote communication error occurs or stack is empty
     */
    int pop() throws RemoteException;
    
    /**
     * Checks if the stack is empty.
     * @return true if the stack is empty, false otherwise
     * @throws RemoteException if a remote communication error occurs
     */
    boolean isEmpty() throws RemoteException;
    
    /**
     * Waits for the specified time before popping the top value.
     * @param millis milliseconds to wait before performing the pop operation
     * @return the top value from the stack after the delay
     * @throws RemoteException if a remote communication error occurs or stack is empty
     */
    int delayPop(int millis) throws RemoteException;
}