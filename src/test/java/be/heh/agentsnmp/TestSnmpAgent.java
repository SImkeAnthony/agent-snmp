package be.heh.agentsnmp;

import be.heh.agentsnmp.agent.SnmpAgent;
import be.heh.agentsnmp.mo.MOCreator;
import be.heh.serversnmp.SnmpServer;
import org.junit.jupiter.api.Test;
import org.snmp4j.smi.OID;

import java.io.IOException;

public class TestSnmpAgent {
    static final OID sysDescr = new OID(".1.3.6.1.2.1.1.1.0");
    private String address = "udp:127.0.0.1/161";
    private SnmpAgent agent= new SnmpAgent("127.0.0.1/161");;
    private SnmpServer sever = new SnmpServer(this.address);;

    public TestSnmpAgent() throws IOException {
    }
    private void init() throws IOException{
        try{
            this.agent.start();
            this.agent.unregisterManagedObject(this.agent.getSnmpv2MIB());
            this.agent.registerManagedObject(MOCreator.createReadOnly(this.sysDescr,"this description is set by anthony"));
            this.sever.start();
            System.out.println(this.sever.getAsString(this.sysDescr));
        }catch (Exception e){
            System.out.println("Error runtime (init test): "+e.getMessage());
        }

    }
    @Test
    public void testSysDescr() throws IOException{
        init();
    }

}
