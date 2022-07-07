package com.global.api.tests;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.SSLSocketFactoryEx;
import com.global.api.utils.IOUtils;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ThreeDSecureAcsClient {
    private String _serviceUrl;

    public ThreeDSecureAcsClient(String url) {
        _serviceUrl = url;
    }

    public AcsResponse authenticate(String payerAuthRequest) throws ApiException {
        return authenticate(payerAuthRequest, "");
    }
    public AcsResponse authenticate(String payerAuthRequest, String merchantData) throws ApiException {
        HashMap<String, String> kvps = new HashMap<String, String>();
        kvps.put("PaReq", payerAuthRequest);
        kvps.put("TermUrl", "https://www.mywebsite.com/process3dSecure");
        kvps.put("MD", merchantData);

        String rawResponse;
        try {
            byte[] postData = buildData(kvps).getBytes("UTF-8");

            HttpsURLConnection conn = (HttpsURLConnection) new URL((_serviceUrl).trim()).openConnection();
            conn.setSSLSocketFactory(new SSLSocketFactoryEx());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.addRequestProperty("Content-Length", postData.length + "");

            DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
            requestStream.write(postData);
            requestStream.flush();
            requestStream.close();

            InputStream responseStream = conn.getInputStream();
            rawResponse = IOUtils.readFully(responseStream);
            responseStream.close();

            if (conn.getResponseCode() != 200)
                throw new ApiException(String.format("Acs request failed with response code: %s", conn.getResponseCode()));
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }

        AcsResponse rvalue = new AcsResponse();
        rvalue.setAuthResponse(GetInputValue(rawResponse, "PaRes"));
        rvalue.setMerchantData(GetInputValue(rawResponse, "MD"));
        return rvalue;
    }

    private String buildData(HashMap<String, String> kvps) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry: kvps.entrySet()) {
            if(first)
                first = false;
            else result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private String GetInputValue(String raw, String inputValue) {
        if(raw == null)
            return null;

        String searchString = String.format("name=\"%s\" value=\"", inputValue);

        int index = raw.indexOf(searchString);
        if (index > -1) {
            index = index + searchString.length();

            int length = raw.indexOf("\"", index) - index;
            return raw.substring(index, index + length);
        }
        return null;
    }
}