package com.global.api.terminals.abstractions;

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

    String getAttachmentResponseText();
    String getBatchCloseResponseText();
    String getEmvOfflineDeclineResponseText();
    String getEmvPDLResponseText();
    String getEmvTransactionCertificateResponseText();
    String getHeartBeatResponseText();
    String getReversalResponseText();
    String getSafResponseText();
}
