package be.heh.agentsnmp.manager;

import org.snmp4j.agent.mo.DefaultMOTable;
import org.snmp4j.agent.mo.MOScalar;

import java.util.List;

public interface Manager {
    public List<MOScalar> getMOScalars();
    public List<DefaultMOTable> getMOTables();
    public void initMOIdentity();
    public void initMOVariables();
    public void initMOScalars();
    public void initMOTables();
    public void initEntriesRow();
    public MOIdentity getIdentity();
}
