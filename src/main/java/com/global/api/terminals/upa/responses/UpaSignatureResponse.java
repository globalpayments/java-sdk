package com.global.api.terminals.upa.responses;

import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.utils.JsonDoc;
import lombok.Getter;

import java.util.Base64;

//import javax.xml.bind.DatatypeConverter;

public class UpaSignatureResponse implements ISignatureResponse {
    private String status;
    private String deviceResponseCode;
    private String deviceResponseText;
    private String signatureData;
    @Getter
    private String transactionType;

    private static final String DATA = "data";
    private static final String CMD_RESULT = "cmdResult";
    private static final String RESULT = "result";
    private static final String SUCCESS = "success";
    private static final String ZERO = "00";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String RESPONSE = "response";
    private static final String SIGNATURE_DATA = "signatureData";

    public UpaSignatureResponse(JsonDoc jsonDoc) {
        JsonDoc responseData = jsonDoc.get(DATA);

        if (responseData != null) {
            JsonDoc cmdResult = responseData.get(CMD_RESULT);

            if (cmdResult != null) {
                status = cmdResult.getString(RESULT);
                deviceResponseCode = status.equalsIgnoreCase(SUCCESS) ? ZERO : cmdResult.getString(ERROR_CODE);
                deviceResponseText = cmdResult.getString(ERROR_MESSAGE);
            }
            transactionType = responseData.getString(RESPONSE);

            JsonDoc innerData = responseData.get(DATA);
            if (innerData != null){
                signatureData = innerData.getString(SIGNATURE_DATA);
            }

        }
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getCommand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCommand(String command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(String version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    @Override
    public void setDeviceResponseCode(String deviceResponseCode) {
        this.deviceResponseCode = deviceResponseCode;
    }

    @Override
    public String getDeviceResponseText() {
        return deviceResponseText;
    }

    @Override
    public void setDeviceResponseText(String deviceResponseText) {
        this.deviceResponseText = deviceResponseText;
    }

    @Override
    public byte[] getSignatureData() {
        return Base64.getDecoder().decode(signatureData);
        //return DatatypeConverter.parseBase64Binary(signatureData);
    }
}
