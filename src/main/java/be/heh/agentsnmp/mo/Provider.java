package be.heh.agentsnmp.mo;

import org.snmp4j.agent.io.MOPersistenceProvider;
import org.snmp4j.agent.mo.MOPriorityProvider;

import java.io.IOException;

public class Provider implements MOPersistenceProvider {
    @Override
    public void restore(String uri, int importMode) throws IOException {

    }

    @Override
    public void restore(String uri, int importMode, MOPriorityProvider priorityProvider) throws IOException {
        MOPersistenceProvider.super.restore(uri, importMode, priorityProvider);
    }

    @Override
    public void store(String uri) throws IOException {

    }

    @Override
    public void store(String uri, MOPriorityProvider priorityProvider) throws IOException {

    }

    @Override
    public boolean isValidPersistenceURI(String uri) {
        return false;
    }

    @Override
    public String getPersistenceProviderID() {
        return null;
    }

    @Override
    public String getDefaultURI() {
        return null;
    }
}
