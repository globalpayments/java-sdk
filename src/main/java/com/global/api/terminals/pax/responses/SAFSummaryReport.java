package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.MessageReader;

public class SAFSummaryReport extends PaxDeviceResponse {
    
    private String safTotalCount;
    private String safTotalAmount;

    public String getSafTotalCount() {
        return safTotalCount;
    }

    public void setSafTotalCount(String safTotalCount) {
        this.safTotalCount = safTotalCount;
    }

    public String getSafTotalAmount() {
        return safTotalAmount;
    }

    public void setSafTotalAmount(String safTotalAmount) {
        this.safTotalAmount = safTotalAmount;
    }

    public SAFSummaryReport(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.R11_RSP_SAF_SUMMARY_REPORT);
    }
    
    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);
        
        if(deviceResponseCode.equals("000000")) {
            safTotalCount = mr.readToCode(ControlCodes.FS);
            safTotalAmount = mr.readToCode(ControlCodes.FS);
        }
    }

}
