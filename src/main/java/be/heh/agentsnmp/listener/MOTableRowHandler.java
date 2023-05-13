package be.heh.agentsnmp.listener;

import be.heh.agentsnmp.agent.SnmpAgent;
import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.AgentConfigManager;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.example.Snmp4jDemoMib;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.TimeStamp;
import org.snmp4j.smi.*;

public class MOTableRowHandler implements MOTableRowListener<Snmp4jDemoMib.Snmp4jDemoEntryRow> {

    @Getter
    @Setter
    private Modules modules;
    @Getter
    @Setter
    private AgentConfigManager agent;

    public MOTableRowHandler(Modules modules, AgentConfigManager agent){
        setModules(modules);
        setAgent(agent);
    }

    @Override
    public void rowChanged(MOTableRowEvent<Snmp4jDemoMib.Snmp4jDemoEntryRow> event) {
        if((event.getType()==MOTableRowEvent.CREATE)||(event.getType()==MOTableRowEvent.UPDATED)){
            //ignore
            return;
        }
        Counter32 counter = (Counter32) event.getRow().getValue(Snmp4jDemoMib.colSnmp4jDemoEntryCol3);
        if(counter==null){
            counter = new Counter32(0);
            ((MOMutableTableRow) event.getRow()).setValue(Snmp4jDemoMib.colSnmp4jDemoEntryCol3,counter);
        }
        counter.increment();
        //update TimeStamp
        TimeStamp timeStamp = (TimeStamp) event.getTable().getColumn(Snmp4jDemoMib.colSnmp4jDemoEntryCol4);
        timeStamp.update((MOMutableTableRow) event.getRow(),Snmp4jDemoMib.colSnmp4jDemoEntryCol4);
        //fire notification
        Integer32 type = new Integer32(Snmp4jDemoMib.Snmp4jDemoTableRowModificationEnum.updated);
        switch (event.getType()){
            case MOTableRowEvent.ADD :
                type.setValue(Snmp4jDemoMib.Snmp4jDemoTableRowModificationEnum.created);
                break;
            case MOTableRowEvent.DELETE:
                type.setValue(Snmp4jDemoMib.Snmp4jDemoTableRowModificationEnum.deleted);
                break;
            default:
                System.out.println("Event type "+event.getType()+" is not supported");
        }

        VariableBinding[] payload = new VariableBinding[2];
        OID table = event.getTable().getOID();
        OID updateCount = new OID(table);
        updateCount.append(Snmp4jDemoMib.colSnmp4jDemoEntryCol3);
        updateCount.append(event.getRow().getIndex());

        OID modifyType = new OID(table);
        modifyType.append(Snmp4jDemoMib.colSnmp4jDemoTableRowModification);
        modifyType.append(event.getRow().getIndex());

        payload[0] = new VariableBinding(updateCount,counter);
        payload[1] = new VariableBinding(modifyType,type);
        getModules().getSnmp4jDemoMib().snmp4jDemoEvent(
            getAgent().getNotificationOriginator(),
                new OctetString(),
                payload
        );
    }
}
