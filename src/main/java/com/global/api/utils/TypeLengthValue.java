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

	private byte[] data = new byte[0];
	private TLVFormat format = TLVFormat.Standard;

	public TypeLengthValue() {

	}

	public TypeLengthValue(byte[] data) {
		this.data = data;
	}

	public TLVFormat getTLVFormat() {
		return format;
	}

	public void setTLVFormat(TLVFormat format) {
		this.format = format;
	}

	public Object getValue(byte type, Class returnType, TLVFormat format) throws Exception {
		if (data.length == 0) {
			throw new Exception("No data to parse.");
		}

		String buffer = new String(data, StandardCharsets.UTF_8);
		String getBuffer = new String(new byte[] { type }, StandardCharsets.UTF_8);
		Integer index = buffer.indexOf(getBuffer);

		if (index >= 0) {
			byte[] lengthBuffer = { data[index + 1], data[index + 2] };
			Integer length = 0;

			if ((format != null && format == TLVFormat.Standard) || this.format == TLVFormat.Standard) {
				length = Integer.parseInt(TerminalUtilities.getString(lengthBuffer), 16);
			} else if ((format != null && format == TLVFormat.State) || this.format == TLVFormat.State) {
				length = Integer.parseInt(TerminalUtilities.getString(lengthBuffer));
			} else {
				throw new Exception("Unsupported TLV Format.");
			}

			byte[] arrValue = Arrays.copyOfRange(data, index + 3, length + 3);
			int endLength = index + length + 3;
			data = Extensions.subArray(data, 0, index);
			data = Extensions.subArray(data, endLength, data.length - endLength);
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
