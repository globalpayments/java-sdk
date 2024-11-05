package com.global.api.terminals.upa.responses;

import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.utils.JsonDoc;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchReportResponse extends UpaResponseHandler implements ITerminalReport {
    public BatchReportResponse(JsonDoc responseData) {
            try {
                parseResponse(responseData);
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
            JsonDoc response = isGpApiResponse(responseData) ? responseData.get("response") : responseData.get("data");
            //@TODO continue with the mapping
    }
}
