package be.heh.agentsnmp.usm;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
public class User {
    @Getter
    @Setter
    private OctetString securityName;
    @Getter
    @Setter
    private OID authProtocol;
    @Getter
    @Setter
    private OctetString authPassPhrase;
    @Getter
    @Setter
    private OID privProtocol;
    @Getter
    @Setter
    private OctetString privPassPhrase;

    public User(OctetString securityName,OID authProtocol,OctetString authPassPhrase,OID privProtocol,OctetString privPassPhrase){
        setSecurityName(securityName);
        setAuthProtocol(authProtocol);
        setAuthPassPhrase(authPassPhrase);
        setPrivProtocol(privProtocol);
        setPrivPassPhrase(privPassPhrase);
    }
}
