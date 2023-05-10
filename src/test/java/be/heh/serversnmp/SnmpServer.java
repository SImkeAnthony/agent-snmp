package be.heh.serversnmp;

import org.ietf.jgss.Oid;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SnmpServer {
    Snmp snmp = null;
    String address = null;

    public SnmpServer(String addr){
        address = addr;
    }

    public static void main(String[] args) throws IOException{
        SnmpServer server = new SnmpServer("udp:127.0.0.1/161");
        server.start();
        String sysDescr = server.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
        System.out.println(sysDescr);
    }

    public void start() throws IOException{
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    public String getAsString(OID oid) throws IOException{
        ResponseEvent event = get(new OID[]{oid});
        return event.getResponse().get(0).getVariable().toString();
    }

    public ResponseEvent get(OID oids[]) throws IOException{
        PDU pdu = new PDU();
        for(OID oid:oids){
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu,getTarget(),null);
        if(event != null){
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    private Target getTarget(){
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
}
