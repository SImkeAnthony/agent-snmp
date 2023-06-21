package be.heh.agentsnmp.manager;

import be.heh.agentsnmp.manager.entities.Service;
import be.heh.agentsnmp.mib.MOIdentity;
import be.heh.agentsnmp.mib.MOVariable;
import be.heh.agentsnmp.mib.MibBrowser;
import be.heh.agentsnmp.mib.entry.MOServiceEntry;
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

public class ServiceManager implements Manager {

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
    private List<Service> services = new ArrayList<>();

    public ServiceManager() throws IOException {
        setMibBrowser(new MibBrowser(new File("mib/personal-mib.json")));
        initServices();
        initMOMapping();
        initMOIdentity();
        initMOVariables();
        initMOScalars();
        initMOTables();
        initEntriesRow();
    }

    private void initMOMapping(){
        getMOMapping().put("sNumber",1);
    }
    private void initServices() {
        int id = 1;
        getServices().add(new Service((long)id,"web-planing-access","this is a site web to access on planing platform","8088-3132"));
        id++;
        getServices().add(new Service((long)id,"database-access","this is a access to a database","5432"));
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
    public void initMOIdentity() {setMoIdentity(new MOIdentity("service", new OID("1.3.6.1.2.1.4")));}

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
                            new OctetString(String.valueOf(getServices().size()))
                    ));
                    break;
                }
                default:{System.err.println(moVariable.getName()+" is not supported yet");}
            }
        });
    }
    @Override
    public void initMOTables() {
        setDefaultMOTables(getMibBrowser().getMOTableBySubTree(getMoIdentity().getOid()));
    }

    @Override
    public void initEntriesRow() {
        getDefaultMOTables().parallelStream().forEach(table->{
            if(table.getID().equals(new OID(getMoIdentity().getOid().toString()+".2"))){
                getServices().parallelStream().forEach(service->{
                    table.addRow(new MOServiceEntry(
                            new OID(service.getId().toString()),
                            service.getName(),
                            service.getDescription(),
                            service.getPort()
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
}
