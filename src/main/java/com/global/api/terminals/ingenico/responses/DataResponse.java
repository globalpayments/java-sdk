package com.global.api.terminals.ingenico.responses;

import java.math.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.global.api.utils.*;
import com.global.api.terminals.ingenico.variables.*;

public class DataResponse {

	private String _authCode;
	private BigDecimal _finalAmount;
	private PaymentMethod _paymentMethod;
	private BigDecimal _cashbackAmount;
	private BigDecimal _gratuityAmount;
	private BigDecimal _availableAmount;
	private String _dccCode;
	private BigDecimal _dccAmount;
	private TransactionSubTypes _txnSubType;
	private BigDecimal _splitSaleAmount;
	private DynamicCurrencyStatus _dccStatus;

	private byte[] _buffer;

	// For less memory allocation;
	private byte _C = 67;
	private byte _Z = 90;
	private byte _Y = 89;
	private byte _M = 77;
	private byte _A = 65;
	private byte _U = 85;
	private byte _O = 79;
	private byte _P = 80;
	private byte _T = 84;
	private byte _S = 83;
	private byte _D = 68;

	public DataResponse(byte[] buffer) {
		_buffer = buffer;
		ParseData();
	}

	public String getAuthorizationCode() {
		return _authCode != null ? _authCode : "";
	}

	private void setAuthorizationCode(String value) {
		_authCode = value;
	}

	public BigDecimal getFinalAmount() {
		return _finalAmount;
	}

	public void setFinalAmount(BigDecimal value) {
		_finalAmount = value;
	}

	public PaymentMethod getPaymentMethod() {
		return _paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod value) {
		_paymentMethod = value;
	}

	public BigDecimal getCashbackAmount() {

		return _cashbackAmount;
	}

	public void setCashbackAmount(BigDecimal value) {
		_cashbackAmount = value;
	}

	public BigDecimal getGratuityAmount() {
		return _gratuityAmount;
	}

	public void setGratuityAmount(BigDecimal value) {
		_gratuityAmount = value;
	}

	public BigDecimal getAvailableAmount() {
		return _availableAmount;
	}

	public void setAvailableAmount(BigDecimal value) {
		_availableAmount = value;
	}

	public String getDccCode() {
		return _dccCode;
	}

	public void setDccCode(String value) {
		_dccCode = value;
	}

	public BigDecimal getDccAmount() {
		return _dccAmount;
	}

	public void setDccAmount(BigDecimal value) {
		_dccAmount = value;
	}

	public TransactionSubTypes getTransactionSubType() {
		return _txnSubType;
	}

	public void setTransactionSubType(TransactionSubTypes value) {
		_txnSubType = value;
	}

	public BigDecimal getSplitSaleAmount() {
		return _splitSaleAmount;
	}

	public void setSplitSaleAmount(BigDecimal value) {
		_splitSaleAmount = value;
	}

	public DynamicCurrencyStatus getDccStatus() {
		return _dccStatus;
	}

	public void setDccStatus(DynamicCurrencyStatus value) {
		_dccStatus = value;
	}

	private void ParseData() {
		_authCode = (String) getValueOfRespField(_C, String.class);
		_cashbackAmount = (BigDecimal) getValueOfRespField(_Z, BigDecimal.class);
		_gratuityAmount = (BigDecimal) getValueOfRespField(_Y, BigDecimal.class);
		_finalAmount = (BigDecimal) getValueOfRespField(_M, BigDecimal.class);
		_availableAmount = (BigDecimal) getValueOfRespField(_A, BigDecimal.class);
		_dccCode = (String) getValueOfRespField(_U, String.class);
		_dccAmount = (BigDecimal) getValueOfRespField(_O, BigDecimal.class);
		_txnSubType = (TransactionSubTypes) getValueOfRespField(_T, TransactionSubTypes.class);
		_dccStatus = (DynamicCurrencyStatus) getValueOfRespField(_D, DynamicCurrencyStatus.class);
		_splitSaleAmount = (BigDecimal) getValueOfRespField(_S, BigDecimal.class);
		_paymentMethod = (PaymentMethod) getValueOfRespField(_P, PaymentMethod.class);
	}

	private Object getValueOfRespField(byte toGet, Class returnType) {
		String sBuffer = new String(_buffer, StandardCharsets.UTF_8);
		String sGet = new String(new byte[] { toGet }, StandardCharsets.UTF_8);
		int index = sBuffer.indexOf(sGet);
		
		if (index >= 0) {
			byte[] lengthBuffer = { _buffer[index + 1], _buffer[index + 2] };
			int length = Integer.parseInt(new String(lengthBuffer, StandardCharsets.UTF_8), 16);
			
			byte[] arrValue = Arrays.copyOfRange(_buffer, index + 3, length + 3);
			int endLength = index + length + 3;
			_buffer = Extensions.subArray(_buffer, 0, index);
			_buffer = Extensions.subArray(_buffer, endLength, _buffer.length - endLength);
			String strValue = new String(arrValue, StandardCharsets.UTF_8);

			if (returnType == BigDecimal.class)
				return Extensions.toAmount(strValue);
			 else if (returnType == String.class)
				return strValue;
			else if (returnType == TransactionSubTypes.class) 
				return TransactionSubTypes.getEnumName(Integer.parseInt(new String(arrValue, StandardCharsets.UTF_8).substring(0, 0), 16));
			 else if (returnType == DynamicCurrencyStatus.class) 
				return DynamicCurrencyStatus.getEnumName(Integer.parseInt(strValue));
			 else if (returnType == PaymentMethod.class) 
				return PaymentMethod.getEnumName(Integer.parseInt(strValue));
			 else
				throw new RuntimeException("Data type not supported in parsing of response data.");
		}
		
		return null;
	}
}
