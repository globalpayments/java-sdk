package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.PayrollConnector;
import com.global.api.utils.StringUtils;

public class PayrollConfig extends Configuration {
    private String username;
    private String password;
    private String apiKey;

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

    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void configureContainer(ConfiguredServices services) {
        PayrollConnector payrollConnector = new PayrollConnector();
        payrollConnector.setUsername(username);
        payrollConnector.setPassword(password);
        payrollConnector.setApiKey(apiKey);
        payrollConnector.setServiceUrl(serviceUrl);
        payrollConnector.setTimeout(timeout);

        services.setPayrollConnector(payrollConnector);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if(StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password) || StringUtils.isNullOrEmpty(apiKey))
            throw new ConfigurationException("Username, Password, and ApiKey cannot be null.");
    }
}
