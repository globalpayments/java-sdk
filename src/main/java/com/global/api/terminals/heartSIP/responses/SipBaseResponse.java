package com.global.api.terminals.heartSIP.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SipBaseResponse extends TerminalResponse {
    private String response;
    private String ecrId;
    private String sipId;

    public String getEcrId() {
        return ecrId;
    }
    public void setEcrId(String ecrId) {
        this.ecrId = ecrId;
    }
    public String getSipId() {
        return sipId;
    }
    public void setSipId(String sipId) {
        this.sipId = sipId;
    }

    public SipBaseResponse(byte[] buffer, String... messageIds) throws ApiException {
        response = "";
        for(byte b: buffer)
            response += (char)b;

        String[] messages = response.replace("\n", "").split("\\r");
        for(String message: messages) {
            if(StringUtils.isNullOrEmpty(message))
                continue;

            Element root = ElementTree.parse(message).get("SIP");
            this.command = root.getString("Response");
            if(this.command != null && !Arrays.asList(messageIds).contains(this.command))
                throw new MessageException(String.format("Excpected %s but recieved %s", StringUtils.join(", ", messageIds), this.command));

            this.version = root.getString("Version");
            this.ecrId = root.getString("ECRId");
            this.sipId = root.getString("SIPId");
            this.status = root.getString("MultipleMessage");
            this.deviceResponseCode = normalizeResponse(root.getString("Result"));
            this.deviceResponseText = root.getString("ResultText");

            mapResponse(root);
        }
        finalizeResponse();
    }

    protected void mapResponse(Element response) { }
    protected void finalizeResponse() { }

    protected String normalizeResponse(String response) {
        List<String> acceptedCodes = Arrays.asList("0", "85");
        if(acceptedCodes.contains(response))
            return "00";
        return response;
    }

    public String toString() {
        return response;
    }
}
