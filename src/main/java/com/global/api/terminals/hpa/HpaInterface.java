package com.global.api.terminals.hpa;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.hpa.builders.HpaAdminBuilder;
import com.global.api.terminals.hpa.responses.*;
import com.global.api.utils.StringUtils;

import java.util.LinkedList;

public class HpaInterface extends DeviceInterface<HpaController> {
    HpaInterface(HpaController controller) {
        super(controller);
    }

    @Override
    public IDeviceResponse cancel() throws ApiException {
        return reset();
    }

    @Override
    public IDeviceResponse closeLane() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.LANE_CLOSE.getValue()));
    }

    @Override
    public IInitializeResponse initialize() throws ApiException {
        return _controller.sendAdminMessage(InitializeResponse.class, new HpaAdminBuilder(HpaMsgId.GET_INFO_REPORT.getValue()));
    }

    @Override
    public IDeviceResponse openLane() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.LANE_OPEN.getValue()));
    }

    @Override
    public IDeviceResponse reboot() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.REBOOT.getValue()));
    }

    @Override
    public IDeviceResponse reset() throws ApiException {
        return _controller.sendAdminMessage(SipBaseResponse.class, new HpaAdminBuilder(HpaMsgId.RESET.getValue()));
    }

    @Override
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SIGNATURE_FORM.getValue())
                .set("FormText", "PLEASE SIGN YOUR NAME");
        return _controller.sendAdminMessage(SignatureResponse.class, builder);
    }

    /*
    * @Deprecated Replaced by {@link #endOfDay()}
     */
    @Deprecated
    public IBatchCloseResponse batchClose() throws ApiException {
        return _controller.sendAdminMessage(BatchResponse.class, new HpaAdminBuilder(HpaMsgId.BATCH_CLOSE.getValue(), HpaMsgId.GET_BATCH_REPORT.getValue()));
    }
    
    public IDeviceResponse startCard(PaymentMethodType paymentMethodType) throws ApiException {
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.START_CARD.getValue())
                .set("CardGroup", paymentMethodType.toString());
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }

    public IDeviceResponse lineItem(String leftText, String rightText, String runningLeftText, String runningRightText) throws ApiException {
        if(StringUtils.isNullOrEmpty(leftText)) {
            throw new ApiException("You need to provide at least the left text.");
        }

        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.LINE_ITEM.getValue())
                .set("LineItemTextLeft", leftText)
                .set("LineItemTextRight", rightText)
                .set("LineItemRunningTextLeft", runningLeftText)
                .set("LineItemRunningTextRight", runningRightText);
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }

    public ISAFResponse sendStoreAndForward() throws ApiException {
    	return _controller.sendAdminMessage(SAFResponse.class, new HpaAdminBuilder(HpaMsgId.SEND_SAF.getValue()));
    }
    
    public IDeviceResponse setStoreAndForwardMode(boolean enabled) throws ApiException {
    	HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SET_PARAMETER.getValue())
                .set("FieldCount", "1")
                .set("Key", "STORMD")
                .set("Value", enabled ? "1" : "0");
        return _controller.sendAdminMessage(SipBaseResponse.class, builder);
    }

    public IDeviceResponse setStoreAndForwardMode(SafMode mode) throws ApiException {
        if(mode == SafMode.STAY_ONLINE || mode == SafMode.STAY_OFFLINE) {
            HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SET_PARAMETER.getValue())
                    .set("FieldCount", "1")
                    .set("Key", "STORMD")
                    .set("Value", mode.getValue());
            return _controller.sendAdminMessage(SipBaseResponse.class, builder);
        } else {
            throw new UnsupportedTransactionException("HPA only supports STAY_ONLINE or STAY_OFFLINE.");
        }
    }
    
    public IDeviceResponse sendFile(SendFileType imageType, String filePath) throws ApiException {
        if(filePath == null) {
            throw new ApiException("Filename is required for SendFile");
        }

        //Load the File
        HpaFileUpload fileUpload = new HpaFileUpload(imageType, filePath);

        //Build the initial message
        HpaAdminBuilder builder = new HpaAdminBuilder(HpaMsgId.SEND_FILE.getValue()) {{ setKeepAlive(true); }}
                .set("FileName", fileUpload.getFileName())
                .set("FileSize", fileUpload.getFileSize())
                .set("MultipleMessage", "1");

        SipSendFileResponse response = _controller.sendAdminMessage(SipSendFileResponse.class, builder);
        if(response.getDeviceResponseCode().equals("00")) {
            LinkedList<String> fileParts = fileUpload.getFileParts(response.getMaxDataSize() / 5);
            String lastElement = fileParts.getLast();

            for(String filePart: fileParts) {
                final String multipleMessage = filePart.equals(lastElement) ? "0" : "1";

                SipSendFileResponse dataResponse = _controller.sendAdminMessage(SipSendFileResponse.class,
                        new HpaAdminBuilder(HpaMsgId.SEND_FILE.getValue()) {{
                            setKeepAlive(multipleMessage.equals("1"));
                            setAwaitResponse(multipleMessage.equals("0"));
                        }}
                        .set("FileData", filePart)
                        .set("MultipleMessage", multipleMessage)
                    );

                if (dataResponse != null) {
                    response = dataResponse;
                }
            }
            return response;
        }
        else throw new ApiException(String.format("Failed to upload file: %s", response.getDeviceResponseText()));
    }

    public IEODResponse endOfDay() throws ApiException {
        return _controller.sendAdminMessage(EODResponse.class, new HpaAdminBuilder(
                HpaMsgId.END_OF_DAY.getValue(),
                HpaMsgId.REVERSAL.getValue(),
                HpaMsgId.EMV_OFFLINE_DECLINE.getValue(),
                HpaMsgId.EMV_TC.getValue(),
                HpaMsgId.ATTACHMENT.getValue(),
                HpaMsgId.SEND_SAF.getValue(),
                HpaMsgId.GET_BATCH_REPORT.getValue(),
                HpaMsgId.HEARTBEAT.getValue(),
                HpaMsgId.BATCH_CLOSE.getValue(),
                HpaMsgId.EMV_PARAMETER_DOWNLOAD.getValue(),
                HpaMsgId.TRANSACTION_CERTIFICATE.getValue())
        );
    }

    public void dispose() {
        try { closeLane(); }
        catch(ApiException e) { /* NOM NOM */ }
        finally {
            _controller.dispose();
        }
    }
}
