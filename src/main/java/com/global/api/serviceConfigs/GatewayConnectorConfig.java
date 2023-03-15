package com.global.api.serviceConfigs;

import com.global.api.entities.enums.LogicProcessFlag;
import com.global.api.entities.enums.TerminalType;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.NetworkGateway;
import com.global.api.gateways.events.IGatewayEventHandler;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.enums.*;
import lombok.Setter;

@Setter
public abstract class GatewayConnectorConfig extends NetworkGateway implements IPaymentGateway {
	protected AcceptorConfig acceptorConfig;
	protected IBatchProvider batchProvider;
	protected ConnectionType connectionType = ConnectionType.ISDN;
	//protected IGatewayEventHandler gatewayEventHandler;
	protected String merchantType;
	protected MessageType messageType = MessageType.Heartland_POS_8583;
	protected String nodeIdentification;
	protected ProtocolType protocolType;
	protected IStanProvider stanProvider;
	protected String terminalId;
	protected String uniqueDeviceId;
	protected String binTerminalId;
	protected String binTerminalType;
	protected CardDataInputCapability inputCapabilityCode;
	protected String softwareVersion;
	protected LogicProcessFlag logicProcessFlag;
	protected TerminalType terminalType;
	protected String unitNumber;
	protected NetworkProcessingFlag processingFlag;
    protected String companyId;

}
