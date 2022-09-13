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
public abstract class Configuration {
    @Getter @Setter protected boolean enableLogging = false;
    @Getter @Setter protected IRequestLogger requestLogger;
    @Getter @Setter protected Environment environment = Environment.TEST;
    @Getter @Setter protected String serviceUrl;
    @Getter @Setter protected Proxy webProxy;
    @Getter @Setter protected HashMap<Host, ArrayList<HostError>> simulatedHostErrors;
    protected int timeout = 30000;
    @Getter @Setter protected boolean validated;
    @Getter @Setter protected HashMap<String, String> dynamicHeaders;

    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public abstract void configureContainer(ConfiguredServices services) throws ConfigurationException;

    public void validate() throws ConfigurationException {
        this.validated = true;
    }
}
