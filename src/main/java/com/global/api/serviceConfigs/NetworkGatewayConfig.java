package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.VapsConnector;
import com.global.api.utils.StringUtils;

public class NetworkGatewayConfig extends Configuration {
    private AcceptorConfig acceptorConfig;
    private IBatchProvider batchProvider;
    private String companyId;
    private ConnectionType connectionType = ConnectionType.ISDN;
    private String merchantType;
    private MessageType messageType = MessageType.Heartland_POS_8583;
    private String nodeIdentification;
    private Integer primaryPort;
    private ProtocolType protocolType = ProtocolType.TCP_IP;
    private String secondaryEndpoint;
    private Integer secondaryPort;
    private IStanProvider stanProvider;
    private String terminalId;
    private String uniqueDeviceId;

    public AcceptorConfig getAcceptorConfig() {
        return acceptorConfig;
    }
    public void setAcceptorConfig(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
    }
    public IBatchProvider getBatchProvider() {
        return batchProvider;
    }
    public void setBatchProvider(IBatchProvider batchProvider) {
        this.batchProvider = batchProvider;
    }
    public String getCompanyId() {
        return companyId;
    }
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public ConnectionType getConnectionType() {
        return connectionType;
    }
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    public String getMerchantType() {
        return merchantType;
    }
    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    public String getNodeIdentification() {
        if(!StringUtils.isNullOrEmpty(nodeIdentification)) {
            return nodeIdentification;
        }
        return "    ";
    }
    public void setNodeIdentification(String nodeIdentification) {
        this.nodeIdentification = nodeIdentification;
    }
    public String getPrimaryEndpoint() { return serviceUrl; }
    public void setPrimaryEndpoint(String value) { this.serviceUrl = value; }
    public Integer getPrimaryPort() {
        return primaryPort;
    }
    public void setPrimaryPort(Integer primaryPort) {
        this.primaryPort = primaryPort;
    }
    public ProtocolType getProtocolType() {
        return protocolType;
    }
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
    public String getSecondaryEndpoint() {
        return secondaryEndpoint;
    }
    public void setSecondaryEndpoint(String secondaryEndpoint) {
        this.secondaryEndpoint = secondaryEndpoint;
    }
    public Integer getSecondaryPort() {
        return secondaryPort;
    }
    public void setSecondaryPort(Integer secondaryPort) {
        this.secondaryPort = secondaryPort;
    }
    public IStanProvider getStanProvider() {
        return stanProvider;
    }
    public void setStanProvider(IStanProvider stanProvider) {
        this.stanProvider = stanProvider;
    }
    public String getTerminalId() {
        return terminalId;
    }
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }
    public void setUniqueDeviceId(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }

    public void configureContainer(ConfiguredServices services) {
        VapsConnector gateway = new VapsConnector();
        // connection fields
        gateway.setPrimaryEndpoint(serviceUrl);
        gateway.setPrimaryPort(primaryPort);
        gateway.setSecondaryEndpoint(secondaryEndpoint);
        gateway.setSecondaryPort(secondaryPort);
        gateway.setTimeout(timeout);
        gateway.setEnableLogging(enableLogging);
        gateway.setForceGatewayTimeout(forceGatewayTimeout);

        // other fields
        gateway.setCompanyId(companyId);
        gateway.setConnectionType(connectionType);
        gateway.setMessageType(messageType);
        gateway.setNodeIdentification(getNodeIdentification());
        gateway.setProtocolType(protocolType);
        gateway.setTerminalId(terminalId);
        gateway.setMerchantType(merchantType);
        gateway.setUniqueDeviceId(uniqueDeviceId);

        // acceptor config
        if(acceptorConfig == null) {
            acceptorConfig = new AcceptorConfig();
        }
        gateway.setAcceptorConfig(acceptorConfig);

        // stan provider
        gateway.setStanProvider(stanProvider);

        // batch provider
        gateway.setBatchProvider(batchProvider);

        services.setGatewayConnector(gateway);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        // must specify an endpoint
        if (StringUtils.isNullOrEmpty(serviceUrl) && StringUtils.isNullOrEmpty(secondaryEndpoint)) {
            throw new ConfigurationException("You must specify a primary or secondary endpoint for processing.");
        }

        // must specify a port
        if (!StringUtils.isNullOrEmpty(serviceUrl) && primaryPort == null) {
            throw new ConfigurationException("You must specify a port for the primary processing endpoint.");
        }

        // must specify a port
        if (!StringUtils.isNullOrEmpty(secondaryEndpoint) && secondaryPort == null) {
            throw new ConfigurationException("You must specify a port for the secondary processing endpoint.");
        }

        // acceptor config validate
        if(acceptorConfig != null) {
            acceptorConfig.validate();acceptorConfig.validate();
        }

        // company id
        if(StringUtils.isNullOrEmpty(companyId)) {
            throw new ConfigurationException("You must provide a company id.");
        }

        // terminal id
        if(StringUtils.isNullOrEmpty(terminalId)) {
            throw new ConfigurationException("You must provide a terminal id.");
        }

        // node identification
        if(!StringUtils.isNullOrEmpty(nodeIdentification) && nodeIdentification.length() != 4) {
            throw new ConfigurationException("Node identification must only be 4 characters in length.");
        }
    }
}
