package Proposer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Communication.*;

class Proposer {
    long proposalId = -1;
    String value = null;
    List<Socket> acceptors = new LinkedList<Socket>(); // Sockets for all acceptor nodes
    int majority;
    int nPromised, nAccepted;
    PrintWriter out;
    BufferedReader in;
    final int nodeId;

    // Main, arguments are URIs of each Acceptor (servers)
    public static void main(String[] args) {
        Proposer proposer = new Proposer();
        proposer.run(args, "hello world");
    }

    // Constructors: requires globally unique nodeId
    public Proposer(int nodeId) {
        this.nodeId = nodeId;
    }

    // Default constructor gives nodeId = 0
    public Proposer() {
        this.nodeId = 0;
    }

    public long generateUniqueProposalId() {
        long timestamp = System.nanoTime();
        return (long) (timestamp) | (nodeId  & 0xFFFF);
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
    public void startConnection(String ip, int port) throws UnknownHostException, IOException {
        acceptors.add(new Socket(ip, port));
    }

    // Send message to a single acceptor socket. Returns response
    public Message sendMessage(Socket acceptor, Message message) throws IOException {
        // initialise communication streams
        out = new PrintWriter(acceptor.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(acceptor.getInputStream()));

        // send message
        out.println(message.messageToString());
        // get response
        String type = in.readLine();
        String id = in.readLine();
        String value = in.readLine();

        // handle response
        Message proposal = new Message(MessageType.valueOf(type), Integer.parseInt(id), value);

        // disconnect communication streams
        in.close();
        out.close();

        return proposal;
    }

    // Closes connection with specific acceptor
    public void stopConnection(Socket acceptor) throws IOException {
        acceptor.close();
        acceptors.remove(acceptor);
    }

    public void propose(String value) {
        this.value = value;
        // new proposal ID must be greater than all previous values.
        this.proposalId = System.nanoTime();
        boolean prepareDone = false, acceptDone = false;
        try {
            // Send Prepare messages to ALL acceptors until recv PREPARE_OK from majority
            while (!prepareDone) {
                for (Socket acceptor : acceptors) {
                    Message response = sendMessage(acceptor, new Message(MessageType.PREPARE, proposalId, value));
                    // handle responses, and stop sending messages once majority reached.
                    if (prepareDone = handlePromise(response))
                        break;
                }
            }
            // Send Accept messages to ALL acceptors until recv ACCEPT_OK from majority
            while (!acceptDone) {
                for (Socket acceptor : acceptors) {
                    Message response = sendMessage(acceptor, new Message(MessageType.ACCEPT, proposalId, value));
                    // handle responses, and stop sending messages once majority reached.
                    if (acceptDone = handleAccepted(response))
                        break;
                }
            }
            // Send Decide messages to ALL acceptors
            for (Socket acceptor : acceptors) {
                Message response = sendMessage(acceptor, new Message(MessageType.DECIDE, proposalId, value));
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
        }

        return ++nPromised >= majority;
    }

    private boolean handleAccepted(Message message) {
        if (message.type != MessageType.ACCEPT_OK)
            return false;

        return ++nAccepted >= majority;
    }
}
