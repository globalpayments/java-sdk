package com.global.api.terminals.ingenico;

import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.*;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.abstractions.ITerminalResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.ingenico.interfaces.IngenicoSerialInterface;
import com.global.api.terminals.ingenico.interfaces.IngenicoTcpInterface;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalReceiptResponse;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalReportResponse;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.responses.ReverseResponse;
import com.global.api.terminals.ingenico.variables.ExtendedDataTags;
import com.global.api.terminals.ingenico.variables.INGENICO_REQ_CMD;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import com.global.api.utils.Extensions;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class IngenicoController extends DeviceController {
	private IDeviceInterface _device;

	public IngenicoController(ITerminalConfiguration settings) throws ConfigurationException {
		super(settings);
	}

	@Override
	public IDeviceInterface configureInterface() throws ConfigurationException {
		if (_device == null) {
			_device = new IngenicoInterface(this);
		}
		return _device;
	}

	@Override
	public IDeviceCommInterface configureConnector() throws ConfigurationException {
		switch (settings.getConnectionMode()) {
		case SERIAL:
			return new IngenicoSerialInterface(settings, null);
		case TCP_IP_SERVER:
			return new IngenicoTcpInterface(settings);
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public ITerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
		IDeviceMessage request = buildManageTransaction(builder);
		if (builder.getTransactionType() == TransactionType.Reversal) {
			return doReverseRequest(request);
		} else {
			return doRequest(request);
		}
	}

	@Override
	public ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException {
		IDeviceMessage request;
		if (!isObjectNullOrEmpty(builder.getType())) {
			request = buildReportTransaction(builder);
			return reportRequest(request);
		} else {
			request = TerminalUtilities.buildIngenicoRequest(
					String.format(new INGENICO_REQ_CMD().RECEIPT, builder.getReceiptType()),
					settings.getConnectionMode());
			return receiptRequest(request);
		}
	}

	@Override
	public ITerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
		IDeviceMessage request = buildRequestMessage(builder);
		return doRequest(request);
	}

	private IDeviceMessage buildReportTransaction(TerminalReportBuilder builder) throws BuilderException {
		if (!isObjectNullOrEmpty(builder.getType())) {
			String message = Extensions.formatWith(new INGENICO_REQ_CMD().REPORT, builder.getType());
			return TerminalUtilities.buildIngenicoRequest(message, settings.getConnectionMode());
		} else {
			throw new BuilderException("Type of report is missing in request.");
		}
	}

	private IDeviceMessage buildManageTransaction(TerminalManageBuilder builder) throws BuilderException {
		DecimalFormat decimalFormat = new DecimalFormat("00000000");

		Integer _referenceNumber = builder.getReferenceNumber();
		BigDecimal _amount = validateAmount(builder.getAmount());
		Integer _returnRep = 1;
		Integer _paymentMode = 0;
		Integer _paymentType = ((IngenicoInterface) _device).getPaymentMethod() == null ? 0
				: ((IngenicoInterface) _device).getPaymentMethod().getPaymentType();
		String _currencyCode = "826";
		String _privateData = "EXT0100000";
		Integer _immediateAnswer = 0;
		Integer _forceOnline = 0;
		String _extendedData = "0000000000";

		if (!isObjectNullOrEmpty(builder.getAuthCode())) {
			_extendedData = validateExtendedData(builder.getAuthCode(), builder.getExtendedDataTag());
		} else if (!isObjectNullOrEmpty(builder.getTableNumber())) {
			_extendedData = validateExtendedData(builder.getTableNumber(), builder.getExtendedDataTag());
		} else if (builder.getTransactionId() != null && builder.getTransactionType() == TransactionType.Reversal) {
			_extendedData = validateExtendedData(builder.getTransactionId(), ExtendedDataTags.TXN_COMMANDS_PARAMS);
		} else {
			_extendedData = validateExtendedData(builder.getTransactionType().toString(),
					ExtendedDataTags.TXN_COMMANDS);
		}

		String message = String.format("%s%s%s%s%s%s%sA01%sB01%s%s", String.format("%02d", _referenceNumber),
				decimalFormat.format(_amount), _returnRep, _paymentMode, _paymentType, _currencyCode, _privateData,
				_immediateAnswer, _forceOnline, _extendedData);

		return TerminalUtilities.buildIngenicoRequest(message, settings.getConnectionMode());
	}

	private IDeviceMessage buildRequestMessage(TerminalAuthBuilder builder) throws BuilderException {
		String message = "";
		DecimalFormat decimalFormat = new DecimalFormat("00000000");

		Integer _referenceNumber = builder.getReferenceNumber();
		BigDecimal _amount = builder.getAmount();
		Integer _returnRep = 1;
		Integer _paymentMode = 0;
		Integer _paymentType = ((IngenicoInterface) _device).getPaymentMethod().getPaymentType();
		String _currencyCode = "826";
		String _privateData = "EXT0100000";
		Integer _immediateAnswer = 0;
		Integer _forceOnline = 0;
		String _extendedData = "0000000000";

		BigDecimal _cashbackAmount = builder.getCashBackAmount();
		String _authCode = builder.getAuthCode();
		String tableId = builder.getTableNumber();

		if (!isObjectNullOrEmpty(builder.getReportType())) {
			message = String.format(new INGENICO_REQ_CMD().REPORT, builder.getReportType());
		} else {
			_amount = validateAmount(_amount);
			_paymentMode = validatePaymentMode(builder.getPaymentMode());
			_currencyCode = (isObjectNullOrEmpty(builder.getCurrencyCode()) ? _currencyCode
					: builder.getCurrencyCode());

			if (!isObjectNullOrEmpty(tableId)) {
				boolean validateTableId = validateTableReference(tableId);
				if (validateTableId) {
					_extendedData = validateExtendedData(tableId, builder.getExtendedDataTag());
				}
			}

			if (!isObjectNullOrEmpty(_cashbackAmount)) {
				_extendedData = validateExtendedData(_cashbackAmount.toString(), builder.getExtendedDataTag());
			} else if (!isObjectNullOrEmpty(_authCode)) {
				_extendedData = validateExtendedData(_authCode, builder.getExtendedDataTag());
			}

			message = String.format("%s%s%s%s%s%s%sA01%sB01%s%s", String.format("%02d", _referenceNumber),
					decimalFormat.format(_amount), _returnRep, _paymentMode, _paymentType, _currencyCode, _privateData,
					_immediateAnswer, _forceOnline, _extendedData);
		}

		return TerminalUtilities.buildIngenicoRequest(message, settings.getConnectionMode());
	}

	private static boolean isObjectNullOrEmpty(Object value) {
		boolean response = false;

		if (value == null || value.toString().isEmpty()) {
			response = true;
		} else {
			response = false;
		}

		return response;
	}

	private static boolean validateTableReference(String value) throws BuilderException {
		boolean response = false;

		if (!(value.equals(null)) && value.length() <= 8) {
			response = true;
		} else {
			throw new BuilderException("Table number must not be less than or equal 0 or greater than 8 numerics.");
		}

		return response;
	}

	private static int validatePaymentMode(PaymentMode _paymentMode) {
		if (_paymentMode == null) {
			_paymentMode = PaymentMode.APPLICATION;
		}

		return _paymentMode.getPaymentMode();
	}

	private static String validateExtendedData(String value, ExtendedDataTags tags) throws BuilderException {
		String extendedData = "";

		if (!isObjectNullOrEmpty(value))
			switch (tags) {
			case CASHB:
				BigDecimal cashbackAmount = new BigDecimal(value);
				Integer iValue = Integer.parseInt(value);

				if (iValue > 0 && iValue < 1000000) {
					cashbackAmount = cashbackAmount.multiply(new BigDecimal("100"));
				} else if (iValue <= 0) {
					throw new BuilderException("Cashback Amount must not be in less than or equal 0 value.");
				} else {
					throw new BuilderException("Cashback Amount exceed.");
				}

				extendedData = Extensions.formatWith("CASHB=%s;", cashbackAmount);
				break;
			case AUTHCODE:
				extendedData = Extensions.formatWith("AUTHCODE=%s;", value);
				break;
			case TABLE_NUMBER:
				extendedData = Extensions.formatWith("CMD=ID%s;", value);
				break;
			case TXN_COMMANDS:
				TransactionType transType = TransactionType.valueOf(value);
				switch (transType) {
				case Cancel:
					extendedData = new INGENICO_REQ_CMD().CANCEL;
					break;
				case Duplicate:
					extendedData = new INGENICO_REQ_CMD().DUPLICATE;
					break;
				case Reversal:
					extendedData = new INGENICO_REQ_CMD().REVERSE;
					break;
				}
				break;
			case TXN_COMMANDS_PARAMS:
				extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().REVERSE_WITH_ID, value);
				break;
			}

		return extendedData;
	}

	private static BigDecimal validateAmount(BigDecimal _amount) throws BuilderException {
		BigDecimal amount1mil = new BigDecimal("1000000");

		if (_amount == null) {
			throw new BuilderException("Amount can not be null.");
		} else if ((_amount.compareTo(BigDecimal.ZERO) > 0) && (_amount.compareTo(amount1mil) < 0)) {
			_amount = _amount.multiply(new BigDecimal("100"));
		} else if ((_amount.compareTo(amount1mil) == 0) && (_amount.compareTo(amount1mil) > 0)) {
			throw new BuilderException("Amount exceed.");
		} else {
			throw new BuilderException("Invalid input amount.");
		}

		return _amount;
	}

	public ITerminalConfiguration getConfiguration() {
		return settings;
	}

	private IngenicoTerminalReceiptResponse receiptRequest(IDeviceMessage request) throws ApiException {
		byte[] send = send(request);
		return new IngenicoTerminalReceiptResponse(send);
	}

	private IngenicoTerminalReportResponse reportRequest(IDeviceMessage request) throws ApiException {
		byte[] send = send(request);
		return new IngenicoTerminalReportResponse(send);
	}

	private IngenicoTerminalResponse doRequest(IDeviceMessage request) throws ApiException {
		byte[] response = send(request);
		return new IngenicoTerminalResponse(response);
	}

	private ReverseResponse doReverseRequest(IDeviceMessage request) throws ApiException {
		byte[] response = send(request);
		return new ReverseResponse(response);
	}
}
