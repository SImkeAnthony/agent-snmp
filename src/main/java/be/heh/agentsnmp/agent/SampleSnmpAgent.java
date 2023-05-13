package be.heh.agentsnmp.agent;

import be.heh.agentsnmp.listener.MOTableRowHandler;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.example.SampleAgent;
import org.snmp4j.agent.example.Snmp4jDemoMib;
import org.snmp4j.agent.io.DefaultMOPersistenceProvider;
import org.snmp4j.agent.io.ImportMode;
import org.snmp4j.agent.io.MOInputFactory;
import org.snmp4j.agent.io.MOPersistenceProvider;
import org.snmp4j.agent.io.prop.PropertyMOInput;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.SNMPv2MIB;
import org.snmp4j.agent.mo.snmp.TimeStamp;
import org.snmp4j.agent.mo.snmp.dh.DHKickstartParameters;
import org.snmp4j.agent.mo.snmp.dh.DHKickstartParametersImpl;
import org.snmp4j.cfg.EngineBootsCounterFile;
import org.snmp4j.cfg.EngineBootsProvider;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.SnmpConfigurator;
import org.snmp4j.util.ThreadPool;

import java.io.*;
import java.util.*;

public class SampleSnmpAgent {
    @Getter
    @Setter
    private AgentConfigManager agent;
    @Getter
    @Setter
    private MOServer server;
    @Getter
    @Setter
    protected MOServer[] servers;
    private final String configFile;
    private final String dhKickStartInfoPath;
    private final File bootCounterFile;
    private final String tableSizeLimitsFile;
    @Getter
    @Setter
    private EngineBootsCounterFile engineBootsCounterFile;
    @Getter
    @Setter
    private OctetString engineId;
    // MIBs
    @Getter
    @Setter
    private Modules modules;
    @Getter
    @Setter
    private Properties tableSizeLimitsproperties;
    @Getter
    @Setter
    private OctetString context;

    public SampleSnmpAgent(String configFile, File bootCounterFile, String dhKickStartInfoPath,String tableSizeLimitsFile,List<Object> address,OctetString context){
        System.out.println("Starting initialize MOServer...");
        this.server = new DefaultMOServer();
        setServers(new MOServer[]{getServer()});
        System.out.println("End initialize MOServer");
        System.out.println("Set config and bootCounter files");
        if(configFile.matches(".*\\.properties")){
            this.configFile = configFile;
        }else{
            this.configFile = "configFile.cfg";
        }
        this.bootCounterFile = bootCounterFile;
        this.dhKickStartInfoPath = dhKickStartInfoPath;
        this.tableSizeLimitsFile = tableSizeLimitsFile;
        System.out.println("Display file : \n"+this.configFile+"\n"+this.bootCounterFile.getPath()+"\n"+this.dhKickStartInfoPath+"\n"+this.tableSizeLimitsFile);
        setContext(context);
        setEngineBootsCounterFile(new EngineBootsCounterFile(this.bootCounterFile));
        setEngineId(getEngineBootsCounterFile().getEngineId(new OctetString(MPv3.createLocalEngineID())));
        //setup agent
        DefaultMOPersistenceProvider moProvider = new DefaultMOPersistenceProvider(getServers(),this.configFile);
        setTableSizeLimitsproperties(getTableSizeLimitsProperties(this.tableSizeLimitsFile));
        setupAgent(address,this.dhKickStartInfoPath,createMOInputFactory(this.configFile,ImportMode.restoreChanges),moProvider,getEngineBootsCounterFile());
    }

    private void setupAgent(List<Object> address, String dhKickStartInfoPath, MOInputFactory factory,
            MOPersistenceProvider moProvider, EngineBootsProvider bootProvider) {
        System.out.println("Starting setup agent...");
        MessageDispatcher dispatcher = new MessageDispatcherImpl();
        addListenAddress(dispatcher, address);
        Collection<DHKickstartParameters> dhKickstartParameters = Collections.emptyList();
        if (dhKickStartInfoPath != null) {
            File dhKickStartInfoFile = new File(dhKickStartInfoPath);
            try {
                Properties kickStartProperties = new Properties();
                FileInputStream inputStream = new FileInputStream(dhKickStartInfoFile);
                kickStartProperties.load(inputStream);
                inputStream.close();
                dhKickstartParameters = DHKickstartParametersImpl.readFromProperties("org.snmp4j.",
                        kickStartProperties);
            } catch (IOException e) {
                System.err.println("IO Error: " + e.getMessage());
            }
        }
        SnmpConfigurator configurator = new SnmpConfigurator(true);
        System.out.println("Starting config agent manager...");
        setAgent(new AgentConfigManager(
                getEngineId(),
                dispatcher,
                null,
                getServers(),
                ThreadPool.create("poolRequest", 10),
                factory,
                moProvider,
                bootProvider,
                null,
                dhKickstartParameters));
        System.out.println("End config agent manager");
        System.out.println("End setup agent");
    }

    private void addListenAddress(MessageDispatcher dispatcher, List<Object> address) {
        System.out.println("Starting adding listen address...");
        for (Object addressString : address) {
            Address addr = GenericAddress.parse(addressString.toString());
            if (addr == null) {
                System.err.println("Error: can't parse address : " + addressString);
                return;
            }
            TransportMapping<? extends Address> tm = TransportMappings.getInstance().createTransportMapping(addr);
            if (tm == null) {
                System.err.println("Error: any transport mapping available for address " + address);
                return;
            }
            dispatcher.addTransportMapping(tm);
        }
        System.out.println("End adding listen address");
    }

    private MOInputFactory createMOInputFactory(String configFile, ImportMode importMode) {
        MOInputFactory moInputFactory;
        InputStream configInputStream = SampleAgent.class.getResourceAsStream("AgentConfig.properties");
        final Properties props = new Properties();
        if (configFile == null) {
            System.err.println("Error: config file is null");
            return null;
        }
        try {
            configInputStream = new FileInputStream(configFile);
            props.load(configInputStream);
        } catch (FileNotFoundException e) {
            System.err.println("File IO Error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        }
        moInputFactory = () -> new PropertyMOInput(props, this.agent, importMode);
        return moInputFactory;
    }

    protected static Properties getTableSizeLimitsProperties(String tableSizeLimitsFile) {
        InputStream tableSizeLimitsStream = SampleAgent.class.getResourceAsStream("AgentTableSizeLimits.properties");
        if (tableSizeLimitsFile == null) {
            System.err.println("Error: tableSizeLimits file is null");
            return null;
        }
        try {
            tableSizeLimitsStream = new FileInputStream(tableSizeLimitsFile);
            Properties tableSizeProperties = new Properties();
            tableSizeProperties.load(tableSizeLimitsStream);
            return tableSizeProperties;
        } catch (FileNotFoundException e) {
            System.err.println("File IO Error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return null;
        }
    }

    private MOFactory getMOFactory() {
        return DefaultMOFactory.getInstance();
    }

    private void registerMIB() {
        System.out.println("Starting register MIB...");
        if (getModules() == null) {
            setModules(new Modules(getMOFactory()));
            getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .addMOTableRowListener(new MOTableRowHandler(getModules(), getAgent()));
            ((TimeStamp) getModules().getSnmp4jDemoMib().getSnmp4jDemoEntry()
                    .getColumn(Snmp4jDemoMib.idxSnmp4jDemoEntryCol4)).setSysUpTime(agent.getSysUpTime());
        }
        try {
            System.out.println("Register MOServer");
            getModules().registerMOs(getServer(), null);
            /*
             * Alternative methods
             */
            /*
             * System.out.println("Register MOScalar");
             * //register a scalar
             * MOScalar scalar = new MOScalar(
             * new OID("1.3.2.3.6.2.1.1.2"),
             * MOAccessImpl.ACCESS_READ_CREATE,
             * new OctetString("this is the a scalar registration")
             * );
             * getServer().register(scalar,null);
             * 
             * System.out.println("Register MOTable");
             * //register a table
             * MOTableSubIndex[] subIndex = new MOTableSubIndex[]{
             * new MOTableSubIndex(
             * new OID("1.3.2.3.6.2.1.1.1.2"),
             * SMIConstants.SYNTAX_OCTET_STRING,
             * 1,
             * 16
             * )
             * };
             * MOTableIndex index = new MOTableIndex(subIndex,true);
             * MOMutableColumn[] mutableColumns = new MOMutableColumn[]{
             * new MOMutableColumn(
             * 1,
             * SMIConstants.SYNTAX_INTEGER32,
             * MOAccessImpl.ACCESS_READ_CREATE,
             * new Integer32(10),
             * true
             * )
             * };
             * DefaultMOTable table = new DefaultMOTable(
             * new OID("1.3.2.3.6.2.1.1.1"),
             * index,
             * mutableColumns
             * );
             * getServer().register(table,null);
             */
            System.out.println("End register MIB");
        } catch (DuplicateRegistrationException e) {
            System.err.println("Error: duplicate registration server " + Arrays.toString(getServer().getContexts()));
        }
    }

    public void run() {
        System.out.println("Starting run agent...");
        getServer().addContext(getContext());
        System.out.println("Starting initialization of agent...");
        getAgent().initialize();
        System.out.println("End initialization of agent");
        System.out.println("Set SNMPv2MIB");
        SNMPv2MIB contextSNMPv2MIB = new SNMPv2MIB(new OctetString(), new OID(), new Integer32(0));
        try {
            contextSNMPv2MIB.registerMOs(getServer(), getContext());
        } catch (DuplicateRegistrationException e) {
            System.err.println("Error: duplicate registration on SNMP2vMIB");
        }
        System.out.println("Setup Proxy forwarder");
        getAgent().setupProxyForwarder();
        registerMIB();
        System.out.println("Set table size limits");
        getAgent().setTableSizeLimits(getTableSizeLimitsProperties(this.tableSizeLimitsFile));
        // automatically commit change to persistence storage if shutdown
        System.out.println("Register shutdown hook for persistence saving");
        getAgent().registerShutdownHook();
        System.out.println("Run...");
        getAgent().run();
    }
}
