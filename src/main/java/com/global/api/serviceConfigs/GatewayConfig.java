package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.logging.IRequestLogger;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class GatewayConfig extends Configuration {
    // portico & gp-ecom
    private boolean enableLogging;
    protected IRequestLogger requestLogger;

	// portico
    private int siteId;
    private int licenseId;
    private int deviceId;
    private String username;
    private String password;
    private String developerId;
    private String versionNumber;
    private String secretApiKey;

    // gp-ecom
    private String accountId;
    private String merchantId;
    private String rebatePassword;
    private String refundPassword;
    private String sharedSecret;
    private String channel;
    private HostedPaymentConfig hostedPaymentConfig;

    // 3DS
    private String challengeNotificationUrl;
    private String merchantContactUrl;
    private String methodNotificationUrl;
    private Secure3dVersion secure3dVersion;

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
	public boolean isEnableLogging() {
		return enableLogging;
	}
	public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
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
    public HostedPaymentConfig getHostedPaymentConfig() {
        return hostedPaymentConfig;
    }
    public void setHostedPaymentConfig(HostedPaymentConfig hostedPaymentConfig) {
        this.hostedPaymentConfig = hostedPaymentConfig;
    }
    public void setRequestLogger(IRequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }
    public IRequestLogger getRequestLogger() { return requestLogger; }

    // 3DS
    public String getChallengeNotificationUrl() {
        return challengeNotificationUrl;
    }
    public void setChallengeNotificationUrl(String challengeNotificationUrl) {
        this.challengeNotificationUrl = challengeNotificationUrl;
    }
    public String getMerchantContactUrl() {
        return merchantContactUrl;
    }
    public void setMerchantContactUrl(String merchantContactUrl) {
        this.merchantContactUrl = merchantContactUrl;
    }
    public String getMethodNotificationUrl() {
        return methodNotificationUrl;
    }
    public void setMethodNotificationUrl(String methodNotificationUrl) {
        this.methodNotificationUrl = methodNotificationUrl;
    }
    public Secure3dVersion getSecure3dVersion() {
        return secure3dVersion;
    }
    public void setSecure3dVersion(Secure3dVersion secure3dVersion) {
        this.secure3dVersion = secure3dVersion;
    }

    public void configureContainer(ConfiguredServices services) {
        if(!StringUtils.isNullOrEmpty(merchantId)) {
            if(StringUtils.isNullOrEmpty(serviceUrl)) {
                if(environment.equals(Environment.TEST)) {
                    serviceUrl = ServiceEndpoints.GLOBAL_ECOM_TEST.getValue();
                }
                else serviceUrl = ServiceEndpoints.GLOBAL_ECOM_PRODUCTION.getValue();
            }

            RealexConnector gateway = new RealexConnector();
            gateway.setMerchantId(merchantId);
            gateway.setAccountId(accountId);
            gateway.setSharedSecret(sharedSecret);
            gateway.setChannel(channel);
            gateway.setRebatePassword(rebatePassword);
            gateway.setRefundPassword(refundPassword);
            gateway.setTimeout(timeout);
            gateway.setServiceUrl(serviceUrl);
            gateway.setProxy(proxy);
            gateway.setHostedPaymentConfig(hostedPaymentConfig);
            gateway.setEnableLogging(enableLogging);

            services.setGatewayConnector(gateway);
            services.setRecurringConnector(gateway);

            // set default
            if(secure3dVersion == null) {
                secure3dVersion = Secure3dVersion.ONE;
            }

            // secure 3d v1
            if(secure3dVersion.equals(Secure3dVersion.ONE) || secure3dVersion.equals(Secure3dVersion.ANY)) {
                services.setSecure3dProvider(Secure3dVersion.ONE, gateway);
            }

            // secure 3d v2
            if(secure3dVersion.equals(Secure3dVersion.TWO) || secure3dVersion.equals(Secure3dVersion.ANY)) {
                Gp3DSProvider secure3d2 = new Gp3DSProvider();
                secure3d2.setMerchantId(merchantId);
                secure3d2.setAccountId(accountId);
                secure3d2.setSharedSecret(sharedSecret);
                secure3d2.setServiceUrl(environment.equals(Environment.TEST) ? ServiceEndpoints.THREE_DS_AUTH_TEST.getValue() : ServiceEndpoints.THREE_DS_AUTH_PRODUCTION.getValue());
                secure3d2.setMerchantContactUrl(merchantContactUrl);
                secure3d2.setMethodNotificationUrl(methodNotificationUrl);
                secure3d2.setChallengeNotificationUrl(challengeNotificationUrl);
                secure3d2.setEnableLogging(enableLogging);
                secure3d2.setProxy(proxy);

                services.setSecure3dProvider(Secure3dVersion.TWO, secure3d2);
            }
        }
        else {
            if(StringUtils.isNullOrEmpty(serviceUrl)) {
                if(environment.equals(Environment.TEST)) {
                    serviceUrl = ServiceEndpoints.PORTICO_TEST.getValue();
                }
                else serviceUrl = ServiceEndpoints.PORTICO_PRODUCTION.getValue();
            }

            PorticoConnector gateway = new PorticoConnector();
            gateway.setSiteId(siteId);
            gateway.setLicenseId(licenseId);
            gateway.setDeviceId(deviceId);
            gateway.setUsername(username);
            gateway.setPassword(password);
            gateway.setSecretApiKey(secretApiKey);
            gateway.setDeveloperId(developerId);
            gateway.setVersionNumber(versionNumber);
            gateway.setTimeout(timeout);
            gateway.setServiceUrl(serviceUrl + "/Hps.Exchange.PosGateway/PosGatewayService.asmx");
            gateway.setEnableLogging(enableLogging);
            gateway.setRequestLogger(requestLogger);
            services.setGatewayConnector(gateway);

            PayPlanConnector payplan = new PayPlanConnector();
            payplan.setEnableLogging(enableLogging);
            payplan.setSecretApiKey(secretApiKey);
            payplan.setTimeout(timeout);
            String payplanEndpoint = environment == Environment.TEST || serviceUrl.contains("cert.")
                    ? "/Portico.PayPlan.v2/"
                    : "/PayPlan.v2/";
            payplan.setServiceUrl(serviceUrl + payplanEndpoint);

            services.setRecurringConnector(payplan);
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

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

        // secure 3d
        if(secure3dVersion != null) {
            // ensure we have the fields we need
            if(secure3dVersion.equals(Secure3dVersion.TWO) || secure3dVersion.equals(Secure3dVersion.ANY)) {
                if(StringUtils.isNullOrEmpty(challengeNotificationUrl)) {
                    throw new ConfigurationException("The challenge notification URL is required for 3DS v2 processing.");
                }

                if(StringUtils.isNullOrEmpty(methodNotificationUrl)) {
                    throw new ConfigurationException("The method notification URL is required for 3DS v2 processing.");
                }
            }
        }
    }
}
