package be.heh.agentsnmp.mib;


import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MibBrowser {
    @Getter
    @Setter
    File mibFile;
    @Getter
    @Setter
    JSONObject jsonObject;
    @Getter
    Map<String,MOAccess> access = new HashMap();
    @Getter
    Map<String, Integer> syntax = new HashMap();


    public MibBrowser(File mibFile) throws IOException {
        setMibFile(mibFile);
        String jsonContent = new String(Files.readAllBytes(Paths.get(getMibFile().getPath())), StandardCharsets.ISO_8859_1);
        setJsonObject(new JSONObject(jsonContent));
        completeMapAccess();
        completeMapSyntax();
    }

    private void completeMapAccess(){
        getAccess().put("read-only",MOAccessImpl.ACCESS_READ_ONLY);
        getAccess().put("write-only",MOAccessImpl.ACCESS_WRITE_ONLY);
        getAccess().put("read-write",MOAccessImpl.ACCESS_READ_WRITE);
        getAccess().put("for-notify",MOAccessImpl.ACCESS_FOR_NOTIFY);
        getAccess().put("read-create",MOAccessImpl.ACCESS_READ_CREATE);
        getAccess().put("not-accessible",MOAccessImpl.ACCESS_FOR_NOTIFY);
    }
    private void completeMapSyntax(){
        getSyntax().put("OCTET STRING",SMIConstants.SYNTAX_OCTET_STRING);
        getSyntax().put("DOUBLE",SMIConstants.SYNTAX_OCTET_STRING);
        getSyntax().put("INTEGER",SMIConstants.SYNTAX_INTEGER32);
    }

    public List<MOVariable> getAllMOVariables(){
        List<MOVariable> moVariables = new ArrayList<>();
        Iterator<String> keys = getJsonObject().keys();
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype") && mo.get("nodetype").equals("scalar")){
                    //manage here
                    MOVariable moVariable = new MOVariable(mo.get("name").toString(),new OID(mo.get("oid").toString()), getAccess().get(mo.get("maxaccess")),mo.get("description").toString());
                    moVariables.add(moVariable);
                }
            }
        }
        return moVariables;
    }

    public List<MOVariable> getMOVariablesBySubTree(OID oidIdentity){
        List<MOVariable> moVariables = new ArrayList<>();
        Iterator<String> keys = getJsonObject().keys();
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype") && mo.get("nodetype").equals("scalar") && mo.get("oid").toString().contains(oidIdentity.toString())){
                    //manage here
                    MOVariable moVariable = new MOVariable(mo.get("name").toString(),new OID(mo.get("oid").toString()), getAccess().get(mo.get("maxaccess")),mo.get("description").toString());
                    moVariables.add(moVariable);
                }
            }
        }
        return moVariables;
    }
    public List<DefaultMOTable> getMOTableBySubTree(OID oidIdentity){
        List<DefaultMOTable> defaultMOTables = new ArrayList<>();
        MOTableSubIndex[] moTableSubIndexes = new MOTableSubIndex[]{
                new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER)
        };
        Iterator<String> keys = getJsonObject().keys();
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype") && mo.get("nodetype").equals("table") && mo.get("oid").toString().contains(oidIdentity.toString())){
                    defaultMOTables.add(new DefaultMOTable(
                            new OID(mo.get("oid").toString()),
                            new MOTableIndex(moTableSubIndexes,false),
                            getColumnBySubTree(getOIDEntryByOIDTable(new OID(mo.get("oid").toString())))
                    ));
                }
            }
        }
        return defaultMOTables;
    }

    private OID getOIDEntryByOIDTable(OID oidTable){
        Iterator<String> keys = getJsonObject().keys();
        int colId = 1;
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype") && mo.get("nodetype").equals("row") && mo.get("oid").toString().contains(oidTable.toString())){
                    return new OID(mo.get("oid").toString());
                }
            }
        }
        return new OID("");
    }

    private MOMutableColumn[] getColumnBySubTree(OID oidIdentity){
        List<MOMutableColumn> moMutableColumns = new ArrayList<>();
        Iterator<String> keys = getJsonObject().keys();
        int colId = 1;
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype") && mo.get("nodetype").equals("column") && mo.get("oid").toString().contains(oidIdentity.toString())){
                    moMutableColumns.add(new MOMutableColumn(
                            colId,
                            getSyntax().get(mo.get("syntax").toString()),
                            getAccess().get(mo.get("maxaccess").toString())
                    ));
                    colId++;
                }
            }
        }
        return moMutableColumns.toArray(MOMutableColumn[]::new);
    }

}
