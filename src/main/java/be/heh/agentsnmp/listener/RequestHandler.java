package be.heh.agentsnmp.listener;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.*;
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
    private String ipAddress;
    @Getter
    @Setter
    private int port;
    @Getter
    @Setter
    private String communityString;
    public RequestHandler(Snmp snmp,String ipAddress, int port,String communityString){
        setSnmp(snmp);
        setIpAddress(ipAddress);
        setPort(port);
        setCommunityString(communityString);
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

        // Créez un ResponseEvent à partir de l'événement de commande
        ResponseEvent<A> responseEvent = new ResponseEvent<>(this, event.getPeerAddress(), pdu, responsePdu,null);

        // Envoyez le ResponseEvent
        try {
            // Envoyez la réponse
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
        } catch (Exception e) {
            System.err.println("Error sending response : "+e.getMessage());
        }

    }
}
