package com.global.api.terminals.ingenico.responses;

import java.util.Arrays;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.SalesMode;
import com.global.api.terminals.ingenico.variables.StatusResponseCode;
import com.global.api.terminals.ingenico.variables.TLVFormat;
import com.global.api.terminals.ingenico.variables.TerminalStatus;
import com.global.api.utils.Extensions;
import com.global.api.utils.TypeLengthValue;

public class TerminalStateResponse extends IngenicoTerminalResponse implements IDeviceResponse {
	private TerminalStatus terminalStatus;
	private SalesMode salesMode;
	private String terminalCapabilities;
	private String additionalTerminalCap;
	private String appVersion;
	private String handsetNumber;
	private String terminalId;

	public TerminalStateResponse(byte[] buffer) {
		super(buffer, ParseFormat.State);
	}

	public TerminalStatus getTerminalStatus() {
		return terminalStatus;
	}

	public SalesMode getSalesMode() {
		return salesMode;
	}

	public String getTerminalCapabilities() {
		return terminalCapabilities;
	}

	public String getAdditionalTerminalCapabilities() {
		return additionalTerminalCap;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getHandsetNumber() {
		return handsetNumber;
	}

	public String getTerminalId() {
		return terminalId;
	}

	@Override
	public void parseResponse(byte[] response) {
		try {
			if (response != null) {
				if (response.length < new INGENICO_GLOBALS().RAW_RESPONSE_LENGTH) {
					byte[] responseLength = new byte[new INGENICO_GLOBALS().RAW_RESPONSE_LENGTH];
					response = Arrays.copyOfRange(response, 0, responseLength.length);
				}

				super.parseResponse(response);
				TypeLengthValue tlv = new TypeLengthValue(Extensions.subArray(response, 12, 67));

				String terminalStatusData = (String) tlv
						.getValue((byte) StatusResponseCode.Status.getStatusResponseCode(), String.class, TLVFormat.State);
				terminalStatus = TerminalStatus.getEnumName(Integer.parseInt(terminalStatusData.substring(0, 1)));
				salesMode = SalesMode.getEnumName(Integer.parseInt(terminalStatusData.substring(1, 2)));
				terminalCapabilities = terminalStatusData.substring(2, 8);
				additionalTerminalCap = terminalStatusData.substring(8, 18);
				appVersion = (String) tlv.getValue((byte) StatusResponseCode.AppVersion.getStatusResponseCode(),
						String.class, TLVFormat.State);
				handsetNumber = (String) tlv.getValue((byte) StatusResponseCode.HandsetNumber.getStatusResponseCode(),
						String.class, TLVFormat.State);
				terminalId = (String) tlv.getValue((byte) StatusResponseCode.TerminalId.getStatusResponseCode(),
						String.class, TLVFormat.State);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
