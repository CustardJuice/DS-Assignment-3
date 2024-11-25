package Tests;

import org.junit.Assert;
import org.junit.Test;

import Communication.Identifier;

public class IdentifierTests {
    @Test
    public void proposalIdIsUnique() {
        int id1 = 1, id2 = 2;
        Identifier p1 = new Identifier();
        Identifier p2 = new Identifier();

        long a = p1.generateUniqueProposalId(id1), b, c;
        for (int i = 0; i < 10; i++) {
            // assert ids are monotonically increasing for p1
            System.out.println(b = p1.generateUniqueProposalId(id1));
            Assert.assertTrue(a < b);
            a = b;

            // assert ids are unique for p1 and p2
            System.out.println(c = p2.generateUniqueProposalId(id2));
            Assert.assertNotEquals(a, c);
        }
    }

    @Test
    public void proposalIdStoresNodeId() {
        int nodeId = 123;
        Identifier id = new Identifier();

        long proposalId = id.generateUniqueProposalId(nodeId);
        int decodedId = id.decodeNodeId(proposalId);

        Assert.assertEquals(nodeId, decodedId);
    }
}
