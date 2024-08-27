package com.global.api.terminals.diamond.interfaces;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.enums.TerminalReportType;
import com.global.api.terminals.diamond.DiamondController;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.subgroups.PrintData;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiamondInterface implements IDeviceInterface {
    private final DiamondController controller;

    public DiamondInterface(DiamondController controller) {
        this.controller = controller;
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal tipAmount) {
        return (new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit))
                .withGratuity(tipAmount);
    }

    @Override
    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder debitSale() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder debitRefund() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder debitVoid() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder giftSale() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withAmount(amount);
    }

    @Override
    public TerminalManageBuilder giftVoid() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder giftBalance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift);
    }

    @Override
    public TerminalAuthBuilder ebtBalance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.Gift);
    }

    @Override
    public TerminalAuthBuilder ebtPurchase() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder ebtRefund() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        return new TerminalReportBuilder(TerminalReportType.LocalDetailReport);
    }

    @Override
    public IBatchReportResponse getBatchSummary() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IBatchReportResponse getBatchSummary(String batchId) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IBatchReportResponse getBatchDetails() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder deletePreAuth() {
        return (new TerminalManageBuilder(TransactionType.Delete, PaymentMethodType.Credit))
                .withTransactionModifier(TransactionModifier.DeletePreAuth);
    }

    @Override
    public ISAFResponse safSummaryReport(String printData, String reportData) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) {
        return (new TerminalManageBuilder(TransactionType.Auth, PaymentMethodType.Credit))
                .withTransactionModifier(TransactionModifier.Incremental)
                .withAmount(amount);
    }

    @Override
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {

    }

    @Override
    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {

    }

    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public void cancel() throws ApiException {

    }

    @Override
    public void cancel(Integer cancelParams) throws ApiException {

    }

    @Override
    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IInitializeResponse initialize() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse ping() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public void sendReady() throws ApiException {

    }

    @Override
    public IDeviceResponse registerPOS(RegisterPOS data) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse printReceipt(PrintData data) throws ApiException {
        return null;
    }

    @Override
    public String getParams() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public ISignatureResponse promptForSignature() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse reboot() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse reset() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public ISAFResponse sendStoreAndForward() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder reverse() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        return null;
    }

    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        return null;
    }

    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        return (IBatchCloseResponse) new TerminalAuthBuilder(TransactionType.BatchClose)
                .execute();
    }

    @Override
    public IEODResponse endOfDay() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder creditAuth() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit);
    }

    @Override
    public TerminalManageBuilder creditCapture() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(null);
    }

    @Override
    public TerminalAuthBuilder creditRefund() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
                .withAmount(null);
    }

    @Override
    public TerminalAuthBuilder creditSale() throws ApiException {
        return creditSale(null);
    }

    @Override
    public TerminalAuthBuilder creditVerify() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder creditVoid() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    @Override
    public TerminalManageBuilder voidRefund() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public TerminalManageBuilder refundById(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
    }

    @Override
    public TerminalManageBuilder refundById() {
        return new TerminalManageBuilder(TransactionType.Refund, PaymentMethodType.Credit);
    }

    @Override
    public void dispose() {

    }
}
