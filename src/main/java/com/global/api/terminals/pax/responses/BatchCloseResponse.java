package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.pax.subgroups.HostResponse;
import com.global.api.utils.MessageReader;

public class BatchCloseResponse extends PaxDeviceResponse implements IBatchCloseResponse {
    private String totalCount;
    private String totalAmount;
    private String timeStamp;
    private String tid;
    private String mid;
    private String batchNumber;
    private String sequenceNumber;

    public String getTotalCount() {
        return totalCount;
    }
    public String getTotalAmount() {
        return totalAmount;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public String getTid() {
        return tid;
    }
    public String getMid() {
        return mid;
    }
    public String getBatchNumber() {
        return batchNumber;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public BatchCloseResponse(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.B01_RSP_BATCH_CLOSE);
    }

    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);

        hostResponse = new HostResponse(mr);
        totalCount = mr.readToCode(ControlCodes.FS);
        totalAmount = mr.readToCode(ControlCodes.FS);
        timeStamp = mr.readToCode(ControlCodes.FS);
        tid = mr.readToCode(ControlCodes.FS);
        mid = mr.readToCode(ControlCodes.ETX);

        if(this.hostResponse != null)
            this.batchNumber = this.hostResponse.getBatchNumber();
    }
}
