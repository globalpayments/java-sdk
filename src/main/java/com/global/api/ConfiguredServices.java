package com.global.api;

import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class ConfiguredServices implements IDisposable {
    private IPaymentGateway gatewayConnector;
    private IRecurringGateway recurringConnector;
    @Getter @Setter private IReportingService reportingService;
    private IDeviceInterface deviceInterface;
    private DeviceController deviceController;
    private TableServiceConnector tableServiceConnector;
    private PayrollConnector payrollConnector;
    private HashMap<Secure3dVersion, ISecure3dProvider> secure3dProviders;
    private IBillingProvider billingProvider;
    @Getter @Setter private IOpenBankingProvider openBankingProvider;
    @Getter @Setter private IPayFacProvider payFacProvider;

    IPaymentGateway getGatewayConnector() {
        return gatewayConnector;
    }
    public void setGatewayConnector(IPaymentGateway gatewayConnector) {
        this.gatewayConnector = gatewayConnector;
    }
    IRecurringGateway getRecurringConnector() {
        return recurringConnector;
    }
    public void setRecurringConnector(IRecurringGateway recurringConnector) {
        this.recurringConnector = recurringConnector;
    }
    IDeviceInterface getDeviceInterface() {
        return deviceInterface;
    }
    DeviceController getDeviceController() {
        return deviceController;
    }
    public void setDeviceController(DeviceController deviceController) throws ConfigurationException {
        this.deviceController = deviceController;
        deviceInterface = deviceController.configureInterface();
    }
    TableServiceConnector getTableServiceConnector() {
        return tableServiceConnector;
    }
    public void setTableServiceConnector(TableServiceConnector tableServiceConnector) {
        this.tableServiceConnector = tableServiceConnector;
    }
    PayrollConnector getPayrollConnector() {
        return payrollConnector;
    }
    public void setPayrollConnector(PayrollConnector payrollConnector) {
        this.payrollConnector = payrollConnector;
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
    public IBillingProvider getBillingProvider() {
        return billingProvider;
    }
    public void setBillingProvider(IBillingProvider billingProvider) {
        this.billingProvider = billingProvider;
    }

    public void setPayFacProvider(IPayFacProvider provider) {
        if (this.payFacProvider == null) {
            this.payFacProvider = provider;
        }
    }

    public ConfiguredServices() {
        secure3dProviders = new HashMap<Secure3dVersion, ISecure3dProvider>();
    }

    public void dispose() {
        deviceController.dispose();
    }
}
