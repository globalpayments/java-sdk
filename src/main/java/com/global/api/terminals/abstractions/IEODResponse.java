package com.global.api.terminals.abstractions;

import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;

public interface IEODResponse extends IDeviceResponse {
    IDeviceResponse getAttachmentResponse();
    IDeviceResponse getBatchCloseResponse();
    IBatchReportResponse getBatchReportResponse();
    IDeviceResponse getEmvOfflineDeclineResponse();
    IDeviceResponse getEmvPDLResponse();
    IDeviceResponse getEmvTransactionCertificateResponse();
    IDeviceResponse getHeartBeatResponse();
    IDeviceResponse getReversalResponse();
    ISAFResponse getSAFResponse();
    String getBatchId();
    UpaMessageId getMessageId();
    String getResponseCode();

    String getAttachmentResponseText();
    String getBatchCloseResponseText();
    String getEmvOfflineDeclineResponseText();
    String getEmvPDLResponseText();
    String getEmvTransactionCertificateResponseText();
    String getHeartBeatResponseText();
    String getReversalResponseText();
    String getSafResponseText();
}
