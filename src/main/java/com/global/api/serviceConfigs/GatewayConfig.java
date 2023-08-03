package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class GatewayConfig extends Configuration {
    private GatewayProvider gatewayProvider;
    /// Determines whether to use the data reporting service or not
    private boolean useDataReportingService = false;
    // Client Id for Global Payments Data Services
    private String dataClientId;
    // Client Secret for Global Payments Data Services
    private String dataClientSecret;
    // The UserId for the Global Payment Data Services
    private String dataClientUserId;
    /// The Url of the Global Data Service
    private String dataClientServiceUrl;

    public GatewayConfig(GatewayProvider provider) {
        this.gatewayProvider = provider;
    }

    @Override
    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        throw new UnsupportedOperationException();
        // TODO: Implement DataServicesConnector
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
    }
}
