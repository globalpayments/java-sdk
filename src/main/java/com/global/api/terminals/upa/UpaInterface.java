package com.global.api.terminals.upa;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.enums.TerminalReportType;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.terminals.upa.Entities.Enums.UpaSafReportDataType;
import com.global.api.terminals.upa.Entities.TokenInfo;
import com.global.api.terminals.upa.responses.*;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.terminals.upa.subgroups.UpaSafReportParams;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TimeZone;

import static com.global.api.terminals.upa.Entities.Constants.READY_MESSAGE;

public class UpaInterface extends DeviceInterface<UpaController> {
    public UpaInterface(UpaController controller) {
        super(controller);
    }

    @Override
    public IDeviceResponse lineItem(String leftText, String rightText) throws ApiException {
        JsonDoc param = new JsonDoc();

        if (leftText != null && leftText.length() <= 20) {
            param.set("lineItemLeft", leftText);
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
                _controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaDeviceResponse(responseObj, UpaMessageId.LineItemDisplay);
    }

    @Override
    public IDeviceResponse cancel() throws ApiException {
        return cancel(1);
    }

    public IDeviceResponse cancel(Integer cancelParam) throws ApiException {
        JsonDoc param = new JsonDoc();
        param.set("displayOption", cancelParam);

        JsonDoc body = new JsonDoc();
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.CancelTransaction,
                _controller.getRequestId().toString(),
                body
        );

        JsonDoc response = sendRequest(message);
        return new UpaDeviceResponse(response, UpaMessageId.CancelTransaction);
    }

    @Override
    public IEODResponse endOfDay() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.EODProcessing,
                _controller.getRequestId().toString(),
                null
        );
        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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
                _controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    @Override
    public IBatchReportResponse getBatchDetails(String batchId, boolean printReport) throws ApiException {


        JsonDoc param = new JsonDoc();
        if (batchId != null) {
            param.set("batch", batchId);
        }
        if (printReport) {
            param.set("reportOutput", "Print|ReturnData");
        }

        JsonDoc body = new JsonDoc();
        body.set("params", param);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetBatchDetails,
                _controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    @Override
    public IBatchReportResponse getOpenTabDetails() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetOpenTabDetails,
                _controller.getRequestId().toString(),
                null
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    @Override
    public IBatchReportResponse findBatches() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.AvailableBatches,
                _controller.getRequestId().toString(),
                null
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaReportResponse(responseObj);
    }

    @Override
    public IDeviceResponse sendReady() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                READY_MESSAGE
        );

        JsonDoc response = sendRequest(message);
        return new UpaDeviceResponse(response);
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
                _controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaDeviceResponse(responseObj, UpaMessageId.RegisterPOS);
    }

    @Override
    public IDeviceResponse print(PrintData data) throws ApiException {
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
                _controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaDeviceResponse(responseObj, UpaMessageId.PrintData);
    }

    public IDeviceResponse ping() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.Ping,
                _controller.getRequestId().toString(),
                null // no response for ping
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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
        String requestId = _controller.getRequestId().toString();
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.SetDebugLevel, requestId, body);
        byte[] responseBytes = _controller.send(message);
        String responseString = new String(responseBytes, StandardCharsets.UTF_8);
        JsonDoc responseObj = JsonDoc.parse(responseString);
        return new UpaTransactionResponse(responseObj);
    }


    @Override
    public IDeviceResponse getDebugLevel() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetDebugLevel,
                _controller.getRequestId().toString(),
                null
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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
                _controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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
                _controller.getRequestId().toString(),
                body
        );
        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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
        DeviceMessage deviceMessage = TerminalUtilities.buildMessage(UpaMessageId.ExecuteUDDataFile, _controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(deviceMessage), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse injectUDDataFile(UDData udData) throws ApiException, IOException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        String content;
        if (udData.getFileType().equals(UDFileType.HTML5)) {
            content = TerminalUtilities.buildStringFromFile(udData.getFilePath()).replace('\"', '\'');
        } else {
            content = TerminalUtilities.buildToBase64Content(udData.getFilePath(), UpaMessageId.InjectUDDataFile, true);
        }
        param.set("fileType", udData.getFileType().toString());
        param.set("fileName", udData.getFileName());
        param.set("content", content);
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.InjectUDDataFile, _controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);

    }

    @Override
    public IDeviceResponse getConfigContents(TerminalConfigType configType) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();
        param.set("configType", String.valueOf(configType.getValue()));
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetConfigContents, _controller.getRequestId().toString(), body);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }


    public IDeviceResponse getAppInfo() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetAppInfo, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    public IDeviceResponse clearDataLake() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.ClearDataLake, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);

    }

    @Override
    public IDeviceResponse returnToIdle() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.ReturnToIdle, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));//replace for json "response"
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
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.LoadUDDataFile, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
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
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.RemoveUDDataFile, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
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
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.Scan, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
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
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.PrintData, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    public IDeviceResponse setTimeZone(TimeZone timezone) throws ApiException {
        if (timezone == null) {
            throw new ApiException("...::: TimeZone is Mandatory :::...");
        }
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        params.set("timeZone", timezone.getID());
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.SetTimeZone, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    public IDeviceResponse getParams(ArrayList<String> parameters) throws ApiException {
        if (parameters == null || parameters.isEmpty()) {
            throw new ApiException("...::: Parameters are Mandatory :::...");
        }
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();
        String[] parametersArray = parameters.toArray(new String[0]);
        params.set("configuration", parametersArray);
        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetParam, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    public IDeviceResponse reboot() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.Reboot,
                _controller.getRequestId().toString(),
                null // no response for reboot
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaDeviceResponse(responseObj, UpaMessageId.Reboot);
    }

    public IDeviceResponse reset() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.Restart,
                _controller.getRequestId().toString(),
                null // no response for reboot
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaDeviceResponse(responseObj, UpaMessageId.Restart);
    }

    public TerminalManageBuilder reverse() throws ApiException {
        return new TerminalManageBuilder(TransactionType.Reversal, PaymentMethodType.Credit);
    }

    public ISAFResponse sendStoreAndForward() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.SendSAF,
                _controller.getRequestId().toString(),
                null // no response for sendSAF
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
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

    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Activate, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public String getParams() throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc configuration = new JsonDoc();

        String[] options = {"ALL"};
        configuration.set("configuration", options);
        body.set("params", configuration);

        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetParam,
                _controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        if (responseObj == null) {
            throw new ApiException("No valid response from UPA!");
        }
        return responseObj.toString();
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
                _controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        return new UpaSafResponse(responseObj);
    }

    /**
     * Set Report params to run in the background for Android     *
     */
    @Override
    public ISAFResponse safSummaryReportInBackground(UpaSafReportParams reportParams) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc params = new JsonDoc();

        String reportOutput;
        UpaSafReportDataType dataType = reportParams.getDataType();

        switch (dataType) {
            case REPORT_DATA:
                reportOutput = "ReturnData";
                break;
            case REPORT_N_PRINT:
                reportOutput = "Print|ReturnData";
                break;
            default:
                reportOutput = "Print";
                break;
        }

        params.set("reportOutput", reportOutput);
        if (reportParams.isBackgroundTask()) {
            params.set("background", "true");
        }

        body.set("params", params);
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetSAFReport,
                _controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );

        if (responseObj == null) {
            throw new ApiException("No valid response from UPA!");
        }
        return new UpaSafResponse(responseObj);
    }

    @Override
    public ISAFResponse safDelete(String referenceNumber, String transactionNumber) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc transaction = new JsonDoc();

        if (transactionNumber != null) {
            transaction.set("tranNo", transactionNumber);
        }
        if (referenceNumber != null) {
            transaction.set("referenceNumber", referenceNumber);
        }
        body.set("transaction", transaction);


        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.DeleteSAF,
                _controller.getRequestId().toString(),
                body
        );

        message.setAwaitResponse(true);
        byte[] resp;
        resp = _controller.send(message);
        JsonDoc responseObj = JsonDoc.parse(
                new String(resp, StandardCharsets.UTF_8)
        );

        return new UpaSafResponse(responseObj);
    }

    public ISignatureResponse getSignatureFile(SignatureData data) throws ApiException {
        JsonDoc body = new JsonDoc();
        JsonDoc param = new JsonDoc();

        if (data.getPrompt1() != null) {
            param.set("prompt1", data.getPrompt1());
        }
        if (data.getPrompt2() != null) {
            param.set("prompt2", data.getPrompt2());
        }
        if (data.getDisplayOption() != null) {
            param.set("displayOption", data.getDisplayOption());
        }
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(
                UpaMessageId.GetSignature,
                _controller.getRequestId().toString(),
                body
        );

        JsonDoc responseObj = JsonDoc.parse(
                new String(_controller.send(message), StandardCharsets.UTF_8)
        );
        return new UpaSignatureResponse(responseObj);
    }

    public TerminalReportBuilder getBatchReport() throws ApiException {
        return new TerminalReportBuilder(TerminalReportType.GetBatchReport);
    }

    //<editor-fold description="Helper Methods">

    private JsonDoc sendRequest(IDeviceMessage message) throws ApiException {
        byte[] rawResponse = _controller.send(message);
        return JsonDoc.parse(new String(rawResponse, StandardCharsets.UTF_8));
    }

    //</editor-fold>

    @Override
    public TerminalManageBuilder updateTaxInfo(BigDecimal amount) {

        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
                .withTransactionModifier(TransactionModifier.UpdateTaxInfo)
                .withTaxAmount(amount);
    }

    @Override
    public IDeviceResponse communicationCheck() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.CommunicationCheck, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public IDeviceResponse logOn() throws ApiException {
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.Logon, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    @Override
    public TerminalManageBuilder updateLodgingDetails(BigDecimal amount) {

        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
                .withTransactionModifier(TransactionModifier.UpdateLodgingDetails)
                .withAmount(amount);
    }
    /**
     * Injects a carousel logo into the device.
     *
     * @param udData The {@link UDData} object containing the file name and file path of the logo to be injected.
     * @return An {@link IDeviceResponse} object containing the response from the device.
     * @throws ApiException If an error occurs during the injection process.
     * @throws MessageException If the file name is invalid or does not meet the required criteria.
     * <p>
     * The file name must:
     * - Start with the prefix `brand_logo_`.
     * - Have a valid extension (.jpg, .jpeg, .bmp, .png, .gif).
     * - Not be empty.
     */
    @Override
    public IDeviceResponse injectCarouselLogo(UDData udData) throws ApiException {

        if (udData.getFileName().isEmpty()) {
            throw new MessageException(udData.getFileName() + " is a mandatory parameter.");
        }
        // Check prefix
        if (!udData.getFileName().toLowerCase().startsWith("brand_logo_")) {
            throw new MessageException("FileName must start with 'brand_logo_'.");
        }
        // Check extension
        if (!udData.getFileName().matches("^brand_logo_[^\\\\/:*?\"<>|]+\\.(jpg|jpeg|bmp|png|gif)$")) {
            throw new MessageException("FileName must have a valid extension (.jpg, .jpeg, .bmp, .png, .gif).");
        }

        // Extract file extension from FileName
        String extension = udData.getFileName().substring(udData.getFileName().lastIndexOf('.') + 1).toLowerCase();
        if (extension.isEmpty()) {
            throw new MessageException("FileName must include a valid file extension.");
        }

        String content = "";
        content = "data:image/" + extension + ";base64," + TerminalUtilities.buildToBase64Content(udData.getFilePath(), UpaMessageId.InjectCarouselLogo, true);

        JsonDoc param = new JsonDoc();
        param.set("fileName", udData.getFileName());
        param.set("content", content);

        JsonDoc body = new JsonDoc();
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.InjectCarouselLogo, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    /**
     * Removes a carousel logo from the device.
     *
     * @param udData The {@link UDData} object containing the file name of the logo to be removed.
     * @return An {@link IDeviceResponse} object containing the response from the device.
     * @throws ApiException If an error occurs during the removal process.
     * @throws MessageException If the file name is invalid or does not meet the required criteria.
     *
     * <p>
     * The file name must:
     * - Include a valid file extension (e.g., .jpg, .jpeg, .bmp, .png, .gif).
     * - Not contain a file path or invalid characters.
     * </p>
     */
    @Override
    public IDeviceResponse removeCarouselLogo(UDData udData) throws ApiException {

        if (udData.getFileName().isEmpty()) {
            throw new MessageException(udData.getFileName() + " is a mandatory parameter.");
        }
        // Ensure fileName includes an extension and does not contain a file path
        if (!udData.getFileName().matches("^[^\\\\/:*?\"<>|]+\\.[a-zA-Z0-9]+$")) {
            throw new MessageException("FileName must include a file extension and must not contain a file path.");
        }

        JsonDoc param = new JsonDoc();
        param.set("fileName", udData.getFileName());

        JsonDoc body = new JsonDoc();
        body.set("params", param);
        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.RemoveCarouselLogo, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    /**
     * Manages a token by validating its details and sending a request to the device.
     *
     * @param tokenInfo The {@link TokenInfo} object containing the token details.
     *                  - `expiryMonth`: The expiration month of the token (1-12).
     *                  - `expiryYear`: The expiration year of the token (4-digit, greater than 1999).
     *                  - `token`: The token value (alphanumeric, 1-24 characters).
     * @return An {@link IDeviceResponse} object containing the response from the device.
     * @throws ApiException If an error occurs during the process.
     * @throws MessageException If the token details are invalid (e.g., invalid month, year, or token length).
     */
    @Override
    public IDeviceResponse manageToken(TokenInfo tokenInfo) throws ApiException {
        int expiryMonth;
        try {
            expiryMonth = Integer.parseInt(tokenInfo.getExpiryMonth());
            if (expiryMonth < 1 || expiryMonth > 12) {
                throw new MessageException("ExpiryMonth must be an integer between 1 and 12.");
            }
        } catch (NumberFormatException e) {
            throw new MessageException("ExpiryMonth must be an integer between 1 and 12.");
        }

        int expiryYear;
        try {
            expiryYear = Integer.parseInt(tokenInfo.getExpiryYear());
            if (expiryYear <= 1999 || tokenInfo.getExpiryYear().length() != 4) {
                throw new MessageException("ExpiryYear must be a 4-digit positive integer greater than 1999.");
            }
        } catch (NumberFormatException e) {
            throw new MessageException("ExpiryYear must be a 4-digit positive integer greater than 1999.");
        }
        // Validate token length: AN(1-24)
        if (tokenInfo.getToken() == null || tokenInfo.getToken().isEmpty() || tokenInfo.getToken().length() > 24) {
            throw new MessageException("Token must be alphanumeric and between 1 and 24 characters in length.");
        }

        JsonDoc params = new JsonDoc();
        params.set("tokenValue", tokenInfo.getToken());
        params.set("expiryMonth", tokenInfo.getExpiryMonth());
        params.set("expiryYear", tokenInfo.getExpiryYear());

        JsonDoc body = new JsonDoc();
        body.set("params", params);

        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.ManageToken, _controller.getRequestId().toString(), body);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }

    /**
     * Retrieves the details of the last End of Day (EOD) process.
     *
     * @return An {@link IDeviceResponse} object containing the response from the device.
     * @throws ApiException If an error occurs during the retrieval process.
     */
    @Override
    public IDeviceResponse getLastEod() throws ApiException {

        DeviceMessage message = TerminalUtilities.buildMessage(UpaMessageId.GetLastEOD, _controller.getRequestId().toString(), null);
        message.setAwaitResponse(true);
        JsonDoc responseObj = JsonDoc.parse(new String(_controller.send(message), StandardCharsets.UTF_8));
        return new UpaTransactionResponse(responseObj);
    }
}
