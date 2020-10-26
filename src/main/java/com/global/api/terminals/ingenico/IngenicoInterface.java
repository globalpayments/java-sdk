package com.global.api.terminals.ingenico;

import java.math.BigDecimal;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.responses.POSIdentifierResponse;
import com.global.api.terminals.ingenico.responses.TerminalStateResponse;
import com.global.api.terminals.ingenico.variables.INGENICO_REQ_CMD;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.PaymentType;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;

public class IngenicoInterface extends DeviceInterface<IngenicoController> implements IDeviceInterface {
	private PaymentType paymentMethod = null;

	IngenicoInterface(IngenicoController controller) {
		super(controller);
	}

	public PaymentType getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentType paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public void setOnMessageSent(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setOnBroadcastMessageReceived(IBroadcastMessageInterface onBroadcastReceived) {
		this.onBroadcastMessage = onBroadcastReceived;
	}

	public void setOnPayAtTableRequest(IPayAtTableRequestInterface onPayAtTableRequest) {
		this.onPayAtTableRequest = onPayAtTableRequest;
	}

	public ISignatureResponse promptForSignature() throws ApiException {
		return null;
	}

	@Override
	public TerminalAuthBuilder sale(BigDecimal amount) throws ApiException {
		paymentMethod = PaymentType.SALE;
		return super.sale(amount);
	}

	@Override
	public TerminalAuthBuilder refund(BigDecimal amount) throws ApiException {
		paymentMethod = PaymentType.REFUND;
		return super.refund(amount);
	}

	@Override
	public TerminalManageBuilder capture(BigDecimal amount) throws ApiException {
		paymentMethod = PaymentType.COMPLETION;
		return super.capture(amount);
	}

	@Override
	public TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException {
		paymentMethod = PaymentType.PREAUTH;
		return super.authorize(amount);
	}

	@Override
	public TerminalAuthBuilder verify() throws ApiException {
		paymentMethod = PaymentType.ACCOUNT_VERIFICATION;
		return super.verify();
	}

	@Override
	public TerminalReportBuilder getReport(ReportTypes type) throws ApiException {
		return super.getReport(type);
	}

	@Override
	public TerminalReportBuilder getLastReceipt(ReceiptType type) throws ApiException {
		return super.getLastReceipt(type);
	}

	@Override
	public TerminalAuthBuilder payAtTableResponse() throws ApiException {
		return super.payAtTableResponse();
	}

	@Override
	public IDeviceResponse duplicate() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().DUPLICATE);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response, ParseFormat.Transaction);
	}

	@Override
	public IDeviceResponse cancel() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().CANCEL);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response, ParseFormat.Transaction);
	}

	@Override
	public TerminalManageBuilder reverse(BigDecimal amount) throws ApiException {
		if (amount != null) {
			return super.reverse(amount);
		} else {
			throw new UnsupportedTransactionException("Amount can't be null");
		}
	}

	@Override
	public IDeviceResponse getTerminalConfiguration() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().CALL_TMS);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response, ParseFormat.Transaction);
	}

	@Override
	public IDeviceResponse testConnection() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().LOGON);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response, ParseFormat.Transaction);
	}

	@Override
	public IDeviceResponse getTerminalStatus() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().STATE);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new TerminalStateResponse(response);
	}

	@Override
	public IDeviceResponse reboot() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().RESET);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response, ParseFormat.Transaction);
	}

	@Override
	public IInitializeResponse initialize() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().PID);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new POSIdentifierResponse(response);
	}
}