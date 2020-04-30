package com.global.api.terminals;

import java.math.BigDecimal;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.SendFileType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public abstract class DeviceInterface<T extends DeviceController> implements IDeviceInterface {
	protected T _controller;
	protected IRequestIdProvider _requestIdProvider;

	public IMessageSentInterface onMessageSent;
	public IBroadcastMessageInterface onBroadcastMessage;

	public void setOnMessageSent(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setOnBroadcastMessageReceived(IBroadcastMessageInterface onBroadcastMessage) {
		this.onBroadcastMessage = onBroadcastMessage;
	}

	public DeviceInterface(T controller) {
		_controller = controller;
		_controller.setOnMessageSentHandler(new IMessageSentInterface() {
			public void messageSent(String message) {
				if (onMessageSent != null)
					onMessageSent.messageSent(message);
			}
		});

		_controller.setOnBroadcastMessageHandler(new IBroadcastMessageInterface() {
			public void broadcastReceived(String code, String message) {
				if (onBroadcastMessage != null)
					onBroadcastMessage.broadcastReceived(code, message);
			}
		});
		
		_requestIdProvider  = _controller.requestIdProvider();
	}

	// admin methods
	public IDeviceResponse cancel() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}
	public IDeviceResponse closeLane() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse disableHostResponseBeep() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public ISignatureResponse getSignatureFile() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IInitializeResponse initialize() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText,
			String runningRightText) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse openLane() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse reboot() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse reset() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public ISAFResponse sendStoreAndForward() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IEODResponse endOfDay() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// batching
	public IBatchCloseResponse batchClose() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// credit calls
	public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditAuth() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalManageBuilder creditCapture() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditRefund() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditSale() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder creditVerify() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalManageBuilder creditVoid() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// debit calls
	public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder debitSale() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder debitRefund() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// gift calls
	public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder giftSale() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder giftAddValue() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalManageBuilder giftVoid() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder giftBalance() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// ebt calls
	public TerminalAuthBuilder ebtBalance() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtPurchase() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtRefund() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// generic calls
	public TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalManageBuilder capture(BigDecimal amount) throws ApiException {
		return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder refund(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder sale(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder verify() throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit)
				.withAmount(new BigDecimal(0.01));
	}

	public TerminalReportBuilder getReport(ReportTypes type) throws ApiException {
		return new TerminalReportBuilder(type);
	}

	public TerminalReportBuilder getLastReceipt(ReceiptType type) throws ApiException {
		return new TerminalReportBuilder(type);
	}

	public IDeviceResponse duplicate() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	// for confirmation to RE
	public TerminalManageBuilder reverse(BigDecimal amount) throws ApiException {
		return new TerminalManageBuilder(TransactionType.Reversal, PaymentMethodType.Credit).withAmount(amount);
	}

	public void dispose() {
		_controller.dispose();
	}
}
