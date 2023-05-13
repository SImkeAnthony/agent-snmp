package be.heh.agentsnmp.agent;

import be.heh.agentsnmp.engineboot.BootProvider;
import be.heh.agentsnmp.mo.MOFactory;
import be.heh.agentsnmp.targetmib.Target;
import be.heh.agentsnmp.usm.User;
import be.heh.agentsnmp.vacm.Access;
import be.heh.agentsnmp.vacm.Group;
import be.heh.agentsnmp.vacm.View;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.Snmp;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.io.DefaultMOPersistenceProvider;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;

public class SnmpAgent {
    @Getter
    @Setter
    private Snmp snmp;
    @Getter
    @Setter
    private AgentConfigManager configurateur;
    @Getter
    @Setter
    private OctetString localEngineId;
    @Getter
    @Setter
    private DefaultMOServer[] pMOServer;
    @Getter
    @Setter
    VacmMIB vacm;
    @Getter
    @Setter
    private USM usm;
    @Getter
    @Setter
    private String moPersistenceFile;
    @Getter
    @Setter
    private String bootCounterFile;
    @Getter
    @Setter
    private OctetString address;

    public SnmpAgent(String ipAddress,int port,String moPersistenceFile,String bootCounterFile) throws IOException {
        setMoPersistenceFile(moPersistenceFile);
        setBootCounterFile(bootCounterFile);
        setAddress(new OctetString(new UdpAddress(ipAddress+"/"+port).getValue()));
        intiMOServer();
        System.out.println("Starting initialize SNMP...");
        setSnmp(new Snmp());
        getSnmp().addTransportMapping(new DefaultUdpTransportMapping());
        setLocalEngineId(new OctetString(MPv3.createLocalEngineID()));
        System.out.println("End initialize SNMP");
        initVacm();
        initUSM();
        System.out.println("Starting initialize Configurateur...");
        setConfigurateur(new AgentConfigManager(
                getLocalEngineId(),
                getSnmp().getMessageDispatcher(),
                getVacm(),
                getPMOServer(),
                ThreadPool.create("poolRequest", 10),
                new MOFactory(getMoPersistenceFile()),
                new DefaultMOPersistenceProvider(getPMOServer(),getMoPersistenceFile()),
                new BootProvider(getBootCounterFile())
        ));
        System.out.println("End initialize Configurateur...");
        getSnmp().listen();
        System.out.println("SNMP listen...");
    }

    private void initTargetMIB(){
        System.out.println("Starting initialize TargetMIB...");
        getConfigurateur().getSnmpTargetMIB().addDefaultTDomains();
        Target defaultTarget = new Target(
                new OctetString("notificationV3"),
                TransportDomains.transportDomainUdpIpv4,
                getAddress(),
                1000,
                5,
                new OctetString("notify"),
                new OctetString("v3Notify"),
                StorageType.nonVolatile
        );
        boolean addControl = addTargetMIB(defaultTarget);
        if(!addControl){
            System.err.println("Error: can't add default TargetMIB");
        }
        System.out.println("End initialize TargetMIB...");
    }
    private void intiMOServer(){
        System.out.println("Starting initialize MOServer...");
        setPMOServer(new DefaultMOServer[]{new DefaultMOServer()});
        getPMOServer()[0].addContext(new OctetString("public"));
        System.out.println("End initialize MOServer");
    }

    private void initUSM(){
        System.out.println("Starting initialize USM...");
        setUsm(new USM(SecurityProtocols.getInstance(),getLocalEngineId(),0));
        getUsm().setEngineDiscoveryEnabled(true);
        SecurityModels.getInstance().addSecurityModel(getUsm());
        getSnmp().getMessageDispatcher().addMessageProcessingModel(new MPv3(usm.getLocalEngineID().getValue()));
        User defaultUser = new User(new OctetString("anthony"),AuthMD5.ID,new OctetString("Silver-Major-Knight-16"),PrivDES.ID,new OctetString("Silver-Major-Knight-16"));
        boolean addControle = addUsmUser(defaultUser);
        if(!addControle){
            System.err.println("Error: can't add default UsmUser");
        }
        System.out.println("End initialize USM");
    }
    private void initVacm() {
        setVacm(new VacmMIB(getPMOServer()));
        //add groups and
        try {
            System.out.println("Starting initialize Vacm...");
            Group defaultGroup = new Group(SecurityModel.SECURITY_MODEL_USM,new OctetString("anthony"),new OctetString("public"), StorageType.nonVolatile);
            boolean addControl = addGroupToVacm(defaultGroup);
            if(!addControl){
                System.out.println("Error: can't add default group");
            }
            Access defaultAccess = new Access(
                    new OctetString("public"),
                    new OctetString(),
                    SecurityModel.SECURITY_MODEL_USM,
                    SecurityLevel.AUTH_PRIV,
                    MutableVACM.VACM_MATCH_EXACT,
                    new OctetString("readViewAnthony"),
                    new OctetString("writeViewAnthony"),
                    new OctetString("notifyViewAnthony"),
                    StorageType.nonVolatile
            );
            addControl = addAccessToVacm(defaultAccess);
            if(!addControl){
                System.err.println("Error: can't add default access");
            }
            View defaultReadView = new View(
                    new OctetString("readViewAnthony"),
                    new OID("1"),
                    new OctetString(),
                    VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile
            );
            View defaultWriteView = new View(
                    new OctetString("writeViewAnthony"),
                    new OID("1"),
                    new OctetString(),
                    VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile
            );
            View defaultNotifyView = new View(
                    new OctetString("notifyViewAnthony"),
                    new OID("1"),
                    new OctetString(),
                    VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile
            );
            addControl = addSubTreeViewToVacm(defaultReadView);
            if(!addControl){
                System.err.println("Error: can't add default read view");
            }
            addControl = addSubTreeViewToVacm(defaultWriteView);
            if(!addControl){
                System.err.println("Error: can't add default write view");
            }
            addControl = addSubTreeViewToVacm(defaultNotifyView);
            if(!addControl){
                System.err.println("Error: can't add default notify view");
            }
            System.out.println("End initialize Vacm");
        }catch (RuntimeException e){
            System.err.println("Runtime Error: "+e.getMessage());
        }
    }
    public boolean addGroupToVacm(Group groupVacm){
        if(getVacm().hasSecurityToGroupMapping(groupVacm.getSecurityModel(),groupVacm.getSecurityName())){
            return false;
        }else{
            getVacm().addGroup(groupVacm.getSecurityModel(),groupVacm.getSecurityName(),groupVacm.getGroupName(),groupVacm.getStorageType());
            return true;
        }
    }

    public boolean addAccessToVacm(Access access){
        try {
            getVacm().addAccess(
                    access.getGroupName(),
                    access.getPrefixContext(),
                    access.getSecurityModel(),
                    access.getSecurityLevel(),
                    access.getMatchingPolicy(),
                    access.getReadViewName(),
                    access.getWriteViewName(),
                    access.getNotifyViewName(),
                    access.getStorageType()
            );
            return true;
        }catch (RuntimeException e){
            System.err.println("Runtime Error: "+e.getMessage());
            return false;
        }
    }

    public boolean addSubTreeViewToVacm(View view){
        try {
            getVacm().addViewTreeFamily(
                    view.getViewName(),
                    view.getSubTree(),
                    view.getMask(),
                    view.getInclusionPolicy(),
                    view.getStorageType()
            );
            return true;
        }catch (RuntimeException e){
            System.err.println("Runtime Error: "+e.getMessage());
            return false;
        }
    }

    public boolean addUsmUser(User user){
        try {
            getUsm().addUser(new UsmUser(
                    user.getSecurityName(),
                    user.getAuthProtocol(),
                    user.getAuthPassPhrase(),
                    user.getPrivProtocol(),
                    user.getPrivPassPhrase()
            ));
            return true;
        }catch (RuntimeException e){
            System.err.println("Runtime Error: "+e.getMessage());
            return false;
        }
    }

    public boolean addTargetMIB(Target target){
        try {
            getConfigurateur().getSnmpTargetMIB().addTargetAddress(
                    target.getName(),
                    target.getTransportDomain(),
                    target.getAddress(),
                    target.getTimeOut(),
                    target.getRetries(),
                    target.getTagList(),
                    target.getParameters(),
                    target.getStorageType()
            );
            return true;
        }catch (RuntimeException e){
            System.err.println("Runtime Error: "+e.getMessage());
            return false;
        }
    }

    public void start(){
        try {
            getConfigurateur().initialize();
            getConfigurateur().configure();
            initTargetMIB();
            getConfigurateur().run();
        }catch (RuntimeException e){
            System.out.println("Runtime Error: "+e.getMessage());
            getConfigurateur().shutdown();
        }
    }
}
