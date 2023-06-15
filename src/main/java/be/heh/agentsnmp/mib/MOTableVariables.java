package be.heh.agentsnmp.mib;

import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

public class MOTableVariables implements MOTableRow {
    @Override
    public OID getIndex() {
        return null;
    }

    @Override
    public Variable getValue(int column) {
        return null;
    }

    @Override
    public MOTableRow getBaseRow() {
        return null;
    }

    @Override
    public void setBaseRow(MOTableRow baseRow) {

    }

    @Override
    public int size() {
        return 0;
    }
}
