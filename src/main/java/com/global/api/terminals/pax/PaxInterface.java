package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.pax.responses.BatchCloseResponse;
import com.global.api.terminals.pax.responses.InitializeResponse;
import com.global.api.terminals.pax.responses.PaxDeviceResponse;

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
    public TerminalAuthBuilder creditAuth(int referenceNumber) throws ApiException {
        return creditAuth(referenceNumber, null);
    }
    public TerminalAuthBuilder creditAuth(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalManageBuilder creditCapture(int referenceNumber) throws ApiException {
        return creditCapture(referenceNumber, null);
    }
    public TerminalManageBuilder creditCapture(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditRefund(int referenceNumber) throws ApiException {
        return creditRefund(referenceNumber, null);
    }
    public TerminalAuthBuilder creditRefund(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditSale(int referenceNumber) throws ApiException {
        return creditSale(referenceNumber, null);
    }
    public TerminalAuthBuilder creditSale(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder creditVerify(int referenceNumber) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit).withReferenceNumber(referenceNumber);
    }

    public TerminalManageBuilder creditVoid(int referenceNumber) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit).withReferenceNumber(referenceNumber);
    }
    //</editor-fold>

    //<editor-fold desc="DEBIT MESSAGES">
    public TerminalAuthBuilder debitRefund(int referenceNumber) throws ApiException {
        return debitRefund(referenceNumber, null);
    }
    public TerminalAuthBuilder debitRefund(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder debitSale(int referenceNumber) throws ApiException {
        return debitSale(referenceNumber, null);
    }
    public TerminalAuthBuilder debitSale(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit).withReferenceNumber(referenceNumber).withAmount(amount);
    }
    //</editor-fold>

    //<editor-fold desc="EBT MESSAGES">
    public TerminalAuthBuilder ebtBalance(int referenceNumber) {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.EBT).withReferenceNumber(referenceNumber);
    }

    public TerminalAuthBuilder ebtPurchase(int referenceNumber) {
        return ebtPurchase(referenceNumber, null);
    }
    public TerminalAuthBuilder ebtPurchase(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder ebtRefund(int referenceNumber) {
        return ebtRefund(referenceNumber, null);
    }
    public TerminalAuthBuilder ebtRefund(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.EBT).withReferenceNumber(referenceNumber).withAmount(amount);
    }

    public TerminalAuthBuilder ebtWithdrawal(int referenceNumber) {
        return ebtWithdrawal(referenceNumber, null);
    }
    public TerminalAuthBuilder ebtWithdrawal(int referenceNumber, BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.BenefitWithdrawal, PaymentMethodType.EBT).withReferenceNumber(referenceNumber).withAmount(amount);
    }
    //</editor-fold>

    //<editor-fold desc="GIFT MESSAGES">
    public TerminalAuthBuilder giftSale(int referenceNumber) throws ApiException {
        return giftSale(referenceNumber, null);
    }
    public TerminalAuthBuilder giftSale(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Gift).withReferenceNumber(referenceNumber).withAmount(amount).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue(int referenceNumber) throws ApiException {
        return giftAddValue(referenceNumber, null);
    }
    public TerminalAuthBuilder giftAddValue(int referenceNumber, BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withReferenceNumber(referenceNumber)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid(int referenceNumber) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Gift).withReferenceNumber(referenceNumber).withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance(int referenceNumber) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift).withReferenceNumber(referenceNumber).withCurrency(CurrencyType.Currency);
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
