package com.global.api.terminals.diamond;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.Region;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DiamondCloudConfig extends ConnectionConfig {
    private String statusUrl;

    @Override
    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl = (environment == Environment.PRODUCTION)
                    ? (Region.EU.toString().equals(this.region != null ? this.region : "")
                    ? ServiceEndpoints.DIAMOND_CLOUD_PROD_EU.getValue()
                    : ServiceEndpoints.DIAMOND_CLOUD_PROD.getValue())
                    : ServiceEndpoints.DIAMOND_CLOUD_TEST.getValue();
        }
        this.region = this.region != null ? this.region : Region.US.toString();
        super.configureContainer(services);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
        if (getConnectionMode() == ConnectionModes.DIAMOND_CLOUD) {
            if (StringUtils.isNullOrEmpty(isvId) || StringUtils.isNullOrEmpty(secretKey)) {
                throw new ConfigurationException("ISV ID and secretKey are required for " + ConnectionModes.DIAMOND_CLOUD);
            }
        }
    }
}
