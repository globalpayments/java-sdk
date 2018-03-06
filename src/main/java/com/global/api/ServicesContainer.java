package com.global.api;

import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;
import com.global.api.terminals.heartSIP.HeartSipController;
import com.global.api.terminals.pax.PaxController;
import com.global.api.utils.StringUtils;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class ServicesContainer implements IDisposable {
    private HashMap<String, ConfiguredServices> configurations;
    private static ServicesContainer instance;

    public IDeviceInterface getDeviceInterface(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getDeviceInterface();
        return null;
    }
    public DeviceController getDeviceController(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getDeviceController();
        return null;
    }
    public IPaymentGateway getGateway(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getGatewayConnector();
        return null;
    }
    public IRecurringGateway getRecurring(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getRecurringConnector();
        return null;
    }
    public TableServiceConnector getTableService(String configName) {
        if(configurations.containsKey(configName))
            return configurations.get(configName).getReservationConnector();
        return null;
    }

    public static ServicesContainer getInstance() throws ApiException {
        if(instance != null)
            return instance;
        else throw new ApiException("Services container not configured.");
    }

    public static void configure(ServicesConfig config) throws ConfigurationException {
        configure(config, "default");
    }
    public static void configure(ServicesConfig config, String configName) throws ConfigurationException {
        config.validate();

        ConfiguredServices cs = new ConfiguredServices();

        // configure devices
        if(config.getDeviceConnectionConfig() != null) {
            switch(config.getDeviceConnectionConfig().getDeviceType()) {
                case PAX_S300:
                    cs.setDeviceController(new PaxController(config.getDeviceConnectionConfig()));
                    break;
                case HSIP_ISC250:
                    cs.setDeviceController(new HeartSipController(config.getDeviceConnectionConfig()));
                default:
                    break;
            }
        }

        // configure table service
        if(config.getTableServiceProvider() != null) {
            if(config.getTableServiceProvider().equals(TableServiceProviders.FreshTxt)) {
                TableServiceConnector tableServiceConnector = new TableServiceConnector();
                tableServiceConnector.setServiceUrl("https://www.freshtxt.com/api31/");
                tableServiceConnector.setTimeout(config.getTimeout());
                cs.setReservationConnector(tableServiceConnector);
            }
        }

        if(!StringUtils.isNullOrEmpty(config.getMerchantId())) {
            RealexConnector gateway = new RealexConnector();
            gateway.setMerchantId(config.getMerchantId());
            gateway.setAccountId(config.getAccountId());
            gateway.setSharedSecret(config.getSharedSecret());
            gateway.setChannel(config.getChannel());
            gateway.setRebatePassword(config.getRebatePassword());
            gateway.setRefundPassword(config.getRefundPassword());
            gateway.setTimeout(config.getTimeout());
            gateway.setServiceUrl(config.getServiceUrl());
            gateway.setHostedPaymentConfig(config.getHostedPaymentConfig());

            cs.setGatewayConnector(gateway);
            cs.setRecurringConnector(gateway);
        }
        else {
            PorticoConnector gateway = new PorticoConnector();
            gateway.setSiteId(config.getSiteId());
            gateway.setLicenseId(config.getLicenseId());
            gateway.setDeviceId(config.getDeviceId());
            gateway.setUsername(config.getUsername());
            gateway.setPassword(config.getPassword());
            gateway.setSecretApiKey(config.getSecretApiKey());
            gateway.setDeveloperId(config.getDeveloperId());
            gateway.setVersionNumber(config.getVersionNumber());
            gateway.setTimeout(config.getTimeout());
            gateway.setServiceUrl(config.getServiceUrl() + "/Hps.Exchange.PosGateway/PosGatewayService.asmx");
            cs.setGatewayConnector(gateway);

            PayPlanConnector payplan = new PayPlanConnector();
            payplan.setSecretApiKey(config.getSecretApiKey());
            payplan.setTimeout(config.getTimeout());
            payplan.setServiceUrl(config.getServiceUrl() + "/Portico.PayPlan.v2/");

            cs.setRecurringConnector(payplan);
        }

        if(instance == null)
            instance = new ServicesContainer();

        instance.addConfiguration(configName, cs);
    }

    private ServicesContainer() {
        configurations = new HashMap<String, ConfiguredServices>();
    }

    private void addConfiguration(String configName, ConfiguredServices cs) {
        if(configurations.containsKey(configName))
            configurations.remove(configName);
        configurations.put(configName, cs);
    }

    public void dispose() {
        for(ConfiguredServices cs : configurations.values())
            cs.dispose();
    }
}
