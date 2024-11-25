// package Proposer;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.Socket;
// import java.net.URI;
// import java.net.URISyntaxException;
// import java.net.UnknownHostException;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.concurrent.atomic.AtomicLong;

// import Communication.*;
// import Logger.Logger;

// public class Proposer {
//     private class AcceptorConnection {
//         BufferedReader in;
//         PrintWriter out;
//         Socket socket;

//         public AcceptorConnection(Socket socket, BufferedReader in, PrintWriter out) {
//             this.socket = socket;
//             this.in = in;
//             this.out = out;
//         }
//     }

//     AtomicLong proposalId = new AtomicLong(-1);
//     String value = null;
//     List<AcceptorConnection> acceptors = new LinkedList<>();
//     int majority;
//     AtomicInteger nPromised = new AtomicInteger(0);
//     AtomicInteger nAccepted = new AtomicInteger(0);
//     final int nodeId;
//     final Logger logger;
//     final Identifier identifier = new Identifier();
//     final String myRole = "PROPOSER";
//     final String otherRole = "ACCEPTOR";

//     // Constructors
//     public Proposer(int nodeId, String loggerPath) {
//         this.nodeId = nodeId;
//         logger = new Logger(loggerPath);
//     }

//     public Proposer() {
//         this.nodeId = 0;
//         logger = new Logger("log.txt");
//     }

//     public void run(String[] uris, String val) {
//         try {
//             for (String s : uris) {
//                 URI uri = new URI(s);
//                 startConnection(uri.getHost(), uri.getPort());
//             }
//             majority = (acceptors.size() / 2) + 1;
//             propose(val);
//         } catch (URISyntaxException | IOException e) {
//             System.err.println("Error running Proposer.");
//         }
//     }

//     public void startConnection(String ip, int port) throws UnknownHostException, IOException {
//         Socket socket = new Socket(ip, port);
//         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//         acceptors.add(new AcceptorConnection(socket, in, out));
//     }

//     public void propose(String value) {
//         this.value = value;
//         this.proposalId.set(identifier.generateUniqueProposalId(nodeId));
//         System.out.println("Proposing value: " + value + " with proposalId: " + proposalId);

//         // Log the new proposal
//         logger.logMessage(myRole, nodeId, otherRole, -1, MessageType.PERSIST, value, proposalId.get());

//         // Start Prepare phase
//         CompletableFuture<Void> preparePhase = CompletableFuture.allOf(
//                 acceptors.stream().map(acceptor -> sendPrepare(acceptor)).toArray(CompletableFuture[]::new)
//         );

//         preparePhase.thenRun(() -> {
//             if (nPromised.get() >= majority) {
//                 System.out.println("Received majority PREPARE_OK. Moving to Accept phase.");
//                 startAcceptPhase();
//             } else {
//                 System.err.println("Failed to receive majority PREPARE_OK.");
//             }
//         });
//     }

//     private CompletableFuture<Void> sendPrepare(AcceptorConnection acceptor) {
//         return CompletableFuture.runAsync(() -> {
//                 Message prepareMessage = new Message(MessageType.PREPARE, proposalId.get(), value);
//                 logger.logSend(prepareMessage, myRole, otherRole, nodeId, acceptors.indexOf(acceptor));
//                 sendMessage(acceptor, prepareMessage).thenAccept(response -> {
//                     if (handlePromise(response)) {
//                         System.out.println("Promise received from acceptor.");
//                     }
//                 });
//         });
//     }

//     private void startAcceptPhase() {
//         CompletableFuture<Void> acceptPhase = CompletableFuture.allOf(
//                 acceptors.stream().map(acceptor -> sendAccept(acceptor)).toArray(CompletableFuture[]::new)
//         );

//         acceptPhase.thenRun(() -> {
//             if (nAccepted.get() >= majority) {
//                 System.out.println("Received majority ACCEPT_OK. Moving to Decide phase.");
//                 startDecidePhase();
//             } else {
//                 System.err.println("Failed to receive majority ACCEPT_OK.");
//             }
//         });
//     }

//     private CompletableFuture<Void> sendAccept(AcceptorConnection acceptor) {
//         return CompletableFuture.runAsync(() -> {
//                 Message acceptMessage = new Message(MessageType.ACCEPT, proposalId.get(), value);
//                 logger.logSend(acceptMessage, myRole, otherRole, nodeId, acceptors.indexOf(acceptor));
//                 sendMessage(acceptor, acceptMessage).thenAccept(response -> {
//                     if (handleAccepted(response)) {
//                         System.out.println("Accept received from acceptor.");
//                     }
//                 });
 
//         });
//     }

//     private void startDecidePhase() {
//         acceptors.forEach(acceptor -> {
//             try {
//                 Message decideMessage = new Message(MessageType.DECIDE, proposalId.get(), value);
//                 logger.logSend(decideMessage, myRole, otherRole, nodeId, acceptors.indexOf(acceptor));
//                 sendMessageNoWait(acceptor, decideMessage);
//             } catch (IOException e) {
//                 System.err.println("Failed to send DECIDE message to acceptor: " + e.getMessage());
//             }
//         });
//     }

//     public CompletableFuture<Message> sendMessage(AcceptorConnection acceptor, Message message) {
//         return CompletableFuture.supplyAsync(() -> {
//             try {
//                 acceptor.out.println(message.messageToString());
//                 String type = acceptor.in.readLine();
//                 String id = acceptor.in.readLine();
//                 String value = acceptor.in.readLine();
//                 return new Message(MessageType.valueOf(type), Long.parseLong(id), value);
//             } catch (IOException e) {
//                 throw new RuntimeException("Failed to communicate with acceptor: " + e.getMessage());
//             }
//         });
//     }

//     public void sendMessageNoWait(AcceptorConnection acceptor, Message message) throws IOException {
//         acceptor.out.println(message.messageToString());
//     }

//     private boolean handlePromise(Message message) {
//         if (message.type != MessageType.PREPARE_OK) return false;

//         if (message.value != null && message.proposalId > proposalId.get()) {
//             value = message.value;
//             proposalId.set(message.proposalId);
//         }

//         return nPromised.incrementAndGet() >= majority;
//     }

//     private boolean handleAccepted(Message message) {
//         return message.type == MessageType.ACCEPT_OK && nAccepted.incrementAndGet() >= majority;
//     }
// }
