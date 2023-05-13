package be.heh.agentsnmp.mo;

import org.snmp4j.agent.io.*;
import org.snmp4j.smi.Variable;

import java.io.IOException;
public class Input implements MOInput {
    @Override
    public int getImportMode() {
        return 0;
    }

    @Override
    public Context readContext() throws IOException {
        return null;
    }

    @Override
    public void skipContext(Context context) throws IOException {

    }

    @Override
    public MOInfo readManagedObject() throws IOException {
        return null;
    }

    @Override
    public void skipManagedObject(MOInfo mo) throws IOException {

    }

    @Override
    public Variable readVariable() throws IOException {
        return null;
    }

    @Override
    public Sequence readSequence() throws IOException {
        return null;
    }

    @Override
    public IndexedVariables readIndexedVariables() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
