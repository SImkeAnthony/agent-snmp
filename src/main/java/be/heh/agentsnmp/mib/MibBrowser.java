package be.heh.agentsnmp.mib;


import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

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

    public MibBrowser(File mibFile) throws IOException {
        setMibFile(mibFile);
        String jsonContent = new String(Files.readAllBytes(Paths.get(getMibFile().getPath())), StandardCharsets.ISO_8859_1);
        setJsonObject(new JSONObject(jsonContent));
        completeMapAccess();
    }

    private void completeMapAccess(){
        getAccess().put("read-only",MOAccessImpl.ACCESS_READ_ONLY);
        getAccess().put("write-only",MOAccessImpl.ACCESS_WRITE_ONLY);
        getAccess().put("read-write",MOAccessImpl.ACCESS_READ_WRITE);
        getAccess().put("for-notify",MOAccessImpl.ACCESS_FOR_NOTIFY);
        getAccess().put("read-create",MOAccessImpl.ACCESS_READ_CREATE);
        getAccess().put("not-accessible",MOAccessImpl.ACCESS_READ_ONLY);
    }

    public List<MOVariable> formatJsonMibToMoVariables(){
        List<MOVariable> moVariables = new ArrayList<>();
        Iterator<String> keys = getJsonObject().keys();
        while (keys.hasNext()){
            JSONObject mo = getJsonObject().getJSONObject(keys.next());
            if(mo.has("class")){
                if(mo.get("class").equals("objecttype")){
                    //manage here
                    MOVariable moVariable = new MOVariable(mo.get("name").toString(),new OID(mo.get("oid").toString()), getAccess().get(mo.get("maxaccess")),new OctetString("default"),mo.get("description").toString());
                    moVariables.add(moVariable);
                }
            }
        }
        return moVariables;
    }

}
