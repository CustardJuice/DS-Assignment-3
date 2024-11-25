package Communication;

// Class to control generating and decoding proposal Ids
public class Identifier {
    public synchronized long generateUniqueProposalId(int nodeId) {
        long timestamp = System.nanoTime();
        return (long) (timestamp << 16) | (nodeId & 0xFFFF);
    }

    public int decodeNodeId(long proposalId) {
        return (int) (proposalId & 0xFFFF);
    }

    public long decodeTimestamp(long proposalId) {
        return proposalId >> 16;
    }
}
