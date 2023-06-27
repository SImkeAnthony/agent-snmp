package be.heh.agentsnmp.mib.entry;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOMVStorageEntry implements MOTableRow {

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
    private Double frequency;
    @Getter
    @Setter
    private Double latency;

    public MOMVStorageEntry(OID oidIndex,String reference,Double available,Double frequency,Double latency){
        setOidIndex(oidIndex);
        setReference(reference);
        setAvailable(available);
        setFrequency(frequency);
        setLatency(latency);
    }

    @Override
    public OID getIndex() {
        return getOidIndex();
    }

    @Override
    public Variable getValue(int column) {
        switch (column){
            case 1:{return getIndex();}
            case 2:{return new OctetString(getReference());}
            case 3:{return new OctetString(Double.toString(getAvailable()));}
            case 4:{return new OctetString(Double.toString(getFrequency()));}
            case 5:{return new OctetString(Double.toString(getLatency()));}
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
        return 5;
    }
}
