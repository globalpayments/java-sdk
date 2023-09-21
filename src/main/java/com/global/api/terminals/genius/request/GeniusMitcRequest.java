package com.global.api.terminals.genius.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Accessors(chain = true)
@Setter
@Getter
public class GeniusMitcRequest {
    private HttpMethod verb = HttpMethod.GET;
    private String endpoint;
    private String requestBody = "";
    private HashMap<String, String> queryStringParams;

    public GeniusMitcRequest() {
        queryStringParams = new HashMap<>();
    }

    public enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT");

        private final String value;

        HttpMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
