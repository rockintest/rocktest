package io.rocktest.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

public class Id extends RockModule {

    @AllArgsConstructor
    private class Seq {
        int value=0;
        int step=0;
    }

    private Map<String,Seq> seqs= new HashMap<String,Seq>();
    private static Logger LOG = LoggerFactory.getLogger(Id.class);

    public Map<String, Object> initseq(Map<String, Object> params) {

        String name=getStringParam(params,"name","default");
        Integer val=getIntParam(params,"value");
        Integer step=getIntParam(params,"step",1);
        seqs.put(name,new Seq(val,step));

        return null;
    }


    public Map<String, Object> seq(Map<String, Object> params) {

        String name=getStringParam(params,"name","default");

        Seq seq=seqs.get(name);
        if(seq==null) {
            seq=new Seq(0,1);
            seqs.put(name,seq);
        }

        Map<String, Object> ret = new HashMap<>();

        ret.put("result", seq.value);
        seq.value+=seq.step;

        return ret;
    }


    public Map<String, Object> uuid(Map<String, Object> params) {

        Map<String, Object> ret = new HashMap<>();
        ret.put("result", UUID.randomUUID());

        return ret;
    }


}
