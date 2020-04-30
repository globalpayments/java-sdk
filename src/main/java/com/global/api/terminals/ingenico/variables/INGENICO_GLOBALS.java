package com.global.api.terminals.ingenico.variables;

public class INGENICO_GLOBALS {
	public final String CANCEL = "CMD=CANCEL";
	public final String BROADCAST = "BROADCAST CODE";
	public final String TID_CODE = "TID CODE";
	public final String KEEP_ALIVE_RESPONSE = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TID CODE=\"%s\">OK</TID>";
	public static boolean KEEPALIVE = true;
	public final int IP_PORT = 18101;
}
