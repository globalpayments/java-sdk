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
public class PorticoConfig extends GatewayConfig {
    // Account's site ID
    private int siteId;
    // Account's license ID
    private int licenseId;
    // Account's device ID
    private int deviceId;
    // Account's username
    private String username;
    // Account's password
    private String password;
    // Integration's developer ID
    // This is provided at the start of an integration's certification.
    private String developerId;
    // Integration's version number
    private String versionNumber;
    // Account's secret API key
    private String secretApiKey;
    // A unique device id to be passed with each transaction
    private String uniqueDeviceId;
    //Name and Version of the SDK used for integration, where applicable.  Expected for users of the Heartland SDK.
    private String sdkNameVersion;
    // ProPay CertificationStr Value
    private String certificationStr;
    // ProPay TerminalID Value
    private String terminalID;
    // ProPay X509 Certificate Location
    private String X509CertificatePath;
    // ProPay X509 Certificate Base64 String (Optional: Can be used instead of X509CertificatePath)
    private String X509CertificateBase64String;
    // If true (default), use the US ProPay endpoints. If false, use the Canadian ProPay endpoints
    private boolean proPayUS = true;

    private String getPayPlanEndpoint() {
        if (
                (!StringUtils.isNullOrEmpty(secretApiKey) && secretApiKey.toLowerCase().contains("cert")) ||
                (StringUtils.isNullOrEmpty(secretApiKey) && Environment.TEST.equals(environment))
        ) {
            return "/Portico.PayPlan.v2/";
        }

        return "/PayPlan.v2/";
    }

    public PorticoConfig() {
        super(GatewayProvider.PORTICO);
    }

    public void configureContainer(ConfiguredServices services) {

        if(StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl = environment.equals(Environment.PRODUCTION) ?
                    ServiceEndpoints.PORTICO_PRODUCTION.getValue() :
                    ServiceEndpoints.PORTICO_TEST.getValue();
        }

        PorticoConnector gateway =
                new PorticoConnector()
                        .setSiteId(siteId)
                        .setLicenseId(licenseId)
                        .setDeviceId(deviceId)
                        .setUsername(username)
                        .setPassword(password)
                        .setSecretApiKey(secretApiKey)
                        .setDeveloperId(developerId)
                        .setVersionNumber(versionNumber)
                        .setSdkNameVersion(sdkNameVersion);

        gateway
                        .setTimeout(timeout)
                        .setServiceUrl(serviceUrl + "/Hps.Exchange.PosGateway/PosGatewayService.asmx")
                        .setEnableLogging(enableLogging)
                        .setRequestLogger(requestLogger)
                        .setWebProxy(webProxy);

        services.setGatewayConnector(gateway);

        // no data connector
        if (StringUtils.isNullOrEmpty(getDataClientId())) {
            services.setReportingService(gateway);
        }

        PayPlanConnector payplan = new PayPlanConnector();
        payplan.setSecretApiKey(secretApiKey);
        payplan.setTimeout(timeout);
        payplan.setServiceUrl(serviceUrl + getPayPlanEndpoint());
        payplan.setEnableLogging(enableLogging);
        payplan.setRequestLogger(requestLogger);
        payplan.setWebProxy(webProxy);

        services.setRecurringConnector(payplan);
        // TODO: Implement ProPayConnector
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
    }

}
