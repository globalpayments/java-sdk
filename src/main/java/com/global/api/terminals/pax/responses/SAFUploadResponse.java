package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.MessageReader;

public class SAFUploadResponse extends PaxDeviceResponse {
    
    private Integer totalCount;
    private Integer totalAmount;
    private String timeStamp;
    private Integer safUploadedCount;
    private Integer safUploadedAmount;
    private Integer safFailedCount;
    private Integer safFailedTotal;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getSafUploadedCount() {
        return safUploadedCount;
    }

    public void setSafUploadedCount(Integer safUploadedCount) {
        this.safUploadedCount = safUploadedCount;
    }

    public Integer getSafUploadedAmount() {
        return safUploadedAmount;
    }

    public void setSafUploadedAmount(Integer safUploadedAmount) {
        this.safUploadedAmount = safUploadedAmount;
    }

    public Integer getSafFailedCount() {
        return safFailedCount;
    }

    public void setSafFailedCount(Integer safFailedCount) {
        this.safFailedCount = safFailedCount;
    }

    public Integer getSafFailedTotal() {
        return safFailedTotal;
    }

    public void setSafFailedTotal(Integer safFailedTotal) {
        this.safFailedTotal = safFailedTotal;
    }

    public SAFUploadResponse(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.B09_RSP_SAF_UPLOAD);
    }
    
    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);
        
        if (deviceResponseCode.equals("000000")) {
            totalCount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            totalAmount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            timeStamp = mr.readToCode(ControlCodes.FS);
            safUploadedCount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            safUploadedAmount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            safFailedCount = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            safFailedTotal = Integer.parseInt(mr.readToCode(ControlCodes.FS));
        }
    }

}