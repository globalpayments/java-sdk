package com.global.api.terminals;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.IRawRequestBuilder;

public class DeviceMessage implements IDeviceMessage {
    private byte[] buffer;
    private boolean keepAlive;
    private boolean awaitResponse;

    public DeviceMessage(byte[] buffer){
        this.buffer = buffer;
    }

    public byte[] getSendBuffer() { return this.buffer; }

    @Override
    public IRawRequestBuilder getRequestBuilder() {
        return null;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isAwaitResponse() {
        return awaitResponse;
    }

    public void setAwaitResponse(boolean awaitResponse) {
        this.awaitResponse = awaitResponse;
    }

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
