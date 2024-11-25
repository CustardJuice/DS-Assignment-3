package Proposer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import Communication.*;
import Logger.Logger;

public class Proposer {
    private class AcceptorConnection {
        BufferedReader in;
        PrintWriter out;
        Socket socket;

        public AcceptorConnection(Socket socket, BufferedReader in, PrintWriter out) {
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }

    public long proposalId = -1;
    String value = null;
    List<AcceptorConnection> acceptors = new LinkedList<AcceptorConnection>();
    int majority;
    int nPromised, nAccepted;
    final int nodeId;
    final Logger logger;
    final Identifier identifier = new Identifier();
    final String myRole = "PROPOSER";
    final String otherRole = "ACCEPTOR";

    // Constructors: requires globally unique nodeId and filepath to logger
    public Proposer(int nodeId, String loggerPath) {
        this.nodeId = nodeId;
        logger = new Logger(loggerPath);
    }

    // Default constructor gives nodeId = 0
    public Proposer() {
        this.nodeId = 0;
        logger = new Logger("log.txt");
    }

    public void run(String[] uris, String val) {
        try {
            for (String s : uris) {
                URI uri = new URI(s);
                startConnection(uri.getHost(), uri.getPort());
            }
            majority = (acceptors.size() / 2) + 1;
            propose(val);

        } catch (URISyntaxException | IOException e) {
            System.err.println("Error running Proposer.");
        }
    }

    // Add new Socket connection to acceptors list
    @SuppressWarnings("resource")
    public void startConnection(String ip, int port) throws UnknownHostException, IOException {
        Socket socket = new Socket(ip, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // add to list of connections
        acceptors.add(new AcceptorConnection(socket, in, out));
    }

    // Send message to a single acceptor socket. Returns response
    public Message sendMessage(AcceptorConnection acceptor, Message message) throws IOException {
        // send message
        System.out.println("Sending message: " + "Type: " + message.type.toString() + " ID: " + message.proposalId
                + " Value: " + message.value + " to acceptor with port: " + acceptor.socket.getLocalPort());
        acceptor.out.println(message.messageToString());
        // get response
        String type = acceptor.in.readLine();
        String id = acceptor.in.readLine();
        String value = acceptor.in.readLine();

        // handle response
        Message proposal = new Message(MessageType.valueOf(type), Long.parseLong(id), value);

        return proposal;
    }

    // Send message to a single acceptor socket. DOES NOT WAIT for response
    public void sendMessageNoWait(AcceptorConnection acceptor, Message message) throws IOException {
        System.out.println("Sending message: " + "Type: " + message.type.toString() + " ID: " + message.proposalId
                + " Value: " + message.value + " to acceptor with port: " + acceptor.socket.getLocalPort());
        acceptor.out.println(message.messageToString());
    }

    public void propose(String value) {
        this.value = value;
        // new proposal ID must be greater than all previous values.
        this.proposalId = identifier.generateUniqueProposalId(nodeId);
        boolean prepareDone = false, acceptDone = false;
        try {
            // log new proposalId and value
            logger.logMessage(myRole, nodeId, otherRole, -1, MessageType.PERSIST, value, proposalId);
            System.out.println("Persisting. proposalId: " + proposalId + " value: " + value);
            // Send Prepare messages to ALL acceptors until recv PREPARE_OK from majority
            while (!prepareDone) {
                for (AcceptorConnection acceptor : acceptors) {
                    Message m = new Message(MessageType.PREPARE, proposalId, value);
                    logger.logSend(m, myRole, otherRole, nodeId, acceptors.indexOf(acceptor));
                    Message response = sendMessage(acceptor, m);
                    // handle responses, and stop sending messages once majority reached.
                    if (prepareDone = handlePromise(response)) {
                        System.out.println("Received majority PREPARE_OK. Moving onto Accept phase");
                        break;
                    }
                }
            }
            // Send Accept messages to ALL acceptors until recv ACCEPT_OK from majority
            while (!acceptDone) {
                for (AcceptorConnection acceptor : acceptors) {
                    Message m = new Message(MessageType.ACCEPT, proposalId, value);
                    logger.logSend(m, myRole, otherRole, nodeId, acceptors.indexOf(acceptor));
                    Message response = sendMessage(acceptor, m);
                    // handle responses, and stop sending messages once majority reached.
                    if (acceptDone = handleAccepted(response)) {
                        System.out.println("Received majority ACCEPT_OK. Moving onto Decide phase");
                        break;
                    }
                }
            }

            // Log Decision
            logger.logMessage(myRole, nodeId, otherRole, -1, MessageType.DECIDE, value, proposalId);
            // Send Decide messages to ALL acceptors
            for (AcceptorConnection acceptor : acceptors) {
                sendMessageNoWait(acceptor, new Message(MessageType.DECIDE, proposalId, value));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Collect promises and decide on value to propose. Returns true if majority has
    // been reached.
    private boolean handlePromise(Message message) {
        if (message.type != MessageType.PREPARE_OK)
            return false;

        // if an acceptor sent a different value with a greater proposal id
        if (message.value != value && message.proposalId > proposalId) {
            value = message.value;
            proposalId = message.proposalId;
            System.out.println(
                    "Acceptor sent higher proposalId: " + proposalId + ". Updating proposalId and value: " + value);
        }

        return ++nPromised >= majority;
    }

    private boolean handleAccepted(Message message) {
        if (message.type != MessageType.ACCEPT_OK)
            return false;

        return ++nAccepted >= majority;
    }
}
