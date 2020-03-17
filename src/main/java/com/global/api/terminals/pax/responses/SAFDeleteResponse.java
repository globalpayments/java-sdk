package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.MessageReader;

public class SAFDeleteResponse extends PaxDeviceResponse {
    
    private Integer safDeletedCount;

    public Integer getSafDeletedCount() {
        return safDeletedCount;
    }

    public void setSafDeletedCount(Integer safDeletedCount) {
        this.safDeletedCount = safDeletedCount;
    }

    public SAFDeleteResponse(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.B11_RSP_DELETE_SAF_FILE);
    }
    
    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);

        if(deviceResponseCode.equals("000000")) {
            safDeletedCount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
        }
    }

}
