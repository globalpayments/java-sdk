package com.global.api;

import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.services.InstallmentService;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class ConfiguredServices implements IDisposable {
    @Getter @Setter private IPaymentGateway gatewayConnector;
    @Getter @Setter private IRecurringGateway recurringConnector;
    @Getter @Setter private IReportingService reportingService;
    @Getter private IDeviceInterface deviceInterface;
    @Getter private DeviceController deviceController;
    @Getter @Setter private TableServiceConnector tableServiceConnector;
    @Getter @Setter private PayrollConnector payrollConnector;
    @Getter @Setter private IFraudCheckService fraudService;
    @Getter @Setter private IFileProcessingService fileProcessingService;
    @Setter @Getter private IBillingProvider billingProvider;
    @Getter @Setter private IOpenBankingProvider openBankingProvider;
    @Getter @Setter private IProPayProvider proPayProvider;
    @Getter @Setter private IPayFacProvider payFacProvider;
    private final HashMap<Secure3dVersion, ISecure3dProvider> secure3dProviders;
    @Getter @Setter private IInstallmentService installmentService;

    public void setDeviceController(DeviceController deviceController) throws ConfigurationException {
        this.deviceController = deviceController;
        deviceInterface = deviceController.configureInterface();
    }

    public ISecure3dProvider getSecure3dProvider(Secure3dVersion version) {
        if(secure3dProviders.containsKey(version)) {
            return secure3dProviders.get(version);
        }
        else if(version.equals(Secure3dVersion.ANY)) {
            ISecure3dProvider provider = secure3dProviders.get(Secure3dVersion.TWO);
            if(provider == null) {
                provider = secure3dProviders.get(Secure3dVersion.ONE);
            }
            return provider;
        }
        return null;
    }

    public void setSecure3dProvider(Secure3dVersion version, ISecure3dProvider provider) {
        secure3dProviders.put(version, provider);
    }

    public ConfiguredServices() {
        secure3dProviders = new HashMap<>();
    }

    public void dispose() {
        deviceController.dispose();
    }

}
