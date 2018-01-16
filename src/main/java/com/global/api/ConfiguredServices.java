package com.global.api;

import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.IRecurringGateway;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;

class ConfiguredServices implements IDisposable {
    private IPaymentGateway gatewayConnector;
    private IRecurringGateway recurringConnector;
    private IDeviceInterface deviceInterface;
    private DeviceController deviceController;
    private TableServiceConnector reservationConnector;

    public IPaymentGateway getGatewayConnector() {
        return gatewayConnector;
    }
    public void setGatewayConnector(IPaymentGateway gatewayConnector) {
        this.gatewayConnector = gatewayConnector;
    }
    public IRecurringGateway getRecurringConnector() {
        return recurringConnector;
    }
    public void setRecurringConnector(IRecurringGateway recurringConnector) {
        this.recurringConnector = recurringConnector;
    }
    public IDeviceInterface getDeviceInterface() {
        return deviceInterface;
    }
    public DeviceController getDeviceController() {
        return deviceController;
    }
    public void setDeviceController(DeviceController deviceController) throws ConfigurationException {
        this.deviceController = deviceController;
        deviceInterface = deviceController.configureInterface();
    }
    public TableServiceConnector getReservationConnector() {
        return reservationConnector;
    }
    public void setReservationConnector(TableServiceConnector reservationConnector) {
        this.reservationConnector = reservationConnector;
    }

    public void dispose() {
        deviceController.dispose();
    }
}
