package Tests;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import Proposer.Proposer;

public class PaxosTests {
    private String host = "http://127.0.0.1:";
    String folder = "src/Tests/Logs/";

    @Test
    public void singleProposer() {
        Proposer m1 = new Proposer(1, folder + "m1.txt");
        String[] acceptorUris = { host + "4562", host + "4563" };
        m1.run(acceptorUris, "M1");
    }

    // NOTE: ENSURE M3 IS RAN AS ACCEPTOR USING "make m3"
    @Test
    public void twoProposers() {
        // Create M1 and M2 (Proposers)
        Proposer m1 = new Proposer(1, folder + "m1.txt");
        Proposer m2 = new Proposer(2, folder + "m2.txt");
        String val1 = "M1";
        String val2 = "M2";
        String[] acceptorUris = {
                host + "4563",
                host + "4564",
                host + "4565",
                host + "4566",
                host + "4567",
                host + "4568",
                host + "4569"
        };

        // start proposers (clients)
        m1.run(acceptorUris, val1);
        m2.run(acceptorUris, val2);
    }

    @Test
    public void reset() {
        for (int i = 1; i <= 9; i++) {
            try {
                PrintWriter writer = new PrintWriter(folder + "m" + i + ".txt");
                writer.write("");
                writer.close();

            } catch (FileNotFoundException e) {
                System.err.println("Could not find file for M" + i);
                e.printStackTrace();
            }
        }
    }
}
