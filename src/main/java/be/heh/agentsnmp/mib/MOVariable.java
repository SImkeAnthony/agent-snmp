package be.heh.agentsnmp.mib;

import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

public class MOVariable extends MOScalar {
    public MOVariable(OID oid, MOAccess access, Variable value){
        super(oid,access,value);
    }
}
