package com.global.api.terminals.upa.responses;

import com.global.api.entities.exceptions.GatewayException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class BatchList extends UpaTerminalReport {
    @Getter @Setter
    private String ecrId;
    @Getter @Setter
    private List<Integer> batches;

    private static final String INVALID_RESPONSE_FORMAT = "The response received is not in the proper format.";

    public BatchList(JsonElement jsonResponse) throws GatewayException {
        parseResponse(jsonResponse);
    }

    private void parseResponse(JsonElement jsonResponse) throws GatewayException {
        if ((jsonResponse.isJsonObject() && !jsonResponse.getAsJsonObject().has("data")) ||
                (jsonResponse.getAsJsonObject().has("data") && !jsonResponse.getAsJsonObject().get("data").isJsonObject()) ||
                (jsonResponse.getAsJsonObject().has("data") && !jsonResponse.getAsJsonObject().get("data").getAsJsonObject().has("cmdResult"))) {
            throw new GatewayException(INVALID_RESPONSE_FORMAT);
        }

        JsonObject dataNode = jsonResponse.getAsJsonObject().get("data").getAsJsonObject();
        JsonObject cmdResult = dataNode.get("cmdResult").getAsJsonObject();

        status = cmdResult.has("result") ? cmdResult.get("result").getAsString() : null;
        command = dataNode.has("response") ? dataNode.get("response").getAsString() : null;
        ecrId = dataNode.has("ecrId") ? dataNode.get("ecrId").getAsString() : null;

        if (status != null && !status.equals("Success")) {
            deviceResponseText = String.format("Error: %s - %s", cmdResult.get("errorCode").getAsString(), cmdResult.get("errorMessage").getAsString());
            return;
        }

        if (batches == null) {
            batches = new ArrayList<>();
        }

        for (JsonElement batch : dataNode.getAsJsonArray("batchesAvail")) {
            batches.add(batch.getAsInt());
        }
    }
}
