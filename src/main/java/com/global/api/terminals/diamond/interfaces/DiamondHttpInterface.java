package com.global.api.terminals.diamond.interfaces;

import com.global.api.entities.Request;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.gateways.SSLSocketFactoryEx;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.diamond.DiamondCloudConfig;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.IOUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class DiamondHttpInterface implements IDeviceCommInterface {
    private final DiamondCloudConfig _settings;
    protected Map<String, String> headers;
    private String lastConnectionError = null;
    private String AuthorizationId;

    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;

    public DiamondHttpInterface(ITerminalConfiguration settings) {
        _settings = (DiamondCloudConfig) settings;
    }

    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public void connect() {

        char[] isvIDAsArray = _settings.getIsvID().toCharArray();
        char[] reversedIsvIDAsArray = getReversedArray(isvIDAsArray);
        String reversedString = new String(reversedIsvIDAsArray);
        String data = reversedString + AuthorizationId;
        String authorizationToken = null;

        try {
            authorizationToken = getHMACSHA256Hash(data, _settings.getSecretKey());
        } catch (Exception e) {
            lastConnectionError = e.getMessage();
        }

        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put("Authorization", "Bearer " + authorizationToken);
    }

    private char[] getReversedArray(char[] stringArray) {

        if (stringArray == null) {
            return null;
        } else if (stringArray.length == 0) {
            return stringArray;
        }

        char[] reversedArray = new char[stringArray.length];

        for (int i = 0; i < stringArray.length; i++) {
            reversedArray[i] = stringArray[stringArray.length - 1 - i];
        }

        return reversedArray;
    }

    private String repeat8times(String valueToRepeat) {
        String repeated = "";
        for (int i = 1; i < 8; i++) {
            repeated = repeated + valueToRepeat;
        }
        return repeated;
    }

    private String getHMACSHA256Hash(String data, String apiSecret) throws NoSuchAlgorithmException, InvalidKeyException {

        String preparedSecret = repeat8times(apiSecret);

        byte[] preparedSecretBytes = preparedSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(preparedSecretBytes, "HmacSHA256");

        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] hashSignature = sha256HMAC.doFinal(dataBytes);

        String hashSignatureAsString = Hex.encodeHexString(hashSignature);
        return hashSignatureAsString;
    }

    public void disconnect() {
        // TODO(mfranzoy): do we need to do anything here?
    }

    private String getVerb(JsonDoc bufferAsJsonDoc) throws GatewayException {

        String verb = null;

        JsonDoc verbData = bufferAsJsonDoc.get("verb");

        if (verbData != null) {
            verb = verbData.getStringOrNull("Method");
            if (verb == null) {
                throw new GatewayException("Payment type not supported!");
            }
        } else {
            verb = bufferAsJsonDoc.getStringOrNull("verb");
        }

        if (verb == null) {
            verb = Request.HttpMethod.Post.getValue();
        }
        return verb;
    }

    private void obtainAuthorizationId(JsonDoc queryParams) {
        if (StringUtils.isNullOrEmpty(queryParams.getString("cloud_id"))) {
            AuthorizationId = queryParams.getString("POS_ID");
        } else {
            AuthorizationId = queryParams.getString("cloud_id");
        }
    }

    public byte[] send(IDeviceMessage message) throws GatewayException, MessageException {

        if (this.onMessageSent != null) {
            this.onMessageSent.messageSent(message.toString());
        }

        String buffer = new String(message.getSendBuffer(), StandardCharsets.UTF_8);
        JsonDoc bufferAsJsonDoc = JsonDoc.parse(buffer);

        JsonDoc queryParams = bufferAsJsonDoc.get("queryParams");
        obtainAuthorizationId(queryParams);

        connect();

        if (lastConnectionError != null) {
            throw new MessageException(String.format("Could not connect to the device. %s", lastConnectionError));
        }

        String queryString = buildQueryString(queryParams);
        String verb = getVerb(bufferAsJsonDoc);

        JsonDoc data = bufferAsJsonDoc.get("body");

        try {
            String serviceUrl = _settings.getServiceUrl();
            String endpoint = bufferAsJsonDoc.getString("endpoint");

            String url = serviceUrl + endpoint + queryString;

            HttpsURLConnection httpClient = (HttpsURLConnection) new URL((url).trim()).openConnection();
            httpClient.setRequestMethod(verb);
            httpClient.setConnectTimeout(30000);
            httpClient.setSSLSocketFactory(new SSLSocketFactoryEx());
            httpClient.setDoInput(true);
            httpClient.setDoOutput(true);
            setHeaders(httpClient);

            JsonDoc requestJsonDoc = new JsonDoc()
                    .set("verb", verb)
                    .set("url", serviceUrl + endpoint)
                    .set("content_length", data.toString().length())
                    .set("content", data);

            OutputStream out = null;
            try {

                if (data != null) {
                    out = httpClient.getOutputStream();
                    out.write(data.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                _settings.getLogManagementProvider().RequestSent(generateRequestLog(requestJsonDoc));

                int statusResponse = httpClient.getResponseCode();
                if (statusResponse != HttpStatus.SC_OK) {
                    throw new ApiException("ERROR: status code " + statusResponse);
                }

                InputStream responseStream = httpClient.getInputStream();
                String rawResponse = IOUtils.readFully(responseStream);

                checkResponse(rawResponse);
                _settings.getLogManagementProvider().ResponseReceived(generateResponseLog(rawResponse));

                return rawResponse.getBytes(StandardCharsets.UTF_8);
            } catch (Exception exc) {
                throw new GatewayException("Error occurred while sending the request.", exc);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

        } catch (Exception e) {
            throw new GatewayException("Device " + _settings.getDeviceType() + " error: " + e.getMessage());
        }
    }

    private void checkResponse(String rawResponse) throws GatewayException {
        if (JsonDoc.isJson(rawResponse)) {
            JsonDoc responseBodyJsonDoc = JsonDoc.parse(rawResponse);
            if (responseBodyJsonDoc.has("status") && "error".equals(responseBodyJsonDoc.getStringOrNull("status"))) {
                String code = responseBodyJsonDoc.getStringOrNull("code");
                String responseMessage = responseBodyJsonDoc.getStringOrNull("message");
                throw new GatewayException("Status Code: " + code + " - " + responseMessage);
            }
        }
    }

    @Override
    public void setMessageSentHandler(IMessageSentInterface messageInterface) {
        this.onMessageSent = messageInterface;
    }

    private String buildQueryString(JsonDoc queryStringParams) throws GatewayException {

        if (queryStringParams == null) {
            return "";
        }

        StringBuilder queryBuilder = new StringBuilder();
        try {
            for (String key : queryStringParams.getKeys()) {
                queryBuilder.append(URLEncoder.encode(key, "UTF-8"));
                queryBuilder.append("=");
                queryBuilder.append(URLEncoder.encode(queryStringParams.getStringOrNull(key), "UTF-8"));
                queryBuilder.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            throw new GatewayException("Could not build query parameter: " + e.getMessage());
        }

        if (queryBuilder.length() > 0) {
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        }

        queryBuilder.insert(0, "?");

        return queryBuilder.toString();
    }

    private void setHeaders(HttpsURLConnection conn) {
        this.headers.put("Content-Type", "application/json; charset=utf-8");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String key = header.getKey();
            String value = header.getValue();
            conn.addRequestProperty(key, value);
        }
    }

    private String generateRequestLog(JsonDoc request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request: ").append(request.getString("verb")).append(" ").append(request.getString("url")).append("\n");

        for (Map.Entry<String, String> header : this.headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(String.join(", ", header.getValue())).append("\n");
        }

        sb.append("Content-Length: ").append(request.getString("content_length")).append("\n");
        sb.append(request.getString("content")).append("\n");

        return sb.toString();
    }

    private String generateResponseLog(String response) {
        return "Response: " + response + "\n";
    }

}
