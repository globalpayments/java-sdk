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

    public SignatureResponse(byte[] response, PaxMsgId... messageIds) throws ApiException {
        super(response, messageIds);
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
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setStatus(String status) {
		// TODO Auto-generated method stub
		
	}
	public String getCommand() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setCommand(String command) {
		// TODO Auto-generated method stub
		
	}
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setVersion(String version) {
		// TODO Auto-generated method stub
		
	}
	public String getDeviceResponseCode() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setDeviceResponseCode(String deviceResponseCode) {
		// TODO Auto-generated method stub
		
	}
	public String getDeviceResponseText() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setDeviceResponseText(String deviceResponseMessage) {
		// TODO Auto-generated method stub
		
	}
	public byte[] getSignatureData() {
		// TODO Auto-generated method stub
		return null;
	}
}
