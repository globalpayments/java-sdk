package com.global.api.entities.gpApi;

import com.global.api.entities.gpApi.entities.GpApiAccount;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class GpApiTokenResponse {
    private String token;
    private String type;
    private String appId;
    private String appName;
    private Date timeCreated;
    private int secondsToExpire;
    private String email;
    private String merchantId;
    private String merchantName;
    private GpApiAccount[] accounts;

    public String getDataAccountName() {
        return getAccountName("DAA_");
    }

    public String getDisputeManagementAccountName() {
        return getAccountName("DIA_");
    }

    public String getTokenizationAccountName() {
        return getAccountName("TKA_");
    }

    public String getTransactionProcessingAccountName() {
        return getAccountName("TRA_");
    }

    private String getAccountName(String accountPrefix) {
        for (GpApiAccount account : accounts) {
            if (account.getId() != null && account.getId().startsWith(accountPrefix))
                return account.getName();
        }
        return null;
    }

    public GpApiTokenResponse(String jsonString) {
        JsonDoc doc = JsonDoc.parse(jsonString);

        mapResponseValues(doc);
    }

    private void mapResponseValues(JsonDoc doc) {
        token = doc.getString("token");
        type = doc.getString("type");
        appId = doc.getString("app_id");
        appName = doc.getString("app_name");
        timeCreated = doc.getDate("time_created", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        secondsToExpire = doc.getInt("seconds_to_expire");
        email = doc.getString("email");

        if (doc.has("scope")) {
            JsonDoc scope = doc.get("scope");
            merchantId = scope.getString("merchant_id");
            merchantName = scope.getString("merchant_name");
            if (scope.has("accounts")) {
                List<GpApiAccount> accountList = new ArrayList<>();
                for (JsonDoc account : scope.getEnumerator("accounts")) {
                    GpApiAccount gpApiAccount = new GpApiAccount();
                    gpApiAccount.setId(account.getString("id"));
                    gpApiAccount.setName(account.getString("name"));

                    accountList.add(gpApiAccount);
                }

                accounts = accountList.toArray(new GpApiAccount[0]);
            }
        }
    }
}