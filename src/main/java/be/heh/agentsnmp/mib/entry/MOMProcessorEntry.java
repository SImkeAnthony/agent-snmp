package be.heh.agentsnmp.mib.entry;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

public class MOMProcessorEntry implements MOTableRow {

    @Getter
    @Setter
    private OID oidIndex;
    @Getter
    @Setter
    private String reference;
    @Getter
    @Setter
    private int core;
    @Getter
    @Setter
    private int vCore;
    @Getter
    @Setter
    private Double frequency;

    public MOMProcessorEntry(OID oidIndex,String reference,int core,int vCore,Double frequency){
        setOidIndex(oidIndex);
        setReference(reference);
        setCore(core);
        setVCore(vCore);
        setFrequency(frequency);
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
            case 3:{return new OctetString(Integer.toString(getCore()));}
            case 4:{return new OctetString(Integer.toString(getVCore()));}
            case 5:{return new OctetString(Double.toString(getFrequency()));}
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
