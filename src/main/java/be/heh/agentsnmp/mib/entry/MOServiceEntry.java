package be.heh.agentsnmp.mib.entry;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOServiceEntry implements MOTableRow {
    @Getter
    @Setter
    private OID oidIndex;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String port;
    public MOServiceEntry(OID oidIndex,String name,String description,String port){
        setOidIndex(oidIndex);
        setName(name);
        setDescription(description);
        setPort(port);
    }

    @Override
    public OID getIndex() {
        return getOidIndex();
    }

    @Override
    public Variable getValue(int column) {
        switch (column){
            case 1:{return getIndex();}
            case 2:{return new OctetString(getName());}
            case 3:{return new OctetString(getDescription());}
            case 4:{return new OctetString(getPort());}
            default:{System.err.println("no column has number "+column);return null;}
        }
    }

    @Override
    public MOTableRow getBaseRow() {
        return this;
    }

    @Override
    public void setBaseRow(MOTableRow baseRow) {
        //not implemented
    }

    @Override
    public int size() {
        return 4;
    }
}
