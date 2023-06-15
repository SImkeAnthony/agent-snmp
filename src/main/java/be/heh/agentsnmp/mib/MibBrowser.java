package be.heh.agentsnmp.mib;

import lombok.Getter;
import lombok.Setter;
import net.percederberg.mibble.*;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MibBrowser {

    @Getter
    MibLoader mibLoader = new MibLoader();
    @Getter
    @Setter
    File mibFile;
    @Getter
    @Setter
    Mib mib;

    public MibBrowser(File mibFile){
        setMibFile(mibFile);
        getMibLoader().addDir(mibFile.getParentFile());
        setMib(loadMib());
    }
    private Mib loadMib(){
        try {
            System.out.println("load mib file ...");
            return getMibLoader().load(getMibFile());
        }catch (IOException | MibLoaderException e){
            System.err.println("Error to load mibFile : "+e.getMessage());
            return null;
        }
    }
    public void displayMibInfo(){
        try {
            Iterator symbols = Objects.requireNonNull(loadMib()).getAllSymbols().iterator();
            MibSymbol symbol;
            while (symbols.hasNext()){
                symbol = (MibSymbol) symbols.next();
                System.out.println("mib-value : "+symbol.getName()+" : "+extractOid(symbol));
            }
        }catch (NullPointerException e){
            System.err.println("Error to display MIB info : "+e.getMessage());
        }
    }

    private ObjectIdentifierValue extractOid(MibSymbol symbol){
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                return (ObjectIdentifierValue) value;
            }
        }
        return null;
    }
}
