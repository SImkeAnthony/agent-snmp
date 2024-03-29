package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;
import be.heh.agentsnmp.tools.NetworkTool;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        String engineBootsCounterFileName = "engineBootsCounterFile.txt";
        String configFileName = "configurator.properties";
        String configMOFileName = "configMOI.cfg";

        List<String> listenAddresses = new ArrayList<>(NetworkTool.getPrivateIPV4Addresses());

        int listenPort = 161;
        List<String> contexts = new ArrayList<>();
        contexts.add("public");
        contexts.add("Silver-King-Rogue-16");
        SnmpAgent agent = new SnmpAgent(engineBootsCounterFileName,configFileName,configMOFileName,listenAddresses,listenPort,contexts);
        if(listenAddresses.size() != 0){
            agent.run(listenAddresses.get(0),listenPort);
        }
    }
}