package be.heh.agentsnmp.manager.entities;

import lombok.Getter;
import lombok.Setter;

public class Service {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private  String description;
    @Getter
    @Setter
    private String port;

    public Service(String name,String description,String port){
        setName(name);
        setDescription(description);
        setPort(port);
    }
    public Service(Long id,String name,String description,String port){
        setId(id);
        setName(name);
        setDescription(description);
        setPort(port);
    }

    @Override
    public String toString(){
        return String.join(" : ", id.toString(),name,description,port);
    }
}
