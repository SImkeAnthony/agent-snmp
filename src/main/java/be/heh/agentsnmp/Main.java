package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SnmpAgent agent = new SnmpAgent("127.0.0.1",162,"moPersistenceFile.cfg","bootCounterFile.cfg");
        agent.start();
    }
}