package com.global.api.utils;

import com.global.api.entities.exceptions.ApiException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultipartForm {
    protected MultipartEntity content;

    public MultipartEntity getContent() {
        return content;
    }

    public MultipartForm() throws ApiException {
        content = new MultipartEntity(HttpMultipartMode.STRICT);
        add("json", "1");
    }

    public MultipartForm set(String key, String value) throws ApiException {
        return set(key, value, false);
    }
    public MultipartForm set(String key, String value, boolean force) throws ApiException {
        if(!StringUtils.isNullOrEmpty(value) || force)
            add(key, value);
        return this;
    }

    public MultipartForm set(String key, Integer value) throws ApiException {
        return set(key, value, false);
    }
    public MultipartForm set(String key, Integer value, boolean force) throws ApiException {
        if(value != null || force)
            add(key, value.toString());
        return this;
    }

    public MultipartForm set(String key, Date value) throws ApiException {
        return set(key, value, false);
    }
    public MultipartForm set(String key, Date value, boolean force) throws ApiException {
        if(value != null || force) {
            SimpleDateFormat format = new SimpleDateFormat("hh:MM:ss");
            add(key, format.format(value));
        }
        return this;
    }

    private void add(String key, String value) throws ApiException {
        try {
            content.addPart(key, new StringBody(value));
        }
        catch(UnsupportedEncodingException exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }
}
