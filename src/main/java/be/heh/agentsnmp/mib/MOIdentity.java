package be.heh.agentsnmp.mib;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.smi.OID;

public class MOIdentity {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private OID oid;

    public MOIdentity(String name,OID oid){
        setName(name);
        setOid(oid);
    }
    @Override
    public String toString(){
        return String.join(" : ",name,oid.toString());
    }
}
