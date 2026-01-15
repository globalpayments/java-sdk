package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.logging.IRequestLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

@Accessors(chain = true)
@Getter @Setter
public abstract class Configuration {
    protected int connectionTimeout = 5000;
    protected HashMap<String, String> dynamicHeaders;
    protected boolean enableLogging = false;
    protected Environment environment = Environment.TEST;
    protected IRequestLogger requestLogger;
    protected String serviceUrl;
    protected HashMap<Host, ArrayList<HostError>> simulatedHostErrors;
    protected int timeout = 30000;
    protected boolean validated;
    protected Proxy webProxy;

    public abstract void configureContainer(ConfiguredServices services) throws ConfigurationException;

    public void validate() throws ConfigurationException {
        this.validated = true;
    }
}
