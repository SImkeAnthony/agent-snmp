package be.heh.agentsnmp.manager;

import be.heh.agentsnmp.manager.entities.Interface;
import be.heh.agentsnmp.mib.MOVariable;
import be.heh.agentsnmp.mib.MibBrowser;
import be.heh.agentsnmp.mib.entry.MOIfEntry;
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

public class IfManager implements Manager{

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
    @Setter
    private List<DefaultMOTable> defaultMOTables = new ArrayList<>();
    @Getter
    private HashMap<String,Integer> MOMapping = new HashMap<>();
    @Getter
    private List<Interface> interfaces = new ArrayList<>();
    public IfManager() throws IOException {
        setMibBrowser(new MibBrowser(new File("mib/personal-mib.json")));
        initInterfaces();
        initMOMapping();
        initMOIdentity();
        initMOVariables();
        initMOScalars();
        initMOTables();
        initEntriesRow();
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
        return getDefaultMOTables();
    }

    @Override
    public void initMOIdentity() {
        setMoIdentity(new MOIdentity("system",new OID("1.3.6.1.2.1.2")));
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
                            new OctetString(String.valueOf(getInterfaces().size()))
                    ));
                    break;
                }
                default:{System.err.println(moVariable.getName()+" is not supported yet");}
            }
        });
    }
    @Override
    public void initMOTables(){
        setDefaultMOTables(getMibBrowser().getMOTableBySubTree(getMoIdentity().getOid()));
    }
    @Override
    public void initEntriesRow(){
        getDefaultMOTables().parallelStream().forEach(table->{
            if(table.getID().equals(new OID(getMoIdentity().getOid().toString()+".2"))){
                getInterfaces().parallelStream().forEach(anInterface ->{
                    table.addRow(new MOIfEntry(
                            new OID(anInterface.getId().toString()),
                            anInterface.getDescription(),
                            anInterface.getMacAddress(),
                            anInterface.getIpAddress()
                    ));
                });
            }else {
                System.err.println("table "+table.getID().toString()+" is not supported yet");
            }
        });
    }

    @Override
    public MOIdentity getIdentity() {
        return getMoIdentity();
    }

    private void initMOMapping(){
        getMOMapping().put("ifNumber",1);
    }

    private void initInterfaces(){
        int id = 1;
        getInterfaces().add(new Interface((long) id,"intel R wifi 2.4 gHz","56-65-03-a3-c6-d6","192.168.0.12"));
        id++;getInterfaces().add(new Interface((long)id,"intel R NIC ethernet 10GB","23-d6-a6-56-84-96","192.168.0.40"));
    }

}
