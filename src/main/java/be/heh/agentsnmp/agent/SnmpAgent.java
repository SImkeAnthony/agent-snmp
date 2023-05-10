package be.heh.agentsnmp.agent;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;

import java.io.File;
import java.io.IOException;

public class SnmpAgent extends BaseAgent {
    private String serverAddress;

    public SnmpAgent(String ipAddress) throws IOException{

        /*
        * Create a Snmp Agent with a config file, boot counter file and command processor to handle the SNMP request
        * Parameters :
        * -> config file : is a file to register informations about configuration of the agent
        * -> boot counter file : this file register informations about the number of boot's agent
        */
        super(new File("conf.agent"),new File("bootCounter.agent"), new CommandProcessor(
                new OctetString(MPv3.createLocalEngineID())
        ));
        this.serverAddress=ipAddress;
    }

    //add community to security name
    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[]{
                new OctetString("public"),
                new OctetString("cpublic"), //security name
                getAgent().getContextEngineID(), //local engine ID
                new OctetString("public"), //default context name
                new Integer32(StorageType.nonVolatile), //storage type
                new Integer32(RowStatus.active), //row status
        };

        SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
                new OctetString("public2public").toSubIndex(true),com2sec
        );
        communityMIB.getSnmpCommunityEntry().addRow(row);
    }

    // Add initial notification target and filters
    @Override
    protected void addNotificationTargets(SnmpTargetMIB targetMIB, SnmpNotificationMIB notificationMIB) {
        // TODO: 10-05-23  
    }

    // Add users to the USM
    @Override
    protected void addUsmUser(USM usm) {
        // TODO: 10-05-23
    }

    // Add VACM configuration
    @Override
    protected void addViews(VacmMIB vacm){
        vacm.addGroup(
                SecurityModel.SECURITY_MODEL_SNMPv2c,
                new OctetString("cpublic"),
                new OctetString("v1v2group"),
                StorageType.nonVolatile
        );
        vacm.addAccess(
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
        vacm.addViewTreeFamily(
                new OctetString("fullReadView"),
                new OID("1.3"),
                new OctetString(),
                VacmMIB.vacmViewExcluded,
                StorageType.nonVolatile
        );
    }

    //Unregister the basic MIB modules from the agent's MOServer (managed object server)
    @Override
    protected void unregisterManagedObjects(){
        // TODO: 10-05-23
    }

    //Register additional managed object at the agent's server
    @Override
    protected void registerManagedObjects(){
        // TODO: 10-05-23
    }

    protected void initTransportMapping() throws IOException{
        transportMappings = new TransportMapping[1];
        Address addr = GenericAddress.parse(serverAddress);
        TransportMapping tm = TransportMappings.getInstance().createTransportMapping(addr);
        transportMappings[0] = tm;
    }

    // Start method invokes some initialization methods needed to start agent
    public void start() throws IOException{
        init();
        addShutdownHook();
        getServer().addContext(new OctetString("public"));
        finishInit();
        run();
        sendColdStartNotification();
    }

    //Clients can register the Managed objects they need from the MOCreator class
    public void registerManagedObject(ManagedObject mo){
        try {
            server.register(mo,null);
        }catch (DuplicateRegistrationException e){
            System.out.println("new runtime error : duplicate registration manage object : "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void unregisterManagedObject(MOGroup group){
        group.unregisterMOs(server,getContext(group));
    }
}
