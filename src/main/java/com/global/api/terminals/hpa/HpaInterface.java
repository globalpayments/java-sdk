package com.global.api.terminals.hpa;

import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.HpaMsgId;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.hpa.responses.SipBaseResponse;
import com.global.api.terminals.hpa.responses.SipBatchResponse;
import com.global.api.terminals.hpa.responses.SipInitializeResponse;
import com.global.api.terminals.hpa.responses.SipSignatureResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import java.math.BigDecimal;

public class HpaInterface implements IDeviceInterface {
    private HpaController _controller;

    private IMessageSentInterface onMessageSent;
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public HpaInterface(HpaController controller) throws ConfigurationException {
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
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneClose</Request><RequestId>"+ _controller.requestIdProvider.getRequestId() +"</RequestId></SIP>", HpaMsgId.LANE_CLOSE.getValue());
    }

    public IInitializeResponse initialize() throws ApiException {
        return _controller.sendMessage(SipInitializeResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>GetAppInfoReport</Request><RequestId>"+ _controller.requestIdProvider.getRequestId() +"</RequestId></SIP>", HpaMsgId.GET_INFO_REPORT.getValue());
    }
    public IDeviceResponse openLane() throws ApiException {
    	return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneOpen</Request><RequestId>"+ _controller.requestIdProvider.getRequestId() +"</RequestId></SIP>", HpaMsgId.LANE_OPEN.getValue());
    }

    public IDeviceResponse reboot() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reboot</Request><RequestId>"+ _controller.requestIdProvider.getRequestId() +"</RequestId></SIP>", HpaMsgId.REBOOT.getValue());
    }

    public IDeviceResponse reset() throws ApiException {
        return _controller.sendMessage(SipBaseResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reset</Request><RequestId>"+ _controller.requestIdProvider.getRequestId() +"</RequestId></SIP>", HpaMsgId.RESET.getValue());
    }

    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException("Signature data for this device type is automatically returned in the terminal response.");
    }

    public ISignatureResponse promptForSignature() throws ApiException {
        return promptForSignature(null);
    }
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        return _controller.sendMessage(SipSignatureResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>SignatureForm</Request><FormText>PLEASE SIGN YOUR NAME</FormText></SIP>", HpaMsgId.SIGNATURE_FORM.getValue());
    }

    public IBatchCloseResponse batchClose() throws ApiException {
        return _controller.sendMessage(SipBatchResponse.class, "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>CloseBatch</Request></SIP>", HpaMsgId.BATCH_CLOSE.getValue(), HpaMsgId.GET_BATCH_REPORT.getValue());
    }

    public TerminalAuthBuilder creditAuth() throws ApiException {
        return creditAuth(null);
    }
    public TerminalAuthBuilder creditAuth(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withAmount(amount);
    }

    public TerminalManageBuilder creditCapture() throws ApiException {
        return creditCapture(null);
    }
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
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
