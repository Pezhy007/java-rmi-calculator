# Java RMI Calculator Server

A distributed calculator built with Java RMI (Remote Method Invocation), supporting multiple concurrent clients operating on a shared server-side stack. Developed as part of a third-year Distributed Systems course at the University of Adelaide.

## Overview

Clients connect to a remote server and push values and operations onto a stack. The server processes operations and returns results remotely — demonstrating the fundamentals of distributed method invocation, synchronisation, and multi-threaded server design.

```
Client A ──┐
Client B ──┼──▶ RMI Server ──▶ Stack [ 5, 3, 8 ]
Client C ──┘
```

## Features

- Remote stack operations over Java RMI
- Supports `min`, `max`, `lcm`, and `gcd` operations across all stacked values
- `delayPop` — delayed pop with configurable millisecond wait (demonstrates async behaviour)
- Multi-client support with shared server-side stack
- Thread-safe implementation with synchronisation
- Automated test suite covering single-client and multi-client scenarios
- Bonus: per-client isolated stacks (each client maintains its own stack on the server)

## Remote Interface

```java
void pushValue(int val);           // Push integer onto stack
void pushOperation(String operator); // Apply min/max/lcm/gcd across stack
int pop();                         // Pop and return top value
boolean isEmpty();                 // Check if stack is empty
int delayPop(int millis);          // Wait millis ms then pop
```

## Build & Run

**Compile:**
```bash
javac Calculator.java CalculatorImplementation.java CalculatorServer.java CalculatorClient.java
```

**Start RMI registry and server:**
```bash
rmiregistry &
java CalculatorServer
```

**Run client:**
```bash
java CalculatorClient
```

**Run with multiple clients:**
```bash
java CalculatorClient &
java CalculatorClient &
java CalculatorClient
```

## Technical Details

- **Language:** Java
- **Communication:** Java RMI
- **Concurrency:** Synchronised methods to prevent race conditions across concurrent clients
- **Operations:** LCM and GCD computed across all values currently on the stack
