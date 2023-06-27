package be.heh.agentsnmp.mib.entry;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOIfEntry implements MOTableRow {

    @Getter
    @Setter
    private OID oidIndex;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String macAddress;
    @Getter
    @Setter
    private String ipAddress;
    public MOIfEntry(OID index,String description,String macAddress,String ipAddress){
        setOidIndex(index);
        setDescription(description);
        setMacAddress(macAddress);
        setIpAddress(ipAddress);
    }
    @Override
    public OID getIndex() {
        return getOidIndex();
    }

    @Override
    public Variable getValue(int column) {
        System.out.println("get info if at column : "+column);
        switch (column){
            case 1:{
                return getIndex();
            }
            case 2:{
                return new OctetString(getDescription());
            }
            case 3:{
                return new OctetString(getMacAddress());
            }
            case 4:{
                return new OctetString(getIpAddress());
            }
            default:{
                System.err.println("no column has number "+column);
                return null;
            }
        }
    }

    @Override
    public MOTableRow getBaseRow() {
        return this;
    }

    @Override
    public void setBaseRow(MOTableRow baseRow) {
        //not implement
    }

    @Override
    public int size() {
        return 4;
    }
}
