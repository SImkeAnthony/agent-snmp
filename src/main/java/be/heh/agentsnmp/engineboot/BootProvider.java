package be.heh.agentsnmp.engineboot;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.snmp4j.cfg.EngineBootsProvider;

import java.io.*;

public class BootProvider implements EngineBootsProvider {
    @Getter
    @Setter
    private String file;
    @Getter
    @Setter
    private int bootCounter;
    public BootProvider(String bootCounterFile) throws IOException {
        setFile(bootCounterFile);
        setBootCounter(0);
        try (FileWriter writer = new FileWriter(getFile())) {
            writer.write(String.valueOf(getBootCounter()));
        } catch (IOException e) {
            System.err.println("IO Error: "+e.getMessage());
        }
    }
    @Override
    public int updateEngineBoots() {
        setBootCounter(getEngineBoots()+1);
        try (FileWriter writer = new FileWriter(getFile())) {
            writer.write(String.valueOf(getBootCounter()));
        } catch (IOException e) {
            System.err.println("IO Error: "+e.getMessage());
        }
        return getBootCounter();
    }

    @Override
    public int getEngineBoots() {
        try(BufferedReader reader = new BufferedReader(new FileReader(getFile()))){
            String content = reader.readLine();
            setBootCounter(Integer.parseInt(content));
        }catch (IOException e){
            System.err.println("IO Error: "+e.getMessage());
        }catch (NumberFormatException e){
            System.err.println("Format Error: "+e.getMessage());
        }
        return getBootCounter();
    }
}
