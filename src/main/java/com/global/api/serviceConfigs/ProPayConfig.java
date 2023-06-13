package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.ProPayConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Setter
@Getter
public class ProPayConfig extends GatewayConfig{
    // ProPay CertificationStr Value
    private String certificationStr;
    // ProPay TerminalID Value
    private String terminalID;
    // ProPay X509 Certificate Location
    private String x509CertificatePath;
    // ProPay X509 Certificate Base64 String (Optional: Can be used instead of X509CertificatePath)
    private String x509CertificateBase64String;
    // If true (default), use the US ProPay endpoints. If false, use the Canadian ProPay endpoints
    private boolean isProPayUS = true;

    public ProPayConfig() {
        super(GatewayProvider.PROPAY);
    }

    @Override
    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        ProPayConnector connector = new ProPayConnector();
        if (certificationStr != null && certificationStr.length() > 0) {
            if (environment == Environment.TEST) {
                serviceUrl = (isProPayUS ? ServiceEndpoints.PROPAY_TEST : ServiceEndpoints.PROPAY_TEST_CANADIAN).getValue();
            }
            else {
                serviceUrl = (isProPayUS ? ServiceEndpoints.PROPAY_PRODUCTION : ServiceEndpoints.PROPAY_PRODUCTION_CANADIAN).getValue();
            }

            connector.setCertStr(certificationStr);
            connector.setTerminalID(terminalID);
            connector.setTimeout(timeout);
            connector.setServiceUrl(serviceUrl);
            connector.setX509CertPath(x509CertificatePath);
            connector.setX509Base64String(x509CertificateBase64String);
            connector.setRequestLogger(requestLogger);
            connector.setWebProxy(webProxy);
            connector.setEnableLogging(true);
            };
            services.setProPayProvider(connector);
    }
}
