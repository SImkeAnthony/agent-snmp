package be.heh.agentsnmp.vacm;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

public class View {
    @Getter
    @Setter
    private OctetString viewName;
    @Getter
    @Setter
    private OID subTree;
    @Getter
    @Setter
    private OctetString mask;
    @Getter
    @Setter
    private int inclusionPolicy;
    @Getter
    @Setter
    private int storageType;
    public View(OctetString viewName,OID subTree, OctetString mask,int inclusionPolicy,int storageType){
        setViewName(viewName);
        setSubTree(subTree);
        setMask(mask);
        setInclusionPolicy(inclusionPolicy);
        setStorageType(storageType);
    }
}
