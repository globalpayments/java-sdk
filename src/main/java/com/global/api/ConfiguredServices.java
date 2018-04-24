package com.global.api;

import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.IRecurringGateway;
import com.global.api.gateways.PayrollConnector;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;

public class ConfiguredServices implements IDisposable {
    private IPaymentGateway gatewayConnector;
    private IRecurringGateway recurringConnector;
    private IDeviceInterface deviceInterface;
    private DeviceController deviceController;
//    private OnlineBoardingConnector boardingConnector;
    private TableServiceConnector tableServiceConnector;
    private PayrollConnector payrollConnector;

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
//    public OnlineBoardingConnector getBoardingConnector() {
//        return boardingConnector;
//    }
//    public void setBoardingConnector(OnlineBoardingConnector boardingConnector) {
//        this.boardingConnector = boardingConnector;
//    }
    public TableServiceConnector getTableServiceConnector() {
        return tableServiceConnector;
    }
    public void setTableServiceConnector(TableServiceConnector tableServiceConnector) {
        this.tableServiceConnector = tableServiceConnector;
    }
    public PayrollConnector getPayrollConnector() {
        return payrollConnector;
    }
    public void setPayrollConnector(PayrollConnector payrollConnector) {
        this.payrollConnector = payrollConnector;
    }

    public void dispose() {
        deviceController.dispose();
    }
}
