# Key Files
## Proposer.java:
Implements the Proposer role in Paxos.
Handles the preparation, acceptance, and decision phases
Tracks promises and acceptances using atomic counters.
Logs events for persistence and debugging.
## Acceptor.java:
Implements the Acceptor role in Paxos.
Maintains state variables such as the highest promised proposal ID and the last accepted value.
Responds to messages based on Paxos protocol rules.
Handles incoming connections continuously while the Proposer remains connected.
## Message.java:
Encapsulates the structure of messages exchanged between nodes.
Supports types such as PREPARE, ACCEPT, and DECIDE.
## Logger.java:
Logs events for persistence and debugging.
Helps ensure Paxos state can be reconstructed in case of failure.

## How to Build and Run
## Prerequisites
Java Development Kit (JDK) version 8 or higher.
A terminal or shell environment to execute the application.
make utility to build and run the project.
Building the Project
## Compile the code:
make compile

## Running the Project
### Start Acceptor M4-M9 nodes in separate terminals:
make acceptors
This command launches 5 Acceptor processes in separate terminals, each listening on a different port. Ports are given by 4560 + nodeId (e.g. port 4564 for M4, and 4569 for M9).
### Start single Acceptor
make acceptor  PORT=port ID=id FILE=file.txt MILLIS_DELAY=delay
e.g. make acceptor PORT=4567 ID=7 FILE=m7.txt MILLIS_DELAY=100
### Start M1, M2, M3 as Acceptors
If a test requires it, M1, M2, and M3 can each individually be ran as Acceptors instead of Proposers. This can be done with the commands.
make m1
make m2
make m3

## Automated Testing
Functions to run proposers and test code are found in the Tests directory.
### Paxos Protocol
Once Acceptors are running, and the correct ports are passed to the Proposers, then the protocol can be tested.
### Reset
The reset() test can be called to clear the logs of m1...m9
### Logger
Test for the Logger can be found in LoggerTests.java
### Identifier
Unit test for the Identifier class can be found in IdentifierTest.java. This checks that the Identifer generates unique, monotonic ids.