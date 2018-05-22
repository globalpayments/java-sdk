package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;

public class HostResponse implements IResponseSubGroup {
    private String hostResponseCode;
    private String hostResponseMessage;
    private String authCode;
    private String hostReferenceNumber;
    private String traceNumber;
    private String batchNumber;

    public String getHostResponseCode() {
        return hostResponseCode;
    }
    public String getHostResponseMessage() {
        return hostResponseMessage;
    }
    public String getAuthCode() {
        return authCode;
    }
    public String getHostReferenceNumber() {
        return hostReferenceNumber;
    }
    public String getTraceNumber() {
        return traceNumber;
    }
    public String getBatchNumber() {
        return batchNumber;
    }

    public HostResponse(MessageReader mr) {
        String values = mr.readToCode(ControlCodes.FS);
        if(values == null || values.equals(""))
            return;

        String[] data = values.split("\\[US\\]");
        try{
            hostResponseCode = data[0];
            hostResponseMessage = data[1];
            authCode = data[2];
            hostReferenceNumber = data[3];
            traceNumber = data[4];
            batchNumber = data[5];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            // Eating this
        }
    }
}