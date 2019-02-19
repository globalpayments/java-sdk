package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.pax.responses.BatchCloseResponse;
import com.global.api.terminals.pax.responses.InitializeResponse;
import com.global.api.terminals.pax.responses.PaxDeviceResponse;
import com.global.api.terminals.pax.responses.SignatureResponse;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

class PaxInterface implements IDeviceInterface {
    private PaxController controller;
    private IMessageSentInterface onMessageSent;

    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    PaxInterface(PaxController controller) {
        this.controller = controller;
        this.controller.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    //<editor-fold desc="ADMIN MESSAGES">
    // A00 - INITIALIZE
    public IInitializeResponse initialize() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A00_INITIALIZE));
        return new InitializeResponse(response);
    }

    // A08 - GET SIGNATURE
    public ISignatureResponse getSignatureFile() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A08_GET_SIGNATURE, 0, ControlCodes.FS));
        return new SignatureResponse(response);
    }

    // A14 - CANCEL
    public void cancel() throws ApiException {
        if(controller.getConnectionMode() == ConnectionModes.HTTP)
            throw new MessageException("The cancel command is not available in HTTP mode.");
        controller.send(TerminalUtilities.buildRequest(PaxMsgId.A14_CANCEL));
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
                (transactionId != null) ? 1: 0, ControlCodes.FS,
                (transactionId != null) ? transactionId : "", ControlCodes.FS,
                (transactionId != null) ? "00" : "", ControlCodes.FS,
                300));
        SignatureResponse signatureResponse = new SignatureResponse(response);
        if(signatureResponse.getDeviceResponseCode() == "000000") {
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
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A04_SET_VARIABLE,
                "00",
                ControlCodes.FS,
                "hostRspBeep",
                ControlCodes.FS,
                "N"
        ));
        return new PaxDeviceResponse(response, PaxMsgId.A05_RSP_SET_VARIABLE);
    }

    public IDeviceResponse closeLane() throws ApiException {
        if(controller.getDeviceType().equals(DeviceType.PAX_S300))
            throw new UnsupportedTransactionException("The S300 does not support this call.");
        throw new UnsupportedTransactionException();
    }
    public IDeviceResponse openLane() throws ApiException {
        if(controller.getDeviceType().equals(DeviceType.PAX_S300))
            throw new UnsupportedTransactionException("The S300 does not support this call.");
        throw new UnsupportedTransactionException();
    }
    //</editor-fold>

    //<editor-fold desc="CREDIT MESSAGES">
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
    //</editor-fold>

    //<editor-fold desc="DEBIT MESSAGES">
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
    //</editor-fold>

    //<editor-fold desc="EBT MESSAGES">
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
    //</editor-fold>

    //<editor-fold desc="GIFT MESSAGES">
    public TerminalAuthBuilder giftSale() throws ApiException {
        return giftSale(null);
    }
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Gift).withAmount(amount).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue() throws ApiException {
        return giftAddValue(null);
    }
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Gift).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift).withCurrency(CurrencyType.Currency);
    }
    //</editor-fold>

    //<editor-fold desc="CASH MESSAGES">
    //</editor-fold>

    //<editor-fold desc="CHECK MESSAGES">
    //</editor-fold>

    //<editor-fold desc="BATCH MESSAGES">
    public IBatchCloseResponse batchClose() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(
                PaxMsgId.B00_BATCH_CLOSE,
                new SimpleDateFormat("YYYYMMDDhhmmss").format(new Date())));
        return new BatchCloseResponse(response);
    }
    //</editor-fold>

    //<editor-fold desc="REPORTING MESSAGES">
    //</editor-fold>

    public void dispose() {
        // not used
    }
}
