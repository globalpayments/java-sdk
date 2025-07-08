package com.global.api.terminals.upa.responses;

import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.JsonDoc;

import static com.global.api.entities.enums.GatewayProvider.GP_API;

public class UpaDeviceResponse extends TerminalResponse {
    protected UpaMessageId messageId;

    public UpaDeviceResponse(JsonDoc responseObj) {
        this(responseObj, null);
    }
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
        } else {
            requestId = responseObj.getStringOrNull("id");
            deviceResponseText = responseObj.getStringOrNull("status");
            if (deviceResponseText.equalsIgnoreCase("COMPLETE")) deviceResponseCode = "00";
        }
    }
}
