package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.GeniusConnector;
import com.global.api.utils.StringUtils;

public class GeniusConfig extends GatewayConfig {
    private String clerkId;
    private String merchantName;
    private String merchantSiteId;
    private String merchantKey;
    private String registerNumber;
    private String dba;
    private String terminalId;

    public String getClerkId() {
        return clerkId;
    }

    public void setClerkId(String clerkId) {
        this.clerkId = clerkId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantSiteId() {
        return merchantSiteId;
    }

    public void setMerchantSiteId(String merchantSiteId) {
        this.merchantSiteId = merchantSiteId;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getDba() {
        return dba;
    }

    public void setDba(String dba) {
        this.dba = dba;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public GeniusConfig() {
        super(GatewayProvider.GENIUS);
    }

    @Override
    public void configureContainer(ConfiguredServices services) {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            if (environment.equals(Environment.TEST)) {
                serviceUrl = ServiceEndpoints.GENIUS_API_TEST.getValue();
            }
            else serviceUrl = ServiceEndpoints.GENIUS_API_PRODUCTION.getValue();
        }

        GeniusConnector gateway = new GeniusConnector();
        gateway.setMerchantName(merchantName);
        gateway.setMerchantSiteId(merchantSiteId);
        gateway.setMerchantKey(merchantKey);
        gateway.setRegisterNumber(registerNumber);
        gateway.setTerminalId(terminalId);
        gateway.setTimeout(timeout);
        gateway.setServiceUrl(serviceUrl);
        gateway.setEnableLogging(isEnableLogging());
        gateway.setWebProxy(webProxy);

        services.setGatewayConnector(gateway);
    }

    @Override
    public void validate() throws ConfigurationException {
        if (StringUtils.isNullOrEmpty(merchantSiteId)) {
            throw new ConfigurationException("MerchantSiteId is required for this configuration.");
        }
        else if (StringUtils.isNullOrEmpty(merchantName)) {
            throw new ConfigurationException("MerchantName is required for this configuration.");
        }
        else if (StringUtils.isNullOrEmpty(merchantKey)) {
            throw new ConfigurationException("MerchantKey is required for this configuration.");
        }
    }
}