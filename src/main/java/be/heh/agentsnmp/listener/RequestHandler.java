package be.heh.agentsnmp.listener;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.*;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public RequestHandler(Snmp snmp,AgentConfigManager manager){
        setSnmp(snmp);
        setAgentConfigManager(manager);
        setResponsePdu(new PDU());
        setResponseScopedPdu(new ScopedPDU());
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
        Set<VariableBinding> variableBindings = new HashSet<>();
        List<? extends VariableBinding> workedList = pdu.getVariableBindings();
        for(VariableBinding variableBinding : workedList){
            Variable variable = getAgentConfigManager().getVariable(variableBinding.getOid().format());
            VariableBinding responseVariableBinding = new VariableBinding(variableBinding.getOid(),variable);
            variableBindings.add(responseVariableBinding);
        }
        return variableBindings;
    }

    private void getSnmpV1Handler(CommandResponderEvent event){
        // Traitez le PDU et générez la réponse souhaitée
        getResponsePdu().setType(PDU.RESPONSE);
        getResponsePdu().setRequestID(event.getPDU().getRequestID());

        // Ajoutez les variables de liaison (OID et valeur) à la réponse
        Set<VariableBinding> workedSet = getVariablesOIDs(event.getPDU());
        for(VariableBinding variableBinding : workedSet){
            getResponsePdu().add(variableBinding);
        }
        System.out.println("Response pdu : "+getResponsePdu());

        //Create community
        /*
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(event.getSecurityName()));
        target.setAddress(event.getPeerAddress());
        target.setRetries(3);
        target.setTimeout(5000);
        target.setVersion(SnmpConstants.version1);
        */

        // Envoyez le ResponseEvent
        try {
            /*
             * Send the response PDU
             *
             * Methode 1 : with MessageDispatcher
             */

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

            /*
             * Send PDu response
             *
             * Methode 2 : with Snmp component directly not working on another NIC of loopback
             */
            //getSnmp().send(getResponsePdu(),target);
            getResponsePdu().clear();
        } catch (Exception e) {
            System.err.println("Error sending response : "+e.getMessage());
        }
    }
}
