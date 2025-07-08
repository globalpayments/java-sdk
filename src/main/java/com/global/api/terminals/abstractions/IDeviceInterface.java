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
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.terminals.upa.subgroups.UpaSafReportParams;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TimeZone;

public interface IDeviceInterface extends IDisposable {
    String getEcrId();

    void setEcrId(String ecrId);

    void setOnMessageSent(IMessageSentInterface onMessageSent);

    void setOnMessageReceived(IMessageReceivedInterface onMessageReceived);


    //<editor-fold description="ADMIN CALLS">

    IDeviceResponse broadcastConfiguration(boolean enable) throws ApiException;

    IDeviceResponse cancel() throws ApiException;

    // void cancel(Integer cancelParams) throws ApiException; // UPA

    // IDeviceResponse clearDataLake() throws ApiException; //UPA

    IDeviceResponse closeLane() throws ApiException;

    IDeviceResponse deleteImage(String fileName) throws ApiException;

    // IDeviceResponse disableHostResponseBeep() throws ApiException;

    IDeviceResponse executeUDDataFile(UDData udData) throws ApiException;

    // IDeviceResponse getAppInfo() throws ApiException; //UPA

    IDeviceResponse getConfigContents(TerminalConfigType configType) throws ApiException;

    IDeviceResponse getDebugInfo(Enum logFile) throws ApiException;

    IDeviceResponse getDebugLevel() throws ApiException;

    String getParams() throws ApiException; //UPA

    // IDeviceResponse getParams(ArrayList<String> parameters) throws ApiException; //UPA

    ISignatureResponse getSignatureFile() throws ApiException;

    ISignatureResponse getSignatureFile(SignatureData data) throws ApiException;

    IInitializeResponse initialize() throws ApiException;

    IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException;

    IDeviceResponse lineItem(String leftText, String rightText) throws ApiException;

    IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException;

    IDeviceResponse loadUDDataFile(UDData udData) throws ApiException;

    IDeviceResponse openLane() throws ApiException;

    // IDeviceResponse ping() throws ApiException; // UPA

    @Deprecated
    IDeviceResponse Print(PrintData printData) throws ApiException;

    IDeviceResponse print(PrintData printData) throws ApiException;

    // IDeviceResponse printReceipt(PrintData data) throws ApiException; //UPA

    ISignatureResponse promptForSignature() throws ApiException;

    ISignatureResponse promptForSignature(String transactionId) throws ApiException;

    IDeviceResponse reboot() throws ApiException;

    //IDeviceResponse registerPOS(RegisterPOS data) throws ApiException; //UPA

    IDeviceResponse removeUDDataFile(UDData udData) throws ApiException, UnsupportedOperationException;

    IDeviceResponse reset() throws ApiException;

    IDeviceResponse returnToIdle() throws ApiException;

    @Deprecated
    IDeviceResponse Scan(ScanData scanData) throws ApiException;

    IDeviceResponse scan(ScanData scanData) throws ApiException;

    IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException;

    IDeviceResponse sendReady() throws ApiException; //UPA

    IDeviceResponse setDebugLevel(DebugLevel[] debugLevels, Enum logToConsole) throws ApiException;

    //IDeviceResponse setTimeZone(TimeZone timezone) throws ApiException; //UPA

    IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException;

    IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException;

    //</editor-fold>

    //<editor-fold description="BATCHING">

    IBatchCloseResponse batchClose() throws ApiException;

    IEODResponse endOfDay() throws ApiException;

    IBatchReportResponse findBatches() throws ApiException;

    IBatchReportResponse getBatchDetails() throws ApiException;

    IBatchReportResponse getBatchDetails(String batchId) throws ApiException;

    IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException;

    TerminalReportBuilder getBatchReport() throws ApiException;

    //</editor-fold>

    //<editor-fold description="STORE AND FORWARD">

    SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException;

    ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException;

    SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException;

    ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException;

    ISAFResponse safSummaryReportInBackground(UpaSafReportParams reportParams) throws ApiException;

    SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException;

    ISAFResponse sendStoreAndForward() throws ApiException;

    IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException;

    IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException;

    //</editor-fold>

    //<editor-fold description="GIFT">

    @Deprecated
    TerminalAuthBuilder giftAddValue() throws ApiException;

    @Deprecated
    TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException;

    //</editor-fold>


    //<editor-fold description="EBT">

    @Deprecated
    TerminalAuthBuilder ebtPurchase() throws ApiException;

    @Deprecated
    TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException;

    @Deprecated
    TerminalAuthBuilder ebtWithdrawal() throws ApiException;

    @Deprecated
    TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException;

    //</editor-fold>


    //<editor-fold description="REPORTING">

    IBatchReportResponse getOpenTabDetails() throws ApiException;

    TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException;

    TerminalManageBuilder increasePreAuth(BigDecimal amount) throws ApiException;

    TerminalReportBuilder localDetailReport() throws ApiException;

    //</editor-fold>

    //<editor-fold description="PROCESSING">

    TerminalAuthBuilder addValue() throws ApiException;

    TerminalAuthBuilder addValue(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder balance() throws ApiException;

    TerminalManageBuilder capture() throws ApiException;

    TerminalManageBuilder capture(BigDecimal amount) throws ApiException;

    @Deprecated
    TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException;

    @Deprecated
    TerminalAuthBuilder debitRefund() throws ApiException;

    TerminalManageBuilder deletePreAuth() throws ApiException;

    TerminalAuthBuilder purchase() throws ApiException;

    TerminalAuthBuilder purchase(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder refund() throws ApiException;

    TerminalAuthBuilder refund(BigDecimal amount) throws ApiException;

    TerminalManageBuilder refundById() throws ApiException;

    TerminalManageBuilder refundById(BigDecimal amount) throws ApiException;

    TerminalManageBuilder reverse() throws ApiException;

    TerminalAuthBuilder sale(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder startTransaction(BigDecimal amount, TransactionType transactionType) throws ApiException;

    TerminalManageBuilder tipAdjust(BigDecimal amount) throws ApiException;

    TerminalAuthBuilder verify() throws ApiException;

    @Deprecated
    TerminalManageBuilder Void() throws ApiException;

    TerminalManageBuilder voidTransaction() throws ApiException;

    TerminalAuthBuilder withdrawal() throws ApiException;

    TerminalAuthBuilder withdrawal(BigDecimal amount) throws ApiException;
}