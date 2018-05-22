package com.global.api;

import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.TableServiceConnector;
import com.global.api.serviceConfigs.BoardingConfig;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.serviceConfigs.PayrollConfig;
import com.global.api.serviceConfigs.TableServiceConfig;
import com.global.api.services.TableService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.utils.StringUtils;

public class ServicesConfig {
    private GatewayConfig gatewayConfig;
    private ConnectionConfig deviceConnectionConfig;
    private TableServiceConfig tableServiceConfig;
    private PayrollConfig payrollConfig;
    private BoardingConfig boardingConfig;

    public GatewayConfig getGatewayConfig() {
        return gatewayConfig;
    }
    public void setGatewayConfig(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }
    public ConnectionConfig getDeviceConnectionConfig() {
        return deviceConnectionConfig;
    }
    public void setDeviceConnectionConfig(ConnectionConfig deviceConnectionConfig) {
        this.deviceConnectionConfig = deviceConnectionConfig;
    }
    public TableServiceConfig getTableServiceConfig() {
        return tableServiceConfig;
    }
    public void setTableServiceConfig(TableServiceConfig tableServiceConfig) {
        this.tableServiceConfig = tableServiceConfig;
    }
    public PayrollConfig getPayrollConfig() {
        return payrollConfig;
    }
    public void setPayrollConfig(PayrollConfig payrollConfig) {
        this.payrollConfig = payrollConfig;
    }
    public BoardingConfig getBoardingConfig() {
        return boardingConfig;
    }
    public void setBoardingConfig(BoardingConfig boardingConfig) {
        this.boardingConfig = boardingConfig;
    }
    public void setTimeout(int timeout) {
        if(gatewayConfig != null)
            gatewayConfig.setTimeout(timeout);
        if(deviceConnectionConfig != null)
            deviceConnectionConfig.setTimeout(timeout);
        if(tableServiceConfig != null)
            tableServiceConfig.setTimeout(timeout);
        if(payrollConfig != null)
            payrollConfig.setTimeout(timeout);
    }

    protected void validate() throws ConfigurationException {
        if(gatewayConfig != null)
            gatewayConfig.validate();
        if(deviceConnectionConfig != null)
            deviceConnectionConfig.validate();
        if(tableServiceConfig != null)
            tableServiceConfig.validate();
        if(payrollConfig != null)
            payrollConfig.validate();
    }
}
