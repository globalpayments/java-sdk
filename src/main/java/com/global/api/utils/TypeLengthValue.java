package com.global.api.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.ingenico.variables.DynamicCurrencyStatus;
import com.global.api.terminals.ingenico.variables.PaymentMethod;
import com.global.api.terminals.ingenico.variables.TLVFormat;
import com.global.api.terminals.ingenico.variables.TransactionSubTypes;

public class TypeLengthValue {

	private byte[] _data = new byte[0];
	private TLVFormat _format = TLVFormat.Standard;

	public TypeLengthValue() {

	}

	public TypeLengthValue(byte[] data) {
		_data = data;
	}

	public TLVFormat getTLVFormat() {
		return _format;
	}
	
	public Object getValue(byte type, Class returnType, TLVFormat format) throws Exception {
		if (_data.length == 0) {
			throw new Exception("No data to parse.");
		}
		
		if (format != null) {
			_format = format;
		}

		String buffer = new String(_data, StandardCharsets.UTF_8);
		String getBuffer = new String(new byte[] { type }, StandardCharsets.UTF_8);
		Integer index = buffer.indexOf(getBuffer);

		if (index >= 0) {
			byte[] lengthBuffer = { _data[index + 1], _data[index + 2] };
			Integer length = 0;

			if (_format == TLVFormat.Standard) {
				length = Integer.parseInt(TerminalUtilities.getString(lengthBuffer), 16);
			} else if (_format == TLVFormat.State || _format == TLVFormat.PayAtTable) {
				length = Integer.parseInt(TerminalUtilities.getString(lengthBuffer));
			} else {
				throw new Exception("Unsupported TLV Format.");
			}

			int endLength = index + length + 3;
			byte[] arrValue = Arrays.copyOfRange(_data, index + 3, endLength);
			
			byte[] cuttedData = Arrays.copyOfRange(_data, 0, index);
			byte[] excessData = Arrays.copyOfRange(_data, endLength, _data.length);
			
			MessageWriter msg = new MessageWriter();
			for (int i = 0; i < cuttedData.length; i++) {
				msg.add(cuttedData[i]);
				
				if (i == (cuttedData.length - 1)) {
					for (int j = 0; j < excessData.length; j++) {
						msg.add(excessData[j]);
					}
				}
			}
			
			if (index > 0) {
				_data = msg.toArray();	
			} else {
				_data = Extensions.subArray(_data, endLength, _data.length - endLength);
			}
			
			String strValue = new String(arrValue, StandardCharsets.UTF_8);

			if (returnType == BigDecimal.class) {
				return Extensions.toAmount(strValue);
			} else if (returnType == String.class) {
				return strValue;
			} else if (returnType == TransactionSubTypes.class) {
				return TransactionSubTypes.getEnumName(
						Integer.parseInt(new String(arrValue, StandardCharsets.UTF_8).substring(0, 0), 16));
			} else if (returnType == DynamicCurrencyStatus.class) {
				return DynamicCurrencyStatus.getEnumName(Integer.parseInt(strValue));
			} else if (returnType == PaymentMethod.class) {
				return PaymentMethod.getEnumName(Integer.parseInt(strValue));
			} else {
				throw new RuntimeException("Data type not supported in parsing of TLV data");
			}
		}

		return null;
	}
}
