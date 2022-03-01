package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.logging.IRequestLogger;
import com.global.api.utils.IOUtils;
import com.global.api.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.entity.mime.MultipartEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class Gateway {
    private String contentType;
    private boolean enableLogging;
    private IRequestLogger requestLogger;
    private StringBuilder logEntry = new StringBuilder();
    private final String lSChar = System.getProperty("line.separator");
    protected HashMap<String, String> headers;
    protected HashMap<String, String> dynamicHeaders;
    protected int timeout;
    protected String serviceUrl;
    protected Proxy proxy;

    // ----------------------------------------------------------------------
    // TODO: Remove if it is not more useful
    // ----------------------------------------------------------------------
    private static JsonParser parser = new JsonParser();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String toPrettyJson(String jsonString) {
        JsonObject json = parser.parse(jsonString).getAsJsonObject();
        return gson.toJson(json);
    }
    // ----------------------------------------------------------------------
    public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}
    public void setRequestLogger(IRequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }
	public HashMap<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public String getServiceUrl() {
        return serviceUrl;
    }
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
    public Proxy getProxy() {
        return this.proxy;
    }
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
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
            if (proxy != null) {
                conn = (HttpsURLConnection) new URL((serviceUrl + endpoint + queryString).trim()).openConnection(proxy);
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
                logEntry.append("================================================================================").append(lSChar);
                logEntry.append("Endpoint:       ").append(endpoint).append(lSChar);
                logEntry.append("Verb:           ").append(verb).append(lSChar);
                logEntry.append("Headers:        ").append(conn.getRequestProperties()).append(lSChar);
                logEntry.append("Proxy:          ").append((proxy != null) ? proxy.toString() : "none").append(lSChar);
            }

            if (!verb.equals("GET")) {
                byte[] request = data.getBytes();

                conn.setDoOutput(true);
                conn.addRequestProperty("Content-Length", String.valueOf(request.length));

                if (this.enableLogging || this.requestLogger != null) {
                    if (acceptJson()) {
                        if (!StringUtils.isNullOrEmpty(data)) {
                            logEntry.append("Request Body: ").append(lSChar).append(toPrettyJson(data)).append(lSChar).append(lSChar);
                        }
                    } else {
                        logEntry.append("Request Body: ").append(StringUtils.mask(data)).append(lSChar).append(lSChar);
                    }

                    outputLogging(true);
                }

                DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
                requestStream.write(request);
                requestStream.flush();
                requestStream.close();
            }
            else if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("Request Params: ").append(queryString).append(lSChar);
            }

            InputStream responseStream = conn.getInputStream();

            String rawResponse = getRawResponse(responseStream);

            responseStream.close();
            if (this.enableLogging || this.requestLogger != null) {
                if (acceptJson()) {
                    logEntry.append("--------------------------------------------------------------------------------").append(lSChar);
                    logEntry.append("Response Code: ").append(conn.getResponseCode()).append(" ").append(conn.getResponseMessage()).append(lSChar);
                    logEntry.append("Response: ").append(toPrettyJson(rawResponse)).append(lSChar);
                    logEntry.append("================================================================================").append(lSChar);
                } else {
                    logEntry.append(rawResponse).append(lSChar);
                }

                outputLogging(false);
            }

            GatewayResponse response = new GatewayResponse();
            response.setStatusCode(conn.getResponseCode());
            response.setRawResponse(rawResponse);
            return response;
        }
        catch(Exception exc) {
            if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("--------------------------------------------------------------------------------").append(lSChar);
                logEntry.append(exc.getMessage()).append(lSChar);
                logEntry.append("================================================================================").append(lSChar);

                outputLogging(false);
            }

            try {
                assert conn != null;
                throw new GatewayException("Error occurred while communicating with gateway.", exc, String.valueOf(conn.getResponseCode()), getRawResponse(conn.getErrorStream()));
            } catch (IOException e) {   // Legacy GatewayException
                throw new GatewayException("Error occurred while communicating with gateway.", exc);
            }
        }
    }

    public String getRawResponse(InputStream responseStream) throws IOException {
        String rawResponse;
        if (acceptGzipEncoding()) {
            // Decompress GZIP response
            GZIPInputStream gzis = new GZIPInputStream(responseStream);
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader in = new BufferedReader(reader);

            StringBuilder decompressedResponse = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                decompressedResponse.append(line);
            }
            rawResponse = decompressedResponse.toString();
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

            OutputStream out = conn.getOutputStream();
			if (this.enableLogging || this.requestLogger != null) {
                logEntry.append("================================================================================").append(lSChar);
                logEntry.append("Request: ").append(content).append(lSChar);

                outputLogging(true);
            }
            content.writeTo(out);
            out.flush();
            out.close();

            InputStream responseStream = conn.getInputStream();
            String rawResponse = IOUtils.readFully(responseStream);
            responseStream.close();
            if (this.enableLogging || this.requestLogger != null) {
                logEntry.append(content).append(lSChar);

                outputLogging(false);
            }

            GatewayResponse response = new GatewayResponse();
            response.setStatusCode(conn.getResponseCode());
            response.setRawResponse(rawResponse);
            return response;
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
                serviceUrl.endsWith("globalpay.com/ucp") &&
                        (
                                "GET".equalsIgnoreCase(verb) &&
                                        (
                                                endpoint.startsWith("/settlement/deposits") ||
                                                        endpoint.startsWith("/settlement/disputes") ||
                                                        endpoint.startsWith("/disputes")
                                        )                             ||
                                        "POST".equalsIgnoreCase(verb) &&
                                                (
                                                        endpoint.startsWith("/disputes") &&
                                                                endpoint.endsWith("/acceptance")
                                                )
                        );
    }

    private void outputLogging(boolean isRequest) {
        if (this.enableLogging) {
            System.out.print(logEntry);
        }

        if (this.requestLogger != null) {
            try {
                if (isRequest) {
                    this.requestLogger.RequestSent(logEntry.toString());
                } else {
                    this.requestLogger.ResponseReceived(logEntry.toString());
                }
            } catch (IOException e) {
                //eat the exception
            }
        }

        logEntry.delete(0, logEntry.length());
    }

}