package com.global.api.entities.billing;

public class BillingResponse {
    /**
     * Indicates if the action was succesful
     */
    protected boolean isSuccessful;

    /**
     * The response code from the Billing Gateway
     */
    protected String responseCode;

    /**
     * The response message from the Billing Gateway
     */
    protected String responseMessage;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setIsSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
