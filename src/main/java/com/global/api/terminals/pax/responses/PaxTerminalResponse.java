package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.ITerminalResponse;

public class PaxTerminalResponse extends PaxBaseResponse implements ITerminalResponse {

	public PaxTerminalResponse(byte[] buffer, PaxMsgId[] messageIds) throws MessageException {
		super(buffer, messageIds);
		// TODO Auto-generated constructor stub
	}

	public boolean getCardPresent() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getTaxExempt() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCardHolderVerificationMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCardHolderVerificationMethod(String cardHolderVerificationMethod) {
		// TODO Auto-generated method stub
		
	}

}
