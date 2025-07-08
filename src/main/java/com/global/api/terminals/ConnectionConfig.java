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
import lombok.Setter;

@Getter @Setter
public class ConnectionConfig extends Configuration implements ITerminalConfiguration {
    private ConnectionModes connectionMode;
    private DeviceType deviceType;
    private String configName;
    private IRequestIdProvider requestIdProvider;
    private IRequestLogger requestLogger;
    @Deprecated
    private IRequestLogger logManagementProvider;

    // TCP CONNECTIONS
    private String ipAddress;
    private int port;

    // SERIAL CONNECTIONS
    private BaudRate baudRate;
    private Parity parity;
    private StopBits stopBits;
    private DataBits dataBits;

    // DIAMOND CLOUD CONNECTIONS
    protected String secretKey;
    protected String isvId;
    protected String serviceUrl;
    protected String posId;
    protected String region;

    // MEET IN THE CLOUD CONNECTIONS
    private MitcConfig geniusMitcConfig;
    private GatewayConfig gatewayConfig;

    // AIDL CONNECTIONS
    private IAidlService aidlService;

    public ConnectionConfig() {
        timeout = 30000;
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
