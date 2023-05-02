package com.global.api.entities.gpApi;

import com.global.api.entities.gpApi.entities.GpApiAccount;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class GpApiTokenResponse {
    final static String DATA_ACCOUNT_NAME_PREFIX = "DAA_";
    final static String DISPUTE_MANAGEMENT_ACCOUNT_NAME_PREFIX = "DIA_";
    final static String TOKENIZATION_ACCOUNT_NAME_PREFIX = "TKA_";
    final static String TRANSACTION_PROCESSING_ACCOUNT_NAME_PREFIX = "TRA_";
    final static String RIKS_ASSESSMENT_ACCOUNT_NAME_PREFIX = "RAA_";
    final static String MERCHANT_MANAGEMENT_ACCOUNT_NAME_PREFIX = "MMA_";

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
        return getAccountName(DATA_ACCOUNT_NAME_PREFIX);
    }

    public String getDisputeManagementAccountName() {
        return getAccountName(DISPUTE_MANAGEMENT_ACCOUNT_NAME_PREFIX);
    }

    public String getTokenizationAccountName() {
        return getAccountName(TOKENIZATION_ACCOUNT_NAME_PREFIX);
    }

    public String getTransactionProcessingAccountName() {
        return getAccountName(TRANSACTION_PROCESSING_ACCOUNT_NAME_PREFIX);
    }

    public String getRiskAssessmentAccountName() {
        return getAccountName(RIKS_ASSESSMENT_ACCOUNT_NAME_PREFIX);
    }

    public String getMerchantManagementAccountName() {
        return getAccountName(MERCHANT_MANAGEMENT_ACCOUNT_NAME_PREFIX);
    }

    public String getDataAccountID() {
            return getAccountID(DATA_ACCOUNT_NAME_PREFIX);
    }

    public String getDisputeManagementAccountID() {
            return getAccountID(DISPUTE_MANAGEMENT_ACCOUNT_NAME_PREFIX);
    }

    public String getTokenizationAccountID(){
            return getAccountID(TOKENIZATION_ACCOUNT_NAME_PREFIX);
    }

    public String getTransactionProcessingAccountID(){
            return getAccountID(TRANSACTION_PROCESSING_ACCOUNT_NAME_PREFIX);
    }

    public String getRiskAssessmentAccountID(){
            return getAccountID(RIKS_ASSESSMENT_ACCOUNT_NAME_PREFIX);
    }

    public String getMerchantManagementAccountID() {
        return getAccountID(MERCHANT_MANAGEMENT_ACCOUNT_NAME_PREFIX);
    }

    private String getAccountID(String accountPrefix) {
        for (GpApiAccount account : accounts) {
            if (account.getId() != null && account.getId().startsWith(accountPrefix))
                return account.getId();
        }
        return null;
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