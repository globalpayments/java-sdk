package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ConfigurationException;

public abstract class Configuration {
    protected boolean enableLogging = false;
    protected Environment environment = Environment.TEST;
    protected boolean forceGatewayTimeout = false;
    protected String serviceUrl;
    protected int timeout = 30000;
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

    public boolean isEnableLogging() {
        return enableLogging;
    }
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public boolean isForceGatewayTimeout() {
        return forceGatewayTimeout;
    }
    public void setForceGatewayTimeout(boolean forceGatewayTimeout) {
        this.forceGatewayTimeout = forceGatewayTimeout;
    }

    public Environment getEnvironment() {
        return environment;
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isValidated() {
        return validated;
    }

    public abstract void configureContainer(ConfiguredServices services) throws ConfigurationException;

    public void validate() throws ConfigurationException {
        this.validated = true;
    }
}
