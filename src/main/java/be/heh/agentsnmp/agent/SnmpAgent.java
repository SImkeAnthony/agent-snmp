package be.heh.agentsnmp.agent;

import be.heh.agentsnmp.listener.MOTableRowHandler;
import be.heh.agentsnmp.mo.Factory;
import be.heh.agentsnmp.mib.Target;
import be.heh.agentsnmp.usm.User;
import be.heh.agentsnmp.vacm.Access;
import be.heh.agentsnmp.vacm.Group;
import be.heh.agentsnmp.vacm.View;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.example.Snmp4jDemoMib;
import org.snmp4j.agent.io.DefaultMOPersistenceProvider;
import org.snmp4j.agent.io.ImportMode;
import org.snmp4j.agent.io.MOInputFactory;
import org.snmp4j.agent.io.prop.PropertyMOInput;
import org.snmp4j.agent.mo.DefaultMOFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TimeStamp;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.cfg.EngineBootsCounterFile;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ThreadPool;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

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
    private MOServer[] servers;
    @Getter
    @Setter
    private MOServer server;
    @Getter
    @Setter
    private OctetString context;
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
    private String configFile;
    @Getter
    @Setter
    private DefaultMOPersistenceProvider defaultMOPersistenceProvider;
    @Getter
    @Setter
    private EngineBootsCounterFile engineBootsCounterFile;
    @Getter
    @Setter
    private Factory factory;
    @Getter
    @Setter
    private OctetString address;
    @Getter
    @Setter
    private MessageDispatcher messageDispatcher;
    @Getter
    @Setter
    private Modules modules;

    @SuppressWarnings("unchecked")
    public SnmpAgent(String ipAddress,int port,String moPersistenceFile,String bootCounterFile,String configFile,OctetString context) throws IOException {
        setMoPersistenceFile(moPersistenceFile);
        setBootCounterFile(bootCounterFile);
        setConfigFile(configFile);
        setContext(context);
        setEngineBootsCounterFile(new EngineBootsCounterFile(new File(getBootCounterFile())));
        setAddress(new OctetString(new UdpAddress(ipAddress+"/"+port).getValue()));
        initMOServer();
        System.out.println("Starting initialize SNMP...");
        TransportMapping<? extends Address> tm = TransportMappings.getInstance().createTransportMapping(GenericAddress.parse("udp:"+ipAddress+"/"+port));
        setMessageDispatcher(new MessageDispatcherImpl());
        setSnmp(new Snmp());
        getSnmp().addTransportMapping(tm);
        getMessageDispatcher().addTransportMapping(tm);
        setLocalEngineId(new OctetString(MPv3.createLocalEngineID()));
        System.out.println("End initialize SNMP");
        initVacm();
        initUSM();
        System.out.println("Starting initialize Configurateur...");
        setDefaultMOPersistenceProvider(new DefaultMOPersistenceProvider(getServers(),getMoPersistenceFile()));
        setFactory(new Factory(getMoPersistenceFile()));
        setConfigurateur(new AgentConfigManager(
                getLocalEngineId(),
                getSnmp().getMessageDispatcher(),
                getVacm(),
                getServers(),
                ThreadPool.create("poolRequest", 10),
                createMOFactory(),
                getDefaultMOPersistenceProvider(),
                getEngineBootsCounterFile()
        ));
        System.out.println("End initialize Configurateur...");
        getSnmp().listen();
        System.out.println("SNMP listen...");
    }

    private void initMOServer(){
        System.out.println("Starting initialize MOServer...");
        setServer(new DefaultMOServer());
        getServer().addContext(getContext());
        setServers(new MOServer[]{getServer()});
        System.out.println("End initialize MOServer...");
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

    private void initUSM(){
        System.out.println("Starting initialize USM...");
        setUsm(new USM(SecurityProtocols.getInstance(),getLocalEngineId(),0));
        getUsm().setEngineDiscoveryEnabled(true);
        SecurityModels.getInstance().addSecurityModel(getUsm());
        getSnmp().getMessageDispatcher().addMessageProcessingModel(new MPv3(usm.getLocalEngineID().getValue()));
        User defaultUser = new User(new OctetString("anthony"),AuthMD5.ID,new OctetString("Silver-Major-Knight-16"),PrivDES.ID,new OctetString("Silver-Major-Knight-16"));
        boolean addControl = addUsmUser(defaultUser);
        if(!addControl){
            System.err.println("Error: can't add default UsmUser");
        }
        System.out.println("End initialize USM");
    }
    private void initVacm() {
        setVacm(new VacmMIB(getServers()));
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
    private MOInputFactory createMOFactory(){
        MOInputFactory moInputFactory;
        final Properties props = new Properties();
        if (getConfigFile() == null) {
            System.err.println("Error: config file is null");
            return null;
        }
        try {
            InputStream configInputStream = new FileInputStream(getConfigFile());
            props.load(configInputStream);
        } catch (FileNotFoundException e) {
            System.err.println("File IO Error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        }
        moInputFactory = () -> new PropertyMOInput(props, getConfigurateur(), ImportMode.replaceCreate);
        return moInputFactory;
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

    private MOFactory getMOFactory() {
        return DefaultMOFactory.getInstance();
    }
    public boolean registerMIB(){
        System.out.println("Starting register MIB...");
        if (getModules() == null) {
            setModules(new Modules(getMOFactory()));
            getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .addMOTableRowListener(new MOTableRowHandler(getModules(), getConfigurateur()));
            ((TimeStamp) getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .getColumn(Snmp4jDemoMib.idxSnmp4jDemoEntryCol4)).setSysUpTime(getConfigurateur().getSysUpTime());
        }
        try {
            System.out.println("Register MOScalar");
            MOScalar scalar = new MOScalar(
            new OID("1.3.2.3.6.2.1.1.2"),
            MOAccessImpl.ACCESS_READ_CREATE,
            new OctetString("this is the a scalar registration")
            );
            getServer().register(scalar,null);
            getModules().registerMOs(getServer(), null);
            System.out.println("End register MIB");
            return true;
        } catch (DuplicateRegistrationException e) {
            System.err.println("Error: duplicate registration server " + Arrays.toString(getServer().getContexts()));
            return false;
        }
    }

    public void start(){
        try {
            getConfigurateur().initialize();
            initTargetMIB();
            getConfigurateur().setupProxyForwarder();
            registerMIB();
            getConfigurateur().registerShutdownHook();
            System.out.println("Run...");
            getConfigurateur().run();
        }catch (RuntimeException e){
            System.out.println("Runtime Error: "+e.getMessage());
            getConfigurateur().shutdown();
        }
    }
}
