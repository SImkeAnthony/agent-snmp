package be.heh.agentsnmp.vacm;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.smi.OctetString;

public class Group {
    @Getter
    @Setter
    private int securityModel;
    @Getter
    @Setter
    private OctetString securityName;
    @Getter
    @Setter
    private OctetString groupName;
    @Getter
    @Setter
    private int storageType;

    public Group(int securityModel, OctetString securityName, OctetString groupName, int storageType){
        setSecurityModel(securityModel);
        setSecurityName(securityName);
        setGroupName(groupName);
        setStorageType(storageType);
    }
}
