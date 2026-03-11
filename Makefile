# Makefile for Java RMI Calculator Assignment

# Java compiler and runtime
JC = javac
JAVA = java
JFLAGS = -cp .

# Source files
SOURCES = Calculator.java CalculatorImplementation.java CalculatorServer.java CalculatorClient.java MultiClientDemo.java

# Class files (targets)
CLASSES = $(SOURCES:.java=.class)

# Default target - compile all classes
all: $(CLASSES)

# Rule to compile .java files to .class files
%.class: %.java
	$(JC) $(JFLAGS) $<

# Compile everything (explicit target)
compile: $(CLASSES)
	@echo "Compilation successful"

# Clean compiled files
clean:
	rm -f *.class

# Test compilation
test: compile
	@echo "All files compiled successfully"

# Run server (for local testing)
server: compile
	$(JAVA) CalculatorServer

# Run client (for local testing)
client: compile
	$(JAVA) CalculatorClient

# Run multi-client demo
demo: compile
	$(JAVA) MultiClientDemo

# Help target
help:
	@echo "Available targets:"
	@echo "  all      - Compile all Java files (default)"
	@echo "  compile  - Compile all Java files"
	@echo "  clean    - Remove all .class files"
	@echo "  test     - Test compilation"
	@echo "  server   - Run server (local testing)"
	@echo "  client   - Run client (local testing)"
	@echo "  demo     - Run multi-client demo"

# Declare phony targets
.PHONY: all compile clean test server client demo help