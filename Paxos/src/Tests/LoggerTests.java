package Tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import Communication.Identifier;
import Communication.Message;
import Communication.MessageType;
import Logger.Logger;
import Proposer.Proposer;

public class LoggerTests {
    @Test
    public void LogReveiveTest() {
        String file = "src/Tests/Logs/log_test.txt";
        Logger logger = new Logger(file);
        long id = new Identifier().generateUniqueProposalId(0);
        Message m = new Message(MessageType.PREPARE, id, "val1");
        JSONParser parser = new JSONParser();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String last = "", line = "";

            logger.logReceive(m, "PROPOSER", "ACCEPTOR", 2);

            while ((line = reader.readLine()) != null) {
                last = line;
            }

            JSONObject json = (JSONObject) parser.parse(last);
            Assert.assertEquals("ACCEPTOR", ((JSONObject) json.get("receiver")).get("role"));
            Assert.assertEquals(2, (long) ((JSONObject) json.get("receiver")).get("id"));
            Assert.assertEquals("PROPOSER", ((JSONObject) json.get("sender")).get("role"));
            Assert.assertEquals(0, (long) ((JSONObject) json.get("sender")).get("id"));
            Assert.assertEquals("PREPARE", (String) json.get("messageType"));
            Assert.assertEquals("val1", (String) json.get("consensusValue"));
            Assert.assertEquals(id, (long) json.get("proposalId"));

        } catch (IOException e) {
            System.err.println("Logger file not found.");
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("Could not parse JSON data in " + file);
            e.printStackTrace();
        }
    }
}
