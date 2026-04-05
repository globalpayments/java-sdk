package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.logging.IRequestLogger;
import lombok.Getter;
import lombok.Setter;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

@Getter @Setter
public abstract class Configuration {
    protected boolean enableLogging = false;
    protected IRequestLogger requestLogger;
    protected Environment environment = Environment.TEST;
    protected String serviceUrl;
    protected Proxy webProxy;
    protected HashMap<Host, ArrayList<HostError>> simulatedHostErrors;
    protected int timeout = 30000;
    protected boolean validated;
    protected HashMap<String, String> dynamicHeaders;
    protected int connectionTimeout = 5000;


    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public abstract void configureContainer(ConfiguredServices services) throws ConfigurationException;

    public void validate() throws ConfigurationException {
        this.validated = true;
    }
}
