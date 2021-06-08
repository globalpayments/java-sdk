package com.global.api.entities.gpApi;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Accessors(chain = true)
public class GpApiRequest {
    @Getter @Setter private HttpMethod verb = HttpMethod.Get;
    @Getter @Setter private String endpoint;
    @Getter @Setter private String RequestBody = "";
    @Getter private HashMap<String, String> queryStringParams;

    GpApiRequest() {
        queryStringParams = new HashMap<>();
    }

    void addQueryStringParam(String name, String value) {
        if (!StringUtils.isNullOrEmpty(name) && !StringUtils.isNullOrEmpty(value)) {
            queryStringParams.put(name, value);
        }
    }

    public enum HttpMethod {
        Get("GET"),
        Post("POST"),
        Patch("PATCH"),
        Delete("DELETE");

        private final String value;
        HttpMethod(String value) { this.value = value; }

        public String getValue() {
            return value;
        }
    }

}