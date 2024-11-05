package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.*;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

class PaxInterface extends DeviceInterface {
    private PaxController controller;
    private IMessageSentInterface onMessageSent;

    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    @Override
    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {
        //Intentional left blank
    }

    PaxInterface(PaxController controller) {
        this.controller = controller;
        this.controller.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    //<editor-fold desc="ADMIN MESSAGES">
    // A00 - INITIALIZE
    public IInitializeResponse initialize() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A00_INITIALIZE));
        return new InitializeResponse(response);
    }

    // A08 - GET SIGNATURE
    public ISignatureResponse getSignatureFile() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A08_GET_SIGNATURE, 0, ControlCodes.FS));
        return new SignatureResponse(response);
    }

    // A14 - CANCEL
    public void cancel() throws ApiException {
        if(controller.getConnectionMode() == ConnectionModes.HTTP)
            throw new MessageException("The cancel command is not available in HTTP mode.");
        controller.send(TerminalUtilities.buildRequest(PaxMsgId.A14_CANCEL));
    }

    // A16 - RESET
    public IDeviceResponse reset() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A16_RESET));
        return new PaxDeviceResponse(response, PaxMsgId.A17_RSP_RESET);
    }

    // A18 - Update Image
    public IDeviceResponse updateResource(UpdateResourceFileType fileType, byte[] fileData, boolean isHttpDeviceConnectionMode) throws ApiException {
        int size = isHttpDeviceConnectionMode ? 3000 : 4000;
        byte[] response = null;
        int offset = 0;

        final String LAST_DATA_PACKET_FALSE = "0";
        final String LAST_DATA_PACKET_TRUE = "1";
        final String DEVICE_RESPONSE_SUCCESS_CODE = "000000";


        while (offset < fileData.length) {
            int length = Math.min(size, fileData.length - offset);
            byte[] datapacket = new byte[length];
            System.arraycopy(fileData, offset, datapacket, 0, length);

            boolean isLastDataPacket = (offset + length) == fileData.length;
            String base64String = Base64.encodeBase64String(datapacket);
            response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A18_UPDATE_RESOURCE_FILE,
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
    public ISignatureResponse promptForSignature() throws ApiException {
        return promptForSignature(null);
    }
    public ISignatureResponse promptForSignature(String transactionId) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A20_DO_SIGNATURE,
                (transactionId != null) ? 1: 0, ControlCodes.FS,
                (transactionId != null) ? transactionId : "", ControlCodes.FS,
                (transactionId != null) ? "00" : "", ControlCodes.FS,
                300));
        SignatureResponse signatureResponse = new SignatureResponse(response);
        if(signatureResponse.getDeviceResponseCode() == "000000") {
            return getSignatureFile();
        }
        return signatureResponse;
    }

    // A22 - Delete Image
    public IDeviceResponse deleteImage(String fileName) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A22_DELETE_IMAGE,fileName));
        return new PaxDeviceResponse(response, PaxMsgId.A23_RSP_DELETE_IMAGE);

    }

    // A26 - REBOOT
    public IDeviceResponse reboot() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A26_REBOOT));
        return new PaxDeviceResponse(response, PaxMsgId.A27_RSP_REBOOT);
    }

    public IDeviceResponse disableHostResponseBeep() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A04_SET_VARIABLE,
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

    //<editor-fold desc="CREDIT MESSAGES">
    public TerminalAuthBuilder creditAuth() throws ApiException {
        return authorize(null);
    }
    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        return authorize(amount);
    }

    public TerminalManageBuilder creditCapture() throws ApiException {
        return creditCapture(null);
    }
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return capture(amount);
    }

    public TerminalAuthBuilder creditRefund() throws ApiException {
        return creditRefund(null);
    }
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return refund(amount);
    }

    public TerminalAuthBuilder creditSale() throws ApiException {
        return sale(null);
    }
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return sale(amount);
    }

    public TerminalAuthBuilder creditVerify() throws ApiException {
        return verify();
    }

    public TerminalManageBuilder creditVoid() throws ApiException {
        return Void();
    }
    //</editor-fold>

    //<editor-fold desc="DEBIT MESSAGES">
    public TerminalAuthBuilder debitRefund() throws ApiException {
        return debitRefund(null);
    }
    public TerminalAuthBuilder debitRefund(BigDecimal amount) throws ApiException {
        return refund(amount).withPaymentMethodType(PaymentMethodType.Debit);
    }

    public TerminalAuthBuilder debitSale() throws ApiException {
        return sale(null).withPaymentMethodType(PaymentMethodType.Debit);
    }
    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        return sale(amount).withPaymentMethodType(PaymentMethodType.Debit);
    }
    //</editor-fold>

    //<editor-fold desc="EBT MESSAGES">
    public TerminalAuthBuilder ebtBalance() throws ApiException {
        return balance().withPaymentMethodType(PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtPurchase() {
        return ebtPurchase(null);
    }
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.EBT).withAmount(amount);
    }

    public TerminalAuthBuilder ebtRefund() throws ApiException {
        return ebtRefund(null);
    }
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        return refund(amount).withPaymentMethodType(PaymentMethodType.EBT);
    }

    public TerminalAuthBuilder ebtWithdrawal() {
        return ebtWithdrawal(null);
    }
    public TerminalAuthBuilder ebtWithdrawal(BigDecimal amount) {
        return new TerminalAuthBuilder(TransactionType.BenefitWithdrawal, PaymentMethodType.EBT).withAmount(amount);
    }
    //</editor-fold>

    //<editor-fold desc="GIFT MESSAGES">
    public TerminalAuthBuilder giftSale() throws ApiException {
        return giftSale(null);
    }
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        return sale(amount)
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftAddValue() throws ApiException {
        return giftAddValue(null);
    }
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency)
                .withAmount(amount);
    }

    public TerminalManageBuilder giftVoid() throws ApiException {
        return Void()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Currency);
    }

    public TerminalAuthBuilder giftBalance() throws ApiException {
        return  balance().withCurrency(CurrencyType.Currency);
    }
    //</editor-fold>

    //<editor-fold desc="CASH MESSAGES">
    //</editor-fold>

    //<editor-fold desc="CHECK MESSAGES">
    //</editor-fold>

    //<editor-fold desc="BATCH MESSAGES">
    public IBatchCloseResponse batchClose() throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(
                PaxMsgId.B00_BATCH_CLOSE,
                new SimpleDateFormat("YYYYMMDDhhmmss").format(new Date())));
        return new BatchCloseResponse(response);
    }
    //</editor-fold>

    //<editor-fold desc="REPORTING MESSAGES">
    //</editor-fold>

    public void dispose() {
        // not used
    }

    // SAF
    public IDeviceResponse setStoreAndForwardMode(SafMode mode) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.A54_SET_SAF_PARAMETERS,
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

    public SAFUploadResponse safUpload(SafUpload safUploadIndicator) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.B08_SAF_UPLOAD,
                safUploadIndicator));
        SAFUploadResponse uploadResponse = new SAFUploadResponse(response);
        return uploadResponse;
    }

    public SAFDeleteResponse safDelete(SafDelete safDeleteIndicator) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.B10_DELETE_SAF_FILE,
                safDeleteIndicator));
        SAFDeleteResponse deleteResponse = new SAFDeleteResponse(response);
        return deleteResponse;
    }
    public TerminalReportBuilder localDetailReport() throws ApiException {
       return new TerminalReportBuilder();
    }

    public SAFSummaryReport safSummaryReport(SafReportSummary safReportIndicator) throws ApiException {
        byte[] response = controller.send(TerminalUtilities.buildRequest(PaxMsgId.R10_SAF_SUMMARY_REPORT,
                safReportIndicator));
        SAFSummaryReport summaryResponse = new SAFSummaryReport(response);
        return summaryResponse;
    }

    public TerminalManageBuilder tipAdjust(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit)
                .withGratuity(amount);
    }
}
