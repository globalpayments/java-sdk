package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.pax.responses.BatchCloseResponse;
import com.global.api.terminals.pax.responses.InitializeResponse;
import com.global.api.terminals.pax.responses.PaxDeviceResponse;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.pax.responses.SignatureResponse;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

class PaxInterface extends DeviceInterface<PaxController> implements IDeviceInterface {
	private PaxController controller;

	PaxInterface(PaxController controller) {
		super(controller);
	}
	
	public void setOnMessageSent(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	// <editor-fold desc="ADMIN MESSAGES">
	// A00 - INITIALIZE
	public IInitializeResponse initialize() throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A00_INITIALIZE));
		return new InitializeResponse(response);
	}

	// A08 - GET SIGNATURE
	public ISignatureResponse getSignatureFile() throws ApiException {
		byte[] response = controller
				.send(TerminalUtilities.buildRequest(PaxMsgId.A08_GET_SIGNATURE, 0, ControlCodes.FS));
		return new SignatureResponse(response);
	}

	// A14 - CANCEL
	public IDeviceResponse cancel() throws ApiException {
		if (controller.getConnectionMode() == ConnectionModes.HTTP)
			throw new MessageException("The cancel command is not available in HTTP mode.");
		controller.send(TerminalUtilities.buildRequest(PaxMsgId.A14_CANCEL));
		return null;
	}

	// A16 - RESET
	public IDeviceResponse reset() throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A16_RESET));
		return new PaxDeviceResponse(response, PaxMsgId.A17_RSP_RESET);
	}

	// A20 - DO SIGNATURE
	public ISignatureResponse promptForSignature() throws ApiException {
		return promptForSignature(null);
	}

	public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A20_DO_SIGNATURE,
				(transactionId != null) ? 1 : 0, ControlCodes.FS, (transactionId != null) ? transactionId : "",
				ControlCodes.FS, (transactionId != null) ? "00" : "", ControlCodes.FS, 300));
		SignatureResponse signatureResponse = new SignatureResponse(response);
		if (signatureResponse.getDeviceResponseCode() == "000000") {
			return getSignatureFile();
		}
		return signatureResponse;
	}

	// A26 - REBOOT
	public IDeviceResponse reboot() throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A26_REBOOT));
		return new PaxDeviceResponse(response, PaxMsgId.A27_RSP_REBOOT);
	}

	public IDeviceResponse disableHostResponseBeep() throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A04_SET_VARIABLE, "00",
				ControlCodes.FS, "hostRspBeep", ControlCodes.FS, "N", ControlCodes.FS, ControlCodes.FS, ControlCodes.FS,
				ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS));
		return new PaxDeviceResponse(response, PaxMsgId.A05_RSP_SET_VARIABLE);
	}

	public IDeviceResponse closeLane() throws ApiException {
		if (controller.getDeviceType().equals(DeviceType.PAX_S300))
			throw new UnsupportedTransactionException("The S300 does not support this call.");
		throw new UnsupportedTransactionException();
	}

	public IDeviceResponse openLane() throws ApiException {
		if (controller.getDeviceType().equals(DeviceType.PAX_S300))
			throw new UnsupportedTransactionException("The S300 does not support this call.");
		throw new UnsupportedTransactionException();
	}

	public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
		throw new UnsupportedTransactionException();
	}

	public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText,
			String runningRightText) throws ApiException {
		if (controller.getDeviceType().equals(DeviceType.PAX_S300))
			throw new UnsupportedTransactionException("The S300 does not support this call.");
		throw new UnsupportedTransactionException();
	}

	public ISAFResponse sendStoreAndForward() throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}

	public IEODResponse endOfDay() throws ApiException {
		throw new UnsupportedTransactionException();
	}

	public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
		throw new UnsupportedTransactionException("This function is not supported by the currently configured device.");
	}
	// </editor-fold>

	// <editor-fold desc="CREDIT MESSAGES">
	public TerminalAuthBuilder creditAuth() throws ApiException {
		return creditAuth(null);
	}

	public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalManageBuilder creditCapture() throws ApiException {
		return creditCapture(null);
	}

	public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
		return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder creditRefund() throws ApiException {
		return creditRefund(null);
	}

	public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder creditSale() throws ApiException {
		return creditSale(null);
	}

	public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withAmount(amount);
	}

	public TerminalAuthBuilder creditVerify() throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit);
	}

	public TerminalManageBuilder creditVoid() throws ApiException {
		return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
	}
	// </editor-fold>

	// <editor-fold desc="DEBIT MESSAGES">
	public TerminalAuthBuilder debitRefund() throws ApiException {
		return debitRefund(null);
	}

	public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit).withAmount(amount);
	}

	public TerminalAuthBuilder debitSale() throws ApiException {
		return debitSale(null);
	}

	public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit).withAmount(amount);
	}
	// </editor-fold>

	// <editor-fold desc="EBT MESSAGES">
	public TerminalAuthBuilder ebtBalance() {
		return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.EBT);
	}

	public TerminalAuthBuilder ebtPurchase() {
		return ebtPurchase(null);
	}

	public TerminalAuthBuilder ebtPurchase(BigDecimal amount) {
		return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT).withAmount(amount);
	}

	public TerminalAuthBuilder ebtRefund() {
		return ebtRefund(null);
	}

	public TerminalAuthBuilder ebtRefund(BigDecimal amount) {
		return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.EBT).withAmount(amount);
	}

	public TerminalAuthBuilder ebtWithdrawal() {
		return ebtWithdrawal(null);
	}

	public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) {
		return new TerminalAuthBuilder(TransactionType.BenefitWithdrawal, PaymentMethodType.EBT).withAmount(amount);
	}
	// </editor-fold>

	// <editor-fold desc="GIFT MESSAGES">
	public TerminalAuthBuilder giftSale() throws ApiException {
		return giftSale(null);
	}

	public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Gift).withAmount(amount)
				.withCurrency(CurrencyType.Currency);
	}

	public TerminalAuthBuilder giftAddValue() throws ApiException {
		return giftAddValue(null);
	}

	public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
		return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
				.withCurrency(CurrencyType.Currency).withAmount(amount);
	}

	public TerminalManageBuilder giftVoid() throws ApiException {
		return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Gift)
				.withCurrency(CurrencyType.Currency);
	}

	public TerminalAuthBuilder giftBalance() throws ApiException {
		return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift)
				.withCurrency(CurrencyType.Currency);
	}
	// </editor-fold>

	// <editor-fold desc="CASH MESSAGES">
	// </editor-fold>

	// <editor-fold desc="CHECK MESSAGES">
	// </editor-fold>

	// <editor-fold desc="BATCH MESSAGES">
	public IBatchCloseResponse batchClose() throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.B00_BATCH_CLOSE,
				new SimpleDateFormat("YYYYMMDDhhmmss").format(new Date())));
		return new BatchCloseResponse(response);
	}
	// </editor-fold>

	// <editor-fold desc="REPORTING MESSAGES">
	// </editor-fold>

	public void dispose() {
		// not used
	}

	// SAF
	public IDeviceResponse setStoreAndForwardMode(SafMode mode) throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A54_SET_SAF_PARAMETERS, mode,
				ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS,
				ControlCodes.FS, ControlCodes.FS, ControlCodes.FS, ControlCodes.FS));
		return new PaxDeviceResponse(response, PaxMsgId.A55_RSP_SET_SAF_PARAMETERS);
	}

	public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
		byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.B08_SAF_UPLOAD, safUploadIndicator));
		SAFUploadResponse uploadResponse = new SAFUploadResponse(response);
		return uploadResponse;
	}

	public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
		byte[] response = controller
				.send(TerminalUtilities.buildRequest(PaxMsgId.B10_DELETE_SAF_FILE, safDeleteIndicator));
		SAFDeleteResponse deleteResponse = new SAFDeleteResponse(response);
		return deleteResponse;
	}

	public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
		byte[] response = controller
				.send(TerminalUtilities.buildRequest(PaxMsgId.R10_SAF_SUMMARY_REPORT, safReportIndicator));
		SAFSummaryReport summaryResponse = new SAFSummaryReport(response);
		return summaryResponse;
	}

	public void setOnBroadcastMessageReceived(IBroadcastMessageInterface onBroadcastReceived) {

	}

	public TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalManageBuilder capture(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalAuthBuilder refund(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalAuthBuilder sale(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalAuthBuilder verify() throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalReportBuilder getReport(ReportTypes reportTypes) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalReportBuilder getLastReceipt(ReceiptType receiptType) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public IDeviceResponse duplicate() throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}

	public TerminalManageBuilder reverse(BigDecimal amount) throws ApiException {
		throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
	}
}