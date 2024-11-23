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
class Acceptor {
    private int promisedId = -1;
    private String acceptedValue = null;
    private ServerSocket serverSocket;
    private Logger logger = new Logger("log.txt");

    public static void main(String[] args) {
        Acceptor acceptor = new Acceptor();
        try {
            acceptor.start(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            try {
                // initialise connection streams
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Read message from Proposer
                Message proposal = new Message();
                String type = in.readLine();
                String id = in.readLine();
                String value = in.readLine();
                try {
                    proposal = new Message(MessageType.valueOf(type), Integer.parseInt(id), value);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid MessageType name: " + type);
                }

                Message response = null;
                switch (proposal.type) {
                    case PREPARE:
                        response = handlePrepare(proposal);
                        break;
                    case ACCEPT:
                        response = handleAccept(proposal);
                        break;
                    case DECIDE:
                        boolean res = handleDecide(proposal);
                        break;
                    default:
                        // ignore other message types
                        break;
                }

                // Send message
                if (response != null) {
                    sendMessage(response);
                }

                // Close connection
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {

            } finally {

            }
        }

        public void sendMessage(Message message) {
            out.println(message.messageToString());
        }
    }

    public synchronized Message handlePrepare(Message message) {
        if (message.proposalId > promisedId) {
            promisedId = message.proposalId;
            // if acceptor has accepted another proposal in the past, send back that
            if (acceptedValue != null) {
                Message response = new Message(MessageType.PREPARE_OK, promisedId, acceptedValue);
                promisedId = message.proposalId;
                acceptedValue = message.value;
                return response;
            }

            // else if this is the first proposal this acceptor has seen, send back this new
            promisedId = message.proposalId;
            acceptedValue = message.value;
            return new Message(MessageType.PREPARE_OK, promisedId, acceptedValue);
        }
        return null;
    }

    public synchronized Message handleAccept(Message message) {
        if (message.proposalId < promisedId) {
            return new Message(MessageType.ACCEPT_REJECT, message.proposalId, message.value);
        }
        acceptedValue = message.value;
        return new Message(MessageType.ACCEPT_OK, message.proposalId, acceptedValue);
    }

    public synchronized boolean handleDecide(Message message) {
        return true;
    }
}
