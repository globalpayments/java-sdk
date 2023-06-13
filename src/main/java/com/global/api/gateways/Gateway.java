package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.logging.IRequestLogger;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.logging.RequestFileLogger;
import com.global.api.utils.IOUtils;
import com.global.api.utils.StringUtils;
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

    private String contentType;
    private boolean enableLogging;
    private IRequestLogger requestLogger;
    private StringBuilder logEntry = new StringBuilder();
    private final String lSChar = System.getProperty("line.separator");
    protected HashMap<String, String> headers;
    protected HashMap<String, String> dynamicHeaders;
    protected int timeout;
    protected String serviceUrl;
    protected Proxy webProxy;

    public Gateway(String contentType) {
        headers = new HashMap<>();
        dynamicHeaders = new HashMap<>();
        this.contentType = contentType;
    }

    protected GatewayResponse sendRequest(String verb, String endpoint) throws GatewayException {
        return sendRequest(verb, endpoint, null, null);
    }
    protected GatewayResponse sendRequest(String verb, String endpoint, String data) throws GatewayException {
        return sendRequest(verb, endpoint, data, null);
    }
    protected GatewayResponse sendRequest(String verb, String endpoint, String data, HashMap<String, String> queryStringParams) throws GatewayException {
        HttpsURLConnection conn = null;
        try{
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

            for (Map.Entry<String, String> header: headers.entrySet()) {
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
                    logEntry.append("Request Body: ").append(lSChar);
                    if (acceptJson()) {
                        if (!StringUtils.isNullOrEmpty(data)) {
                            logEntry.append(toPrettyJson(data));
                        }
                    } else {
                        logEntry.append(StringUtils.mask(data));
                    }

                    generateRequestLog();
                }
                try (DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream())) {
                    requestStream.write(request);
                    requestStream.flush();
                }
            }
            else if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Request Params: ").append(queryString).append(lSChar);
            }

            InputStream responseStream = conn.getInputStream();
            String rawResponse = getRawResponse(responseStream);
            responseStream.close();

            if (this.enableLogging || this.requestLogger != null) {
                if (acceptJson()) {
                    logEntry.append("Response Code: ").append(conn.getResponseCode()).append(" ").append(conn.getResponseMessage()).append(lSChar);
                    logEntry.append("Response Body:").append(lSChar).append(toPrettyJson(rawResponse));
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
        catch(Exception exc) {
            if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Exception:").append(lSChar).append(exc.getMessage());

                generateResponseLog();
            }

            try {
                assert conn != null;
                throw new GatewayException("Error occurred while communicating with gateway.", exc, String.valueOf(conn.getResponseCode()), getRawResponse(conn.getErrorStream()));
            } catch (IOException e) {   // Legacy GatewayException
                throw new GatewayException("Error occurred while communicating with gateway.", exc);
            }
        }
    }

    private void logRequestHeaders(HttpsURLConnection conn, StringBuilder logEntry) {
        Map<String, List<String>> requestHeaders = conn.getRequestProperties();

        if (requestHeaders != null) {
            for (String headerKey : requestHeaders.keySet()) {
                String headerValue = requestHeaders.get(headerKey).toString();
                logEntry.append(headerKey).append(": ").append(headerValue, 1, headerValue.length() - 1).append(lSChar);
            }
        }

        appendAuthorizationHeader(logEntry);

        logEntry.append(lSChar);
    }

    private void appendAuthorizationHeader(StringBuilder logEntry) {
        if (!headers.containsKey(AUTHORIZATION_HEADER_KEY)) {
            return;
        }
        String value = headers.get(AUTHORIZATION_HEADER_KEY);
        logEntry.append(AUTHORIZATION_HEADER_KEY).append(": ").append(value).append(lSChar);
    }

    public String getRawResponse(InputStream responseStream) throws IOException {
        String rawResponse = null;

        if (acceptGzipEncoding()) {

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
        try{
            conn = (HttpsURLConnection)new URL((serviceUrl + endpoint).trim()).openConnection();
            conn.setSSLSocketFactory(new SSLSocketFactoryEx());
            conn.setConnectTimeout(timeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", content.getContentType().getValue());
            conn.addRequestProperty("Content-Length", String.valueOf(content.getContentLength()));

            try(InputStream responseStream = conn.getInputStream();
                OutputStream out = conn.getOutputStream();) {

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
            }catch(Exception exc) {
                throw new GatewayException("Error occurred while sending the request.", exc);
            }
        }
        catch(Exception exc) {
            throw new GatewayException("Error occurred while communicating with gateway.", exc);
        }
    }

    private String buildQueryString(HashMap<String, String> queryStringParams) throws UnsupportedEncodingException {
        if(queryStringParams == null) {
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
                (
                        serviceUrl.endsWith("globalpay.com/ucp") &&
                        (
                                "GET".equalsIgnoreCase(verb) &&
                                        (
                                                endpoint.startsWith(GpApiRequest.DEPOSITS_ENDPOINT) ||
                                                        endpoint.startsWith(GpApiRequest.SETTLEMENT_DISPUTES_ENDPOINT) ||
                                                        endpoint.startsWith(GpApiRequest.DISPUTES_ENDPOINT)
                                        )                             ||
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

}