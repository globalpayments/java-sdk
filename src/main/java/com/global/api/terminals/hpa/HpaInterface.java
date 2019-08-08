package com.global.api.terminals.hpa;

import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.HpaMsgId;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.hpa.builders.HpaAdminBuilder;
import com.global.api.terminals.hpa.responses.SipBaseResponse;
import com.global.api.terminals.hpa.responses.BatchResponse;
import com.global.api.terminals.hpa.responses.EODResponse;
import com.global.api.terminals.hpa.responses.InitializeResponse;
import com.global.api.terminals.hpa.responses.SAFResponse;
import com.global.api.terminals.hpa.responses.SignatureResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class HpaInterface implements IDeviceInterface {
    private HpaController _controller;

    private IMessageSentInterface onMessageSent;
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
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
}
