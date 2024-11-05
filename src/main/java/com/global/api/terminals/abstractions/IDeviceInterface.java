package com.global.api.terminals.abstractions;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TimeZone;

public interface IDeviceInterface extends IDisposable {
    void setOnMessageSent(IMessageSentInterface onMessageSent);

    void setOnMessageReceived(IMessageSentInterface onMessageReceived);

    String getEcrId();
    void setEcrId(String ecrId);

    // admin calls
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #lineItem(String, String)}  will be added
     */
    @Deprecated
    IDeviceResponse addLineItem(String leftText, String rightText) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #lineItem(String, String, String, String)} will be added
     */
    @Deprecated
    IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException;

    IDeviceResponse lineItem(String leftText, String rightText) throws ApiException;

    IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException;

    void cancel() throws ApiException;

    void cancel(Integer cancelParams) throws ApiException; // UPA

    IDeviceResponse closeLane() throws ApiException;

    IDeviceResponse disableHostResponseBeep() throws ApiException;

    ISignatureResponse getSignatureFile() throws ApiException;

    IInitializeResponse initialize() throws ApiException;

    IDeviceResponse openLane() throws ApiException;

    IDeviceResponse ping() throws ApiException; // UPA

    IDeviceResponse getAppInfo() throws ApiException; //UPA

    IDeviceResponse clearDataLake() throws ApiException; //UPA

    IDeviceResponse returnToIdle() throws ApiException;

    IDeviceResponse loadUDDataFile(UDData udData) throws ApiException;

    IDeviceResponse removeUDDataFile(UDData udData) throws ApiException, UnsupportedOperationException;

    IDeviceResponse Scan(ScanData scanData) throws ApiException;

    IDeviceResponse Print(PrintData printData) throws ApiException;

    IDeviceResponse setTimeZone(TimeZone timezone) throws ApiException; //UPA

    IDeviceResponse getParams(ArrayList<String> parameters) throws ApiException; //UPA

    void sendReady() throws ApiException; //UPA

    IDeviceResponse registerPOS(RegisterPOS data) throws ApiException; //UPA

    IDeviceResponse setDebugLevel(DebugLevel[] debugLevels, Enum logToConsole) throws ApiException;

    IDeviceResponse getDebugLevel() throws ApiException;

    IDeviceResponse getDebugInfo(Enum logFile) throws ApiException;

    IDeviceResponse broadcastConfiguration(boolean enable) throws ApiException;

    IDeviceResponse executeUDDataFile(UDData udData) throws ApiException;

    IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException;

    IDeviceResponse getConfigContents(TerminalConfigType configType) throws UnsupportedTransactionException, ApiException;

    IDeviceResponse printReceipt(PrintData data) throws ApiException;//UPA

    String getParams() throws ApiException; //UPA

    ISignatureResponse promptForSignature() throws ApiException;

    ISignatureResponse promptForSignature(String transactionId) throws ApiException;

    IDeviceResponse reboot() throws ApiException;

    IDeviceResponse reset() throws ApiException;

    IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException;

    ISAFResponse sendStoreAndForward() throws ApiException;

    IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException;

    IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException;

    IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException;

    TerminalManageBuilder reverse() throws ApiException;

    ISignatureResponse getSignatureFile(SignatureData data) throws ApiException;

    IDeviceResponse deleteImage(String fileName) throws ApiException;

    IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException;


    // batch calls
    IBatchCloseResponse batchClose() throws ApiException;

    IEODResponse endOfDay() throws ApiException;

    SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException;

    SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException;

    // credit calls
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #authorize(BigDecimal)} will be added
     */
    @Deprecated
    TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #capture(BigDecimal)}   will be added
     */
    @Deprecated
    TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund(BigDecimal)}    will be added
     */
    @Deprecated
    TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Deprecated
    TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Deprecated
    TerminalAuthBuilder creditSale() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Deprecated
    TerminalAuthBuilder creditAuth() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #capture()} will be added
     */
    @Deprecated
    TerminalManageBuilder creditCapture() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund()}   will be added
     */
    @Deprecated
    TerminalAuthBuilder creditRefund() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #verify()} will be added
     */
    @Deprecated
    TerminalAuthBuilder creditVerify() throws ApiException;

    TerminalAuthBuilder verify() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Deprecated
    TerminalManageBuilder creditVoid() throws ApiException;

    TerminalManageBuilder voidRefund() throws ApiException;

    TerminalManageBuilder tipAdjust(BigDecimal amount) throws ApiException;

    // debit calls
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Deprecated
    TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Deprecated
    TerminalAuthBuilder debitSale() throws ApiException;

    TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder debitRefund() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Deprecated
    TerminalManageBuilder debitVoid() throws ApiException;

    // gift calls
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Deprecated
    TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Deprecated
    TerminalAuthBuilder giftSale() throws ApiException;

    TerminalAuthBuilder giftAddValue() throws ApiException;

    TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Deprecated
    TerminalManageBuilder giftVoid() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #balance()} will be added
     */
    @Deprecated
    TerminalAuthBuilder giftBalance() throws ApiException;

    // ebt calls
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #balance()} will be added
     */
    @Deprecated
    TerminalAuthBuilder ebtBalance() throws ApiException;

    TerminalAuthBuilder ebtPurchase() throws ApiException;

    TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException;
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund()}  will be added
     */
    @Deprecated
    TerminalAuthBuilder ebtRefund() throws ApiException;
    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund(BigDecimal)}  will be added
     */
    @Deprecated
    TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder ebtWithdrawal() throws ApiException;

    TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException;

    // report calls
    SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #getBatchReport()}   will be added
     */
    @Deprecated
    IBatchReportResponse getBatchSummary() throws ApiException;

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #getBatchReport()}   will be added
     */
    @Deprecated
    IBatchReportResponse getBatchSummary(String batchId) throws ApiException;

    IBatchReportResponse getBatchDetails() throws ApiException;

    IBatchReportResponse getBatchDetails(String batchId) throws ApiException;

    IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException;

    IBatchReportResponse getOpenTabDetails() throws ApiException;

    IBatchReportResponse findBatches() throws ApiException;

    ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException;

    TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException;

    TerminalManageBuilder refundById(BigDecimal amount) throws ApiException;

    TerminalManageBuilder refundById() throws ApiException;

    ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException;

    TerminalReportBuilder localDetailReport() throws ApiException;

    TerminalManageBuilder increasePreAuth(BigDecimal amount) throws ApiException;

    TerminalManageBuilder deletePreAuth() throws ApiException;

    TerminalAuthBuilder balance() throws ApiException;

    TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder startTransaction(BigDecimal amount, TransactionType transactionType) throws ApiException;

    TerminalManageBuilder Void() throws ApiException;

    TerminalManageBuilder capture() throws ApiException;

    TerminalManageBuilder capture(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder refund() throws ApiException;

    TerminalAuthBuilder refund(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder sale(BigDecimal amount) throws ApiException;

    TerminalReportBuilder getBatchReport() throws ApiException;
}