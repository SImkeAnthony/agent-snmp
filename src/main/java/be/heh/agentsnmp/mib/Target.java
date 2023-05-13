package be.heh.agentsnmp.mib;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

public class Target {

    @Getter
    @Setter
    private OctetString name;
    @Getter
    @Setter
    private OID transportDomain;
    @Getter
    @Setter
    private OctetString address;
    @Getter
    @Setter
    private int timeOut;
    @Getter
    @Setter
    private int retries;
    @Getter
    @Setter
    private OctetString tagList;
    @Getter
    @Setter
    private OctetString parameters;
    @Getter
    @Setter
    private int storageType;

    public Target(OctetString name, OID transportDomain, String ipAddress, int port, int timeOut, int retries, OctetString tagList, OctetString parameters,int storageType){
        setName(name);
        setTransportDomain(transportDomain);
        setAddress(new OctetString(new UdpAddress(ipAddress+"/"+port).getValue()));
        setTimeOut(timeOut);
        setRetries(retries);
        setTagList(tagList);
        setParameters(parameters);
        setStorageType(storageType);
    }
    public Target(OctetString name, OID transportDomain, OctetString address, int timeOut, int retries, OctetString tagList, OctetString parameters,int storageType){
        setName(name);
        setTransportDomain(transportDomain);
        setAddress(address);
        setTimeOut(timeOut);
        setRetries(retries);
        setTagList(tagList);
        setParameters(parameters);
        setStorageType(storageType);
    }
}
