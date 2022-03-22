package com.global.api.terminals.upa.responses;

import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;

public class UpaEODResponse implements IEODResponse {
    protected String batchId;
    protected String deviceResponseCode;
    protected String deviceResponseText;
    protected UpaMessageId messageId;
    protected String responseCode;
    protected String status;

    public UpaEODResponse(JsonDoc responseObj) {
        messageId = UpaMessageId.EODProcessing;
        JsonDoc outerData = responseObj.get("data");

        if (outerData != null) {
            JsonDoc cmdResult = outerData.get("cmdResult");

            if (cmdResult != null) {
                status = cmdResult.getString("result");
                deviceResponseCode = status.equalsIgnoreCase("success") ? "00" : cmdResult.getString("errorCode");
                deviceResponseText = cmdResult.getString("errorMessage");
            }

            JsonDoc innerData = outerData.get("data");

            if (innerData != null) {
                JsonDoc host = innerData.get("host");

                if (host != null) {
                    batchId = host.getString("batchId");
                }
            }
        }
    }

    public String getBatchId() {
        return batchId;
    }

    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    public String getDeviceResponseText() {
        return deviceResponseText;
    }

    public UpaMessageId getMessageId() {
        return messageId;
    }

    public String getStatus() {
        return status;
    }

    public IDeviceResponse getAttachmentResponse() {
        return null;
    }

    public IDeviceResponse getBatchCloseResponse() {
        return null;
    }

    public IBatchReportResponse getBatchReportResponse() {
        return null;
    }

    public IDeviceResponse getEmvOfflineDeclineResponse() {
        return null;
    }

    public IDeviceResponse getEmvPDLResponse() {
        return null;
    }

    public IDeviceResponse getEmvTransactionCertificateResponse() {
        return null;
    }

    public IDeviceResponse getHeartBeatResponse() {
        return null;
    }

    public IDeviceResponse getReversalResponse() {
        return null;
    }

    public ISAFResponse getSAFResponse() {
        return null;
    }

    public void setDeviceResponseCode(String deviceResponseCode) {

    }

    public void setDeviceResponseText(String deviceResponseMessage) {

    }

    public String getResponseCode() {
        return null;
    }

    public String getAttachmentResponseText() {
        return null;
    }

    public String getBatchCloseResponseText() {
        return null;
    }

    public String getEmvOfflineDeclineResponseText() {
        return null;
    }

    public String getEmvPDLResponseText() {
        return null;
    }

    public String getEmvTransactionCertificateResponseText() {
        return null;
    }

    public String getHeartBeatResponseText() {
        return null;
    }

    public String getReversalResponseText() {
        return null;
    }

    public String getSafResponseText() {
        return null;
    }

    public void setStatus(String status) {

    }

    public String getCommand() {
        return null;
    }

    public void setCommand(String command) {

    }

    public String getVersion() {
        return null;
    }

    public void setVersion(String version) {

    }
}
