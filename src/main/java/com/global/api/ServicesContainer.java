package com.global.api;

import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.serviceConfigs.Configuration;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;

import java.util.HashMap;

public class ServicesContainer implements IDisposable {
    private HashMap<String, ConfiguredServices> configurations;
    private static ServicesContainer instance;

    public IDeviceInterface getDeviceInterface(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getDeviceInterface();
        throw new ApiException("The specified configuration has not been configured for terminal interaction.");
    }
    public DeviceController getDeviceController(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getDeviceController();
        throw new ApiException("The specified configuration has not been configured for terminal interaction.");
    }
    public IPaymentGateway getGateway(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getGatewayConnector();
        throw new ApiException("The specified configuration has not been configured for card processing.");
    }
    public PayrollConnector getPayroll(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getPayrollConnector();
        throw new ApiException("The specified configuration has not been configured for payroll.");
    }
    public IRecurringGateway getRecurring(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getRecurringConnector();
        throw new ApiException("The specified configuration has not been configured for recurring processing.");
    }
    public ISecure3dProvider getSecure3d(String configName, Secure3dVersion version) throws ApiException {
        if(configurations.containsKey(configName)) {
            ISecure3dProvider provider = configurations.get(configName).getSecure3dProvider(version);
            if(provider != null) {
                return provider;
            }
            throw new ConfigurationException(String.format("Secure 3d is not configured for %s", version.toString()));
        }
        throw new ConfigurationException("Secure 3d is not configured on the connector");
    }
    public TableServiceConnector getTableService(String configName) throws ApiException {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getTableServiceConnector();
        throw new ApiException("The specified configuration has not been configured for payroll.");
    }
    public IBillingProvider getBillingClient(String configName) throws ApiException {
        if (configurations.containsKey(configName)) {
            return configurations.get(configName).getBillingProvider();
        }

        throw new ConfigurationException("The specified configuration has not been configured for gateway processing.");
    }

    public static ServicesContainer getInstance() {
        if(instance == null)
            instance = new ServicesContainer();
        return instance;
    }

    public static void configure(ServicesConfig config) throws ConfigurationException {
        configure(config, "default");
    }
    public static void configure(ServicesConfig config, String configName) throws ConfigurationException {
        config.validate();

        // configure devices
        configureService(config.getDeviceConnectionConfig(), configName);

        // configure table service
        configureService(config.getTableServiceConfig(), configName);

        // configure payroll
        configureService(config.getPayrollConfig(), configName);

        // configure gateways
        configureService(config.getGatewayConfig(), configName);

        ConfiguredServices cs = new ConfiguredServices();

        // configure devices
        if(config.getDeviceConnectionConfig() != null) {

        }
    }

    public static <T extends Configuration> void configureService(T config) throws ConfigurationException {
        configureService(config, "default");
    }
    public static <T extends Configuration> void configureService(T config, String configName) throws ConfigurationException {
        if(config == null) {
            getInstance().removeConfiguration(configName);
            return;
        }

        if(!config.isValidated()) {
            config.validate();
        }

        ConfiguredServices cs = getInstance().getConfiguration(configName);
        config.configureContainer(cs);

        getInstance().addConfiguration(configName, cs);
    }

    private ServicesContainer() {
        configurations = new HashMap<String, ConfiguredServices>();
    }

    private ConfiguredServices getConfiguration(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName);
        return new ConfiguredServices();
    }

    private void addConfiguration(String configName, ConfiguredServices cs) {
        if(configurations.containsKey(configName)) {
            configurations.remove(configName);
        }
        configurations.put(configName, cs);
    }

    private void removeConfiguration(String configName) {
        if(configurations.containsKey(configName)) {
            configurations.remove(configName);
        }
    }

    public void dispose() {
        for(ConfiguredServices cs : configurations.values())
            cs.dispose();
    }
}
