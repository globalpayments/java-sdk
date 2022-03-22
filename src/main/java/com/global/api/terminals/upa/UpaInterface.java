package com.global.api.terminals.upa;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.SafDelete;
import com.global.api.entities.enums.SafMode;
import com.global.api.entities.enums.SafReportSummary;
import com.global.api.entities.enums.SafUpload;
import com.global.api.entities.enums.SendFileType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.terminals.upa.builders.UpaTerminalManageBuilder;
import com.global.api.terminals.upa.responses.UpaDeviceResponse;
import com.global.api.terminals.upa.responses.UpaEODResponse;
import com.global.api.terminals.upa.responses.UpaReportResponse;
import com.global.api.utils.JsonDoc;

public class UpaInterface implements IDeviceInterface {
    private final UpaController controller;

    public UpaInterface(UpaController _controller) {
        super();
        controller = _controller;
    }

    public IDeviceResponse addLineItem(String leftText, String rightText) throws ApiException {
        JsonDoc param = new JsonDoc();

        if (leftText != null && leftText.length() <= 20) {
            if (leftText.length() > 20) {
                throw new ApiException("Left-side text has 20 char limit.");
            } else {
                param.set("lineItemLeft", leftText);
            }
        } else {
            throw new ApiException("Left-side text is required.");
        }

        if (rightText != null) {
            if (rightText.length() <= 10) {
                param.set("lineItemRight", rightText);
            } else {
                throw new ApiException("Right-side text has 10 char limit.");
            }
        }

        JsonDoc body = new JsonDoc();
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.LineItemDisplay,
            controller.getRequestId().toString(),
            body
        );

        JsonDoc responseObj = JsonDoc.parse(
            new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaDeviceResponse(responseObj, UpaMessageId.LineItemDisplay);  
    }

    public void cancel() throws ApiException {
        cancel(1);
    }

    public void cancel(Integer cancelParam) throws ApiException {
        JsonDoc param = new JsonDoc();
        param.set("displayOption", cancelParam);

        JsonDoc body = new JsonDoc();
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.CancelTransaction,
            controller.getRequestId().toString(),
            body
        );

        controller.send(message);
    }

    public TerminalAuthBuilder creditAuth() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    public TerminalManageBuilder creditCapture() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder creditRefund() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Credit)
            .withAmount(amount);
    }

    public TerminalAuthBuilder creditSale() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
            .withAmount(amount);
    }

    public TerminalAuthBuilder creditVerify() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Verify, PaymentMethodType.Credit);
    }

    public UpaTerminalManageBuilder creditVoid() throws ApiException {
        return new UpaTerminalManageBuilder(TransactionType.Void, PaymentMethodType.Credit);
    }

    public TerminalAuthBuilder debitRefund() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit)
            .withAmount(amount);
    }

    public TerminalAuthBuilder debitSale() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Debit)
            .withAmount(amount);
    }

    public TerminalAuthBuilder ebtBalance() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Balance, PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtPurchase() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT)
            .withAmount(amount);
    }

    public TerminalAuthBuilder ebtRefund() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.EBT)
            .withAmount(amount);
    }

    public IEODResponse endOfDay() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.EODProcessing,
            controller.getRequestId().toString(),
            null // no body for EOD
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
            new String(controller.send(message), StandardCharsets.UTF_8)
        );
        
        return new UpaEODResponse(responseObj);
    }

    public IBatchReportResponse getBatchSummary() throws ApiException {
        return getBatchSummary(null);
    }

    public IBatchReportResponse getBatchSummary(String value) throws ApiException {
        JsonDoc body = new JsonDoc();

        if (value != null) {
            JsonDoc param = new JsonDoc();
            param.set("batch", value);
            body.set("params", param);
        } else {
            body.set("params", "");
        }

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetBatchReport,
                controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    public IBatchReportResponse getBatchDetails() throws ApiException {
        return getBatchDetails(null, false);
    }

    public IBatchReportResponse getBatchDetails(String batchId) throws ApiException {
        return getBatchDetails(batchId, false);
    }

    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (batchId != null) param.set("batch", batchId);
        if (printReport) param.set("reportOutput", "Print|ReturnData");
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.GetBatchDetails,
            controller.getRequestId().toString(),
            body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetOpenTabDetails,
                controller.getRequestId().toString(),
                null
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    public IDeviceResponse ping() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.Ping,
            controller.getRequestId().toString(),
            null // no body for ping
        );

        JsonDoc responseObj = JsonDoc.parse(
            new String(controller.send(message), StandardCharsets.UTF_8)
        );
        
        return new UpaDeviceResponse(responseObj, UpaMessageId.Ping);
    }

    public IDeviceResponse reboot() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.Reboot,
            controller.getRequestId().toString(),
            null // no body for reboot
        );

        JsonDoc responseObj = JsonDoc.parse(
            new String(controller.send(message), StandardCharsets.UTF_8)
        );
        
        return new UpaDeviceResponse(responseObj, UpaMessageId.Reboot);
    }

    public IDeviceResponse reset() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
            UpaMessageId.Reboot,
            controller.getRequestId().toString(),
            null // no body for reboot
        );

        JsonDoc responseObj = JsonDoc.parse(
            new String(controller.send(message), StandardCharsets.UTF_8)
        );
        
        return new UpaDeviceResponse(responseObj, UpaMessageId.Restart);
    }

    public TerminalManageBuilder reverse() throws ApiException {
        return new UpaTerminalManageBuilder(TransactionType.Reversal, PaymentMethodType.Credit);
    }

    public ISAFResponse sendStoreAndForward() throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    // public ISAFResponse sendStoreAndForward() throws ApiException {
        // DeviceMessage message = TerminalUtilities.buildMessage(
        //     UpaMessageId.SendSAF,
        //     controller.getRequestId().toString(),
        //     null // no body for sendSAF
        // );

        // JsonDoc responseObj = JsonDoc.parse(
        //     new String(controller.send(message), StandardCharsets.UTF_8)
        // );

        // return new UpaSAFResponse(responseObj);
    // }

    public TerminalManageBuilder tipAdjust(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
            .withGratuity(amount);
    }

    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IInitializeResponse initialize() throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    public void dispose() {
        // unused in UPA
    }

    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        controller.setMessageSentHandler(onMessageSent);
    }

    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public ISignatureResponse promptForSignature() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IBatchCloseResponse batchClose() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder giftSale() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalManageBuilder giftVoid() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder giftBalance() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText,
            String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException();
    }
}
