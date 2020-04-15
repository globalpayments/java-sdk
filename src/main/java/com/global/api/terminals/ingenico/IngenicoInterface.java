package com.global.api.terminals.ingenico;

import com.global.api.entities.enums.SafDelete;
import com.global.api.entities.enums.SafReportSummary;
import com.global.api.entities.enums.SafUpload;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.*;
import com.global.api.terminals.ingenico.responses.CancelResponse;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.variables.INGENICO_REQ_CMD;
import com.global.api.terminals.ingenico.variables.PaymentType;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.*;

import java.math.BigDecimal;

public class IngenicoInterface extends DeviceInterface<IngenicoController> implements IDeviceInterface {
	private PaymentType paymentMethod = null;

	public PaymentType getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentType paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	IngenicoInterface(IngenicoController controller) {
		super(controller);
	}

	public void setOnMessageSent(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setOnBroadcastMessageReceived(IBroadcastMessageInterface onBroadcastReceived) {
		this.onBroadcastMessage = onBroadcastReceived;
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

	// xml & report management
	@Override
	public TerminalReportBuilder getReport(ReportTypes type) throws ApiException {
		return super.getReport(type);
	}

	@Override
	public TerminalReportBuilder getLastReceipt(ReceiptType type) throws ApiException {
		return super.getLastReceipt(type);
	}

	@Override
	public IDeviceResponse duplicate() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().DUPLICATE);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new IngenicoTerminalResponse(response);
	}

	@Override
	public IDeviceResponse cancel() throws ApiException {
		StringBuilder sb = new StringBuilder();
		sb.append(new INGENICO_REQ_CMD().REQUEST_MESSAGE);
		sb.append(new INGENICO_REQ_CMD().CANCEL);

		byte[] response = _controller
				.send(TerminalUtilities.buildIngenicoRequest(sb.toString(), _controller.getConnectionModes()));
		return new CancelResponse(response);
	}

	@Override
	public TerminalManageBuilder reverse(BigDecimal amount) throws ApiException {
		if (amount != null) {
			return super.reverse(amount);
		} else {
			throw new UnsupportedTransactionException("Amount can't be null");
		}
	}

	public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}
}