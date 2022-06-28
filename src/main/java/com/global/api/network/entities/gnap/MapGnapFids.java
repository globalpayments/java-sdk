package com.global.api.network.entities.gnap;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IStringConstant;
import com.global.api.network.enums.gnap.GnapFIDS;
import com.global.api.network.enums.gnap.GnapSubFids;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.GnapUtils;

public class MapGnapFids {
    MessageWriter mr;
    public MapGnapFids(MessageWriter mr)
    {
        this.mr=mr;
    }

    public  MapGnapFids() {
        mr=new MessageWriter();
    }

    public void addFid(GnapFIDS id,String value) {
        add(id,value);
    }

    public void addSubFid(GnapSubFids id, String value) {
        add(id,value);
    }

    public <T extends IStringConstant> void addFid(GnapFIDS id,T value) {
        add(id,value);
    }

    public <T extends IStringConstant> void addSubFid(GnapFIDS id,T value) {
        add(id,value);
    }

    public void add(GnapFIDS id,String value) {
        if (value != null) {
            GnapUtils.log("FID " + id.getValue() + " (" + id.getFidDesc() + ")", value);
            mr.add(ControlCodes.FS);
            mr.addRange(id.getBytes());
            mr.addRange(value.getBytes());
        }
    }
    public void add(GnapSubFids id,String value) {
        if (value != null) {
            GnapUtils.log("SUBFID " + id.getValue() + " (" + id.getFidDesc() + ")", value);
            mr.add(ControlCodes.RS);
            mr.addRange(id.getBytes());
            mr.addRange(value.getBytes());
        }
    }

    public <T extends IStringConstant> void add(GnapFIDS id,T value) {
        if(value!=null) {
            add(id, value.getValue());
        }
    }

    public <T extends IStringConstant> void add(GnapSubFids id,T value) {
        if(value!=null) {
            add(id, value.getValue());
        }
    }

    public <T extends IStringConstant> void add(T desc, String value) {
        GnapUtils.log(desc.getValue(),value);
        mr.addRange(value.getBytes());
    }

    public void add(String value) {
        mr.addRange(value.getBytes());
    }

    public MessageWriter getWriterObject() {
        return mr;
    }

}
