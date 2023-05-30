package be.heh.agentsnmp.agent;

import be.heh.agentsnmp.listener.MOTableRowHandler;
import be.heh.agentsnmp.listener.RequestHandler;
import lombok.Getter;
import lombok.Setter;
import be.heh.agentsnmp.mib.Target;
import org.snmp4j.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.example.Snmp4jDemoMib;
import org.snmp4j.agent.io.*;
import org.snmp4j.agent.io.prop.PropertyMOInput;
import org.snmp4j.agent.mo.DefaultMOFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TimeStamp;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.cfg.EngineBootsCounterFile;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ThreadPool;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class SnmpAgent {

    @Setter
    @Getter
    private AgentConfigManager agentConfigManager;

    //manage files
    @Setter
    @Getter
    private String engineBootsCounterFileName;
    @Setter
    @Getter
    private EngineBootsCounterFile engineBootsCounterFile;
    @Setter
    @Getter
    private String configFileName;
    @Setter
    @Getter
    private String configMOFileName;

    //SNMP variables
    @Getter
    @Setter
    private Snmp snmp;
    @Getter
    @Setter
    private Modules modules;
    @Getter
    @Setter
    private MOServer[] servers;
    @Getter
    @Setter
    private MOServer server;
    public SnmpAgent(String engineBootsCounterFileName,String configFileName, String configMOFileName,List<String> listenAddresses,int listenPort,List<String> contexts) throws IOException {

        //initialize file
        initFile(engineBootsCounterFileName,configFileName,configMOFileName);

        //create variable needed
        OctetString engineId = getEngineBootsCounterFile().getEngineId(new OctetString(MPv3.createLocalEngineID()));
        setServer(new DefaultMOServer());
        for(String context:contexts){
            getServer().addContext(new OctetString(context));
        }
        setServers(new MOServer[]{getServer()});

        //init snmp
        initSnmp(engineId);

        //initialize transport
        initTransport(listenAddresses,listenPort);

        //initialize agent configurator manager
        System.out.println("Initialize agent configurator manager ...");
        setAgentConfigManager(new AgentConfigManager(
                engineId,
                getSnmp().getMessageDispatcher(),
                null,
                getServers(),
                ThreadPool.create("poolRequest",10),
                createMOInputFactory(ImportMode.restoreChanges),
                new DefaultMOPersistenceProvider(getServers(),getConfigMOFileName()),
                getEngineBootsCounterFile()
        ));
        //set variables agent configurator manager
        getAgentConfigManager().setContext(
                new SecurityModels(),
                new SecurityProtocols(SecurityProtocols.SecurityProtocolSet.maxCompatibility),
                new CounterSupport()
        );
        getSnmp().addCommandResponder(new RequestHandler(getSnmp(),getAgentConfigManager()));
    }

    private void initFile(String engineBootsCounterFileName,String configFileName,String configMOFileName) throws IOException {
        try{
            System.out.println("Initialize files ...");
            setEngineBootsCounterFileName(engineBootsCounterFileName);
            setEngineBootsCounterFile(new EngineBootsCounterFile(new File(getEngineBootsCounterFileName())));
            setConfigFileName(configFileName);
            File configFile = new File(getConfigFileName());
            configFile.createNewFile();
            setConfigMOFileName(configMOFileName);
            File configMOFile = new File(configMOFileName);
            configMOFile.createNewFile();
        }catch (IOException e){
            System.err.println("Error initFile : "+e.getMessage());
        }
    }

    private void initSnmp(OctetString engineId){
        System.out.println("Initialize SNMP ....");
        try{
            setSnmp(new Snmp());
            getSnmp().addTransportMapping(new DefaultUdpTransportMapping());
            getSnmp().setMessageDispatcher(new MessageDispatcherImpl());
            getSnmp().getMessageDispatcher().addMessageProcessingModel(new MPv1());
            getSnmp().getMessageDispatcher().addMessageProcessingModel(new MPv3());

            SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());
            SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());
            USM usm = new USM(SecurityProtocols.getInstance(), engineId, 0);
            usm.setEngineDiscoveryEnabled(true);
            SecurityModels.getInstance().addSecurityModel(usm);

            getSnmp().listen();
            System.out.println("Snmp listen ...");
        }catch (Error e){
            System.err.println("Error initialize SNMP : "+e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void initTransport(List<String> listenAddresses,int listenPort){
        System.out.println("Initialize transport mapping ...");
        try{
            for(String ipAddress : listenAddresses){
                Address address = GenericAddress.parse(String.format("udp:%s/%s",ipAddress , listenPort));
                TransportMapping<? extends Address> transportMapping = TransportMappings.getInstance().createTransportMapping(address);
                transportMapping.listen();
                getSnmp().getMessageDispatcher().addTransportMapping(transportMapping);
                getSnmp().addTransportMapping(transportMapping);
            }
            getSnmp().listen();
        }catch (Exception e){
            System.err.println("Error init transportMapping : "+e.getMessage());
        }
    }

    private void initMIB(String ipAddress,int port){
        System.out.println("Initialize MIB ...");
        getAgentConfigManager().getSnmpTargetMIB().addDefaultTDomains();

        //add default targetMIB
        Target defaultTarget = new Target(
                new OctetString("default"),
                TransportDomains.transportDomainUdpIpv4,
                new OctetString(new UdpAddress(ipAddress + "/" + port).getValue()),
                1000,
                5,
                new OctetString("notify"),
                new OctetString("v3Notify"),
                StorageType.nonVolatile
        );
        Target community = new Target(
                new OctetString("Silver-King-Rogue-16"),
                TransportDomains.transportDomainUdpIpv4,
                new OctetString(new UdpAddress(ipAddress + "/" + port).getValue()),
                1000,
                5,
                null,
                null,
                StorageType.nonVolatile
        );
        addTargetMIB(defaultTarget);
        addTargetMIB(community);

        if (getModules() == null) {
            setModules(new Modules(getMOFactory()));
            getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .addMOTableRowListener(new MOTableRowHandler(getModules(), getAgentConfigManager()));
            ((TimeStamp) getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .getColumn(Snmp4jDemoMib.idxSnmp4jDemoEntryCol4)).setSysUpTime(getAgentConfigManager().getSysUpTime());
        }

        try{
            getModules().registerMOs(getServer(),null);
            //here you can register your MIB internally
        } catch (DuplicateRegistrationException e) {
            System.err.println("Error initMIB : "+e.getMessage());
        }
    }

    private MOFactory getMOFactory() {
        return DefaultMOFactory.getInstance();
    }
    public boolean registerMIB(MOScalar scalar) {
        try{
            System.out.println("Register MIB ...");
            getServer().register(scalar,null);
            return true;
        }catch (DuplicateRegistrationException e){
            System.err.println("Error registerMIB : "+e.getMessage());
            return false;
        }
    }

    public boolean addTargetMIB(Target target) {
        try {
            getAgentConfigManager().getSnmpTargetMIB().addTargetAddress(
                    target.getName(),
                    target.getTransportDomain(),
                    target.getAddress(),
                    target.getTimeOut(),
                    target.getRetries(),
                    target.getTagList(),
                    target.getParameters(),
                    target.getStorageType());
            return true;
        } catch (RuntimeException e) {
            System.err.println("Runtime Error: " + e.getMessage());
            return false;
        }
    }
    private MOInputFactory createMOInputFactory(ImportMode importMode) {
        MOInputFactory configurationFactory;
        InputStream configInputStream = SnmpAgent.class.getResourceAsStream(getConfigFileName());
        final Properties props = new Properties();
        if (getConfigFileName() != null) {
            try {
                configInputStream = new FileInputStream(getConfigFileName());
            } catch (FileNotFoundException e) {
                System.err.println("Error create MOInput factory : "+e.getMessage());
            }
        }
        try {
            props.load(configInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        configurationFactory = () -> new PropertyMOInput(props, getAgentConfigManager(), importMode);
        return configurationFactory;
    }

    public void run(String ipAddress,int port){
        try{
            getAgentConfigManager().initialize();
            initMIB(ipAddress,port);
            getAgentConfigManager().setupProxyForwarder();
            getAgentConfigManager().registerShutdownHook();
            getAgentConfigManager().run();
        }catch (Exception e){
            System.err.println("Error run : "+e.getMessage());
        }
    }
}
