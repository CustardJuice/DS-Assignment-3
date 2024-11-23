# Defome the directories
PROPOSER_DIR = Proposer
ACCEPTOR_DIR = Acceptor
MESSAGE_DIR = Communication
LOGGER_DIR = Logger
TEST_DIR = Tests
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

# Dependencies, etc
CLASSPATH = -cp ./bin:./lib/json-simple-1.1.1.jar
DUMP = -d ./bin
FLAGS = $(CLASSPATH) $(DUMP)

# Define the class names
ACCEPTOR_CLASS = Acceptor
PROPOSER_CLASS = Proposer
MESSAGE_CLASS = Message
LOGGER_CLASS = Logger

# Define the Java files
ACCEPTOR_JAVA = $(SRC_DIR)/$(ACCEPTOR_DIR)/$(ACCEPTOR_CLASS).java
PROPOSER_JAVA = $(SRC_DIR)/$(PROPOSER_DIR)/$(PROPOSER_CLASS).java
MESSAGE_JAVA = $(SRC_DIR)/$(MESSAGE_DIR)/*
LOGGER_JAVA = $(SRC_DIR)/$(LOGGER_DIR)/$(LOGGER_CLASS).java

# Args
PORT = 4567
URI = http://127.0.0.1:$(PORT)

# Default target
all: compile

# Target to compile Java files
compile:
	javac $(FLAGS) $(ACCEPTOR_JAVA) $(PROPOSER_JAVA) $(MESSAGE_JAVA) $(LOGGER_JAVA)
	
# Run acceptor server
acceptor:
	java $(CLASSPATH) $(ACCEPTOR_DIR).$(ACCEPTOR_CLASS) $(PORT)

# Run proposer client
proposer:
	java $(CLASSPATH) $(PROPOSER_DIR).$(PROPOSER_CLASS) $(URI)

# Clean target to remove compiled .class files
clean:
	rm -f $(BIN_DIR)/*.class
