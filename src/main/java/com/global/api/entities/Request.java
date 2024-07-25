package com.global.api.entities;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
public class Request {

    @Getter @Setter private HttpMethod verb = HttpMethod.Get;
    @Getter @Setter private String endpoint;
    @Getter @Setter private String RequestBody = "";
    @Getter private HashMap<String, String> queryStringParams;
    @Getter @Setter
    public Map<String, String> maskedData = new HashMap<>();

    public Request() {
        queryStringParams = new HashMap<>();
    }

    public enum HttpMethod {
        Get("GET"),
        Post("POST"),
        Patch("PATCH"),
        Delete("DELETE"),
        Put("PUT");

        private final String value;
        HttpMethod(String value) { this.value = value; }

        public String getValue() {
            return value;
        }
    }

    public void addQueryStringParam(String name, String value) {
        if (!StringUtils.isNullOrEmpty(name) && !StringUtils.isNullOrEmpty(value)) {
            queryStringParams.put(name, value);
        }
    }
}