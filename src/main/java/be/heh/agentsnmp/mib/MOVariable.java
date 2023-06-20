package be.heh.agentsnmp.mib;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOVariable {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private OID oid;
    @Getter
    @Setter
    private MOAccess moAccess;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    MOScalar moScalar;
    public MOVariable(String name,OID oid, MOAccess access,String description){
        setName(name);
        setOid(oid);
        setMoAccess(access);
        setDescription(description);
    }
    public MOVariable(String name,OID oid, MOAccess access,String description,MOScalar moScalar){
        setName(name);
        setOid(oid);
        setMoAccess(access);
        setDescription(description);
        setMoScalar(moScalar);
    }

    @Override
    public String toString(){
        return "name : "+getName()+";oid : "+getOid()+";access : "+getMoAccess().toString()+";description : "+getDescription();
    }

    //perform method on MOScalar here
}
