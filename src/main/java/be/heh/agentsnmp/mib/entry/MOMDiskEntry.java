package be.heh.agentsnmp.mib.entry;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOMDiskEntry implements MOTableRow {

    @Getter
    @Setter
    private OID oidIndex;
    @Getter
    @Setter
    private String reference;
    @Getter
    @Setter
    private Double available;
    @Getter
    @Setter
    private Double used;

    public MOMDiskEntry(OID oidIndex,String reference,Double available,Double used){
        setOidIndex(oidIndex);
        setReference(reference);
        setAvailable(available);
        setUsed(used);
    }

    @Override
    public OID getIndex() {
        return getOidIndex();
    }

    @Override
    public Variable getValue(int column) {
        switch (column){
            case 0:{return getIndex();}
            case 1:{return new OctetString(getReference());}
            case 2:{return new OctetString(Double.toString(getAvailable()));}
            case 3:{return new OctetString(Double.toString(getUsed()));}
            default:{System.err.println("no column has number "+column);return null;}
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
