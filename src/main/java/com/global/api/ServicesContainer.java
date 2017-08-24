package com.global.api;

import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.*;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.heartSIP.HeartSipController;
import com.global.api.terminals.pax.PaxController;
import com.global.api.utils.StringUtils;

public class ServicesContainer {
    private IPaymentGateway gateway;
    private IRecurringGateway recurring;
    private IDeviceInterface device;
    private DeviceController deviceController;
    private TableServiceConnector tableServiceConnector;
    private static ServicesContainer instance;

    public IDeviceInterface getDeviceInterface() {
        return this.device;
    }
    public DeviceController getDeviceController() {
        return this.deviceController;
    }
    public IPaymentGateway getGateway() {
        return gateway;
    }
    public IRecurringGateway getRecurring() { return recurring; }
    public TableServiceConnector getTableService() { return tableServiceConnector; }

    public static ServicesContainer getInstance() throws ApiException {
        if(instance != null)
            return instance;
        else throw new ApiException("Services container not configured.");
    }

    public static void configure(ServicesConfig config) throws ConfigurationException {
        config.validate();

        // configure devices
        IDeviceInterface deviceInterface = null;
        DeviceController deviceController = null;
        if(config.getDeviceConnectionConfig() != null) {
            switch(config.getDeviceConnectionConfig().getDeviceType()) {
                case PAX_S300:
                    deviceController = new PaxController(config.getDeviceConnectionConfig());
                    deviceInterface = deviceController.configureInterface();
                    break;
                case HSIP_ISC250:
                    deviceController = new HeartSipController(config.getDeviceConnectionConfig());
                    deviceInterface = deviceController.configureInterface();
                default:
                    break;
            }
        }

        // configure table service
        TableServiceConnector tableServiceConnector = null;
        if(config.getTableServiceProvider() != null) {
            if(config.getTableServiceProvider().equals(TableServiceProviders.FreshTxt)) {
                tableServiceConnector = new TableServiceConnector();
                tableServiceConnector.setServiceUrl("https://www.freshtxt.com/api31/");
                tableServiceConnector.setTimeout(config.getTimeout());
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

            instance = new ServicesContainer(gateway, gateway, deviceController, deviceInterface, tableServiceConnector);
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

            PayPlanConnector payplan = new PayPlanConnector();
            payplan.setSecretApiKey(config.getSecretApiKey());
            payplan.setTimeout(config.getTimeout());
            payplan.setServiceUrl(config.getServiceUrl() + "/Portico.PayPlan.v2/");

            instance = new ServicesContainer(gateway, payplan, deviceController, deviceInterface, tableServiceConnector);
        }
    }

    private ServicesContainer(IPaymentGateway gateway, IRecurringGateway recurring, DeviceController deviceController, IDeviceInterface deviceInterface, TableServiceConnector tableServiceConnector) {
        this.gateway = gateway;
        this.recurring = recurring;
        this.deviceController = deviceController;
        this.device = deviceInterface;
        this.tableServiceConnector = tableServiceConnector;
    }
}
