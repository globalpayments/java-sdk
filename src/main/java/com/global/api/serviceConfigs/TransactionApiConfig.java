package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.transactionApi.entities.TransactionApiRegion;
import com.global.api.gateways.TransactionApiConnector;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class TransactionApiConfig extends GatewayConfig {

    @Accessors(chain = true)
    private String appKey;

    @Accessors(chain = true)
    private String appSecret;

    @Accessors(chain = true)
    private String accountCredential;

    @Accessors(chain = true)
    private TransactionApiRegion region;

    public TransactionApiConfig() { super(GatewayProvider.TransactionApi); }

    @Override
    public void configureContainer(ConfiguredServices services) {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl =
                    environment.equals(Environment.TEST) ?
                            ServiceEndpoints.TRANSACTION_API_TEST.getValue() :
                            ServiceEndpoints.TRANSACTION_API_PRODUCTION.getValue();
        }

        TransactionApiConnector transactionApiConnector = new TransactionApiConnector(this);
        transactionApiConnector.setServiceUrl(serviceUrl);
        transactionApiConnector.setEnableLogging(this.isEnableLogging());
        services.setGatewayConnector(transactionApiConnector);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
        if (accountCredential == null || appSecret == null || appKey == null)
            throw new ConfigurationException("accountCredential or appSecret and appKey cannot be null.");
        if (region == null)
            throw new ConfigurationException("region cannot be null.");
    }
}
