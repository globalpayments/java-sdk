package com.global.api.terminals;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.UpaConfigContent;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.Entities.TokenInfo;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.terminals.upa.subgroups.UpaSafReportParams;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class DeviceInterface<TResult extends DeviceController> implements IDeviceInterface {
    protected final TResult _controller;
    public static final String ERROR_MESSAGE = "This method is not supported by the currently configured device.";

    @Getter @Setter
    String ecrId;

    protected IMessageSentInterface onMessageSent;
    protected IMessageReceivedInterface onMessageReceived;

    @Override
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    @Override
    public void setOnMessageReceived(IMessageReceivedInterface onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public DeviceInterface(TResult controller) {
        _controller = controller;
        _controller.setOnMessageSent(message -> {
            if(onMessageSent != null)
                onMessageSent.messageSent(message);
        });
        _controller.setOnMessageReceived(message -> {
            if(onMessageReceived != null) {
                onMessageReceived.messageReceived(message);
            }
        });
    }

    @Override
    public IDeviceResponse broadcastConfiguration(boolean enable) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse cancel() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse executeUDDataFile(UDData udData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse getConfigContents(TerminalConfigType configType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse getDebugInfo(Enum logFile) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse getDebugLevel() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public String getParams() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IInitializeResponse initialize() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse lineItem(String leftText, String rightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse loadUDDataFile(UDData udData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse Print(PrintData printData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse print(PrintData printData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISignatureResponse promptForSignature() throws ApiException {
        return promptForSignature(null);
    }

    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse reboot() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse removeUDDataFile(UDData udData) throws ApiException, UnsupportedOperationException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse reset() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse returnToIdle() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse Scan(ScanData scanData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse scan(ScanData scanData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse sendReady() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse setDebugLevel(DebugLevel[] debugLevels, Enum logToConsole) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IEODResponse endOfDay() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IBatchReportResponse findBatches() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IBatchReportResponse getBatchDetails() throws ApiException {
        return getBatchDetails(null);
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId) throws ApiException {
        return getBatchDetails(null, false);
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalReportBuilder getBatchReport() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISAFResponse safSummaryReportInBackground(UpaSafReportParams reportParams) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public ISAFResponse sendStoreAndForward() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder ebtPurchase() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder addValue() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder addValue(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder authorize(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder balance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift);
    }

    @Override
    public TerminalManageBuilder capture() throws ApiException {
        return capture(null);
    }

    @Override
    public TerminalManageBuilder capture(BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder debitRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder deletePreAuth() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder purchase() throws ApiException {
        return purchase(null);
    }

    @Override
    public TerminalAuthBuilder purchase(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder refund() throws ApiException {
        return refund(null);
    }

    @Override
    public TerminalAuthBuilder refund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalManageBuilder refundById() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder refundById(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder reverse() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder sale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder startTransaction(BigDecimal amount, TransactionType transactionType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder verify() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit);
    }

    @Override
    public TerminalManageBuilder Void() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    @Override
    public TerminalManageBuilder voidTransaction() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    @Override
    public TerminalAuthBuilder withdrawal() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalAuthBuilder withdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public void dispose() { /* NOM NOM */  }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder updateTaxInfo(BigDecimal taxAmount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse communicationCheck() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse logOn() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public TerminalManageBuilder updateLodgingDetails(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse injectCarouselLogo(UDData uDData) throws ApiException {
        throw new MessageException(ERROR_MESSAGE);
    }

    public IDeviceResponse removeCarouselLogo(UDData uDData) throws ApiException {
        throw new MessageException(ERROR_MESSAGE);
    }

    public IDeviceResponse manageToken(TokenInfo tokenInfo) throws ApiException {
        throw new MessageException(ERROR_MESSAGE);
    }

    public IDeviceResponse getLastEod() throws ApiException {
        throw new MessageException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse saveConfigFile(UpaConfigContent upaConfigContent) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse setLogoCarouselInterval(int intervalTime,boolean isFullScreen) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    /**
     * @return
     * @throws ApiException
     */
    @Override
    public IDeviceResponse getBatteryPercentage() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }
}
