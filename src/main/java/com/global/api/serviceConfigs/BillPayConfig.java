package com.global.api.serviceConfigs;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.global.api.ConfiguredServices;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.BillPayProvider;
import com.global.api.utils.StringUtils;

public class BillPayConfig extends Configuration {
    private String apiKey;
    private String merchantName;
    private String username;
    private String password;
    private boolean useBillRecordLookup;

    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public String getMerchantName() {
        return merchantName;
    }
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean getUseBillRecordLookup() {
        return useBillRecordLookup;
    }
    public void setUseBillRecordLookup(boolean useBillRecordLookup) {
        this.useBillRecordLookup = useBillRecordLookup;
    }

    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            if (environment.equals(Environment.TEST)) {
                serviceUrl = ServiceEndpoints.BILLPAY_CERTIFICATION.getValue();
            }
            else {
                serviceUrl = ServiceEndpoints.BILLPAY_PRODUCTION.getValue();
            }
        }

        Credentials credentials = new Credentials();
        credentials.setUserName(username);
        credentials.setPassword(password);
        credentials.setApiKey(apiKey);
        credentials.setMerchantName(merchantName);

        BillPayProvider gateway = new BillPayProvider();
        gateway.setCredentials(credentials);
        gateway.setServiceUrl(serviceUrl + "/BillingDataManagement/v3/BillingDataManagementService.svc/BillingDataManagementService");
        gateway.setTimeout(timeout);
        gateway.setIsBillDataHosted(useBillRecordLookup);

        services.setGatewayConnector(gateway);
        services.setBillingProvider(gateway);
        services.setRecurringConnector(gateway);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if ((StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password)) && StringUtils.isNullOrEmpty(apiKey)) {
            throw new ConfigurationException("Login credentials or an API key is required.");
        }

        if ((!StringUtils.isNullOrEmpty(username) || !StringUtils.isNullOrEmpty(password)) && !StringUtils.isNullOrEmpty(apiKey)) {
            throw new ConfigurationException("Cannot provide both login credentials and an API key.");
        }

        if (StringUtils.isNullOrEmpty(apiKey)) {
            if (StringUtils.isNullOrEmpty(username)) {
                throw new ConfigurationException("Username is missing.");
            }

            if (username.trim().length() > 50) {
                throw new ConfigurationException("Username must be 50 characters or less.");
            }

            if (StringUtils.isNullOrEmpty(password)) {
                throw new ConfigurationException("Password is missing.");
            }

            if (password.trim().length() > 50) {
                throw new ConfigurationException("Password must be 50 characters or less.");
            }
        }

        if (StringUtils.isNullOrEmpty(merchantName)) {
            throw new ConfigurationException("Merchant name is required");
        }

        ArrayList<String> acceptedEndpoints = new ArrayList<>();
        acceptedEndpoints.add(ServiceEndpoints.BILLPAY_CERTIFICATION.getValue());
        acceptedEndpoints.add(ServiceEndpoints.BILLPAY_PRODUCTION.getValue());
        acceptedEndpoints.add(ServiceEndpoints.BILLPAY_TEST.getValue());

        if (!acceptedEndpoints.contains(serviceUrl) && !serviceUrl.contains("localhost")) {
            throw new ConfigurationException("Please use one of the pre-defined BillPay service URLs.");
        }
    }
}
