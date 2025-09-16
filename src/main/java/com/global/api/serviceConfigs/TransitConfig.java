package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.TransitConnector;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class TransitConfig extends GatewayConfig {
    private AcceptorConfig acceptorConfig;
    private String developerId;
    private String deviceId;
    private String merchantId;
    private String transactionKey;
    private String username;
    private String password;

    public TransitConfig() {
        super(GatewayProvider.TRANSIT);
    }

    @Override
    public void configureContainer(ConfiguredServices services) throws ConfigurationException {

        if (StringUtils.isNullOrEmpty(getServiceUrl())) {
            if (Environment.TEST.equals(getEnvironment())) {
                setServiceUrl(ServiceEndpoints.TRANSIT_MULTIPASS_TEST.getValue());
            } else {
                setServiceUrl(ServiceEndpoints.TRANSIT_MULTIPASS_PRODUCTION.getValue());
            }
        }

        TransitConnector gateway = new TransitConnector();
        gateway.setAcceptorConfig(getAcceptorConfig());
        gateway.setDeveloperId(developerId);
        gateway.setDeviceId(deviceId);
        gateway.setMerchantId(merchantId);
        gateway.setTransactionKey(transactionKey);
        gateway.setServiceUrl(getServiceUrl());
        gateway.setTimeout(getTimeout());
        gateway.setRequestLogger(getRequestLogger());
        gateway.setWebProxy(getWebProxy());

        services.setGatewayConnector(gateway);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if (getAcceptorConfig() == null) {
            throw new ConfigurationException("You must provide a valid AcceptorConfig.");
        } else {
            getAcceptorConfig().validate();
        }

        if (StringUtils.isNullOrEmpty(deviceId)) {
            throw new ConfigurationException("DeviceId cannot be null.");
        }

        if (StringUtils.isNullOrEmpty(merchantId)) {
            throw new ConfigurationException("MerchantId cannot be null.");
        }

        if (StringUtils.isNullOrEmpty(transactionKey)) {
            throw new ConfigurationException("TransactionKey cannot be null. Use TransitService.generateTransactionKey(...) to generate a transaction key for the config.");
        }
    }
}