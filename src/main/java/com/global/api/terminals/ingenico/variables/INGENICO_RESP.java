package com.global.api.terminals.ingenico.variables;

import com.global.api.entities.enums.ControlCodes;

public class INGENICO_RESP {
	public final byte ACKNOWLEDGE = ControlCodes.ACK.getByte();
	public final byte ENQUIRY = ControlCodes.ENQ.getByte();
	public final byte NOTACKNOWLEDGE = ControlCodes.NAK.getByte();
	public final byte ENDOFTXN = ControlCodes.EOT.getByte();
	public final String XML = "<CREDIT_CARD_RECEIPT>";
	public final String INVALID = "\u0005\u0004";
	public final String ENDXML = "</CREDIT_CARD_RECEIPT>";
	public final String LFTAG = "LF";
	public final String PAT_EPOS_NUMBER = "00";
}
