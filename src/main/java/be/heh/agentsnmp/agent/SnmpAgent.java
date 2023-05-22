package be.heh.agentsnmp.agent;

import be.heh.agentsnmp.listener.MOTableRowHandler;
import lombok.Getter;
import lombok.Setter;
import be.heh.agentsnmp.mib.Target;
import org.snmp4j.*;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
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
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
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

        //init snmp
        setSnmp(new Snmp());

        //initialize file
        initFile(engineBootsCounterFileName,configFileName,configMOFileName);

        //create variable needed
        OctetString engineId = getEngineBootsCounterFile().getEngineId(new OctetString(MPv3.createLocalEngineID()));
        setServer(new DefaultMOServer());
        for(String context:contexts){
            getServer().addContext(new OctetString(context));
        }
        setServers(new MOServer[]{getServer()});

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

        getSnmp().listen();
        System.out.println("Snmp listen ...");
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

    private void initTransport(List<String> listenAddresses,int listenPort){
        System.out.println("Initialize transport mapping ...");
        try{
            for(String ipAddress : listenAddresses){
                Address address = GenericAddress.parse(String.format("udp:%s/%s",ipAddress , listenPort));
                TransportMapping<? extends Address> transportMapping = TransportMappings.getInstance().createTransportMapping(address);
                getSnmp().getMessageDispatcher().addTransportMapping(transportMapping);
                getSnmp().addTransportMapping(transportMapping);
            }
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
        addTargetMIB(defaultTarget);

        if (getModules() == null) {
            setModules(new Modules(getMOFactory()));
            getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .addMOTableRowListener(new MOTableRowHandler(getModules(), getAgentConfigManager()));
            ((TimeStamp) getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .getColumn(Snmp4jDemoMib.idxSnmp4jDemoEntryCol4)).setSysUpTime(getAgentConfigManager().getSysUpTime());
        }

        try{
            getModules().registerMOs(getServer(),null);
            MOScalar defaultScalar = new MOScalar(
                    new OID("1.3.2.3.6.2.1.1.2"),
                    MOAccessImpl.ACCESS_READ_CREATE,
                    new OctetString("this is the a scalar registration")
            );
            boolean control = registerMIB(defaultScalar);
            if(!control){
                System.err.println("Error initMIB : false return of registerMIB");
            }
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
