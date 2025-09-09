package com.global.api.terminals.diamond.interfaces;

import com.global.api.entities.Request;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.gateways.SSLSocketFactoryEx;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DiamondHttpInterface implements IDeviceCommInterface {
    private final ITerminalConfiguration settings;
    protected Map<String, String> headers;
    private String lastConnectionError = null;
    private String AuthorizationId;

    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;

    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }
    public void setMessageReceivedHandler(IMessageReceivedInterface onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public DiamondHttpInterface(ITerminalConfiguration settings) {
        this.settings = settings;
    }

    public void connect() {
        char[] isvIdAsArray = settings.getIsvId().toCharArray();
        char[] reversedIsvIdAsArray = getReversedArray(isvIdAsArray);
        String reversedString = new String(reversedIsvIdAsArray);
        String data = reversedString + AuthorizationId;

        try {
            String authorizationToken = getHMACSHA256Hash(data, settings.getSecretKey());

            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put("Authorization", "Bearer " + authorizationToken);
        } catch (Exception e) {
            lastConnectionError = e.getMessage();
        }
    }

    public void disconnect() {
        /* NOTHING TO IMPLEMENT */
    }

    public byte[] send(IDeviceMessage message) throws GatewayException, MessageException {
        JsonDoc jsonMessage = JsonDoc.parse(new String(message.getSendBuffer(), StandardCharsets.UTF_8));

        // GET QUERY PARAMS & AUTHORIZATION ID
        JsonDoc queryParams = jsonMessage.get("queryParams");
        obtainAuthorizationId(queryParams);

        connect();
        if (lastConnectionError != null) {
            throw new MessageException(String.format("Could not connect to the device. %s", lastConnectionError));
        }

        try {
            String serviceUrl = settings.getServiceUrl();
            String endpoint = jsonMessage.getString("endpoint");
            String queryString = buildQueryString(queryParams);
            String url = serviceUrl + endpoint + queryString;

            String verb = getVerb(jsonMessage);
            JsonDoc data = jsonMessage.get("body");

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

            try {
                // LOG THE REQUEST
                raiseOnMessageSent(requestJsonDoc.toString());

                // SEND THE REQUEST
                try (OutputStream out = httpClient.getOutputStream()) {
                    out.write(data.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();

                    // CHECK RESPONSE CODE
                    int statusResponse = httpClient.getResponseCode();
                    if (statusResponse != HttpStatus.SC_OK) {
                        throw new MessageException("ERROR: status code " + statusResponse);
                    }

                    // READ RESPONSE
                    try (InputStream responseStream = httpClient.getInputStream()) {
                        String rawResponse = IOUtils.readFully(responseStream);

                        // LOG THE RESPONSE
                        raiseOnMessageReceived(rawResponse.getBytes());

                        checkResponse(rawResponse);

                        return rawResponse.getBytes(StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception exc) {
                throw new GatewayException("Error occurred while sending the request.", exc);
            }

        } catch (Exception e) {
            throw new GatewayException("Device " + settings.getDeviceType() + " error: " + e.getMessage());
        }
    }

    private void raiseOnMessageSent(String message) {
        try {
            if (onMessageSent != null) {
                onMessageSent.messageSent(message);
            }

            if (settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "REQUEST");
                msg.set("message", message);

                settings.getRequestLogger().RequestSent(msg.toString());
            }
        }
        catch(IOException exc) {
            /* Logging should never interfere with processing */
        }
    }

    private void raiseOnMessageReceived(byte[] message) {
        try {
            if(onMessageReceived != null) {
                onMessageReceived.messageReceived(message);
            }

            if(settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "RESPONSE");
                msg.set("message", new String(message));

                settings.getRequestLogger().ResponseReceived(msg.toString());
            }
        }
        catch(IOException exc) {
            /* NOM NOM */
        }
    }

    /* INTERNAL FUNCTIONS */

    private void checkResponse(String rawResponse) throws GatewayException {
        if (JsonDoc.isJson(rawResponse)) {
            JsonDoc response = JsonDoc.parse(rawResponse);
            if (response.has("status") && "error".equals(response.getStringOrNull("status"))) {
                String code = response.getStringOrNull("code");
                String responseMessage = response.getStringOrNull("message");
                throw new GatewayException("Status Code: " + code + " - " + responseMessage);
            }
        }
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
        StringBuilder repeated = new StringBuilder();
        for (int i = 1; i < 8; i++) {
            repeated.append(valueToRepeat);
        }
        return repeated.toString();
    }

    private String getHMACSHA256Hash(String data, String apiSecret) throws NoSuchAlgorithmException, InvalidKeyException {

        String preparedSecret = repeat8times(apiSecret);

        byte[] preparedSecretBytes = preparedSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(preparedSecretBytes, "HmacSHA256");

        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] hashSignature = sha256HMAC.doFinal(dataBytes);

        return Hex.encodeHexString(hashSignature);
    }

    private String getVerb(JsonDoc bufferAsJsonDoc) throws GatewayException {
        String verb;

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
}
