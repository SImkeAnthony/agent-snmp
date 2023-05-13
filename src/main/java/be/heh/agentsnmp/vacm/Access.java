package be.heh.agentsnmp.vacm;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.smi.OctetString;

public class Access {
    @Getter
    @Setter
    private OctetString groupName;
    @Getter
    @Setter
    private OctetString prefixContext;
    @Getter
    @Setter
    private int securityModel;
    @Getter
    @Setter
    private int securityLevel;
    @Getter
    @Setter
    private int matchingPolicy;
    @Getter
    @Setter
    private OctetString readViewName;
    @Getter
    @Setter
    private OctetString writeViewName;
    @Getter
    @Setter
    private OctetString notifyViewName;
    @Getter
    @Setter
    private int storageType;

    public Access(OctetString groupName,OctetString prefixContext,int securityModel,int securityLevel,int matchingPolicy,OctetString readViewName,OctetString writeViewName,OctetString notifyViewName,int storageType){
        setGroupName(groupName);
        setPrefixContext(prefixContext);
        setSecurityModel(securityModel);
        setSecurityLevel(securityLevel);
        setMatchingPolicy(matchingPolicy);
        setReadViewName(readViewName);
        setWriteViewName(writeViewName);
        setNotifyViewName(notifyViewName);
        setStorageType(storageType);
    }
}
