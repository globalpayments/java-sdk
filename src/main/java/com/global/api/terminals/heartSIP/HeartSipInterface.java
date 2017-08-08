package com.global.api.terminals.heartSIP;

import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.HsipMsgId;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.heartSIP.responses.SipBatchResponse;
import com.global.api.terminals.heartSIP.responses.SipBaseResponse;
import com.global.api.terminals.heartSIP.responses.SipInitializeResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;

import java.math.BigDecimal;

public class HeartSipInterface implements IDeviceInterface {
    private HeartSipController _controller;

    private IMessageSentInterface onMessageSent;
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public HeartSipInterface(HeartSipController controller) throws ConfigurationException {
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
        throw new UnsupportedTransactionException("Function is not supported by HeartSIP.");
    }

    public IDeviceResponse closeLane() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneClose</Request></SIP>", HsipMsgId.LANE_CLOSE.getValue());
    }

    public IInitializeResponse initialize() throws ApiException {
        return _controller.sendMessage(SipInitializeResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>GetAppInfoReport</Request></SIP>", HsipMsgId.GET_INFO_REPORT.getValue());
    }

    public IDeviceResponse openLane() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneOpen</Request></SIP>", HsipMsgId.LANE_OPEN.getValue());
    }

    public IDeviceResponse reboot() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reboot</Request></SIP>", HsipMsgId.REBOOT.getValue());
    }

    public IDeviceResponse reset() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reset</Request></SIP>", HsipMsgId.RESET.getValue());
    }

    public IBatchCloseResponse batchClose() throws ApiException {
        return _controller.sendMessage(SipBatchResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>CloseBatch</Request></SIP>", HsipMsgId.BATCH_CLOSE.getValue(), HsipMsgId.GET_BATCH_REPORT.getValue());
    }

    public TerminalAuthBuilder creditAuth(int referenceNumber) throws ApiException {
        return creditAuth(referenceNumber, null);
    }
    public TerminalAuthBuilder creditAuth(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalManageBuilder creditCapture(int referenceNumber) throws ApiException {
        return creditCapture(referenceNumber, null);
    }
    public TerminalManageBuilder creditCapture(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditRefund(int referenceNumber) {
        return creditRefund(referenceNumber, null);
    }
    public TerminalAuthBuilder creditRefund(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditSale(int referenceNumber) {
        return creditSale(referenceNumber, null);
    }
    public TerminalAuthBuilder creditSale(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditVerify(int referenceNumber) {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit).withReferenceNumber(referenceNumber);
    }

    public TerminalManageBuilder creditVoid(int referenceNumber) {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit).withReferenceNumber(referenceNumber);
    }

    public TerminalAuthBuilder debitSale(int referenceNumber) {
        return debitSale(referenceNumber, null);
    }
    public TerminalAuthBuilder debitSale(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder debitRefund(int referenceNumber) {
        return debitRefund(referenceNumber, null);
    }
    public TerminalAuthBuilder debitRefund(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder giftSale(int referenceNumber) {
        return giftSale(referenceNumber, null);
    }
    public TerminalAuthBuilder giftSale(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Gift).withReferenceNumber(referenceNumber).withAmount(amount).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue(int referenceNumber) {
        return giftAddValue(referenceNumber, null);
    }
    public TerminalAuthBuilder giftAddValue(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withReferenceNumber(referenceNumber)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid(int referenceNumber) {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit)
                .withReferenceNumber(referenceNumber)
                .withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance(int referenceNumber) {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift).withReferenceNumber(referenceNumber).withCurrency(CurrencyType.Currency);
    }

    public void dispose() {
        try { closeLane(); }
        catch(ApiException e) { /* NOM NOM */ }
        finally {
            _controller.dispose();
        }
    }
}
