package com.global.api.terminals.abstractions;

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

    // batch calls
    IBatchCloseResponse batchClose() throws ApiException;

    // credit calls
    TerminalAuthBuilder creditAuth(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalManageBuilder creditCapture(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditRefund(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditSale(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder creditAuth(int referenceNumber) throws ApiException;
    TerminalManageBuilder creditCapture(int referenceNumber) throws ApiException;
    TerminalAuthBuilder creditRefund(int referenceNumber) throws ApiException;
    TerminalAuthBuilder creditSale(int referenceNumber) throws ApiException;
    TerminalAuthBuilder creditVerify(int referenceNumber) throws ApiException;
    TerminalManageBuilder creditVoid(int referenceNumber) throws ApiException;

    // debit calls
    TerminalAuthBuilder debitSale(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder debitRefund(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder debitSale(int referenceNumber) throws ApiException;
    TerminalAuthBuilder debitRefund(int referenceNumber) throws ApiException;

    // gift calls
    TerminalAuthBuilder giftSale(int referenceNumber, BigDecimal amount) throws ApiException;
    TerminalAuthBuilder giftSale(int referenceNumber) throws ApiException;
    TerminalAuthBuilder giftAddValue(int referenceNumber) throws ApiException;
    TerminalManageBuilder giftVoid(int referenceNumber) throws ApiException;
    TerminalAuthBuilder giftBalance(int referenceNumber) throws ApiException;
}
