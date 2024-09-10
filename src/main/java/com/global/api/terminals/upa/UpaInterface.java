package com.global.api.terminals.upa;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.terminals.upa.builders.UpaTerminalManageBuilder;
import com.global.api.terminals.upa.responses.*;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TimeZone;

import static com.global.api.terminals.upa.Entities.Constants.READY_MESSAGE;

public class UpaInterface implements IDeviceInterface {
    private static final String ERROR_MESSAGE = "This method is not supported by the currently configured device.";
    private final UpaController controller;
    private static final String PROMPT_ONE = "prompt1";
    private static final String PROMPT_TWO = "prompt2";
    private static final String DISPLAY_OPTION = "displayOption";
    private static final String PARAMS = "params";
    private static final String TRANSACTION_NUMBER = "tranNo";
    private static final String REFERENCE_NUMBER = "referenceNumber";
    private static final String TRANSACTION = "transaction";

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

    public UpaTerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return new UpaTerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    public UpaTerminalManageBuilder creditCapture() throws ApiException {
        return new UpaTerminalManageBuilder(TransactionType.Capture, PaymentMethodType.Credit);
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

    @Override
    public TerminalManageBuilder voidRefund() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder debitRefund() throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Refund, PaymentMethodType.Debit)
                .withAmount(amount);
    }

    @Override
    public TerminalManageBuilder debitVoid() throws ApiException {
        return null;
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
                null
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

    @Override
    public IBatchReportResponse findBatches() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.AvailableBatches,
                controller.getRequestId().toString(),
                null
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    @Override
    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder refundById(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalManageBuilder refundById() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public void sendReady() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                READY_MESSAGE
        );
        controller.send(message);
    }

    public IDeviceResponse registerPOS(RegisterPOS data) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (data == null) {
            return new UpaDeviceResponse(null, UpaMessageId.RegisterPOS);
        }

        if (data.getAppName() == null || data.getAppName().isEmpty()) {
            throw new ApiException("The package name of the application is required.");
        } else {
            param.set("appName", data.getAppName());
        }

        if (data.getLaunchOrder() != null) {
            param.set("launchOrder", data.getLaunchOrder());
        }
        if (data.getRemove() != null) {
            param.set("remove", data.getRemove());
        }
        if (data.getSilent() != null) {
            param.set("silent", data.getSilent());
        }
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.RegisterPOS,
                controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaDeviceResponse(responseObj, UpaMessageId.RegisterPOS);
    }

    @Override
    public IDeviceResponse printReceipt(PrintData data) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (data == null) {
            throw new ApiException("Print data cannot be null!");
        }

        if (StringUtils.isNullOrEmpty(data.getContent())) {
            throw new ApiException("The image data cannot be null or empty.");
        }

        param.set("content", "data:image/jpeg;base64," + data.getContent());

        if (!StringUtils.isNullOrEmpty(data.getLine1())) {
            param.set("line1", data.getLine1());
        }

        if (!StringUtils.isNullOrEmpty(data.getLine2())) {
            param.set("line2", data.getLine2());
        }

        if (data.getDisplayOption() != null) {
            param.set("displayOption", data.getDisplayOption().getValue());
        }

        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.PrintData,
                controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaDeviceResponse(responseObj, UpaMessageId.PrintData);
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

    @Override
    public IDeviceResponse setDebugLevel(DebugLevel[] debugLevels, Enum logToConsole) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (debugLevels == null || debugLevels.length == 0) {
            throw new ApiException("At least one Debug Level is required.");
        }

        StringBuilder sb = new StringBuilder();
        for (DebugLevel level : debugLevels) {
            sb.append(level.name().toUpperCase()).append("|");
        }
        sb.deleteCharAt(sb.length() - 1); // Remove trailing '|'

        param.set("debugLevel", sb.toString());

        if (logToConsole != null) {
            param.set("logToConsole", ((DebugLogsOutput) logToConsole).getValue());
        }
        body.set("params", param);
        String requestId = controller.getRequestId().toString();
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.SetDebugLevel, requestId, body);
        byte[] responseBytes = controller.send(message);
        String responseString = new String(responseBytes, StandardCharsets.UTF_8);
        JsonDoc responseObj = JsonDoc.parse(responseString);
        return new UpaTransactionResponse(responseObj);
    }


    @Override
    public IDeviceResponse getDebugLevel() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetDebugLevel,
                controller.getRequestId().toString(),
                null
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse getDebugInfo(Enum logFile) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        if (logFile != null) {
            param.set("logFile", ((LogFile) logFile).getValue());
            body.set("params", param);
        }
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetDebugInfo,
                controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse broadcastConfiguration(boolean enable) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        param.set("enable", enable ? 1 : 0);
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.BroadcastConfiguration,
                controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse executeUDDataFile(UDData udData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        param.set("fileType", udData.getFileType().toString());
        param.set("slotNum", String.valueOf(udData.getSlot()));
        param.set("displayOption", udData.getDisplayOption() != null ? udData.getDisplayOption().getValue() : null);
        body.set("params", param);
        DeviceMessage deviceMessage = TerminalUtilities.buildMessage(UpaMessageId.ExecuteUDDataFile, controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(deviceMessage), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        String content = "";
        if (udData.getFileType().equals(UDFileType.HTML5)) {
            content = TerminalUtilities.buildStringFromFile(udData.getFilePath()).replace('\"', '\'');
        } else {
            content = TerminalUtilities.buildToBase64Content(udData.getFilePath(), UpaMessageId.InjectUDDataFile, true);
        }
        param.set("fileType", udData.getFileType().toString());
        param.set("fileName", udData.getFileName());
        param.set("content", content);
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.InjectUDDataFile, controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);

    }

    @Override
    public IDeviceResponse getConfigContents(TerminalConfigType configType) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        param.set("configType", String.valueOf(configType.getValue()));
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetConfigContents, controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }


    @Override
    public IDeviceResponse getAppInfo() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetAppInfo, controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse clearDataLake() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.ClearDataLake, controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);

    }

    @Override
    public IDeviceResponse returnToIdle() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.ReturnToIdle, controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));//replace for json "response"
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse loadUDDataFile(UDData udData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        params.set("fileType", String.valueOf(udData.getFileType()));
        params.set("slotNum", String.valueOf(udData.getSlot()));
        params.set("file", udData.getFileName());
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.LoadUDDataFile, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceScreen removeUDDataFile(UDData udData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        params.set("fileType", String.valueOf(udData.getFileType()));
        params.set("slotNum", String.valueOf(udData.getSlot()));
        params.set("file", udData.getFileName());
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.RemoveUDDataFile, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UDScreenResponse(responseObj);
    }

    @Override
    public IDeviceResponse Scan(ScanData scanData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        params.set("header", String.valueOf(scanData.getHeader()));
        params.set("prompt1", String.valueOf(scanData.getPrompt1()));
        params.set("prompt2", scanData.getPrompt2());
        params.set("displayOption", scanData.getDisplayOption() != null ? scanData.getDisplayOption().getValue() : null);
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.Scan, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse Print(PrintData printData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        String content = TerminalUtilities.buildBitMapUPAContent(printData.getFilePath());
        params.set("line1", printData.getLine1());
        params.set("line2", printData.getLine1());
        params.set("displayOption", printData.getDisplayOption() != null ? printData.getDisplayOption().getValue() : null);
        params.set("content", content);
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.PrintData, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse setTimeZone(TimeZone timezone) throws ApiException {
        if (timezone == null) {
            throw new ApiException("...::: TimeZone is Mandatory :::...");
        }
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        params.set("timeZone", timezone.getID());
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.SetTimeZone, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse getParams(ArrayList<String> parameters) throws ApiException {
        if (parameters.isEmpty() || parameters == null) {
            throw new ApiException("...::: Parameters are Mandatory :::...");
        }
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        String[] parametersArray = parameters.toArray(new String[0]);
        params.set("configuration", parametersArray);
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetParam, controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
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
                UpaMessageId.Restart,
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

    public ISAFResponse sendStoreAndForward() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.SendSAF,
                controller.getRequestId().toString(),
                null // no body for sendSAF
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaSafResponse(responseObj);
    }

    public TerminalManageBuilder tipAdjust(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
                .withGratuity(amount);
    }

    public TerminalManageBuilder deletePreAuth() {
        return new TerminalManageBuilder(TransactionType.DeleteOpenTab, PaymentMethodType.Credit);
    }

    public IDeviceResponse closeLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IInitializeResponse initialize() throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public void dispose() {
        // unused in UPA
    }

    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        controller.setMessageSentHandler(onMessageSent);
    }

    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {
        controller.setOnMessageReceivedHandler(onMessageReceived);
    }

    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse openLane() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public ISignatureResponse getSignatureFile() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public ISignatureResponse promptForSignature() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse setStoreAndForwardMode(SafMode safMode) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IDeviceResponse sendFile(SendFileType fileType, String filePath) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public IBatchCloseResponse batchClose() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder giftSale() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder giftAddValue() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Activate, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder giftBalance() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder ebtWithdrawal() throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    public IDeviceResponse addLineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        throw new UnsupportedTransactionException(ERROR_MESSAGE);
    }

    public String getParams() throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc configuration = new JsonDoc();

        String[] options = {"ALL"};
        configuration.set("configuration", options);
        body.set("params", configuration);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetParam,
                controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(new String(controller.send(message), StandardCharsets.UTF_8));
        if(responseObj == null){
            throw new ApiException("No response from UPA!");
        }
        return responseObj.toString();
    }

    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    public UpaSafResponse safSummaryReport(String printData, String reportData) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        param.set("reportOutput", "Print");

        StringBuilder reportOutput = new StringBuilder("");
        if (!StringUtils.isNullOrEmpty(printData)) {
            reportOutput.append(printData);
            reportOutput.append("|");
        }
        if (!StringUtils.isNullOrEmpty(reportData)) {
            reportOutput.append(reportData);
        }

        param.set("reportOutput", String.valueOf(reportOutput));

        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetSAFReport,
                controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaSafResponse(responseObj);
    }

    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc transaction = new JsonDoc();

        if (transactionNumber != null) {
            transaction.set(TRANSACTION_NUMBER, transactionNumber);
        }
        if (referenceNumber != null) {
            transaction.set(REFERENCE_NUMBER, referenceNumber);
        }
        body.set(TRANSACTION, transaction);


        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.DeleteSAF,
                controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);
        byte[] resp;
        resp = controller.send(message);
        JsonDoc responseObj = JsonDoc.parse(
                new String(resp, StandardCharsets.UTF_8)
        );

        return new UpaSafResponse(responseObj);
    }

    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (data.getPrompt1() != null) {
            param.set(PROMPT_ONE, data.getPrompt1());
        }
        if (data.getPrompt2() != null) {
            param.set(PROMPT_TWO, data.getPrompt2());
        }
        if (data.getDisplayOption() != null) {
            param.set(DISPLAY_OPTION, data.getDisplayOption());
        }
        body.set(PARAMS, param);
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetSignature,
                controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaSignatureResponse(responseObj);
    }

    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }
}
