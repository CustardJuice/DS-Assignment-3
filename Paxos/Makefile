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
CLASSPATH = -cp ./bin:./lib/json-simple-1.1.1.jar:./lib/junit-platform-console-standalone-1.11.0.jar
DUMP = -d ./bin
FLAGS = $(CLASSPATH) $(DUMP)

# Define the class names
ACCEPTOR_CLASS = Acceptor
PROPOSER_CLASS = Proposer
LOGGER_CLASS = Logger

# Define the Java files
ACCEPTOR_JAVA = $(SRC_DIR)/$(ACCEPTOR_DIR)/$(ACCEPTOR_CLASS).java
PROPOSER_JAVA = $(SRC_DIR)/$(PROPOSER_DIR)/$(PROPOSER_CLASS).java
MESSAGE_JAVA = $(SRC_DIR)/$(MESSAGE_DIR)/*
LOGGER_JAVA = $(SRC_DIR)/$(LOGGER_DIR)/$(LOGGER_CLASS).java

# Args
BASE_PORT = 4564
BASE_ID = 4
BASE_FILE = $(SRC_DIR)/Tests/Logs/m
MILLIS_DELAY = 0

# Default target
all: compile

# Target to compile Java files
compile:
	javac $(FLAGS) $(ACCEPTOR_JAVA) $(PROPOSER_JAVA) $(MESSAGE_JAVA) $(LOGGER_JAVA)
	
# Run single acceptor server
acceptor:
	java $(CLASSPATH) $(ACCEPTOR_DIR).$(ACCEPTOR_CLASS) $(PORT) $(ID) $(FILE) $(MILLIS_DELAY)

# Run m1 as an acceptor server
m1:
	java $(CLASSPATH) $(ACCEPTOR_DIR).$(ACCEPTOR_CLASS) 4561 1 $(BASE_FILE)1.txt

# Run m2 as an acceptor server
m2:
	java $(CLASSPATH) $(ACCEPTOR_DIR).$(ACCEPTOR_CLASS) 4562 2 $(BASE_FILE)2.txt

# Run m3 as an acceptor server
m3:
	java $(CLASSPATH) $(ACCEPTOR_DIR).$(ACCEPTOR_CLASS) 4563 3 $(BASE_FILE)3.txt

# Run acceptors M4-M9
acceptors:
	@for i in 0 1 2 3 4 5; do \
		ID=$$(( $(BASE_ID) + $$i )); \
		PORT=$$(( $(BASE_PORT) + $$i )); \
		FILE=$(BASE_FILE)$$ID.txt; \
		gnome-terminal -- bash -c "make acceptor PORT=$$PORT ID=$$ID FILE=$$FILE; exec bash"; \
	done

