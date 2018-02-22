package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.utils.MessageReader;

public class InitializeResponse extends PaxDeviceResponse implements IInitializeResponse {
    private String serialNumber;

    public String getSerialNumber() {
        return serialNumber;
    }
    private void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public InitializeResponse(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.A01_RSP_INITIALIZE);
    }

    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);
        setSerialNumber(mr.readToCode(ControlCodes.ETX));
    }
}
