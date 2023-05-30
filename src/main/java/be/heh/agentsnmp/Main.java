package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

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
        int listenPort = 162;
        List<String> contexts = new ArrayList<>();
        contexts.add("public");
        contexts.add("Silver-King-Rogue-16");

        SnmpAgent agent = new SnmpAgent(engineBootsCounterFileName,configFileName,configMOFileName,listenAddresses,listenPort,contexts);
        if(listenAddresses.size() != 0){

            for(MOScalar scalar : getScalarList()){
                agent.registerMIB(scalar);
            }
            agent.run(listenAddresses.get(0),listenPort);
        }
    }

    private static List<MOScalar> getScalarList(){
        List<MOScalar> moScalars = new ArrayList<>();
        MOScalar scalarMacAddress = new MOScalar(
                new OID("1.3.2.3.6.2.1.1.1"),
                MOAccessImpl.ACCESS_READ_ONLY,
                new OctetString("AD-6D-56-E2-FF-23")
        );
        MOScalar scalarIpAddress = new MOScalar(
                new OID("1.3.2.3.6.2.1.2.1"),
                MOAccessImpl.ACCESS_READ_ONLY,
                new OctetString("192.168.0.10")
        );
        MOScalar scalarHostname = new MOScalar(
                new OID("1.3.2.3.6.2.1.3.1"),
                MOAccessImpl.ACCESS_READ_ONLY,
                new OctetString("myLocalComputer")
        );
        MOScalar scalarOs = new MOScalar(
                new OID("1.3.2.3.6.2.1.4.1"),
                MOAccessImpl.ACCESS_READ_ONLY,
                new OctetString("windows 10 education 22H20")
        );
        MOScalar scalarSnmp = new MOScalar(
                new OID("1.3.2.3.6.2.1.5.1"),
                MOAccessImpl.ACCESS_READ_ONLY,
                new OctetString("true")
        );

        moScalars.add(scalarMacAddress);
        moScalars.add(scalarIpAddress);
        moScalars.add(scalarHostname);
        moScalars.add(scalarOs);
        moScalars.add(scalarSnmp);

        return moScalars;

    }
}