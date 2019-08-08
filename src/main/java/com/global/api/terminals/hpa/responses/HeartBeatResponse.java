package com.global.api.terminals.hpa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.Element;

public class HeartBeatResponse extends SipBaseResponse {
    private String transactionTime;

    public String getTransactionTime() {
        return transactionTime;
    }

    public HeartBeatResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);

        responseCode = normalizeResponse(response.getString("Result"));
        responseText = response.getString("ResultText");
        transactionTime = response.getString("TransactionTime");
    }
}
