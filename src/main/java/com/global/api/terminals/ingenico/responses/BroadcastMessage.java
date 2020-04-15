package com.global.api.terminals.ingenico.responses;

import java.util.*;

import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;

public class BroadcastMessage {
	private byte[] buffer;
	private String code;
	private String message;
	private Hashtable<String, String> broadcastData = new Hashtable<String, String>();

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	private void putBroadcastData() {
		broadcastData.put("A0", "CONNECTING");
		broadcastData.put("A1", "CONNECTION MADE");
		broadcastData.put("A2", "APPROVED");
		broadcastData.put("A3", "DECLINED");
		broadcastData.put("A4", "INSERT CARD");
		broadcastData.put("A5", "CARD ERROR");
		broadcastData.put("A6", "PROCESSING ERROR");
		broadcastData.put("A7", "REMOVE CARD");
		broadcastData.put("A8", "TRY AGAIN");
		broadcastData.put("A9", "PRESENT CARD");
		broadcastData.put("AA", "RE-PRESENT CARD");
		broadcastData.put("AB", "CARD NOT SUPPORTED");
		broadcastData.put("AC", "PRESENT ONLY ONE CARD");
		broadcastData.put("AD", "PLEASE WAIT");
		broadcastData.put("AE", "BAD SWIPE");
		broadcastData.put("AF", "CARD EXPIRED");
		broadcastData.put("B0", "DECLINED BY CARD");
		broadcastData.put("B1", "PIN ENTRY");
		broadcastData.put("B2", "CASHBACK AMOUNT ENTRY");
		broadcastData.put("B3", "PAPER OUT");
	}

	public BroadcastMessage(byte[] buffer) throws MessageException {
		putBroadcastData();
		this.buffer = buffer;
		parseBroadcast(this.buffer);
	}

	private void parseBroadcast(byte[] broadcast) throws MessageException {
		if (broadcast.length > 0) {
			String sBroadcast = TerminalUtilities.getString(broadcast);
			int index = sBroadcast.indexOf("BROADCAST CODE");
			int length = 14 + 2; // additional 2 is for extra char
			code = sBroadcast.substring(index + length, index + length + 2);
			message = broadcastData.get(code);
		} else
			throw new MessageException("No broadcast message.");
	}
}
