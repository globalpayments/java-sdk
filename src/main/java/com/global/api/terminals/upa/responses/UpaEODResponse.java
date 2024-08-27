package com.global.api.terminals.upa.responses;

import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;
import lombok.Getter;

public class UpaEODResponse implements IEODResponse {
    protected String batchId;
    protected String deviceResponseCode;
    protected String deviceResponseText;
    protected UpaMessageId messageId;
    protected String responseCode;
    protected String status;
    @Getter
    protected Integer gatewayResponseCode;
    @Getter
    protected String gatewayResponseMessage;
    @Getter
    protected String responseDateTime;
    @Getter
    protected Integer batchSequenceNumber;
    @Getter
    protected Integer multipleMessage;
    private static final String DATA = "data";
    private static final String CMD_RESULT = "cmdResult";
    private static final String RESULT = "result";
    private static final String SUCCESS = "success";
    private static final String ZERO = "00";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String MULTIPLE_MESSAGE = "multipleMessage";
    private static final String HOST = "host";
    private static final String BATCH_ID = "batchId";
    private static final String GATEWAY_RESPONSE_CODE = "gatewayResponseCode";
    private static final String GATEWAY_RESPONSE_MESSAGE = "gatewayResponseMessage";
    private static final String RESPONSE_DATE_TIME = "respDateTime";
    private static final String BATCH_SEQUENCE_NO = "batchSeqNbr";

    public UpaEODResponse(JsonDoc responseObj) {
        messageId = UpaMessageId.EODProcessing;

        JsonDoc outerData = isGpApiResponse(responseObj) ? responseObj.get("response") : responseObj.get(DATA);

        if (outerData != null) {
            JsonDoc cmdResult = outerData.get(CMD_RESULT);

            if (cmdResult != null) {
                status = isGpApiResponse(responseObj) ? responseObj.getString("status") : cmdResult.getString(RESULT);
                boolean isSuccess = (status.equalsIgnoreCase(SUCCESS) || status.equalsIgnoreCase("COMPLETE"));
                deviceResponseCode =  isSuccess ? ZERO : cmdResult.getString(ERROR_CODE);
                deviceResponseText = isSuccess ? status : cmdResult.getString(ERROR_MESSAGE);
            }

            JsonDoc innerData = outerData.get(DATA);

            if (innerData != null) {
             if (innerData.getInt(MULTIPLE_MESSAGE) != null){
                multipleMessage = innerData.getInt(MULTIPLE_MESSAGE);
             }
                JsonDoc host = innerData.get(HOST);

                if (host != null) {
                    batchId = host.getString(BATCH_ID);
                    if (host.getInt(GATEWAY_RESPONSE_CODE) != null) {
                        gatewayResponseCode = host.getInt(GATEWAY_RESPONSE_CODE);
                    }
                    gatewayResponseMessage = host.getString(GATEWAY_RESPONSE_MESSAGE);
                    responseDateTime = host.getString(RESPONSE_DATE_TIME);
                    if (host.getInt(BATCH_SEQUENCE_NO) != null){
                        batchSequenceNumber = host.getInt(BATCH_SEQUENCE_NO);
                    }

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

    private boolean isGpApiResponse(JsonDoc root) {
        if (root.has("data")) {
            return false;
        }
        return true;
    }
}
