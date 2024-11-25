package Logger;

import org.json.simple.JSONObject;

import Communication.Identifier;
import Communication.Message;
import Communication.MessageType;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private final String logFilePath;
    private final SimpleDateFormat dateFormatter;

    // Constructor
    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    // Helper functions
    public synchronized void logReceive(Message message, String senderRole, String receiverRole, int receiverId) {
        int senderId = new Identifier().decodeNodeId(message.proposalId);
        logMessage(senderRole, senderId, receiverRole, receiverId, message.type, message.value, message.proposalId);
    }

    public synchronized void logSend(Message message, String senderRole, String receiverRole, int senderId, int receiverId) {
        logMessage(senderRole, senderId, receiverRole, receiverId, message.type, message.value, message.proposalId);
    }

    // Method to log a Paxos message
    @SuppressWarnings("unchecked") // Suppress warnings for raw JSON.simple API
    public void logMessage(String senderRole, int senderId, String receiverRole, int receiverId,
            MessageType messageType, String consensusValue, long proposalId) {
        // Create a JSON object for the log entry
        JSONObject logEntry = new JSONObject();
        logEntry.put("timestamp", dateFormatter.format(new Date()));

        // Create JSON objects for sender and receiver
        JSONObject sender = new JSONObject();
        sender.put("role", senderRole);
        sender.put("id", senderId);

        JSONObject receiver = new JSONObject();
        receiver.put("role", receiverRole);
        receiver.put("id", receiverId);

        // Add sender, receiver, and message details to the log entry
        logEntry.put("sender", sender);
        logEntry.put("receiver", receiver);
        logEntry.put("messageType", messageType.toString());
        logEntry.put("consensusValue", consensusValue != null ? consensusValue : "null");
        logEntry.put("proposalId", proposalId);

        // Write the log entry to the file
        writeToFile(logEntry.toJSONString());
    }

    // Method to write the structured log message to the file
    private synchronized void writeToFile(String logEntry) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.append(logEntry + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
