package com.global.api.terminals.ingenico.variables;

import com.global.api.entities.enums.ControlCodes;

public class INGENICO_RESP {
	public final String ACKNOWLEDGE = ControlCodes.ACK.toString();
	public final String ENQUIRY = ControlCodes.ENQ.toString();
	public final String NOTACKNOWLEDGE = ControlCodes.NAK.toString();
	public final String ENDOFTXN = ControlCodes.EOT.toString();
	public final String[] XML = { "<CREDIT_CARD_RECEIPT>", "LF" };
	public final String INVALID = "\u0005\u0004";
	public final String ENDXML = "</CREDIT_CARD_RECEIPT>";
}
