package be.heh.agentsnmp.manager.entities;

import lombok.Getter;
import lombok.Setter;

public class Processor {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String reference;
    @Getter
    @Setter
    private Integer core;
    @Getter
    @Setter
    private Integer vCore;
    @Getter
    @Setter
    private Double frequency;

    public Processor(String reference,Integer core, Integer vCore,Double frequency){
        setReference(reference);
        setCore(core);
        setVCore(vCore);
        setFrequency(frequency);
    }
    public Processor(Long id,String reference,Integer core, Integer vCore,Double frequency){
        setId(id);
        setReference(reference);
        setCore(core);
        setVCore(vCore);
        setFrequency(frequency);
    }
    @Override
    public String toString(){
        return String.join(" : ",Long.toString(id),reference,core.toString(),vCore.toString(), frequency.toString());
    }
}
