package com.global.api.terminals;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.EnumUtils;

public class DeviceMessage implements IDeviceMessage {
    byte[] buffer;

    public DeviceMessage(byte[] buffer){
        this.buffer = buffer;
    }

    public byte[] getSendBuffer() { return this.buffer; }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(byte b : buffer){
            if(EnumUtils.isDefined(ControlCodes.class, b)){
                ControlCodes code = EnumUtils.parse(ControlCodes.class, b);
                sb.append(code.toString());
            }
            else sb.append((char)b);
        }

        return sb.toString();
    }
}
