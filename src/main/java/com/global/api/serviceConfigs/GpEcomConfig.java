package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class GpEcomConfig extends GatewayConfig {
    // Account's account ID
    private String accountId;
    // Account's merchant ID
    private String merchantId;
    // Account's rebate password
    private String rebatePassword;
    // Account's refund password
    private String refundPassword;
    // Account's shared secret
    private String sharedSecret;
    // Channel for an integration's transactions (e.g. "internet")
    private String channel;
    // Hosted Payment Page (HPP) configuration
    private HostedPaymentConfig hostedPaymentConfig;

    // 3DS
    private String challengeNotificationUrl;
    private String merchantContactUrl;
    private String methodNotificationUrl;
    private Secure3dVersion secure3dVersion;

    // Open Banking Service
    private ShaHashType shaHashType = ShaHashType.SHA1;
    private boolean enableBankPayment = false;

    public GpEcomConfig() {
        super(GatewayProvider.GP_ECOM);
    }

    public void configureContainer(ConfiguredServices services) {

        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl = environment.equals(Environment.PRODUCTION) ?
                    ServiceEndpoints.GLOBAL_ECOM_PRODUCTION.getValue() :
                    ServiceEndpoints.GLOBAL_ECOM_TEST.getValue();
        }

        GpEcomConnector gateway =
                new GpEcomConnector()
                        .setAccountId(accountId)
                        .setChannel(channel)
                        .setMerchantId(merchantId)
                        .setRebatePassword(rebatePassword)
                        .setRefundPassword(refundPassword)
                        .setSharedSecret(sharedSecret)
                        .setShaHashType(shaHashType)
                        .setHostedPaymentConfig(hostedPaymentConfig);

        gateway
                .setTimeout(timeout)
                .setServiceUrl(serviceUrl)
                .setEnableLogging(enableLogging)
                .setRequestLogger(requestLogger)
                .setWebProxy(webProxy);

        services.setGatewayConnector(gateway);
        services.setRecurringConnector(gateway);

        // set reporting gateway
        if (!isUseDataReportingService()) {
            services.setReportingService(gateway);
        }

        // set default
        if (secure3dVersion == null) {
            secure3dVersion = Secure3dVersion.TWO;
        }

        // secure 3d v2
        if (secure3dVersion.equals(Secure3dVersion.TWO) || secure3dVersion.equals(Secure3dVersion.ANY)) {
            Gp3DSProvider secure3d2 =
                    new Gp3DSProvider()
                            .setMerchantId(merchantId)
                            .setAccountId(accountId)
                            .setSharedSecret(sharedSecret)
                            .setMerchantContactUrl(merchantContactUrl)
                            .setMethodNotificationUrl(methodNotificationUrl)
                            .setChallengeNotificationUrl(challengeNotificationUrl);

            secure3d2
                    .setServiceUrl(environment.equals(Environment.PRODUCTION) ? ServiceEndpoints.THREE_DS_AUTH_PRODUCTION.getValue() : ServiceEndpoints.THREE_DS_AUTH_TEST.getValue())
                    .setEnableLogging(enableLogging)
                    .setWebProxy(webProxy);

            services.setSecure3dProvider(Secure3dVersion.TWO, secure3d2);
        }

        if (enableBankPayment) {
            OpenBankingProvider openBankingProvider =
                    new OpenBankingProvider()
                            .setMerchantId(merchantId)
                            .setAccountId(accountId)
                            .setSharedSecret(sharedSecret)
                            .setShaHashType(shaHashType != null ? shaHashType : ShaHashType.SHA1);

            openBankingProvider.setServiceUrl(environment.equals(Environment.PRODUCTION) ? ServiceEndpoints.OPEN_BANKING_PRODUCTION.getValue() : ServiceEndpoints.OPEN_BANKING_TEST.getValue());
            openBankingProvider.setTimeout(timeout);
            openBankingProvider.setEnableLogging(enableLogging);
            openBankingProvider.setRequestLogger(requestLogger);
            openBankingProvider.setWebProxy(webProxy);

            services.setOpenBankingProvider(openBankingProvider);
        }

    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if (StringUtils.isNullOrEmpty(merchantId)) {
            throw new ConfigurationException("merchantId is required for this configuration.");
        }
        else if (StringUtils.isNullOrEmpty(sharedSecret)) {
            throw new ConfigurationException("sharedSecret is required for this configuration.");
        }

        // secure 3d
        if (secure3dVersion != null) {
            // ensure we have the fields we need
            if (secure3dVersion.equals(Secure3dVersion.TWO) || secure3dVersion.equals(Secure3dVersion.ANY)) {
                if (StringUtils.isNullOrEmpty(challengeNotificationUrl)) {
                    throw new ConfigurationException("The challenge notification URL is required for 3DS v2 processing.");
                }

                if (StringUtils.isNullOrEmpty(methodNotificationUrl)) {
                    throw new ConfigurationException("The method notification URL is required for 3DS v2 processing.");
                }
            }
        }
    }

}