package com.global.api.gateways;

class GatewayResponse {
    private int statusCode;
    private String rawResponse;

    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public String getRawResponse() {
        return rawResponse;
    }
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
