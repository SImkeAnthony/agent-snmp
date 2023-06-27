package be.heh.agentsnmp.listener;

import be.heh.agentsnmp.manager.Manager;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.*;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.mo.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;

import java.util.*;

public class RequestHandler implements CommandResponder {

    @Getter
    @Setter
    private Snmp snmp;
    @Getter
    @Setter
    private AgentConfigManager agentConfigManager;
    @Getter
    @Setter
    private PDU responsePdu;
    @Getter
    @Setter
    private ScopedPDU responseScopedPdu;
    @Setter
    @Getter
    private List<Manager> managers = new ArrayList<>();
    public RequestHandler(Snmp snmp, AgentConfigManager manager, List<Manager> managers){
        setSnmp(snmp);
        setAgentConfigManager(manager);
        setResponsePdu(new PDU());
        setResponseScopedPdu(new ScopedPDU());
        setManagers(managers);
    }
    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        PDU pdu = event.getPDU();
        System.out.println("received pdu : "+pdu);
        switch (event.getPDU().getType()){
            case PDU.GET :
                getSnmpV1Handler(event);
                break;
            case PDU.GETNEXT:
                break;
            default:
                System.out.println("Your request is not supported yet");
        }
    }
    //here get information of OID in the PDU request
    private Set<VariableBinding> getVariablesOIDs(PDU pdu) {
        Set<VariableBinding> variableBindings = new HashSet<>(); //use set to avoid duplicated variableBinding
        pdu.getVariableBindings().forEach(variableBinding -> {
            if(getVariableByOid(variableBinding.getOid().format(),pdu.getVariableBindings())!=null){
                variableBindings.add(new VariableBinding(variableBinding.getOid(),getVariableByOid(variableBinding.getOid().format(),pdu.getVariableBindings())));
            }
        });
        return variableBindings;
    }

    private void getSnmpV1Handler(CommandResponderEvent event){
        // manage pdu and generate desired response
        getResponsePdu().setType(PDU.RESPONSE);
        getResponsePdu().setRequestID(event.getPDU().getRequestID());

        // add variables bindings (OID and value) at the response
        Set<VariableBinding> workedSet = getVariablesOIDs(event.getPDU());
        for(VariableBinding variableBinding : workedSet){
            System.out.println("variableBinding : "+variableBinding);
            getResponsePdu().add(variableBinding);
        }
        System.out.println("Response pdu : "+getResponsePdu());

        // Send the ResponseEvent
        try {
            ResponseEvent responseEvent = new ResponseEvent(event.getSource(),event.getPeerAddress(),event.getPDU(),getResponsePdu(),null);
            getSnmp().getMessageDispatcher().returnResponsePdu(
                    event.getMessageProcessingModel(),
                    event.getSecurityModel(),
                    event.getSecurityName(),
                    event.getSecurityLevel(),
                    responseEvent.getResponse(),
                    event.getMaxSizeResponsePDU(),
                    event.getStateReference(),
                    new StatusInformation()
            );
            getResponsePdu().clear();
        } catch (Exception e) {
            System.err.println("Error sending response : "+e.getMessage());
        }
    }
    private Manager getManagerByOid(String oid){
        for(Manager manager : getManagers()){
            if(oid.contains(manager.getIdentity().getOid().format())){
                return manager;
            }
        }
        return null;
    }

    private Variable getVariableByOid(String oid,List<? extends VariableBinding> variableBindings){
        try{
            Variable variable = null;
            for(MOScalar moScalar : Objects.requireNonNull(getManagerByOid(oid)).getMOScalars()){
                if(moScalar.getOid().format().equals(oid)){
                    variable = moScalar.getValue();
                }
            }
            //if not found in moScalars
            if(variable==null){
                if(indexIsInVariables(variableBindings)){
                    variable = getVariableInTable(oid,variableBindings);
                }
            }
            return variable;
        }catch (NullPointerException e){
            System.err.println("Error to get variable : "+e.getMessage());
            return null;
        }
    }

    private boolean indexIsInVariables(List<? extends  VariableBinding> variableBindings){
        for(Manager manager : getManagers()){
            for(DefaultMOTable defaultMOTable : manager.getMOTables()){
                for (VariableBinding variableBinding : variableBindings){
                    if(variableBinding.getOid().format().equals(defaultMOTable.getOID().format()+".1.1")){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private int getIndexVariablesInVariablesByOidExpected(String oidIndexExpected,List<? extends VariableBinding> variableBindings) {
        for(VariableBinding variableBinding : variableBindings){
           if(variableBinding.getOid().format().equals(oidIndexExpected)){
               return variableBinding.getVariable().toInt();
           }
        }
        return -1;
    }

    private Variable getVariableInTable(String oid,List<? extends VariableBinding> variableBindings){
        try{
            Variable variable = null;
            for(DefaultMOTable defaultMOTable : Objects.requireNonNull(getManagerByOid(oid).getMOTables())){
                if(oid.contains(defaultMOTable.getID().format())){
                    String oidIndexExpected = defaultMOTable.getOID().format()+".1.1";
                    int index = getIndexVariablesInVariablesByOidExpected(oidIndexExpected,variableBindings);
                    int column = Integer.parseInt(oid.split("\\.")[oid.split("\\.").length-1]);
                    if(index !=-1){
                        try{
                            System.out.println( "found ("+index+";"+column+") => "+defaultMOTable.getValue(new OID(String.valueOf(index)),column));
                            //variable = defaultMOTable.getValue(new OID(String.valueOf(index)),column);
                        }catch (Exception e){
                            System.err.println("Error to get variable in table : "+e.getMessage());
                        }
                    }else {
                        System.err.println("Error index not found in pdu variable bindings");
                    }
                }
            }
            return variable;
        }catch (NullPointerException e){
            System.err.println("Error to get variable in table : "+e.getMessage());
            return null;
        }
    }
}
