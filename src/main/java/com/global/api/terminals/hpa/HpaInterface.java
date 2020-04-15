package com.global.api.terminals.hpa;

import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.HpaMsgId;
import com.global.api.entities.enums.SafDelete;
import com.global.api.entities.enums.SafMode;
import com.global.api.entities.enums.SafReportSummary;
import com.global.api.entities.enums.SafUpload;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.SendFileType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.hpa.builders.HpaAdminBuilder;
import com.global.api.terminals.hpa.responses.SipBaseResponse;
import com.global.api.terminals.hpa.responses.SipSendFileResponse;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.hpa.responses.BatchResponse;
import com.global.api.terminals.hpa.responses.EODResponse;
import com.global.api.terminals.hpa.responses.InitializeResponse;
import com.global.api.terminals.hpa.responses.SAFResponse;
import com.global.api.terminals.hpa.responses.SignatureResponse;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

public class HpaInterface extends DeviceInterface<HpaController> implements IDeviceInterface {
    private HpaController _controller;

    HpaInterface(HpaController controller) {
    	super(controller);
    }
    
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException("Function is not supported by the Heartland Payment Application.");
    }

    public IDeviceResponse closeLane() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.LANE_CLOSE.getValue()));
    }

    public IInitializeResponse initialize() throws ApiException {
        return _controller.sendAdminMessage(InitializeResponse.class, new HpaAdminBuilder(HpaMsgId.GET_INFO_REPORT.getValue()));
    }

    public IDeviceResponse openLane() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.LANE_OPEN.getValue()));
    }

    public IDeviceResponse reboot() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.REBOOT.getValue()));
    }

    public IDeviceResponse reset() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.RESET.getValue()));
    }

    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException("Signature data for this device type is automatically returned in the terminal response.");
    }

    public ISignatureResponse promptForSignature() throws ApiException {
        return promptForSignature(null);
    }
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SIGNATURE_FORM.getValue())
                .set("FormText", "PLEASE SIGN YOUR NAME");
        return _controller.sendAdminMessage(SignatureResponse.class, builder);
    }

    /*
    * @Deprecated Replaced by {@link #endOfDay()}
     */
    @Deprecated
    public IBatchCloseResponse batchClose() throws ApiException {
        return _controller.sendAdminMessage(BatchResponse.class, new HpaAdminBuilder(HpaMsgId.BATCH_CLOSE.getValue(), HpaMsgId.GET_BATCH_REPORT.getValue()));
    }
    
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.START_CARD.getValue())
                .set("CardGroup", paymentMethodType.toString());
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }

    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        if(StringUtils.isNullOrEmpty(leftText)) {
            throw new ApiException("You need to provide at least the left text.");
        }

        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.LINE_ITEM.getValue())
                .set("LineItemTextLeft", leftText)
                .set("LineItemTextRight", rightText)
                .set("LineItemRunningTextLeft", runningLeftText)
                .set("LineItemRunningTextRight", runningRightText);
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }
    
    public ISAFResponse sendStoreAndForward() throws ApiException {
    	return _controller.sendAdminMessage(SAFResponse.class, new HpaAdminBuilder(HpaMsgId.SEND_SAF.getValue()));
    }
    
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
    	HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SET_PARAMETER.getValue())
                .set("FieldCount", "1")
                .set("Key", "STORMD")
                .set("Value", enabled ? "1" : "0");
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }
    
    public IDeviceResponse sendFile(SendFileType imageType, String filePath) throws ApiException {
        if(filePath == null) {
            throw new ApiException("Filename is required for SendFile");
        }

        //Load the File
        HpaFileUpload fileUpload = new HpaFileUpload(imageType, filePath);

        //Build the initial message
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SEND_FILE.getValue()) {{ setKeepAlive(true); }}
                .set("FileName", fileUpload.getFileName())
                .set("FileSize", fileUpload.getFileSize())
                .set("MultipleMessage", "1");

        SipSendFileResponse response = _controller.sendAdminMessage(SipSendFileResponse.class, builder);
        if(response.getDeviceResponseCode().equals("00")) {
            LinkedList<String> fileParts = fileUpload.getFileParts(response.getMaxDataSize() / 5);
            String lastElement = fileParts.getLast();

            for(String filePart: fileParts) {
                final String multipleMessage = filePart.equals(lastElement) ? "0" : "1";

                SipSendFileResponse dataResponse = _controller.sendAdminMessage(SipSendFileResponse.class,
                        new HpaAdminBuilder(HpaMsgId.SEND_FILE.getValue()) {{
                            setKeepAlive(multipleMessage.equals("1"));
                            setAwaitResponse(multipleMessage.equals("0"));
                        }}
                        .set("FileData", filePart)
                        .set("MultipleMessage", multipleMessage)
                    );

                if (dataResponse != null) {
                    response = dataResponse;
                }
            }
            return response;
        }
        else throw new ApiException(String.format("Failed to upload file: %s", response.getDeviceResponseText()));
    }

    public IEODResponse endOfDay() throws ApiException {
        return _controller.sendAdminMessage(EODResponse.class, new HpaAdminBuilder(
                HpaMsgId.END_OF_DAY.getValue(),
                HpaMsgId.REVERSAL.getValue(),
                HpaMsgId.EMV_OFFLINE_DECLINE.getValue(),
                HpaMsgId.EMV_TC.getValue(),
                HpaMsgId.ATTACHMENT.getValue(),
                HpaMsgId.SEND_SAF.getValue(),
                HpaMsgId.GET_BATCH_REPORT.getValue(),
                HpaMsgId.HEARTBEAT.getValue(),
                HpaMsgId.BATCH_CLOSE.getValue(),
                HpaMsgId.EMV_PARAMETER_DOWNLOAD.getValue(),
                HpaMsgId.TRANSACTION_CERTIFICATE.getValue())
        );
    }

    public TerminalAuthBuilder creditAuth() {
        return creditAuth(null);
    }
    public TerminalAuthBuilder creditAuth(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withAmount(amount);
    }

    public TerminalManageBuilder creditCapture() {
        return creditCapture(null);
    }
    public TerminalManageBuilder creditCapture(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit).withAmount(amount);
    }

    public TerminalAuthBuilder creditRefund() {
        return creditRefund(null);
    }
    public TerminalAuthBuilder creditRefund(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
    }

    public TerminalAuthBuilder creditSale() {
        return creditSale(null);
    }
    public TerminalAuthBuilder creditSale(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withAmount(amount);
    }

    public TerminalAuthBuilder creditVerify() {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit);
    }

    public TerminalManageBuilder creditVoid() {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder debitSale() {
        return debitSale(null);
    }
    public TerminalAuthBuilder debitSale(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit).withAmount(amount);
    }

    public TerminalAuthBuilder debitRefund() {
        return debitRefund(null);
    }
    public TerminalAuthBuilder debitRefund(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit).withAmount(amount);
    }

    public TerminalAuthBuilder giftSale() {
        return giftSale(null);
    }
    public TerminalAuthBuilder giftSale(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Gift).withAmount(amount).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue() {
        return giftAddValue(null);
    }
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid() {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit)
                .withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance() {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift).withCurrency(CurrencyType.Currency);
    }

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

    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        return ebtWithdrawal(null);
    }
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    public void dispose() {
        try { closeLane(); }
        catch(ApiException e) { /* NOM NOM */ }
        finally {
            _controller.dispose();
        }
    }

    public IDeviceResponse setStoreAndForwardMode(SafMode mode) throws ApiException {
    	if(mode == SafMode.STAY_ONLINE || mode == SafMode.STAY_OFFLINE) {
    		HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SET_PARAMETER.getValue())
    				.set("FieldCount", "1")
    				.set("Key", "STORMD")
    				.set("Value", mode.getValue());
    		return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    	} else {
    		throw new UnsupportedTransactionException("HPA only supports STAY_ONLINE or STAY_OFFLINE.");
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

	public void setOnBroadcastMessageReceived(IBroadcastMessageInterface onBroadcastReceived) {
		
	}

	public IDeviceResponse cancel() throws ApiException {
		return reset();
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