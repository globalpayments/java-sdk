package com.global.api.terminals.genius;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.builders.MitcManageBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.subgroups.PrintData;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;

import java.math.BigDecimal;

public class GeniusInterface implements IDeviceInterface {

    private final GeniusController controller;

    public GeniusInterface(GeniusController _controller){
        super();
        controller = _controller;
    }

    @Override
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit).withAmount(amount);
    }
    @Override
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
    }
    public MitcManageBuilder refundById(BigDecimal amount) throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale,null,TransactionType.Refund).withAmount(amount);
    }

    @Override
    public ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        throw  new UnsupportedTransactionException();
    }

    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        return this.controller.processReport(transactionType, transactionId, transactionIdType);
    }
    @Override
    public TerminalManageBuilder creditVoid() throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale, PaymentMethodType.Credit, TransactionType.Void);
    }
    public MitcManageBuilder debitVoid() throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale, PaymentMethodType.Debit,TransactionType.Void);
    }
    public MitcManageBuilder voidRefund() throws ApiException {
        return new MitcManageBuilder(TransactionType.Refund, PaymentMethodType.Credit ,TransactionType.Void);
    }
    @Override
    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit).withAmount(amount);
    }

    /* Not implemented methods */
    @Override
    public TerminalAuthBuilder creditAuth() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalManageBuilder creditCapture() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder creditRefund() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder creditSale() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public void cancel() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public void cancel(Integer cancelParams) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IInitializeResponse initialize() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IDeviceResponse ping() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public void sendReady() throws ApiException {

    }

    @Override
    public IDeviceResponse registerPOS(RegisterPOS data) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IDeviceResponse printReceipt(PrintData data) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IDeviceResponse reboot() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse reset() throws ApiException {
        throw new UnsupportedTransactionException();
    }


    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException{
        throw new UnsupportedTransactionException();
    }

    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        throw new UnsupportedTransactionException();
    }


    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {

    }
    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IEODResponse endOfDay() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder creditVerify() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder debitSale() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder debitRefund() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtBalance() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtPurchase() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtRefund() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder giftSale() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalManageBuilder giftVoid() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public TerminalAuthBuilder giftBalance() throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public ISAFResponse sendStoreAndForward() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public ISignatureResponse promptForSignature() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public TerminalManageBuilder reverse() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public IBatchReportResponse getBatchSummary() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IBatchReportResponse getBatchSummary(String batchId) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IBatchReportResponse getBatchDetails() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        throw new UnsupportedTransactionException();
    }
    @Override
    public void dispose() {

    }


}
