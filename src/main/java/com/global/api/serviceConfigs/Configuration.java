package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.exceptions.ConfigurationException;

public abstract class Configuration {
    protected int timeout = 65000;
    protected String serviceUrl;
    protected boolean validated;

    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public boolean isValidated() {
        return validated;
    }

    public abstract void configureContainer(ConfiguredServices services) throws ConfigurationException;

    public void validate() throws ConfigurationException {
        this.validated = true;
    }
}
