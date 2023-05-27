package be.heh.agentsnmp.listener;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.*;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.fluent.TargetBuilder;
import org.snmp4j.mp.*;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.transport.TransportType;

import java.io.IOException;

public class RequestHandler implements CommandResponder {

    @Getter
    @Setter
    private Snmp snmp;
    @Getter
    @Setter
    private AgentConfigManager agentConfigManager;
    public RequestHandler(Snmp snmp,AgentConfigManager manager){
        setSnmp(snmp);
        setAgentConfigManager(manager);
    }
    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        PDU pdu = event.getPDU();
        System.out.println("received pdu : "+pdu);
        // Traitez le PDU et générez la réponse souhaitée
        PDU responsePdu = new PDU();
        responsePdu.setType(PDU.RESPONSE);
        responsePdu.setRequestID(pdu.getRequestID());

        // Ajoutez les variables de liaison (OID et valeur) à la réponse
        OID oid = pdu.getAll().get(0).getOid();
        responsePdu.add(new VariableBinding(oid, new OctetString("votre_valeur")));
        System.out.println("Response pdu : "+responsePdu);

        //Create community
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(event.getSecurityName()));
        target.setAddress(event.getPeerAddress());
        target.setRetries(3);
        target.setTimeout(5000);
        target.setVersion(SnmpConstants.version1);

        // Envoyez le ResponseEvent
        try {
            /*
            * Send the response PDU
            *
            * Methode 1 : with MessageDispatcher
            */
            /*
            ResponseEvent responseEvent = new ResponseEvent(event.getSource(),event.getPeerAddress(),pdu,responsePdu,null);
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
            */

            /*
            * Send PDu response
            *
            * Methode 2 : with Snmp component directly
            */
            getSnmp().send(responsePdu,target);
        } catch (Exception e) {
            System.err.println("Error sending response : "+e.getMessage());
        }

    }
}
