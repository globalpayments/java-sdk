package com.global.api.terminals.abstractions;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.messaging.IMessageSentInterface;

import java.math.BigDecimal;

public interface IDeviceInterface extends IDisposable {
    void setOnMessageSent(IMessageSentInterface onMessageSent);

    // admin calls
    IDeviceResponse disableHostResponseBeep() throws ApiException;
    IInitializeResponse initialize() throws ApiException;
    IDeviceResponse reboot() throws ApiException;
    IDeviceResponse reset() throws ApiException;
    void cancel() throws ApiException;
    IDeviceResponse openLane() throws ApiException;
    IDeviceResponse closeLane() throws ApiException;
    ISignatureResponse getSignatureFile() throws ApiException;
    ISignatureResponse promptForSignature() throws ApiException;
    ISignatureResponse promptForSignature(String transactionId) throws ApiException;
    IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException;
    IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException;
    ISAFResponse sendStoreAndForward() throws ApiException;
    IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException;

    // batch calls
    IBatchCloseResponse batchClose() throws ApiException;

    // credit calls
    TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException;
    TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditAuth() throws ApiException;
    TerminalManageBuilder creditCapture() throws ApiException;
    TerminalAuthBuilder creditRefund() throws ApiException;
    TerminalAuthBuilder creditSale() throws ApiException;
    TerminalAuthBuilder creditVerify() throws ApiException;
    TerminalManageBuilder creditVoid() throws ApiException;

    // debit calls
    TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder debitSale() throws ApiException;
    TerminalAuthBuilder debitRefund() throws ApiException;

    // gift calls
    TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder giftSale() throws ApiException;
    TerminalAuthBuilder giftAddValue() throws ApiException;
    TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException;
    TerminalManageBuilder giftVoid() throws ApiException;
    TerminalAuthBuilder giftBalance() throws ApiException;

    // ebt calls
    TerminalAuthBuilder ebtBalance() throws ApiException;
    TerminalAuthBuilder ebtPurchase() throws ApiException;
    TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder ebtRefund() throws ApiException;
    TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException;
    TerminalAuthBuilder ebtWithdrawal() throws ApiException;
    TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException;
}
