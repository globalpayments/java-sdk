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

    public static final String ACCESS_TOKEN_ENDPOINT = "/accesstoken";

    public static final String TRANSACTION_ENDPOINT = "/transactions";

    public static final String PAYMENT_METHODS_ENDPOINT = "/payment-methods";

    public static final String VERIFICATIONS_ENDPOINT = "/verifications";

    public static final String DEPOSITS_ENDPOINT = "/settlement/deposits";

    public static final String DISPUTES_ENDPOINT = "/disputes";

    public static final String SETTLEMENT_DISPUTES_ENDPOINT = "/settlement/disputes";

    public static final String SETTLEMENT_TRANSACTIONS_ENDPOINT = "/settlement/transactions";

    public static final String AUTHENTICATIONS_ENDPOINT = "/authentications";

    public static final String BATCHES_ENDPOINT = "/batches";

    public static final String ACTIONS_ENDPOINT = "/actions";

    public static final String MERCHANT_MANAGEMENT_ENDPOINT = "/merchants";

    public static final String DCC_ENDPOINT = "/currency-conversions";

    public static final String PAYLINK_ENDPOINT = "/links";

    public static final String RISK_ASSESSMENTS = "/risk-assessments";

    public static final String ACCOUNTS_ENDPOINT = "/accounts";

    public static final String TRANSFER_ENDPOINT = "/transfers";

}