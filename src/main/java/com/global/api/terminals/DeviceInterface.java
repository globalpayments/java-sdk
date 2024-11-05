package com.global.api.terminals;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
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
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TimeZone;

public abstract class DeviceInterface implements IDeviceInterface {
    public static final String ERROR_MESSAGE = "This method is not supported by the currently configured device.";
    @Getter
    @Setter
    String ecrId;
    /**
     * @param onMessageSent
     */
    @Override
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {

    }

    /**
     * @param onMessageReceived
     */
    @Override
    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {

    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #lineItem(String, String)}  will be added
     */
    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #lineItem(String, String, String, String)} will be added
     */
    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param leftText
     * @param rightText
     * @param runningLeftText
     * @param runningRightText
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param leftText
     * @param rightText
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse lineItem(String leftText, String rightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }


    /**
     * @throws ApiException
     */
    @Override
    public void cancel() throws ApiException {

    }

    /**
     * @param cancelParams
     * @throws ApiException
     */
    @Override
    public void cancel(Integer cancelParams) throws ApiException {

    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IInitializeResponse initialize() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse ping() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getAppInfo() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse clearDataLake() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse returnToIdle() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param udData
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse loadUDDataFile(UDData udData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param udData
     * @return
     * @throws ApiException
     * @throws UnsupportedOperationException
     */
    @Override
    public IDeviceResponse removeUDDataFile(UDData udData) throws ApiException, UnsupportedOperationException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param scanData
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse Scan(ScanData scanData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param printData
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse Print(PrintData printData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param timezone
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse setTimeZone(TimeZone timezone) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param parameters
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getParams(ArrayList<String> parameters) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @throws ApiException
     */
    @Override
    public void sendReady() throws ApiException {

    }

    /**
     * @param data
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse registerPOS(RegisterPOS data) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param debugLevels
     * @param logToConsole
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse setDebugLevel(DebugLevel[] debugLevels, Enum logToConsole) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getDebugLevel() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param logFile
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getDebugInfo(Enum logFile) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param enable
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse broadcastConfiguration(boolean enable) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param udData
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse executeUDDataFile(UDData udData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param udData
     * @return
     * @throws ApiException
     * @throws IOException
     */
    @Override
    public IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param configType
     * @return
     * @throws UnsupportedTransactionException
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getConfigContents(TerminalConfigType configType) throws UnsupportedTransactionException, ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param data
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse printReceipt(PrintData data) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public String getParams() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public ISignatureResponse promptForSignature() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param transactionId
     * @return
     * @throws ApiException
     */
    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse reboot() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse reset() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param fileType
     * @param filePath
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public ISAFResponse sendStoreAndForward() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param enabled
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param safMode
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param paymentMethodType
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder reverse() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param data
     * @return
     * @throws ApiException
     */
    @Override
    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param fileName
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param fileType
     * @param fileData
     * @param isHttpDeviceConnectionMode
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IEODResponse endOfDay() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param safUploadIndicator
     * @return
     * @throws ApiException
     */
    @Override
    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param safDeleteIndicator
     * @return
     * @throws ApiException
     */
    @Override
    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated      Will be removed with the next major increase (15.0.0)
     *                  A new method {@link #authorize(BigDecimal)} will be added
     */
    @Override
    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #capture(BigDecimal)}   will be added
     */
    @Override
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund(BigDecimal)}    will be added
     */
    @Override
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Override
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Override
    public TerminalAuthBuilder creditAuth() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #capture()}   will be added
     */
    @Override
    public TerminalManageBuilder creditCapture() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund()}    will be added
     */
    @Override
    public TerminalAuthBuilder creditRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Override
    public TerminalAuthBuilder creditSale() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #verify()} will be added
     */
    @Override
    public TerminalAuthBuilder creditVerify() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder verify() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Override
    public TerminalManageBuilder creditVoid() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder voidRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Override
    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund(BigDecimal)}    will be added
     */
    @Override
    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Override
    public TerminalAuthBuilder debitSale() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund()}    will be added
     */
    @Override
    public TerminalAuthBuilder debitRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Override
    public TerminalManageBuilder debitVoid() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #sale(BigDecimal)}  will be added
     */
    @Override
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     */
    @Override
    public TerminalAuthBuilder giftSale() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #Void()}  will be added
     */
    @Override
    public TerminalManageBuilder giftVoid() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated Will be removed with the next major increase (15.0.0)
     * A new method {@link #balance()} will be added
     */
    @Override
    public TerminalAuthBuilder giftBalance() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated Will be removed with the next major increase (15.0.0)
     * A new method {@link #balance()} will be added
     */
    @Override
    public TerminalAuthBuilder ebtBalance() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder ebtPurchase() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund()}  will be added
     */
    @Override
    public TerminalAuthBuilder ebtRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @deprecated     Will be removed with the next major increase (15.0.0)
     *                 A new method {@link #refund(BigDecimal)}  will be added
     */
    @Override
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param safReportIndicator
     * @return
     * @throws ApiException
     */
    @Override
    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getBatchSummary() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param batchId
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getBatchSummary(String batchId) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getBatchDetails() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param batchId
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getBatchDetails(String batchId) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param batchId
     * @param printReport
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IBatchReportResponse findBatches() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param referenceNumber
     * @param transactionNumber
     * @return
     * @throws ApiException
     */
    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param transactionType
     * @param transactionId
     * @param transactionIdType
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder refundById(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder refundById() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param printData
     * @param reportData
     * @return
     * @throws ApiException
     */
    @Override
    public ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @param amount
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder deletePreAuth() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     *
     */
    @Override
    public void dispose() {

    }

    @Override
    public TerminalAuthBuilder balance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift);
    }

    @Override
    public TerminalAuthBuilder sale(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder authorize(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder startTransaction(BigDecimal amount, TransactionType transactionType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder Void() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    public TerminalManageBuilder capture(BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    public TerminalManageBuilder capture() throws ApiException {
        return capture(null);
    }

    public TerminalAuthBuilder refund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    public TerminalAuthBuilder refund() throws ApiException {
        return refund(null);
    }

    public  TerminalReportBuilder getBatchReport() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }
}
