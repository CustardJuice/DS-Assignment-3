package Acceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import Communication.*;
import Logger.Logger;

// Acts as a server, accepting connections from Proposer clients
public class Acceptor {
    private long promisedId = -1;
    private String promisedValue = null;
    private String acceptedValue = null;
    private ServerSocket serverSocket;
    private final Logger logger;
    private final int nodeId;
    final String myRole = "ACCEPTOR";
    final String otherRole = "PROPOSER";
    final Identifier identifier = new Identifier();
    final int millisDelay; // delay before sending messages

    public Acceptor() {
        nodeId = 100;
        logger = new Logger("log.txt");
        millisDelay = 0;
    }

    public Acceptor(int nodeId, String loggerPath, int millisDelay) {
        this.nodeId = nodeId;
        logger = new Logger(loggerPath);
        this.millisDelay = millisDelay;
    }

    // args[0] : server port REQUIRED
    // args[1] : unique node id REQUIRED
    // args[2] : logger file path REQUIRED
    // args[3] : message delay in milliseconds OPTIONAL
    public static void main(String[] args) {
        try {
            Acceptor acceptor;
            if (args.length > 3) {
                acceptor = new Acceptor(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
            } else {
                acceptor = new Acceptor(Integer.parseInt(args[1]), args[2], 0);
            }
            acceptor.start(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            System.err.println("Could not parse port " + args[0]);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not start server (acceptor) with id " + args[1]);
            e.printStackTrace();
        }
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new ProposalHandler(serverSocket.accept()).start();
    }

    private class ProposalHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ProposalHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            System.out.println("New connection with proposer made through port " + clientSocket.getLocalPort());
            try {
                // Initialize connection streams
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
                // Continuously read messages while the connection is open
                String line;
                while ((line = in.readLine()) != null) {
                    // Parse message from Proposer
                    Message proposal = new Message();
                    try {
                        String type = line;
                        String id = in.readLine();
                        String value = in.readLine();
                        proposal = new Message(MessageType.valueOf(type), Long.parseLong(id), value);
                        System.out.println("Received message: " + "Type: " + type + " ID: " + id + " Value: " + value);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse id: " + line);
                        continue; // Skip to the next message
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid MessageType name: " + line);
                        continue; // Skip to the next message
                    }
        
                    // Handle the message
                    Message response = null;
                    boolean res = false;
                    switch (proposal.type) {
                        case PREPARE:
                            response = handlePrepare(proposal);
                            break;
                        case ACCEPT:
                            response = handleAccept(proposal);
                            break;
                        case DECIDE:
                            res = handleDecide(proposal);
                            break;
                        default:
                            System.err.println("Unknown message type: " + proposal.type);
                            break;
                    }
        
                    // Send response if applicable
                    if (response != null) {
                        // Log the response
                        logger.logSend(response, myRole, otherRole, nodeId, identifier.decodeNodeId(promisedId));
                        sendMessage(response);
                    }
        
                    // If a decision was made, log it
                    if (res) {
                        logger.logMessage(myRole, nodeId, otherRole, -1, MessageType.PERSIST, acceptedValue, promisedId);
                    }
                }
            } catch (IOException e) {
                System.err.println("Acceptor threw IOException: " + e.getMessage());
            } finally {
                // Clean up resources
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
            System.out.println("Connection with proposer closed.");
        }
        

        public void sendMessage(Message message) {
            try {
                Thread.sleep(millisDelay);
            } catch (InterruptedException e) {
                System.err.println("Thread sleep interrupted");
                e.printStackTrace();
            }
            System.out.println("Sending message: " + "Type: " + message.type.toString() + " ID: " + message.proposalId + " Value: " + message.value);
            out.println(message.messageToString());
        }
    }

    public synchronized Message handlePrepare(Message message) {
        if (message.proposalId > promisedId) {
            System.out.println("handlePrepare : Received greater proposalId: " + message.proposalId + ". Updating promisedId");
            promisedId = message.proposalId;
            // if acceptor has accepted another proposal in the past, send back that
            if (promisedValue != null) {
                Message response = new Message(MessageType.PREPARE_OK, promisedId, promisedValue);
                promisedId = message.proposalId;
                promisedValue = message.value;
                return response;
            }

            // else if this is the first proposal this acceptor has seen, send back this new
            promisedId = message.proposalId;
            promisedValue = message.value;
            return new Message(MessageType.PREPARE_OK, promisedId, promisedValue);
        }
        return new Message(MessageType.ACCEPT_REJECT, message.proposalId, message.value);
    }

    public synchronized Message handleAccept(Message message) {
        if (message.proposalId < promisedId) {
            System.out.println("handleAccept : Received lower proposalId: " + message.proposalId);
            return new Message(MessageType.ACCEPT_REJECT, message.proposalId, message.value);
        }
        System.out.println("handleAccept : Received greater or equal proposalId: " + message.proposalId + ". Updating promisedId and acceptedValue: " + message.value);
        acceptedValue = message.value;
        promisedId = message.proposalId;
        return new Message(MessageType.ACCEPT_OK, promisedId, acceptedValue);
    }

    public synchronized boolean handleDecide(Message message) {
        if (message.proposalId < promisedId) {
            System.out.println("handleDecide : Received lower proposalId: " + message.proposalId + ". Will ignore");
            return false;
        }
        return true;
    }
}
