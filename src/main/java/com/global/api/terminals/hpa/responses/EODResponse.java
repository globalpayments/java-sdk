package com.global.api.terminals.hpa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.Element;
import com.global.api.utils.StringUtils;

public class EODResponse extends SipBaseResponse implements IEODResponse {
    private StringBuilder sendSafMessageBuilder;
    private StringBuilder batchReportMessageBuilder;

    private IDeviceResponse attachmentResponse;
    private IDeviceResponse batchCloseResponse;
    private BatchReportResponse batchReportResponse;
    private IDeviceResponse emvOfflineDeclineResponse;
    private IDeviceResponse emvPDLResponse;
    private IDeviceResponse emvTransactionCertificateResponse;
    private HeartBeatResponse heartBeatResponse;
    private IDeviceResponse reversalResponse;
    private SAFResponse safResponse;

    private String attachmentResponseText;
    private String batchCloseResponseText;
    private String emvOfflineDeclineResponseText;
    private String emvPDLResponseText;
    private String emvTransactionCertificationResponseText;
    private String heartBeatResponseText;
    private String reversalResponseText;
    private String safResponseText;

    public IDeviceResponse getAttachmentResponse() {
        return attachmentResponse;
    }
    public IDeviceResponse getBatchCloseResponse() {
        return batchCloseResponse;
    }
    public IDeviceResponse getEmvOfflineDeclineResponse() {
        return emvOfflineDeclineResponse;
    }
    public IDeviceResponse getEmvPDLResponse() {
        return emvPDLResponse;
    }
    public IDeviceResponse getEmvTransactionCertificateResponse() {
        return emvTransactionCertificateResponse;
    }
    public HeartBeatResponse getHeartBeatResponse() {
        return heartBeatResponse;
    }
    public IDeviceResponse getReversalResponse() {
        return reversalResponse;
    }

    public String getAttachmentResponseText() {
        if(attachmentResponse != null) {
            return attachmentResponse.getDeviceResponseText();
        }
        return attachmentResponseText;
    }
    public String getBatchCloseResponseText() {
        if(batchCloseResponse != null) {
            return batchCloseResponse.getDeviceResponseText();
        }
        return batchCloseResponseText;
    }
    public String getEmvOfflineDeclineResponseText() {
        if(emvOfflineDeclineResponse != null) {
            return emvOfflineDeclineResponse.getDeviceResponseText();
        }
        return emvOfflineDeclineResponseText;
    }
    public String getEmvPDLResponseText() {
        if(emvPDLResponse != null) {
            return emvPDLResponse.getDeviceResponseText();
        }
        return emvPDLResponseText;
    }
    public String getEmvTransactionCertificateResponseText() {
        if(emvTransactionCertificateResponse != null) {
            return emvTransactionCertificateResponse.getDeviceResponseText();
        }
        return emvTransactionCertificationResponseText;
    }
    public String getHeartBeatResponseText() {
        if(heartBeatResponse != null) {
            return heartBeatResponse.getResponseText();
        }
        return heartBeatResponseText;
    }
    public String getReversalResponseText() {
        if(reversalResponse != null) {
            return reversalResponse.getDeviceResponseText();
        }
        return reversalResponseText;
    }
    public String getSafResponseText() {
        if(safResponse != null) {
            return safResponse.getDeviceResponseText();
        }
        return safResponseText;
    }

    public BatchReportResponse getBatchReportResponse() {
        return batchReportResponse;
    }
    public SAFResponse getSAFResponse() {
        return safResponse;
    }

    public EODResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);

        if(sendSafMessageBuilder != null) {
            String messages = sendSafMessageBuilder.toString();
            if(!StringUtils.isNullOrEmpty(messages)) {
                try {
                    safResponse = new SAFResponse(messages.getBytes(), "SendSAF");
                }
                catch(ApiException exc) {
                    /* NOM NOM */
                }
            }
        }

        if(batchReportMessageBuilder != null) {
            String messages = batchReportMessageBuilder.toString();
            if(!StringUtils.isNullOrEmpty(messages)) {
                try {
                    batchReportResponse = new BatchReportResponse(messages.getBytes(), "GetBatchReport");
                }
                catch(ApiException exc) {
                    /* NOM NOM */
                }
            }
        }
    }
    
    protected void mapResponse(Element response) {
        super.mapResponse(response);

        if(command.equalsIgnoreCase("SendSAF")) {
            if(sendSafMessageBuilder == null) {
                sendSafMessageBuilder = new StringBuilder();
            }
            sendSafMessageBuilder.append(currentMessage).append('\r');
        }
        else if(command.equalsIgnoreCase("GetBatchReport")) {
            if(batchReportMessageBuilder == null) {
                batchReportMessageBuilder = new StringBuilder();
            }
            batchReportMessageBuilder.append(currentMessage).append('\r');
        }
        else if(command.equalsIgnoreCase("Heartbeat")) {
            try {
                heartBeatResponse = new HeartBeatResponse(currentMessage.getBytes(), "Heartbeat");
            }
            catch(ApiException exc) { /* NOM NOM */ }
        }
        else if(command.equalsIgnoreCase("EOD")) {
            attachmentResponseText = response.getString("Attachment");
            batchCloseResponseText = response.getString("BatchClose");
            emvOfflineDeclineResponseText = response.getString("EMVOfflineDecline");
            emvPDLResponseText = response.getString("EMVPDL");
            emvTransactionCertificationResponseText = response.getString("TransactionCertificate");
            heartBeatResponseText = response.getString("HeartBeat");
            reversalResponseText = response.getString("Reversal");
            safResponseText = response.getString("SendSAF");
        }
        else {
            try {
                SipBaseResponse subResponse = new SipBaseResponse(currentMessage.getBytes(), command);
                if(command.equalsIgnoreCase("Reversal")) { reversalResponse = subResponse; }
                else if(command.equalsIgnoreCase("EMVOfflineDecline")) { emvOfflineDeclineResponse = subResponse; }
                else if(command.equalsIgnoreCase("EMVTC")) { emvTransactionCertificateResponse = subResponse; }
                else if(command.equalsIgnoreCase("Attachment")) { attachmentResponse = subResponse; }
                else if(command.equalsIgnoreCase("BatchClose")) { batchCloseResponse = subResponse; }
                else if(command.equalsIgnoreCase("EMVPDL")) { emvPDLResponse = subResponse; }
            }
            catch(ApiException exc) { /* NOM NOM */ }
        }
    }

    public String getBatchId() {
        return null;
    }

    public UpaMessageId getMessageId() {
        return null;
    }
}
