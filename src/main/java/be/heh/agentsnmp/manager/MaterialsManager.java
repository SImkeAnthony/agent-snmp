package be.heh.agentsnmp.manager;

import be.heh.agentsnmp.manager.entities.PersistentStorage;
import be.heh.agentsnmp.manager.entities.Processor;
import be.heh.agentsnmp.manager.entities.VolatileStorage;
import be.heh.agentsnmp.mib.MOVariable;
import be.heh.agentsnmp.mib.MibBrowser;
import be.heh.agentsnmp.mib.entry.MOMDiskEntry;
import be.heh.agentsnmp.mib.entry.MOMProcessorEntry;
import be.heh.agentsnmp.mib.entry.MOMVStorageEntry;
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

public class MaterialsManager implements Manager{

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
    private List<Processor> processors = new ArrayList<>();
    @Getter
    private List<PersistentStorage> persistentStorages = new ArrayList<>();
    @Getter
    private List<VolatileStorage> volatileStorages = new ArrayList<>();


    public MaterialsManager() throws IOException {
        setMibBrowser(new MibBrowser(new File("mib/personal-mib.json")));
        initProcessors();
        initPersistentStorages();
        initVolatileStorages();
        initMOMapping();
        initMOIdentity();
        initMOVariables();
        initMOScalars();
        initMOTables();
        initEntriesRow();
    }

    private void initVolatileStorages() {
        int id = 1;
        getVolatileStorages().add(new VolatileStorage((long)id,"Crucial RAM 1",16.0,3200.0,16.0));
        id++;
        getVolatileStorages().add(new VolatileStorage((long)id,"Crucial RAM 2",16.0,3200.0,16.0));
    }

    private void initPersistentStorages() {
        int id = 1;
        getPersistentStorages().add(new PersistentStorage((long)id,"samsung ev280",180.0,780.0));
        id++;
        getPersistentStorages().add(new PersistentStorage((long)id,"WD purple 2T0",1200.0,760.0));
    }

    private void initProcessors() {
        int id = 1;
        getProcessors().add(new Processor((long) id, "intel core i9 12900k", 12, 36, 5.9));
        id++;
        getProcessors().add(new Processor((long) id, "intel core i9 12900k", 12, 36, 5.9));

    }

    private void initMOMapping() {
        getMOMapping().put("mProcessorNumber",1);
        getMOMapping().put("mDiskNumber",2);
        getMOMapping().put("mVStorageNumber",3);
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
        setMoIdentity(new MOIdentity("materials",new OID("1.3.6.1.2.1.3")));
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
                            new OctetString(String.valueOf(getProcessors().size()))
                    ));
                    break;
                }
                case 2:{
                    moVariable.setMoScalar(new MOScalar(
                            moVariable.getOid(),
                            moVariable.getMoAccess(),
                            new OctetString(String.valueOf(getPersistentStorages().size()))
                    ));
                    break;
                }
                case 3:{
                    moVariable.setMoScalar(new MOScalar(
                            moVariable.getOid(),
                            moVariable.getMoAccess(),
                            new OctetString(String.valueOf(getVolatileStorages().size()))
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
            if(table.getID().equals(new OID(getMoIdentity().getOid().toString()+".2"))){ //-> processors table
                getProcessors().parallelStream().forEach(processor -> {
                    table.addRow(new MOMProcessorEntry(
                            new OID(processor.getId().toString()),
                            processor.getReference(),
                            processor.getCore(),
                            processor.getVCore(),
                            processor.getFrequency()
                    ));
                });
            }else if(table.getID().equals(new OID(getMoIdentity().getOid().toString()+".4"))){ //-> disk tables
                getPersistentStorages().parallelStream().forEach(disk->{
                    table.addRow(new MOMDiskEntry(
                            new OID(disk.getId().toString()),
                            disk.getReference(),
                            disk.getAvailable(),
                            disk.getUsed()
                    ));
                });
            }else if(table.getID().equals(new OID(getMoIdentity().getOid().toString()+".6"))){ //-> volatile storage table
                try{
                    getVolatileStorages().parallelStream().forEach(vStorage->{
                        table.addRow(new MOMVStorageEntry(
                                new OID(vStorage.getId().toString()),
                                vStorage.getReference(),
                                vStorage.getAvailable(),
                                vStorage.getFrequency(),
                                vStorage.getLatency()
                        ));
                    });
                }catch (NullPointerException e){}

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
