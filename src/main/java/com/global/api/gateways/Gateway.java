package com.global.api.gateways;

import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.logging.IRequestLogger;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.logging.RequestFileLogger;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.IOUtils;
import com.global.api.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.http.entity.mime.MultipartEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.global.api.logging.PrettyLogger.toPrettyJson;

@Accessors(chain = true)
@Getter
@Setter
public abstract class Gateway {

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private final String lSChar = System.getProperty("line.separator");
    protected HashMap<String, String> headers;
    protected HashMap<String, String> dynamicHeaders;
    protected int timeout;
    protected String serviceUrl;
    protected Proxy webProxy;
    protected Environment environment;
    private String contentType;
    private boolean enableLogging;
    private IRequestLogger requestLogger;
    private StringBuilder logEntry = new StringBuilder();
    private ThreadLocal<Map<String, String>> maskedRequestData;

    public Gateway(String contentType) {
        headers = new HashMap<>();
        dynamicHeaders = new HashMap<>();
        this.contentType = contentType;
        maskedRequestData = new ThreadLocal<>();
    }

    protected GatewayResponse sendRequest(String verb, String endpoint) throws GatewayException {
        return sendRequest(verb, endpoint, null, null);
    }

    protected GatewayResponse sendRequest(String verb, String endpoint, String data) throws GatewayException {
        return sendRequest(verb, endpoint, data, null);
    }

    protected GatewayResponse sendRequest(String verb, String endpoint, String data, HashMap<String, String> queryStringParams) throws GatewayException {
        HttpsURLConnection conn = null;
        try {
            String queryString = buildQueryString(queryStringParams);
            if (webProxy != null) {
                conn = (HttpsURLConnection) new URL((serviceUrl + endpoint + queryString).trim()).openConnection(webProxy);
            } else {
                conn = (HttpsURLConnection) new URL((serviceUrl + endpoint + queryString).trim()).openConnection();
            }
            conn.setSSLSocketFactory(new SSLSocketFactoryEx());
            conn.setConnectTimeout(timeout);
            conn.setDoInput(true);
            // ----------------------------------------------------------------------
            // Fix: Supports PATCH requests in HttpsURLConnection on JAVA & Android
            // ----------------------------------------------------------------------
            if ("PATCH".equalsIgnoreCase(verb)) {
                setRequestMethod(conn, verb);
            } else {
                conn.setRequestMethod(verb);
            }
            // ----------------------------------------------------------------------

            // If Content-Type is added for some GP-API endpoints we get a 502: Bad gateway error
            if (!contentTypeNotAllowedEndpoints(verb, endpoint)) {
                conn.addRequestProperty("Content-Type", String.format("%s; charset=UTF-8", contentType));
            }

            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.addRequestProperty(header.getKey(), header.getValue());
            }

            if (dynamicHeaders != null) {
                for (Map.Entry<String, String> dynamicHeader : dynamicHeaders.entrySet()) {
                    conn.addRequestProperty(dynamicHeader.getKey(), dynamicHeader.getValue());
                }
            }

            if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Endpoint:       ").append(verb).append(" ").append(serviceUrl).append(endpoint).append(lSChar);
                logEntry.append("Proxy:          ").append((webProxy != null) ? webProxy.toString() : "none").append(lSChar).append(lSChar);
                logEntry.append("Headers:        ").append(lSChar);
                logRequestHeaders(conn, logEntry);
            }

            if (!verb.equals("GET")) {
                byte[] request = data.getBytes();

                conn.setDoOutput(true);
                conn.addRequestProperty("Content-Length", String.valueOf(request.length));

                if (this.enableLogging || this.requestLogger != null) {
                    String maskedRequest = maskFieldsIfNeeded(data);
                    logEntry.append("Request Body: ").append(lSChar);
                    if (acceptJson()) {
                        if (!StringUtils.isNullOrEmpty(maskedRequest)) {
                            logEntry.append(toPrettyJson(maskedRequest));
                        }
                    } else {
                        logEntry.append(StringUtils.mask(maskedRequest));
                    }

                    generateRequestLog();
                }
                try (DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream())) {
                    requestStream.write(request);
                    requestStream.flush();
                }
            } else if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Request Params: ").append(queryString).append(lSChar);
            }

            try (InputStream responseStream = conn.getInputStream()) {
                String rawResponse = getRawResponse(responseStream, "gzip".equalsIgnoreCase(conn.getContentEncoding()));

                if (this.enableLogging || this.requestLogger != null) {
                    if (acceptJson()) {
                        logEntry.append("Response Code: ").append(conn.getResponseCode()).append(" ").append(conn.getResponseMessage()).append(lSChar);
                        logEntry.append(lSChar).append("Response Headers:").append(lSChar);
                        logResponseHeaders(conn, logEntry);
                        String maskedResponse = maskFieldsIfNeeded(rawResponse);
                        logEntry.append("Response Body:").append(lSChar).append(toPrettyJson(maskedResponse));
                    } else {
                        logEntry.append(rawResponse);
                    }

                    generateResponseLog();
                }

                GatewayResponse response = new GatewayResponse();
                response.setStatusCode(conn.getResponseCode());
                response.setRawResponse(rawResponse);
                return response;
            }
        } catch (Exception exc) {
            if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Exception:").append(lSChar).append(exc.getMessage());

                generateResponseLog();
            }

            try {
                assert conn != null;
                throw new GatewayException("Error occurred while communicating with gateway.", exc, String.valueOf(conn.getResponseCode()), getRawResponse(conn.getErrorStream(), "gzip".equalsIgnoreCase(conn.getContentEncoding())));
            } catch (IOException e) {   // Legacy GatewayException
                throw new GatewayException("Error occurred while communicating with gateway.", exc);
            }
        }
    }

    private void logRequestHeaders(HttpsURLConnection conn, StringBuilder logEntry) {
        Map<String, List<String>> requestHeaders = conn.getRequestProperties();

        logHeaders(logEntry, requestHeaders);

        appendAuthorizationHeader(logEntry);

        logEntry.append(lSChar);
    }

    private void logResponseHeaders(HttpsURLConnection conn, StringBuilder logEntry) {
        Map<String, List<String>> responseHeaders = conn.getHeaderFields();

        logHeaders(logEntry, responseHeaders);

        logEntry.append(lSChar);
    }

    private void logHeaders(StringBuilder logEntry, Map<String, List<String>> responseHeaders) {
        if (responseHeaders != null) {
            for (String headerKey : responseHeaders.keySet()) {
                String headerValue = responseHeaders.get(headerKey).toString();
                logEntry.append("  ").append(headerKey).append(": ").append(headerValue, 1, headerValue.length() - 1).append(lSChar);
            }
        }
    }

    private void appendAuthorizationHeader(StringBuilder logEntry) {
        if (!headers.containsKey(AUTHORIZATION_HEADER_KEY)) {
            return;
        }
        String value = headers.get(AUTHORIZATION_HEADER_KEY);
        logEntry.append(AUTHORIZATION_HEADER_KEY).append(": ").append(value).append(lSChar);
    }

    public String getRawResponse(InputStream responseStream) throws IOException {
        return getRawResponse(responseStream, true);
    }

    public String getRawResponse(InputStream responseStream, Boolean isGzip) throws IOException {
        String rawResponse = null;

        if (isGzip) {

            // Decompress GZIP response
            try (GZIPInputStream gzis = new GZIPInputStream(responseStream);
                 InputStreamReader reader = new InputStreamReader(gzis);
                 BufferedReader in = new BufferedReader(reader)) {

                StringBuilder decompressedResponse = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    decompressedResponse.append(line);
                }
                rawResponse = decompressedResponse.toString();
            } catch (Exception e) {
                throw new IOException("Error while parsing the response," + e);
            }

        } else {
            rawResponse = IOUtils.readFully(responseStream);

        }
        return rawResponse;
    }

    protected GatewayResponse sendRequest(String endpoint, MultipartEntity content) throws GatewayException {
        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection) new URL((serviceUrl + endpoint).trim()).openConnection();
            conn.setSSLSocketFactory(new SSLSocketFactoryEx());
            conn.setConnectTimeout(timeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", content.getContentType().getValue());
            conn.addRequestProperty("Content-Length", String.valueOf(content.getContentLength()));

            try (InputStream responseStream = conn.getInputStream();
                 OutputStream out = conn.getOutputStream()) {

                if (this.enableLogging || this.requestLogger != null) {
                    logEntry.append("Request: ").append(content).append(lSChar);

                    generateRequestLog();
                }
                content.writeTo(out);
                out.flush();

                String rawResponse = IOUtils.readFully(responseStream);
                if (this.enableLogging || this.requestLogger != null) {
                    logEntry.append(content).append(lSChar);

                    generateResponseLog();
                }

                GatewayResponse response = new GatewayResponse();
                response.setStatusCode(conn.getResponseCode());
                response.setRawResponse(rawResponse);
                return response;
            } catch (Exception exc) {
                throw new GatewayException("Error occurred while sending the request.", exc);
            }
        } catch (Exception exc) {
            throw new GatewayException("Error occurred while communicating with gateway.", exc);
        }
    }

    protected void addMaskedData(Map<String, String> dataToMask) {
        Map<String, String> localDataToMask = maskedRequestData.get();
        if (localDataToMask == null) {
            localDataToMask = new HashMap<>();
            maskedRequestData.set(localDataToMask);
        }
        localDataToMask.putAll(dataToMask);
    }

    private String buildQueryString(HashMap<String, String> queryStringParams) throws UnsupportedEncodingException {
        if (queryStringParams == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for (Map.Entry<String, String> entry : queryStringParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", URLEncoder.encode(entry.getKey(), "UTF-8"), URLEncoder.encode(entry.getValue(), "UTF-8")));
        }
        return sb.toString();
    }

    private void setRequestMethod(final HttpURLConnection c, final String value) {
        try {
            Object target = c;
            final Field delegate = getField(c.getClass(), "delegate");
            if (delegate != null) {
                delegate.setAccessible(true);
                target = delegate.get(c);
            }
            final Field f = HttpURLConnection.class.getDeclaredField("method");
            f.setAccessible(true);
            f.set(target, value);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
    }

    private Field getField(Class<?> clazz, String fieldName) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = null;
        }
        return field;
    }

    private boolean acceptJson() {
        return
                headers.containsKey("Accept") &&
                        headers.get("Accept").equalsIgnoreCase("application/json");
    }

    private boolean acceptGzipEncoding() {
        return
                headers.containsKey("Accept-Encoding") &&
                        headers.get("Accept-Encoding").equalsIgnoreCase("gzip");
    }

    // For some reason, if Content-Type is added for some GP-API endpoints we get a 502: Bad gateway error
    private boolean contentTypeNotAllowedEndpoints(String verb, String endpoint) {
        return
                (serviceUrl.endsWith("globalpay.com/ucp") &&
                        (
                                "GET".equalsIgnoreCase(verb) &&
                                        (
                                                endpoint.startsWith(GpApiRequest.DEPOSITS_ENDPOINT) ||
                                                        endpoint.startsWith(GpApiRequest.SETTLEMENT_DISPUTES_ENDPOINT) ||
                                                        endpoint.startsWith(GpApiRequest.DISPUTES_ENDPOINT)
                                        ) ||
                                        "POST".equalsIgnoreCase(verb) &&
                                                (
                                                        endpoint.startsWith(GpApiRequest.DISPUTES_ENDPOINT) &&
                                                                endpoint.endsWith("/acceptance")
                                                )
                        )
                ) || serviceUrl.endsWith("paygateway.com/transactions");
    }

    private void generateRequestLog() {
        if (enableLogging) {    // At least we need to print in console
            if (requestLogger == null) {
                new RequestConsoleLogger().RequestSent(logEntry.toString());
            } else {
                try {
                    if (requestLogger instanceof RequestFileLogger) {
                        requestLogger.RequestSent(logEntry.toString());
                        new RequestConsoleLogger().RequestSent(logEntry.toString());
                    } else if (requestLogger instanceof RequestConsoleLogger) {
                        requestLogger.RequestSent(logEntry.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (requestLogger != null) {
                try {
                    requestLogger.RequestSent(logEntry.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        logEntry.delete(0, logEntry.length());
    }

    private void generateResponseLog() {
        if (enableLogging) {    // At least we need to print in console
            if (requestLogger == null) {
                new RequestConsoleLogger().ResponseReceived(logEntry.toString());
            } else {
                try {
                    if (requestLogger instanceof RequestFileLogger) {
                        requestLogger.ResponseReceived(logEntry.toString());
                        new RequestConsoleLogger().ResponseReceived(logEntry.toString());
                    } else if (requestLogger instanceof RequestConsoleLogger) {
                        requestLogger.ResponseReceived(logEntry.toString());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (requestLogger != null) {
                try {
                    requestLogger.ResponseReceived(logEntry.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logEntry.delete(0, logEntry.length());
    }

    private String maskFieldsIfNeeded(String data) {
        Map<String, String> maskedRequestData = this.maskedRequestData.get();
        if ((maskedRequestData == null || maskedRequestData.isEmpty()) && environment != Environment.PRODUCTION)
            return data;
        if (isXml(data)) {
            return maskXml(data);
        } else {
            return maskJson(data);
        }
    }

    private String maskJson(String jsonObject) {
        JsonElement objectToMask = JsonParser.parseString(jsonObject);
        Map<String, String> maskedRequestData = this.maskedRequestData.get();
        for (Map.Entry<String, String> entry : maskedRequestData.entrySet()) {
            String key = entry.getKey();
            String[] keys = key.split("\\.");
            searchAndReplace(objectToMask, keys, entry.getValue(), 0);
        }
        return objectToMask.toString();
    }

    private void searchAndReplace(JsonElement element, String[] keys, String elementToReplace, int currentIndex) {
        if (element.isJsonNull()) return;
        if (element.isJsonArray()) {
            JsonArray ja = (JsonArray) element;
            for (JsonElement je : ja) {
                searchAndReplace(je, keys, elementToReplace, currentIndex);
            }
            return;
        }
        JsonObject jo = (JsonObject) element;
        String currentKey = keys[currentIndex];
        if (!jo.has(currentKey)) return;
        if (currentIndex == (keys.length - 1)) {
            jo.addProperty(currentKey, elementToReplace);
            return;
        }
        searchAndReplace(jo.get(currentKey), keys, elementToReplace, currentIndex + 1);
    }

    private String maskXml(String xmlObject) {
        try {
            ElementTree xml = ElementTree.parse(xmlObject);
            Map<String, String> maskedRequestData = this.maskedRequestData.get();
            if (maskedRequestData == null) return xmlObject;
            for (Map.Entry<String, String> entry : maskedRequestData.entrySet()) {
                String key = entry.getKey();
                String[] keys = key.split("\\.");

                Element element = xml.get(keys[0]);
                for (int i = 1; i < keys.length - 1; i++) {
                    if (!element.has(keys[i])) break;
                    element = element.get(keys[i]);
                }
                if (!element.has(keys[keys.length - 1])) continue;
                element.get(keys[keys.length - 1]).setText(entry.getValue());
            }
            return xml.toString();
        } catch (ApiException exception) {
            return xmlObject;
        }
    }

    private boolean isXml(String data) {
        return data.startsWith("<");
    }
}