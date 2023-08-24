package com.global.api.gateways;

import java.util.LinkedList;

import com.global.api.entities.Transaction;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.enums.CharacterSet;
import com.global.api.network.enums.ConnectionType;
import com.global.api.network.enums.MessageType;
import com.global.api.network.enums.ProtocolType;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.IRequestEncoder;
import lombok.Getter;
import lombok.Setter;

public abstract class NetworkConnector extends NetworkGateway implements IPaymentGateway {

    @Setter
    protected AcceptorConfig acceptorConfig;
    protected IBatchProvider batchProvider;
    protected CharacterSet characterSet = CharacterSet.ASCII;
    @Setter
    protected String companyId;
    @Setter
    protected ConnectionType connectionType;
    @Setter
    protected String merchantType;
    @Setter
    protected MessageType messageType;
    @Setter
    protected String nodeIdentification;
    @Setter
    protected ProtocolType protocolType;
    @Setter
    protected IRequestEncoder requestEncoder;
    @Setter
    protected IStanProvider stanProvider;
    @Setter
    protected String terminalId;
    @Setter
    protected String uniqueDeviceId;
    protected LinkedList<Transaction> resentTransactions;
    protected Transaction resentBatch;
    @Getter
    @Setter
    protected String ewicMerchantId;

    public void setBatchProvider(IBatchProvider batchProvider) {
        this.batchProvider = batchProvider;
        if (this.batchProvider != null && this.batchProvider.getRequestEncoder() != null) {
            requestEncoder = batchProvider.getRequestEncoder();
        }
    }

    public boolean supportsHostedPayments() {
        return false;
    }

}
