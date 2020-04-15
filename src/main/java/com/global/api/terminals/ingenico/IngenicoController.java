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
import com.global.api.terminals.ingenico.variables.INGENICO_REQ_CMD;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import com.global.api.terminals.ingenico.variables.PaymentType;
import com.global.api.terminals.ingenico.variables.TaxFreeType;
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
		Integer referenceNumber = builder.getReferenceNumber();
		BigDecimal amount = validateAmount(builder.getAmount());
		Integer returnRep = 1;
		Integer paymentMode = 0;
		Integer paymentType = ((IngenicoInterface) _device).getPaymentMethod() == null ? 0
				: ((IngenicoInterface) _device).getPaymentMethod().getValue();
		String currencyCode = "826";
		String privateData = "EXT0100000";
		Integer immediateAnswer = 0;
		Integer forceOnline = 0;
		String extendedData = "0000000000";

		if (!isObjectNullOrEmpty(builder.getAuthCode())) {
			extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().AUTHCODE, builder.getAuthCode());
		} else if (!isObjectNullOrEmpty(builder.getTransactionId())
				&& builder.getTransactionType() == TransactionType.Reversal) {
			extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().REVERSE_WITH_ID, builder.getTransactionId());
		} else {
			extendedData = new INGENICO_REQ_CMD().REVERSE;
		}

		DecimalFormat decimalFormat = new DecimalFormat("00000000");
		StringBuilder message = new StringBuilder();
		message.append(String.format("%02d", referenceNumber));
		message.append(decimalFormat.format(amount));
		message.append(returnRep);
		message.append(paymentMode);
		message.append(paymentType);
		message.append(currencyCode);
		message.append(privateData);
		message.append(immediateAnswer);
		message.append(forceOnline);
		message.append(extendedData);

		return TerminalUtilities.buildIngenicoRequest(message.toString(), settings.getConnectionMode());
	}

	private IDeviceMessage buildRequestMessage(TerminalAuthBuilder builder) throws BuilderException {
		Integer referenceNumber = builder.getReferenceNumber();
		BigDecimal amount = builder.getAmount();
		Integer returnRep = 1;
		Integer paymentMode = 0;
		Integer paymentType = ((IngenicoInterface) _device).getPaymentMethod().getValue();
		String currencyCode = "826";
		String privateData = "EXT0100000";
		Integer immediateAnswer = 0;
		Integer forceOnline = 0;
		String extendedData = "0000000000";

		BigDecimal cashbackAmount = builder.getCashBackAmount();
		String authCode = builder.getAuthCode();
		String tableId = builder.getTableNumber();

		if (referenceNumber.getClass() == Integer.class && requestIdProvider() != null) {
			referenceNumber = requestIdProvider().getRequestId();
		}

		if (!isObjectNullOrEmpty(builder.getTaxFreeType())) {
			Integer taxFree = builder.getTaxFreeType().toInteger();
			PaymentType type = PaymentType.getEnumName(taxFree);
			paymentType = type.getValue();
		}

		amount = validateAmount(amount);
		paymentMode = validatePaymentMode(builder.getPaymentMode());
		currencyCode = (!isObjectNullOrEmpty(builder.getCurrencyCode()) ? builder.getCurrencyCode() : currencyCode);

		if (!isObjectNullOrEmpty(tableId)) {
			validateTableReference(tableId);
			extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().TABLE_WITH_ID, tableId);
		} else if (!isObjectNullOrEmpty(authCode)) {
			extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().AUTHCODE, authCode);
		} else if (!isObjectNullOrEmpty(cashbackAmount)) {
			validateCashbackAmount(cashbackAmount);
			cashbackAmount.multiply(new BigDecimal("100"));
			extendedData = Extensions.formatWith(new INGENICO_REQ_CMD().CASHBACK, cashbackAmount);
		}

		DecimalFormat decimalFormat = new DecimalFormat("00000000");
		StringBuilder message = new StringBuilder();
		message.append(String.format("%02d", referenceNumber));
		message.append(decimalFormat.format(amount));
		message.append(returnRep);
		message.append(paymentMode);
		message.append(paymentType);
		message.append(currencyCode);
		message.append(privateData);
		message.append(immediateAnswer);
		message.append(forceOnline);
		message.append(extendedData);

		return TerminalUtilities.buildIngenicoRequest(message.toString(), settings.getConnectionMode());
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

	private static void validateTableReference(String value) throws BuilderException {
		if (value.length() <= 8) {
			throw new BuilderException("Table number must not be less than or equal 0 or greater than 8 numerics.");
		}
	}

	private static Integer validatePaymentMode(PaymentMode paymentMode) {
		if (paymentMode == null) {
			paymentMode = PaymentMode.APPLICATION;
		}

		return paymentMode.getValue();
	}

	private static void validateCashbackAmount(BigDecimal value) throws BuilderException {
		Integer cashback = Integer.parseInt(value.toString());
		if (cashback >= 1000000) {
			throw new BuilderException("Cashback amount exceed.");
		} else if (cashback < 0) {
			throw new BuilderException("Cashback amount must not be less than zero.");
		}
	}

	private static BigDecimal validateAmount(BigDecimal amount) throws BuilderException {
		BigDecimal amount1mil = new BigDecimal("1000000");

		if (amount == null) {
			throw new BuilderException("Amount can not be null.");
		} else if ((amount.compareTo(BigDecimal.ZERO) > 0) && (amount.compareTo(amount1mil) < 0)) {
			amount = amount.multiply(new BigDecimal("100"));
		} else if ((amount.compareTo(amount1mil) == 0) && (amount.compareTo(amount1mil) > 0)) {
			throw new BuilderException("Amount exceed.");
		} else {
			throw new BuilderException("Invalid input amount.");
		}

		return amount;
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