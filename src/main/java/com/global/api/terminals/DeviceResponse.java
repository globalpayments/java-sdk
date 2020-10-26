package com.global.api.terminals;

import com.global.api.terminals.abstractions.IDeviceResponse;

public class DeviceResponse implements IDeviceResponse {
	private String _status;
	private String _command;
	private String _version;
	private String _deviceResponseCode;
	private String _deviceResponseMessage;
	private String _referenceNumber;

	public String getStatus() {
		return _status;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public String getCommand() {
		return _command;
	}

	public void setCommand(String command) {
		_command = command;
	}

	public String getVersion() {
		return _version;
	}

	public void setVersion(String version) {
		_version = version;
	}

	public String getDeviceResponseCode() {
		return _deviceResponseCode;
	}

	public void setDeviceResponseCode(String deviceResponseCode) {
		_deviceResponseCode = deviceResponseCode;
	}

	public String getDeviceResponseText() {
		return _deviceResponseMessage;
	}

	public void setDeviceResponseText(String deviceResponseMessage) {
		_deviceResponseMessage = deviceResponseMessage;
	}
	
	public String getReferenceNumber() {
		return _referenceNumber;
	}
	
	public void setReferenceNumber(String refNumber) {
		_referenceNumber = refNumber;
	}
}
