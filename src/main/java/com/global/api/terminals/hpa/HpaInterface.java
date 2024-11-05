package com.global.api.terminals.hpa;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.hpa.builders.HpaAdminBuilder;
import com.global.api.terminals.hpa.responses.*;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TimeZone;

public class HpaInterface extends DeviceInterface {
    private HpaController _controller;
    private IMessageSentInterface onMessageSent;
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    @Override
    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {
        //Intentional left blank
    }

    HpaInterface(HpaController controller) {
        _controller = controller;
        _controller.setMessageSentHandler(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    public void cancel() throws ApiException {
        reset();
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
        return lineItem(leftText, rightText, runningLeftText, runningRightText);
    }

    public IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
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
        return authorize(null);
    }
    public TerminalAuthBuilder creditAuth(BigDecimal amount) {
        return authorize(amount);
    }

    public TerminalManageBuilder creditCapture() throws ApiException {
        return creditCapture(null);
    }
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return capture(amount);
    }

    public TerminalAuthBuilder creditRefund() throws ApiException {
        return creditRefund(null);
    }
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return refund(amount);
    }

    public TerminalAuthBuilder creditSale() {
        return sale(null);
    }
    public TerminalAuthBuilder creditSale(BigDecimal amount) {
        return sale(amount);
    }

    public TerminalAuthBuilder creditVerify() throws ApiException {
        return verify();
    }

    public TerminalManageBuilder creditVoid() throws ApiException {
        return Void();
    }

    public TerminalAuthBuilder debitSale() {
        return sale(null).withPaymentMethodType(PaymentMethodType.Debit);
    }
    public TerminalAuthBuilder debitSale(BigDecimal amount) {
        return sale(amount).withPaymentMethodType(PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder debitRefund() throws ApiException {
        return debitRefund(null);
    }

    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        return refund(amount)
                .withPaymentMethodType(PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder giftSale() {
        return giftSale(null);
    }
    public TerminalAuthBuilder giftSale(BigDecimal amount) {
        return sale(amount)
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue() {
        return giftAddValue(null);
    }
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid() throws ApiException {
        return Void().withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance() throws ApiException {
        return balance().withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder ebtBalance() throws ApiException {
        return balance().withPaymentMethodType(PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtPurchase() {
        return ebtPurchase(null);
    }
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT).withAmount(amount);
    }

    public TerminalAuthBuilder ebtRefund() throws ApiException {
        return ebtRefund(null);
    }
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        return refund(amount).withPaymentMethodType(PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        return ebtWithdrawal(null);
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
}
