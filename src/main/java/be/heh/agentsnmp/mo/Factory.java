package be.heh.agentsnmp.mo;

import lombok.Getter;
import lombok.Setter;
import org.snmp4j.agent.io.DefaultMOInput;
import org.snmp4j.agent.io.MOInput;
import org.snmp4j.agent.io.MOInputFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Factory implements MOInputFactory {
    @Getter
    @Setter
    private String filePath;
    public Factory(String filePath){
        setFilePath(filePath);
    }
    @Override
    public MOInput createMOInput() {
        try {
            FileInputStream inputStream = new FileInputStream(getFilePath());
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            return new DefaultMOInput(ois);
        } catch (FileNotFoundException e) {
            System.out.println("File not found Error: "+e.getMessage());
            return new Input();
        } catch (IOException e) {
            System.out.println("Runtime Error: "+e.getMessage());
            return new Input();
        }
    }
}
