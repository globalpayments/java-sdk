package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;

import java.util.List;

public abstract class BasePayrollResponse {
    protected int totalRecords;
    protected List<JsonDoc> rawResults;
    protected DateTime timestamp;
    protected int statusCode;
    protected String responseMessage;

    int getTotalRecords() {
        return totalRecords;
    }

    List<JsonDoc> getRawResults() {
        return rawResults;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    int getStatusCode() {
        return statusCode;
    }

    String getResponseMessage() {
        return responseMessage;
    }

    BasePayrollResponse(String rawResponse) throws ApiException {
        JsonDoc doc = JsonDoc.parse(rawResponse);
        MapResponseValues(doc);

        if (statusCode != 200) {
            throw new ApiException(responseMessage);
        }
    }

    protected void MapResponseValues(JsonDoc doc) {
        totalRecords = doc.getInt("TotalRecords");
        rawResults = doc.getEnumerator("Results");
//        timestamp = doc.getValue("Timestamp", (input) => {
//            if(input != null)
//                return DateTime.Parse(input.ToString());
//            return null;
//        });
        statusCode = doc.getInt("StatusCode");
        responseMessage = doc.getString("ResponseMessage");
    }
}
