package be.heh.agentsnmp.manager;

import be.heh.agentsnmp.mib.MOVariable;
import be.heh.agentsnmp.mib.MibBrowser;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.mo.DefaultMOTable;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SysManager implements Manager{

    @Getter
    @Setter
    private MibBrowser mibBrowser;
    @Getter
    @Setter
    private MOIdentity moIdentity;
    @Getter
    @Setter
    private List<MOVariable> moVariables = new ArrayList<>();
    @Getter
    private HashMap<String,Integer> MOMapping = new HashMap<>();

    public SysManager() throws IOException {
        setMibBrowser(new MibBrowser(new File("mib/personal-mib.json")));
        initMOMapping();
        initMOIdentity();
        initMOVariables();
        initMOScalars();
    }
    @Override
    public List<MOScalar> getMOScalars() {
        List<MOScalar> moScalars = new ArrayList<>();
        getMoVariables().parallelStream().forEach(moVariable->{
            moScalars.add(moVariable.getMoScalar());
        });
        return moScalars;
    }

    @Override
    public List<DefaultMOTable> getMOTables() {
        //any table to manage
        return null;
    }

    @Override
    public void initMOIdentity() {
        setMoIdentity(new MOIdentity("system",new OID("1.3.6.1.2.1.1")));
    }

    @Override
    public void initMOVariables() {
        setMoVariables(getMibBrowser().getMOVariablesBySubTree(getMoIdentity().getOid()));
    }

    @Override
    public void initMOScalars() {
        getMoVariables().parallelStream().forEach(moVariable -> {
            int id = getMOMapping().get(moVariable.getName());
            switch (id){
                case 1:{
                    moVariable.setMoScalar(new MOScalar(
                            moVariable.getOid(),
                            moVariable.getMoAccess(),
                            new OctetString(getHostname())
                    ));
                }
                case 2:{
                    moVariable.setMoScalar(new MOScalar(
                            moVariable.getOid(),
                            moVariable.getMoAccess(),
                            new OctetString(getOS())
                    ));
                }
                default:{
                    System.err.println(moVariable.getName()+" is no supported yet");
                }
            }
        });
    }

    @Override
    public void initMOTables() {
        //any MOTable to set
    }

    @Override
    public void initEntriesRow() {
        //any entry row to configure
    }

    private void initMOMapping(){
        getMOMapping().put("sysHostname",1);
        getMOMapping().put("sysOs",2);
    }
    private String getHostname(){
        //implement a real logic to get info
        return "generic.hostname.local";
    }
    private String getOS(){
        //implement a real logic to get info
        return "generic os by generic society";
    }
}
