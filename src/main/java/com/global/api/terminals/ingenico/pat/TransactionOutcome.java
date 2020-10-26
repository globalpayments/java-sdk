package com.global.api.terminals.ingenico.pat;

import java.nio.charset.StandardCharsets;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.DeviceResponse;
import com.global.api.terminals.ingenico.responses.DataResponse;
import com.global.api.terminals.ingenico.variables.TransactionStatus;

public class TransactionOutcome extends DeviceResponse {
	private TransactionStatus _transactionStatus;
	private String _amount;
	private String _currencyCode;
	private String _privateData;
	private DataResponse _repFields;
	
	public TransactionOutcome(byte[] buffer) throws ApiException {
		parseData(buffer);
	}

	public TransactionStatus getTransactionStatus() {
		return _transactionStatus;
	}
	
	public String getAmount() {
		return _amount;
	}
	
	public String getCurrencyCode() {
		return _currencyCode;
	}
	
	public String getPrivateData() {
		return _privateData;
	}
	
	public DataResponse getRepFields() {
		return _repFields;
	}

	private void parseData(byte[] buffer) throws ApiException {
		try {
			String strBuffer = new String(buffer, StandardCharsets.UTF_8);
			
			_transactionStatus = TransactionStatus.getEnumName(Integer.parseInt(strBuffer.substring(2, 3)));
			_amount = strBuffer.substring(3, 11);
			_repFields = new DataResponse(strBuffer.substring(12, 67).getBytes());
			_currencyCode = strBuffer.substring(67, 70);
			_privateData = strBuffer.substring(70, strBuffer.length());
			setDeviceResponseText(strBuffer);
		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return getDeviceResponseText();
	}
}