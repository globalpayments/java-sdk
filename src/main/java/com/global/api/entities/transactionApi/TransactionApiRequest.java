package com.global.api.entities.transactionApi;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Accessors(chain = true)
@Setter
@Getter
public class TransactionApiRequest {

    private HttpMethod verb = HttpMethod.GET;
    private String endpoint;
    private String requestBody = "";
    private HashMap<String, String> queryStringParams;

    TransactionApiRequest() {
        queryStringParams = new HashMap<>();
    }

    void addQueryStringParam(String name, String value) {
        if (!StringUtils.isNullOrEmpty(name) && !StringUtils.isNullOrEmpty(value)) {
            queryStringParams.put(name, value);
        }
    }

    public enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PATCH("PATCH"),
        PUT("PUT"),
        DELETE("DELETE");

        private final String value;

        HttpMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
