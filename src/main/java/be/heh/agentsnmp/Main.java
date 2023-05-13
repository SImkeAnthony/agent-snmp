package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SampleSnmpAgent;
import be.heh.agentsnmp.agent.SnmpAgent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        SnmpAgent agent = new SnmpAgent("127.0.0.1",162,"moPersistenceFile.cfg","bootCounterFile.cfg","configFile.cfg",new OctetString("public"));
        agent.start();

        /*

        ClassLoader classLoader = Main.class.getClassLoader();
        String configFile = "AgentConfig.properties";
        File bootCounterFile = new File("bootCounterFile.cfg");
        String dhKickStartInfoPath = "dhKickStartInfo.cfg";
        String tableSizeLimitsFile = "AgentTableSizeLimits.properties";
        List<Object> address = new ArrayList<Object>();
        address.add("udp:127.0.0.1/162");
        OctetString context = new OctetString("public");
        try {
            //set Variables
            SampleSnmpAgent agent = new SampleSnmpAgent(
                    configFile,
                    bootCounterFile,
                    dhKickStartInfoPath,
                    tableSizeLimitsFile,
                    address,
                    context
            );
            SecurityProtocols.getInstance().addDefaultProtocols();
            //set system description
            agent.getAgent().getSysDescr().setValue("System description of agent".getBytes());
            //set system OID
            agent.getAgent().getSysOID().setValue("1.3.1.6.1.4.1....");
            //set system services
            agent.getAgent().getSysServices().setValue(72);
            agent.run();
            for(int i=0;i<5;i++){
                agent.getAgent().getNotificationOriginator().notify(
                        new OctetString(),
                        SnmpConstants.coldStart,
                        new VariableBinding[]{
                                new VariableBinding(new OID("1.3.4.6.5.3"),new OctetString("hello test")),
                                new VariableBinding(new OID("1.3.4.6.5.3"),new Integer32(15)),
                                new VariableBinding(new OID("1.3.4.6.5.3"),new Gauge32(25856L)),
                                new VariableBinding(new OID("1.3.4.6.5.3"),new OctetString("127.0.0.1")),
                                new VariableBinding(new OID("1.3.4.6.5.3"),new Counter32(56955)),
                        }
                );
            }
        }catch (RuntimeException e){
            System.out.println("Runtime Error (main): "+e.getMessage());
        }

         */
    }
}