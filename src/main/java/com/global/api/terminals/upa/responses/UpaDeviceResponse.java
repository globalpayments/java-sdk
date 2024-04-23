package com.global.api.terminals.upa.responses;

import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;

public class UpaDeviceResponse extends TerminalResponse {
    protected UpaMessageId messageId;

    public UpaDeviceResponse(JsonDoc responseObj, UpaMessageId messageId) {
        this.messageId = messageId;

        if(responseObj == null){
            status = "Failed";
            return;
        }
        JsonDoc data = responseObj.get("data");

        if (data != null) {
            JsonDoc cmdResult = data.get("cmdResult");

            if (cmdResult != null) {
                status = cmdResult.getString("result");

                if (status.equalsIgnoreCase("success")) deviceResponseCode = "00";
            }
        }
    }
}
