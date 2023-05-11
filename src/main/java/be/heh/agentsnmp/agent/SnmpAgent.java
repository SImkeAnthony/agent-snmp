package be.heh.agentsnmp.agent;

import jdk.jfr.Unsigned;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.io.ImportMode;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.ext.AgentppSimulationMib;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.mo.snmp4j.example.Snmp4jHeartbeatMib;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ThreadPool;

import java.io.File;
import java.io.IOException;

public class SnmpAgent extends BaseAgent {
    @Getter
    @Setter
    private String address;
    @Getter
    @Setter
    private Snmp4jHeartbeatMib snmp4jHeartbeatMib;
    @Getter
    @Setter
    private AgentppSimulationMib agentppSimulationMib;

    public SnmpAgent(CommandProcessor commandProcessor) throws IOException {
        super(new File("BootCounter.cfg"),new File("AgentConfig.cfg"),commandProcessor);
        agent.setWorkerPool(ThreadPool.create("requestPool",4));
    }


    @Override
    protected void registerManagedObjects() {
        try {
            server.register(createStaticIfTable(),null);
            server.register(createTableIfxTable(),null);
            getAgentppSimulationMib().registerMOs(server,null);
            getSnmp4jHeartbeatMib().registerMOs(server,null);
        } catch (DuplicateRegistrationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void unregisterManagedObjects() {

        //===========================================================//
        //Here you can unregister these objects previously registered//
        //===========================================================//
    }

    @Override
    protected void addNotificationTargets(SnmpTargetMIB targetMIB, SnmpNotificationMIB notificationMIB) {

        targetMIB.addDefaultTDomains();

        /*
        * SNMP :  version 2c
        */
        targetMIB.addTargetAddress(
                new OctetString("notificationv2c"),
                TransportDomains.transportDomainUdpIpv4,
                new OctetString(new UdpAddress("127.0.0.1/162").getValue()),
                200,
                1,
                new OctetString("notify"),
                new OctetString("v2c"),
                StorageType.permanent
        );

        targetMIB.addTargetParams(
                new OctetString("v2c"),
                MessageProcessingModel.MPv2c,
                SecurityModel.SECURITY_MODEL_SNMPv2c,
                new OctetString("cpublic"),
                SecurityLevel.AUTH_PRIV,
                StorageType.permanent
        );

        /*
        * SNMP : version 3
        */
        targetMIB.addTargetAddress(
                new OctetString("notificationv3"),
                TransportDomains.transportDomainUdpIpv4,
                new OctetString(new UdpAddress("127.0.0.1/1162").getValue()),
                200,
                1,
                new OctetString("notify"),
                new OctetString("v3notify"),
                StorageType.permanent
        );

        targetMIB.addTargetParams(
                new OctetString("v3"),
                MessageProcessingModel.MPv3,
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("v3notify"),
                SecurityLevel.NOAUTH_NOPRIV,
                StorageType.permanent
        );

        /*
        * Default
        */
        notificationMIB.addNotifyEntry(
                new OctetString("default"),
                new OctetString("notify"),
                SnmpNotificationMIB.SnmpNotifyTypeEnum.inform,
                StorageType.permanent
        );
    }

    @Override
    protected void addViews(VacmMIB vacmMIB) {

        //              Group configuration                //
        //-------------------------------------------------//
        //=================================================//
        //          All Configuration possible             //
        //=================================================//
        /*
        * SNMP : version 1
        */
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_SNMPv1,
                new OctetString("cpublic"),
                new OctetString("v1v2group"),
                StorageType.nonVolatile
        );

        /*
         * SNMP : version 2c
         */
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_SNMPv2c,
                new OctetString("cpublic"),
                new OctetString("v1v2group"),
                StorageType.nonVolatile
        );

        /*
         * SNMP : version 3
         */

        // -> SHA256 + DES
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("SHADES"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> MD5 + DES
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("MD5DES"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> test
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("TEST"),
                new OctetString("v3test"),
                StorageType.nonVolatile
        );
        // -> SHA256
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("SHA"),
                new OctetString("v3restricted"),
                StorageType.nonVolatile
        );
        // -> SHA256 + AES128
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("SHAAES128"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> SHA256 + AES192
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("SHAAES192"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> SHA256 + AES256
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("SHAAES256"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> MD5 + AES128
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("MD5AES128"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> MD5 + AES192
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("MD5AES192"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        // -> MD5 + AES256
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("MD5AES256"),
                new OctetString("v3group"),
                StorageType.nonVolatile
        );
        //=================================================//


        //=================================================//
        //            Personal configuration               //
        //=================================================//
        /*
        * SNMP : version 3
        */
        // anthony-auth-priv : MD5 + DES
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_USM,
                new OctetString("anthony"),  //security name => see server
                new OctetString("v3group"),  //group name => see server
                StorageType.nonVolatile
        );
        //=================================================//


        //             Access Configuration                //
        //-------------------------------------------------//
        //=================================================//
        //-> group v1v2group : not necessary
        vacmMIB.addAccess(
                new OctetString("v1v2group"),
                new OctetString("public"),
                SecurityModel.SECURITY_MODEL_ANY,
                SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString("fullReadView"),
                new OctetString("fullWriteView"),
                new OctetString("fullNotifyView"),
                StorageType.nonVolatile
        );
        //-> group v3test : not necessary
        vacmMIB.addAccess(
                new OctetString("v3test"),
                new OctetString(),
                SecurityModel.SECURITY_MODEL_USM,
                SecurityLevel.AUTH_PRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString("testReadView"),
                new OctetString("testWriteView"),
                new OctetString("testNotifyView"),
                StorageType.nonVolatile
        );
        //-> group v3restricted : not necessary
        vacmMIB.addAccess(
                new OctetString("v3restricted"),
                new OctetString(),
                SecurityModel.SECURITY_MODEL_USM,
                SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString("restrictedReadView"),
                new OctetString("restrictedWriteView"),
                new OctetString("restrictedNotifyView"),
                StorageType.nonVolatile
        );
        //-> group v3group
        vacmMIB.addAccess(
                new OctetString("v3group"),
                new OctetString(),
                SecurityModel.SECURITY_MODEL_USM,
                SecurityLevel.AUTH_PRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString("fullReadView"),
                new OctetString("fullWriteView"),
                new OctetString("fullNotifyView"),
                StorageType.nonVolatile
        );
        //=================================================//

        //                   Add Views                     //
        //-------------------------------------------------//
        //=================================================//
        /*
        * TestView : not necessary
        */
        //-> read included
        vacmMIB.addViewTreeFamily(
                new OctetString("testReadView"),
                new OID("1.3.6.1.2"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> read excluded
        vacmMIB.addViewTreeFamily(
                new OctetString("testReadView"),
                new OID("1.3.6.1.2.1.1"),
                new OctetString(),
                VacmMIB.vacmViewExcluded,
                StorageType.nonVolatile
        );
        //-> write
        vacmMIB.addViewTreeFamily(
                new OctetString("testWriteView"),
                new OID("1.3.6.1.2.1"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> notify
        vacmMIB.addViewTreeFamily(
                new OctetString("testNotifyView"),
                new OID("1.3.6.1.2"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );

        /*
        * RestrictedView : not necessary
        */
        //-> read
        vacmMIB.addViewTreeFamily(
                new OctetString("restrictedReadView"),
                new OID("1.3.6.1.2"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> write
        vacmMIB.addViewTreeFamily(
                new OctetString("restrictedWriteView"),
                new OID("1.3.6.1.2.1"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> notify 1
        vacmMIB.addViewTreeFamily(
                new OctetString("restrictedNotifyView"),
                new OID("1.3.6.1.2"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> notify 2
        vacmMIB.addViewTreeFamily(
                new OctetString("restrictedNotifyView"),
                new OID("1.3.6.1.6.3.1"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );

        /*
        * fullView
        */
        //-> read
        vacmMIB.addViewTreeFamily(
                new OctetString("fullReadView"),
                new OID("1.3"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> write
        vacmMIB.addViewTreeFamily(
                new OctetString("fullWriteView"),
                new OID("1.3"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //-> notify
        vacmMIB.addViewTreeFamily(
                new OctetString("fullNotifyView"),
                new OID("1.3"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
        //=================================================//
    }

    @Override
    protected void addUsmUser(USM usm) {

        //======================================================//
        // Here add your usm user configured in the server SNMP //
        //======================================================//

        //-> see configuration in the SNMP server
        UsmUser user = new UsmUser(
                new OctetString("anthony"),
                AuthMD5.ID,
                new OctetString("Silver-Major-Knight-16"),
                PrivDES.ID,
                new OctetString("Silver-Major-Knight-16")
        );
        usm.addUser(user.getSecurityName(),usm.getLocalEngineID(),user);
    }
    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[]{
                new OctetString(),                      //community name
                new OctetString("anthony"),   //security name
                getAgent().getContextEngineID(),       //local engine ID
                new OctetString("public"),    //default context name
                new OctetString(),                      //transport tag
                new Integer32(StorageType.nonVolatile), //storage type
                new Integer32(RowStatus.active)         //row status
        };

        SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
                new OctetString("public2public").toSubIndex(true),
                com2sec
        );
        communityMIB.getSnmpCommunityEntry().addRow(row);
        //snmpCommunityMIB.setSourceAddressFiltering(true);
    }

    protected void registerSnmpMIBs(){
        setSnmp4jHeartbeatMib(
                new Snmp4jHeartbeatMib(
                        super.getNotificationOriginator(),
                        new OctetString(),
                        super.snmpv2MIB.getSysUpTime()
                )
        );
        setAgentppSimulationMib(new AgentppSimulationMib());
        super.registerSnmpMIBs();
    }

    protected void initTransportMappings() throws IOException {
        transportMappings = new TransportMapping[2];
        Address addr = GenericAddress.parse(getAddress());
        TransportMapping tm = TransportMappings.getInstance().createTransportMapping(addr);
        transportMappings[0] = tm;
        transportMappings[1] = new DefaultTcpTransportMapping(new TcpAddress(getAddress()));
    }
    protected void initMessageDispatcher(){
        this.dispatcher = new MessageDispatcherImpl();
        this.mpv3 = new MPv3(this.agent.getContextEngineID().getValue());
        this.usm = new USM(SecurityProtocols.getInstance(),this.agent.getContextEngineID(),this.updateEngineBoots());
        SecurityModels.getInstance().addSecurityModel(this.usm);
        SecurityProtocols.getInstance().addDefaultProtocols();
        this.dispatcher.addMessageProcessingModel(new MPv1());
        this.dispatcher.addMessageProcessingModel(new MPv2c());
        this.dispatcher.addMessageProcessingModel(new MPv3());
        this.initSnmpSession();
    }

    private static DefaultMOTable createTableIfxTable(){
        MOTableSubIndex[] subIndexes =new MOTableSubIndex[]{new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER)};
        MOTableIndex indexDef = new MOTableIndex(subIndexes,false);

        MOColumn[] columns = new MOColumn[19];
        int c = 0;

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_OCTET_STRING,MOAccessImpl.ACCESS_READ_ONLY); //ifName

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifInMulticastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifInBroadcastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifOutMulticastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifOutBroadcastPkts

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCInOctets
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCInUnicastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCInMulticastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCInBroadcastPkts

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCOutOctets
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCOutUnicastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCOutMulticastPkts
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_COUNTER32,MOAccessImpl.ACCESS_READ_ONLY); //ifHCOutBroadcastPkts

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_WRITE); //ifLinkUpDownTrapEnable

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_GAUGE32,MOAccessImpl.ACCESS_READ_ONLY); //ifHighSpeed

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_WRITE); //ifPromiscuousMode

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY); //ifConnectorPresent

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_OCTET_STRING,MOAccessImpl.ACCESS_READ_WRITE); //ifAlias

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_TIMETICKS,MOAccessImpl.ACCESS_READ_ONLY); //ifCounterDiscontinuityTime

        DefaultMOTable ifXTable = new DefaultMOTable(new OID(".1.3.6.1.2.1.31.1.1.1"),indexDef,columns);
        MOMutableTableModel model = (MOMutableTableModel) ifXTable.getModel();
        Variable[] rowValues1 = new Variable[]{
                new OctetString("Ethernet-0"),
                new Integer32(1),
                new Integer32(2),
                new Integer32(3),
                new Integer32(4),
                new Integer32(5),
                new Integer32(6),
                new Integer32(7),
                new Integer32(8),
                new Integer32(9),
                new Integer32(10),
                new Integer32(11),
                new Integer32(12),
                new Integer32(13),
                new Integer32(14),
                new Integer32(15),
                new Integer32(16),
                new OctetString("My Eth"),
                new TimeTicks(1000)
        };

        Variable[] rowValues2 = new Variable[] {
                new OctetString("Loopback"),
                new Integer32(21),
                new Integer32(22),
                new Integer32(23),
                new Integer32(24),
                new Integer32(25),
                new Integer32(26),
                new Integer32(27),
                new Integer32(28),
                new Integer32(29),
                new Integer32(30),
                new Integer32(31),
                new Integer32(32),
                new Integer32(33),
                new Integer32(34),
                new Integer32(35),
                new Integer32(36),
                new OctetString("My loop"),
                new TimeTicks(2000)
        };

        model.addRow(new DefaultMOMutableRow2PC(new OID("1"),rowValues1));
        model.addRow(new DefaultMOMutableRow2PC(new OID("2"),rowValues2));
        ifXTable.setVolatile(true);
        return ifXTable;
    }

    private static DefaultMOTable createStaticIfTable(){
        MOTableSubIndex[] subIndexes = new MOTableSubIndex[]{new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER)};
        MOTableIndex indexDef = new MOTableIndex(subIndexes,false);
        MOColumn[] columns = new MOColumn[8];

        int c = 0;

        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY); //ifIndex
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_OCTET_STRING,MOAccessImpl.ACCESS_READ_ONLY); //ifDescr
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY); //ifType
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY); //ifMtu
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_GAUGE32,MOAccessImpl.ACCESS_READ_ONLY); //ifSpeed
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_OCTET_STRING,MOAccessImpl.ACCESS_READ_ONLY); //ifPhysAddress
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_WRITE); //ifAdminStatus
        columns[c++] = new MOColumn(c,SMIConstants.SYNTAX_INTEGER,MOAccessImpl.ACCESS_READ_ONLY); //ifOperStatus

        DefaultMOTable ifTable = new DefaultMOTable(new OID("1.3.6.1.2.1.2.2.1"),indexDef,columns);
        MOMutableTableModel model = (MOMutableTableModel) ifTable.getModel();

        Variable[] rowValues1 = new Variable[]{
                new Integer32(1),
                new OctetString("eth0"),
                new Integer32(6),
                new Integer32(1500),
                new Gauge32(100000000),
                new OctetString("00:00:00:00:01"),
                new Integer32(1),
                new Integer32(1)
        };
        Variable[] rowValues2 = new Variable[]{
                new Integer32(2),
                new OctetString("loopback"),
                new Integer32(24),
                new Integer32(1500),
                new Gauge32(10000000),
                new OctetString("00:00:00:00:02"),
                new Integer32(1),
                new Integer32(1)
        };

        model.addRow(new DefaultMOMutableRow2PC(new OID("1"),rowValues1));
        model.addRow(new DefaultMOMutableRow2PC(new OID("2"),rowValues2));
        ifTable.setVolatile(true);
        return ifTable;
    }

    private static DefaultMOTable createStaticSnmp4sTable(){
        MOTableSubIndex[] subIndexes = new MOTableSubIndex[] { new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER) };
        MOTableIndex indexDef = new MOTableIndex(subIndexes, false);

        MOColumn[] columns = new MOColumn[8];

        int c = 0;

        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_NULL, MOAccessImpl.ACCESS_READ_ONLY); //testNull
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY); //testBoolean
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY); //ifType
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY); //ifMtu
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_GAUGE32, MOAccessImpl.ACCESS_READ_ONLY); //ifSpeed
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY); //ifPhysAddress
        columns[c++] = new MOMutableColumn(c, SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_WRITE, null); //ifAdminStatus
        columns[c++] = new MOColumn(c, SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY); //ifOperStatus

        DefaultMOTable ifTable = new DefaultMOTable(new OID("1.3.6.1.4.1.50000.1.1"), indexDef, columns);
        MOMutableTableModel model = (MOMutableTableModel) ifTable.getModel();

        Variable[] rowValues1 = new Variable[] {
                new Integer32(1),
                new OctetString("eth0"),
                new Integer32(6),
                new Integer32(1500),
                new Gauge32(100000000),
                new OctetString("00:00:00:00:01"),
                new Integer32(1),
                new Integer32(1)
        };

        Variable[] rowValues2 = new Variable[] {
                new Integer32(2),
                new OctetString("loopback"),
                new Integer32(24),
                new Integer32(1500),
                new Gauge32(10000000),
                new OctetString("00:00:00:00:02"),
                new Integer32(1),
                new Integer32(1)
        };

        model.addRow(new DefaultMOMutableRow2PC(new OID("1"), rowValues1));
        model.addRow(new DefaultMOMutableRow2PC(new OID("2"), rowValues2));
        ifTable.setVolatile(true);
        return ifTable;
    }
    //===========================//
    //        Method Start       //
    //===========================//

    public void start(String ip, int port) throws IOException{
        if(port>0 && port<=65535){
            setAddress(ip+"/"+port);
            init();
            addShutdownHook();
            loadConfig(ImportMode.REPLACE_CREATE);
            getServer().addContext(new OctetString("public"));
            finishInit();
            run();
            sendColdStartNotification();
        }else{
            System.out.println("Error port invalid");
        }
    }
}
