package com.global.api.gateways;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.NetworkException;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.IOUtils;
import com.global.api.utils.StringUtils;

import org.apache.http.entity.mime.MultipartEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

abstract class Gateway {
    private String contentType;
    private boolean enableLogging;
    protected HashMap<String, String> headers;
    protected int timeout;
    protected String serviceUrl;

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
        HttpsURLConnection conn;
        try{
            String queryString = buildQueryString(queryStringParams);
            conn = (HttpsURLConnection)new URL((serviceUrl + endpoint + queryString).trim()).openConnection();
            conn.setSSLSocketFactory(new SSLSocketFactoryEx());
            conn.setConnectTimeout(timeout);
            conn.setDoInput(true);
            conn.setRequestMethod(verb);
            conn.addRequestProperty("Content-Type", String.format("%s; charset=UTF-8", contentType));

            for(String key: headers.keySet()) {
                conn.addRequestProperty(key, headers.get(key));
            }

            if(!verb.equals("GET")) {
                byte[] request = data.getBytes();

                conn.setDoOutput(true);
                conn.addRequestProperty("Content-Length", String.valueOf(request.length));

				if (this.enableLogging)
					System.out.println("Request: " + StringUtils.mask(data));
                DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
                requestStream.write(request);
                requestStream.flush();
                requestStream.close();
            }
            else if (this.enableLogging) {
                    System.out.println("Request: " + endpoint);
            }

            InputStream responseStream = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
            String rawResponse = IOUtils.readFully(responseStream);
            responseStream.close();
			if (this.enableLogging) {
                System.out.println("Response: " + rawResponse);
            }

			if (conn.getResponseCode() != 200) {
				String errorMessage = parseErrorMessage(rawResponse);
				throw new GatewayException(errorMessage, Integer.toString(conn.getResponseCode()), errorMessage);
			}

            GatewayResponse response = new GatewayResponse();
            response.setStatusCode(conn.getResponseCode());
            response.setRawResponse(rawResponse);
            return response;
        } catch (GatewayException e) {
        	throw e;
        } catch(Exception exc) {
            throw new NetworkException("Error occurred while communicating with gateway.", exc);
        }
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

            InputStream responseStream = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();

            String rawResponse = IOUtils.readFully(responseStream);
            responseStream.close();
			if (this.enableLogging) {
                System.out.println("Response: " + rawResponse);
            }

			if (conn.getResponseCode() != 200) {
				String error = parseErrorMessage(rawResponse);
				throw new GatewayException(error, Integer.toString(conn.getResponseCode()), error);
			}

            GatewayResponse response = new GatewayResponse();
            response.setStatusCode(conn.getResponseCode());
            response.setRawResponse(rawResponse);
            return response;
        } catch (GatewayException e) {
        	throw e;
        } catch(Exception exc) {
            throw new NetworkException("Error occurred while communicating with gateway.", exc);
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
    
    private String parseErrorMessage(String rawResponse) throws ApiException {
    	ElementTree tree = ElementTree.parse(rawResponse);
        Element root = tree.get("soap:Fault");
        String error = root.getString("soap:Reason");
        return error;
    }
    
}
