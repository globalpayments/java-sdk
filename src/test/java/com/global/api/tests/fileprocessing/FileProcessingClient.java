package com.global.api.tests.fileprocessing;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileProcessingClient {

    private static final String GP_API_VERSION = "2021-03-22";

    private static final int BUFFER_SIZE = 4096;

    @Getter
    @Setter
    private String uploadUrl;

    @Getter
    @Setter
    public Map<String, String> headers;

    public FileProcessingClient(String uploadUrl) {
        this.uploadUrl = uploadUrl;
        if (headers == null) {
            headers = new HashMap<>();
        }

        headers.put("Content-Type", "text/csv");
    }

    public boolean uploadFile(String filePath) throws Exception {

        File file = new File(filePath);

        if (!file.exists()) {
            throw new Exception("File not found!");
        } else if (file.length() > 100000000) {
            throw new Exception("Max file size 100MB exceeded!");
        }

        return sendRequest(GpApiRequest.HttpMethod.Put, filePath);
    }

    private boolean sendRequest(GpApiRequest.HttpMethod verb, String filePath) throws Exception {

        OutputStream out = null;
        FileInputStream fis = null;

        try {
            String verbAsString = verb.getValue();

            HttpsURLConnection httpClient = (HttpsURLConnection) new URL((uploadUrl).trim()).openConnection();
            httpClient.setRequestMethod(verbAsString);
            httpClient.setConnectTimeout(30000);
            httpClient.setDoInput(true);
            httpClient.setDoOutput(true);
            setHeaders(httpClient);

            out = httpClient.getOutputStream();

            File fileToUpload = new File(filePath);
            fis = new FileInputStream(fileToUpload);

            MultipartEntity multipartEntity = new MultipartEntity();
            multipartEntity.addPart("file", new InputStreamBody(fis, "filename"));
            multipartEntity.writeTo(out);

            out.flush();

            int statusResponse = httpClient.getResponseCode();
            if (statusResponse != HttpStatus.SC_OK) {
                throw new ApiException("ERROR: status code " + statusResponse);
            }

            return true;
        } catch (Exception exc) {
            throw new GatewayException("Error occurred while sending the request.", exc);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }

        }
    }

    private void setHeaders(HttpsURLConnection conn) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String key = header.getKey();
            String value = header.getValue();
            conn.addRequestProperty(key, value);
        }
    }

}
