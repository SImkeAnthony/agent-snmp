package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.mo.snmp.NotificationLogMib;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OctetString;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CommandProcessor commandProcessor = new CommandProcessor(new OctetString(MPv3.createLocalEngineID()));
        SnmpAgent agent = new SnmpAgent(commandProcessor);
        agent.start("127.0.0.1",162);
    }
}