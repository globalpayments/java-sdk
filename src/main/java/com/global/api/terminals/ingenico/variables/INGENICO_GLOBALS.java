package com.global.api.terminals.ingenico.variables;

public class INGENICO_GLOBALS {
	public final String CANCEL = "CMD=CANCEL";
	public final String BROADCAST = "BROADCAST CODE";
	public final String TID_CODE = "TID CODE";
	public final String KEEP_ALIVE_RESPONSE = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TID CODE=\"%s\">OK</TID>";
	public static boolean KEEPALIVE = true;
	public final Integer IP_PORT = 18101;
	public final Integer RAW_RESPONSE_LENGTH = 80;
	public final String PRIVDATA_DEFAULT = "0000000000";
	public final String XML_TAG = "?xml";
	public final String ADDITIONAL_MSG_ROOT = "ADDITIONAL_DATA";
	public final String TRANSFER_DATA_REQUEST = "DATA_TRANSFER";
	public final String TRANSACTION_XML = "CREDIT_CARD_RECEIPT";
	public final String EPOS_TABLE_LIST = "EPOS_TABLE_LIST";
	public final String WINDOWS_ENV = "WINDOWS";
	public final String LINUX_ENV = "LINUX";
}
