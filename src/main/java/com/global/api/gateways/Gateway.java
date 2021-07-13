package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
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
    protected HashMap<String, String> headers;
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
        headers = new HashMap<String, String>();
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

            if (this.enableLogging) {
                System.out.println("================================================================================");
                System.out.println("Endpoint:       " + endpoint);
                System.out.println("Verb:           " + verb);
                System.out.println("Headers:        " + conn.getRequestProperties());
                System.out.println("Proxy:          " + ((proxy != null) ? proxy.toString() : "none"));
            }

            if (!verb.equals("GET")) {
                byte[] request = data.getBytes();

                conn.setDoOutput(true);
                conn.addRequestProperty("Content-Length", String.valueOf(request.length));

                if (this.enableLogging) {
                    if (acceptJson()) {
                        if (!StringUtils.isNullOrEmpty(data)) {
                            System.out.println("Request Body: " + System.getProperty("line.separator") + toPrettyJson(data));
                        }
                    } else {
                        System.out.println("Request Body: " + StringUtils.mask(data));
                    }
                }
                DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
                requestStream.write(request);
                requestStream.flush();
                requestStream.close();
            }
            else if (this.enableLogging) {
                System.out.println("Request Params: " + queryString);
            }

            InputStream responseStream = conn.getInputStream();

            String rawResponse = getRawResponse(verb, endpoint, responseStream);

            responseStream.close();
            if (this.enableLogging) {
                if (acceptJson()) {
                    System.out.println("--------------------------------------------------------------------------------");
                    System.out.println("Response Code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
                    System.out.println("Response: " + System.getProperty("line.separator") + toPrettyJson(rawResponse));
                    System.out.println("================================================================================" + System.getProperty("line.separator"));
                } else {
                    System.out.println("Response: " + rawResponse);
                }
            }

            GatewayResponse response = new GatewayResponse();
            response.setStatusCode(conn.getResponseCode());
            response.setRawResponse(rawResponse);
            return response;
        }
        catch(Exception exc) {
            if (this.enableLogging) {
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("Response: " + System.getProperty("line.separator") + exc.getMessage());
                System.out.println("================================================================================" + System.getProperty("line.separator"));
            }

            try {
                throw new GatewayException("Error occurred while communicating with gateway.", exc, String.valueOf(conn.getResponseCode()), getRawResponse(verb, endpoint, conn.getErrorStream()));
            } catch (IOException e) {   // Legacy GatewayException
                throw new GatewayException("Error occurred while communicating with gateway.", exc);
            }
        }
    }

    public String getRawResponse(String verb, String endpoint, InputStream responseStream) throws IOException {
        String rawResponse;
        if (acceptGzipEncoding(verb, endpoint)) {
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
			if (this.enableLogging) {
                System.out.println("Request: " + content);
            }
            content.writeTo(out);
            out.flush();
            out.close();

            InputStream responseStream = conn.getInputStream();
            String rawResponse = IOUtils.readFully(responseStream);
            responseStream.close();
			if (this.enableLogging) {
                System.out.println("Response: " + rawResponse);
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

    private boolean acceptGzipEncoding(String verb, String endpoint) {
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

}