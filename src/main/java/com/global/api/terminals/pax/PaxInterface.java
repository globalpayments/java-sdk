package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.pax.responses.*;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

class PaxInterface extends DeviceInterface<PaxController> {
    PaxInterface(PaxController controller) {
        super(controller);
    }

    //<editor-fold desc="ADMIN MESSAGES">
    // A00 - INITIALIZE
    @Override
    public IInitializeResponse initialize() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A00_INITIALIZE));
        return new InitializeResponse(response);
    }

    // A08 - GET SIGNATURE
    @Override
    public ISignatureResponse getSignatureFile() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A08_GET_SIGNATURE, 0, ControlCodes.FS));
        return new SignatureResponse(response);
    }

    // A14 - CANCEL
    @Override
    public IDeviceResponse cancel() throws ApiException {
        if(_controller.getConnectionMode() == ConnectionModes.HTTP)
            throw new MessageException("The cancel command is not available in HTTP mode.");

        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A14_CANCEL));
        return new PaxDeviceResponse(response, PaxMsgId.A14_CANCEL);
    }

    // A16 - RESET
    @Override
    public IDeviceResponse reset() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A16_RESET));
        return new PaxDeviceResponse(response, PaxMsgId.A17_RSP_RESET);
    }

    // A18 - Update Image
    @Override
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        int size = isHttpDeviceConnectionMode ? 3000 : 4000;
        byte[] response = null;
        int offset = 0;

        final String LAST_DATA_PACKET_FALSE = "0";
        final String LAST_DATA_PACKET_TRUE = "1";
        final String DEVICE_RESPONSE_SUCCESS_CODE = "000000";


        while (offset < fileData.length) {
            int length = Math.min(size, fileData.length - offset);
            byte[] dataPacket = new byte[length];
            System.arraycopy(fileData, offset, dataPacket, 0, length);

            boolean isLastDataPacket = (offset + length) == fileData.length;
            String base64String = Base64.encodeBase64String(dataPacket);
            response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A18_UPDATE_RESOURCE_FILE,
                    offset,
                    ControlCodes.FS,
                    base64String,
                    ControlCodes.FS,
                    isLastDataPacket ? LAST_DATA_PACKET_FALSE : LAST_DATA_PACKET_TRUE,
                    ControlCodes.FS,
                    fileType.ordinal(),
                    ControlCodes.FS, 0
            ));

            PaxDeviceResponse paxDeviceResponse = new PaxDeviceResponse(response, PaxMsgId.A19_RSP_UPDATE_RESOURCE_FILE);

            if (!paxDeviceResponse.getDeviceResponseCode().equals(DEVICE_RESPONSE_SUCCESS_CODE)) {

                return new PaxDeviceResponse(response, PaxMsgId.A19_RSP_UPDATE_RESOURCE_FILE);

            }

            if (isLastDataPacket) {
                return new PaxDeviceResponse(response, PaxMsgId.A19_RSP_UPDATE_RESOURCE_FILE);

            }

            offset += length;

        }

        return new PaxDeviceResponse( response, PaxMsgId.A19_RSP_UPDATE_RESOURCE_FILE);

    }

    // A20 - DO SIGNATURE
    @Override
    public ISignatureResponse promptForSignature() throws ApiException {
        return promptForSignature(null);
    }

    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A20_DO_SIGNATURE,
                (transactionId != null) ? 1: 0, ControlCodes.FS,
                (transactionId != null) ? transactionId : "", ControlCodes.FS,
                (transactionId != null) ? "00" : "", ControlCodes.FS,
                300));
        SignatureResponse signatureResponse = new SignatureResponse(response);
        if(Objects.equals(signatureResponse.getDeviceResponseCode(), "000000")) {
            return getSignatureFile();
        }
        return signatureResponse;
    }

    // A22 - Delete Image
    @Override
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A22_DELETE_IMAGE,fileName));
        return new PaxDeviceResponse(response, PaxMsgId.A23_RSP_DELETE_IMAGE);

    }

    // A26 - REBOOT
    @Override
    public IDeviceResponse reboot() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A26_REBOOT));
        return new PaxDeviceResponse(response, PaxMsgId.A27_RSP_REBOOT);
    }

    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A04_SET_VARIABLE,
                "00",
                ControlCodes.FS,
                "hostRspBeep",
                ControlCodes.FS,
                "N",
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS
        ));
        return new PaxDeviceResponse(response, PaxMsgId.A05_RSP_SET_VARIABLE);
    }

    //</editor-fold>

    //<editor-fold desc="BATCH MESSAGES">
    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(
                PaxMsgId.B00_BATCH_CLOSE,
                new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())));
        return new BatchCloseResponse(response);
    }
    //</editor-fold>

    // SAF
    @Override
    public IDeviceResponse setStoreAndForwardMode(SafMode mode) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.A54_SET_SAF_PARAMETERS,
                mode,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS,
                ControlCodes.FS));
        return new PaxDeviceResponse(response, PaxMsgId.A55_RSP_SET_SAF_PARAMETERS);
    }

    @Override
    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.B08_SAF_UPLOAD,
                safUploadIndicator));
        return new SAFUploadResponse(response);
    }

    @Override
    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.B10_DELETE_SAF_FILE,
                safDeleteIndicator));
        return new SAFDeleteResponse(response);
    }
    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        return new TerminalReportBuilder();
    }

    @Override
    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        byte[] response = _controller.send(TerminalUtilities.buildRequest(PaxMsgId.R10_SAF_SUMMARY_REPORT,
                safReportIndicator));
        return new SAFSummaryReport(response);
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
                .withGratuity(amount);
    }
}
