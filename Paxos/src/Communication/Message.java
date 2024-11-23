package Communication;

import java.io.Serializable;

public class Message implements Serializable {
    public MessageType type;
    public long proposalId = -1;
    public String value; // Proposed value, if any

    // CONSTRUCTORS
    public Message(MessageType type, long proposalId, String value) {
        this.type = type;
        this.proposalId = proposalId;
        this.value = value;
    }

    public Message(MessageType type, long proposalId) {
        this.type = type;
        this.proposalId = proposalId;
    }

    public Message() {
        //TODO Auto-generated constructor stub
    }

    // METHODS
    // Converts message to string to be transmitted via sockets
    // Format:
    // TYPE
    // ID
    // VALUE or NULL
    public String messageToString() {
        if (type == null) {
            throw new NullPointerException("Message type is null.");
        }
        if (proposalId < 0) {
            throw new NullPointerException("Message proposalId is null.");
        }
        StringBuilder builder = new StringBuilder();
        // type
        builder.append(type.toString());
        builder.append("\r\n");
        // proposalId
        builder.append(proposalId);
        builder.append("\r\n");
        // value OR null
        builder.append(value != null ? value : "NULL");
        return builder.toString();
    }
}
