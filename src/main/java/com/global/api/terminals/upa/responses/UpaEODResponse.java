package com.global.api.terminals.upa.responses;

import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;

public class UpaEODResponse implements IEODResponse {
    protected String batchId;
    protected UpaMessageId messageId;
    protected String responseCode;    

    public UpaEODResponse(JsonDoc responseObj) {
        messageId = UpaMessageId.EODProcessing;
        batchId = responseObj.get("data").get("data").get("host").getString("batchId");

        if (responseObj.get("data").get("cmdResult").getString("result").equals("Success"))
            responseCode = "00";
    }

    public String getBatchId() {
        return batchId;
    }
    
    public UpaMessageId getMessageId() {
        return messageId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getStatus() {
        return null;
    }

    public void setStatus(String status) {
        // Unused
    }

    public String getCommand() {
        return null;
    }

    public void setCommand(String command) {
        // Unused
    }

    public String getVersion() {
        return null;
    }

    public void setVersion(String version) {
        // Unused
    }

    public String getDeviceResponseCode() {
        return null;
    }

    public void setDeviceResponseCode(String deviceResponseCode) {
        // Unused
    }

    public String getDeviceResponseText() {
        return null;
    }

    public void setDeviceResponseText(String deviceResponseMessage) {
        // Unused
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
}
