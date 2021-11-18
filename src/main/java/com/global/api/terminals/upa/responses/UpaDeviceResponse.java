package com.global.api.terminals.upa.responses;

import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;

public class UpaDeviceResponse extends TerminalResponse {
    protected JsonDoc responseObj;
    protected UpaMessageId messageId;

    public UpaDeviceResponse(JsonDoc responseObj, UpaMessageId messageId) {
        this.messageId = messageId;
        parseResponse(responseObj);
    }

    protected void parseResponse(JsonDoc jsonToParse) {
        String result = jsonToParse.get("data")
            .get("cmdResult")
            .getString("result");

        if (result.equals("Success")) {
            this.setDeviceResponseCode("00");
        }
    }
}
