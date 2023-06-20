package be.heh.agentsnmp.manager.entities;

import lombok.Getter;
import lombok.Setter;

public class PersistentStorage {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String reference;
    @Getter
    @Setter
    private Double available;
    @Getter
    @Setter
    private Double used;

    public PersistentStorage(String reference,Double available,Double used){
        setReference(reference);
        setAvailable(available);
        setUsed(used);
    }
    public PersistentStorage(Long id,String reference,Double available,Double used){
        setId(id);
        setReference(reference);
        setAvailable(available);
        setUsed(used);
    }
    @Override
    public String toString(){
        return String.join(" : ",id.toString(),reference, available.toString(), used.toString());
    }
}
