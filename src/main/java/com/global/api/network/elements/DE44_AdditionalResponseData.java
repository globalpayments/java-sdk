package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE44_AdditionalResponseData implements IDataElement<DE44_AdditionalResponseData> {
    private DE44_ActionReasonCode actionReasonCode;
    private String textMessage;

    public DE44_ActionReasonCode getActionReasonCode() {
        return actionReasonCode;
    }
    public void setActionReasonCode(DE44_ActionReasonCode actionReasonCode) {
        this.actionReasonCode = actionReasonCode;
    }
    public String getTextMessage() {
        return textMessage;
    }
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public DE44_AdditionalResponseData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        actionReasonCode = sp.readStringConstant(4, DE44_ActionReasonCode.class);
        textMessage = sp.readRemaining();

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = actionReasonCode.getValue();
        if(textMessage != null) {
            rvalue = rvalue.concat(textMessage);
        }
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
