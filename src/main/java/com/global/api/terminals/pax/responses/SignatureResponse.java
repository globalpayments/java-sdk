package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.utils.MessageReader;

public class SignatureResponse extends PaxBaseResponse implements ISignatureResponse {
    private Integer totalLength;
    private Integer responseLength;

    public int getTotalLength() {
        return totalLength;
    }
    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }
    public int getResponseLength() {
        return responseLength;
    }
    public void setResponseLength(int responseLength) {
        this.responseLength = responseLength;
    }

    public SignatureResponse(byte[] response) throws ApiException {
        super(response, PaxMsgId.A09_RSP_GET_SIGNATURE, PaxMsgId.A21_RSP_DO_SIGNATURE);
    }

    @Override
    protected void parseResponse(MessageReader mr) throws MessageException {
        super.parseResponse(mr);

        if(deviceResponseCode.equals("000000") && command.equals(PaxMsgId.A09_RSP_GET_SIGNATURE.getValue())) {
            totalLength = Integer.parseInt(mr.readToCode(ControlCodes.FS));
            responseLength = Integer.parseInt(mr.readToCode(ControlCodes.FS));

            signatureData = TerminalUtilities.buildSignatureImage(mr.readToCode(ControlCodes.ETX));
        }
    }
}
