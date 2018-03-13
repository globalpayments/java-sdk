package com.global.api;

import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.services.TableService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.utils.StringUtils;

public class ServicesConfig {
    // portico
    private int siteId;
    private int licenseId;
    private int deviceId;
    private String username;
    private String password;
    private String developerId;
    private String versionNumber;
    private String secretApiKey;

    // realex
    private String accountId;
    private String merchantId;
    private String rebatePassword;
    private String refundPassword;
    private String sharedSecret;
    private String channel;
    private HostedPaymentConfig hostedPaymentConfig;

    // device specific
    private ConnectionConfig deviceConnectionConfig;

    // table service
    private TableServiceProviders tableServiceProvider;

    // common
    private String serviceUrl;
    private int timeout;

    public int getSiteId() {
        return siteId;
    }
    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
    public int getLicenseId() {
        return licenseId;
    }
    public void setLicenseId(int licenseId) {
        this.licenseId = licenseId;
    }
    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getDeveloperId() {
        return developerId;
    }
    public void setDeveloperId(String developerId) {
        this.developerId = developerId;
    }
    public String getVersionNumber() {
        return versionNumber;
    }
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
    public String getSecretApiKey() {
        return secretApiKey;
    }
    public void setSecretApiKey(String secretApiKey) {
        this.secretApiKey = secretApiKey;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getMerchantId() {
        return merchantId;
    }
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    public String getRebatePassword() {
        return rebatePassword;
    }
    public void setRebatePassword(String rebatePassword) {
        this.rebatePassword = rebatePassword;
    }
    public String getRefundPassword() {
        return refundPassword;
    }
    public void setRefundPassword(String refundPassword) {
        this.refundPassword = refundPassword;
    }
    public String getSharedSecret() {
        return sharedSecret;
    }
    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public String getServiceUrl() {
        return serviceUrl;
    }
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public HostedPaymentConfig getHostedPaymentConfig() {
        return hostedPaymentConfig;
    }
    public void setHostedPaymentConfig(HostedPaymentConfig hostedPaymentConfig) {
        this.hostedPaymentConfig = hostedPaymentConfig;
    }
    public ConnectionConfig getDeviceConnectionConfig() {
        return deviceConnectionConfig;
    }
    public void setDeviceConnectionConfig(ConnectionConfig deviceConnectionConfig) {
        this.deviceConnectionConfig = deviceConnectionConfig;
    }
    public TableServiceProviders getTableServiceProvider() {
        return tableServiceProvider;
    }
    public void setTableServiceProvider(TableServiceProviders tableServiceProvider) {
        this.tableServiceProvider = tableServiceProvider;
    }

    public ServicesConfig() {
        timeout = 65000;
    }

    protected void validate() throws ConfigurationException {
        // portico api key
        if(!StringUtils.isNullOrEmpty(secretApiKey)) {
            if(siteId != 0 || licenseId != 0 || deviceId != 0 || !StringUtils.isNullOrEmpty(username) || !StringUtils.isNullOrEmpty(password))
                throw new ConfigurationException("Configuration contains both secret api key and legacy credentials. These are mutually exclusive.");
        }

        // portico legacy
        if (siteId != 0 || licenseId != 0 || deviceId != 0 || !StringUtils.isNullOrEmpty(username) || !StringUtils.isNullOrEmpty(password)) {
            if(siteId == 0 || licenseId == 0 || deviceId == 0 || StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password))
                throw new ConfigurationException("Site, License, Device, Username and Password should all have a values for this configuration.");
        }

        // realex
        if (!StringUtils.isNullOrEmpty(sharedSecret) || !StringUtils.isNullOrEmpty(merchantId)) {
            if (StringUtils.isNullOrEmpty(sharedSecret) || StringUtils.isNullOrEmpty(merchantId))
                throw new ConfigurationException("Shared Secret and MerchantId should both have a values for this configuration.");
        }

        // service url
        if (StringUtils.isNullOrEmpty(getServiceUrl()) && (!StringUtils.isNullOrEmpty(secretApiKey) || !StringUtils.isNullOrEmpty(sharedSecret))) {
            throw new ConfigurationException("Service URL could not be determined from the credentials provided. Please specify an endpoint.");
        }
    }
}
