package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.entities.enums.Language;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class GpApiConfig extends GatewayConfig {
    // GP-API app Id
    @Accessors(chain = true)
    private String appId;           // For example: OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj

    // GP-API app Key
    @Accessors(chain = true)
    private String appKey;          // For example: abcDefgHijkLmn12

    // The time left in seconds before the token expires
    @Accessors(chain = true)
    private int secondsToExpire;

    // The time interval set for when the token will expire
    @Accessors(chain = true)
    private IntervalToExpire intervalToExpire;

    // GP-API channel
    private String channel = Channel.CardNotPresent.getValue();

    // GP-API language
    @Accessors(chain = true)
    private Language language = Language.English;

    // GP-API Country. Two letter ISO 3166 country
    @Accessors(chain = true)
    private String country = "US";

    // GP-API Access token information
    private AccessTokenInfo accessTokenInfo;

    public void configureContainer(ConfiguredServices services) {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl =
                    environment.equals(Environment.TEST) ?
                            ServiceEndpoints.GP_API_TEST.getValue() :
                            ServiceEndpoints.GP_API_PRODUCTION.getValue();
        }

        GpApiConnector gpApiConnector = new GpApiConnector(this);

        gpApiConnector.setServiceUrl(serviceUrl);

        gpApiConnector.setEnableLogging(this.isEnableLogging());

        services.setGatewayConnector(gpApiConnector);
        services.setReportingService(gpApiConnector);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if ( accessTokenInfo == null && (appId == null || appKey == null))
            throw new ConfigurationException("accessTokenInfo or appId and appKey cannot be null.");
    }

}