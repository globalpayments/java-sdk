package com.global.api.gateways;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.payroll.*;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;

public class PayrollConnector extends RestGateway {
    private String username;
    private String password;
    private String apiKey;
    private String sessionToken;
    private PayrollEncoder encoder;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public PayrollEncoder getEncoder() {
        if(encoder == null) {
            encoder = new PayrollEncoder();
            encoder.setUsername(username);
            encoder.setApiKey(apiKey);
        }
        return encoder;
    }

    public void setEncoder(PayrollEncoder encoder) {
        this.encoder = encoder;
    }

    public void signIn() throws ApiException {
        PayrollRequest request = SessionInfo.signIn(username, password, getEncoder());

        SessionInfo response = sendEncryptedRequest(request, SessionInfo.class).getResults().get(0);
        if (!StringUtils.isNullOrEmpty(response.getErrorMessage()))
            throw new ApiException(response.getErrorMessage());

        sessionToken = response.getSessionToken();

        // Build the basic request header
        String credentials = String.format("%s|%s", sessionToken, username);
        String basicAuth = Base64.encodeBase64String(credentials.getBytes());
        headers.put("Authorization", String.format("Basic %s", basicAuth));
    }
    public void signOut() throws ApiException {
        sendEncryptedRequest(SessionInfo.signOut(), SessionInfo.class);
    }

    public <T extends PayrollEntity> PayrollResponse<T> sendEncryptedRequest(IPayrollRequestBuilder requestBuilder, Class<T> clazz, Object... args) throws ApiException {
        return sendEncryptedRequest(requestBuilder.buildRequest(getEncoder(), clazz), clazz);
    }

    public <T extends PayrollEntity> PayrollResponse<T> sendEncryptedRequest(PayrollRequest request, Class<T> clazz) throws ApiException {
        try {
            if(clazz != SessionInfo.class && StringUtils.isNullOrEmpty(sessionToken))
                throw new ApiException("Payroll connector is not signed in, please check your configuration.");

            String response = doTransaction("POST", request.getEndpoint(), request.getRequestBody());
            return new PayrollResponse<T>(response, getEncoder(), clazz);
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }

    protected String handleResponse(GatewayResponse response) throws GatewayException {
        if (response.getStatusCode() != 200) {
            String responseMessage = JsonDoc.parseSingleValue(response.getRawResponse(), "ResponseMessage");
            throw new GatewayException(String.format("Status Code: %s - %s", response.getStatusCode(), responseMessage));
        }
        return response.getRawResponse();
    }
}
