package com.global.api.terminals;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.logging.IRequestLogger;
import com.global.api.serviceConfigs.Configuration;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.terminals.abstractions.IAidlService;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.diamond.DiamondCloudConfig;
import com.global.api.terminals.diamond.DiamondController;
import com.global.api.terminals.genius.GeniusController;
import com.global.api.terminals.genius.serviceConfigs.MitcConfig;
import com.global.api.terminals.hpa.HpaController;
import com.global.api.terminals.pax.PaxController;
import com.global.api.terminals.upa.UpaController;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
public class ConnectionConfig extends Configuration implements ITerminalConfiguration {
    private ConnectionModes connectionMode;
    private BaudRate baudRate;
    private Parity parity;
    private StopBits stopBits;
    private DataBits dataBits;
    private String ipAddress;
    private int port;
    private DeviceType deviceType;
    private IRequestIdProvider requestIdProvider;
    private MitcConfig geniusMitcConfig;
    private IRequestLogger logManagementProvider;
    private IAidlService iAidlService;

    public ConnectionConfig() {
        timeout = 30000;
    }

    private GatewayConfig gatewayConfig;
    private String configName;

    public void setConnectionMode(ConnectionModes connectionModes) {
        this.connectionMode = connectionModes;
    }

    public void setAidlService(IAidlService aidlServiceMode) {
        this.iAidlService = aidlServiceMode;
    }

    public void setBaudRate(BaudRate baudRate) {
        this.baudRate = baudRate;
    }

    public void setParity(Parity parity) {
        this.parity = parity;
    }

    public void setStopBits(StopBits stopBits) {
        this.stopBits = stopBits;
    }

    public void setDataBits(DataBits dataBits) {
        this.dataBits = dataBits;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void setRequestIdProvider(IRequestIdProvider requestIdProvider) {
        this.requestIdProvider = requestIdProvider;
    }

    public void setLogManagementProvider(IRequestLogger logManagementProvider) {
        this.logManagementProvider = logManagementProvider;
    }

    public void setGeniusMitcConfig(MitcConfig geniusMitcConfig) {
        this.geniusMitcConfig = geniusMitcConfig;
    }

    @Override
    public void setGatewayConfig(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    @Override
    public GatewayConfig getGatewayConfig() {
        return gatewayConfig;
    }



    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        switch (deviceType) {
            case PAX_DEVICE:
                services.setDeviceController(new PaxController(this));
                break;
            case HPA_ISC250:
                services.setDeviceController(new HpaController(this));
                break;
            case UPA_DEVICE:
                services.setDeviceController(new UpaController(this));
                break;
            case GENIUS_VERIFONE_P400:
                services.setDeviceController(new GeniusController(this));
                break;
            case PAX_ARIES8:
            case PAX_A80:
            case PAX_A35:
            case PAX_A920:
            case PAX_A77:
            case NEXGO_N5:
                services.setDeviceController(new DiamondController((DiamondCloudConfig) this));
                break;
            default:
                break;
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        if (connectionMode == ConnectionModes.TCP_IP || connectionMode == ConnectionModes.HTTP) {
            if (StringUtils.isNullOrEmpty(ipAddress))
                throw new ConfigurationException("IpAddress is required for TCP or HTTP communication modes.");
            if (port == 0)
                throw new ConfigurationException("Port is required for TCP or HTTP communication modes.");
        } else if (connectionMode == ConnectionModes.MEET_IN_THE_CLOUD) {
            if (this.geniusMitcConfig == null && gatewayConfig == null) {
                throw new ConfigurationException("meetInTheCloudConfig or gatewayConfig objects are required for this connection method");
            }
        } else if (connectionMode == ConnectionModes.AIDL) {
            if (deviceType != DeviceType.UPA_DEVICE) {
                throw new ConfigurationException("AIDL is only currently supported on UPA Devices!");
            }
        }
    }
}
