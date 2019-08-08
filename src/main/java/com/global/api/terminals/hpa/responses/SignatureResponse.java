package com.global.api.terminals.hpa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.utils.Element;
import org.apache.commons.codec.binary.Base64;

public class SignatureResponse extends SipBaseResponse implements ISignatureResponse {
    public SignatureResponse(byte[] response, String... messageIds) throws ApiException {
        super(response, messageIds);
    }

    @Override
    protected void mapResponse(Element response) {
        super.mapResponse(response);

        if(deviceResponseCode.equals("00")) {
            String attachmentData = response.getString("AttachmentData");
            if(attachmentData != null) {
                signatureData = Base64.decodeBase64(attachmentData);
            }
        }
    }
}
