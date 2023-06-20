package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        String engineBootsCounterFileName = "engineBootsCounterFile.txt";
        String configFileName = "configurator.properties";
        String configMOFileName = "configMOI.cfg";
        List<String> listenAddresses = new ArrayList<>();
        listenAddresses.add("127.0.0.1");
        listenAddresses.add("192.168.1.3");
        listenAddresses.add("192.168.0.9");
        int listenPort = 162;
        List<String> contexts = new ArrayList<>();
        contexts.add("public");
        contexts.add("Silver-King-Rogue-16");
        SnmpAgent agent = new SnmpAgent(engineBootsCounterFileName,configFileName,configMOFileName,listenAddresses,listenPort,contexts);
        if(listenAddresses.size() != 0){
            agent.run(listenAddresses.get(0),listenPort);
        }
    }
}