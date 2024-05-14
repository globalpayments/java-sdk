package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.NtsConnector;
import com.global.api.gateways.VapsConnector;
import com.global.api.network.entities.NtsData;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

public class ResubmitBuilder extends TransactionBuilder<Transaction> {
    private String authCode;
    private boolean forceToHost;
    private NtsData ntsData;
    private String timestamp;
    private String transactionToken;
    @Getter @Setter
    private String currency;

    public String getAuthCode() {
        return authCode;
    }
    public boolean isForceToHost() { return forceToHost; }
    public NtsData getNtsData() {
        return ntsData;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getTransactionToken() {
        return transactionToken;
    }
    public boolean hasMessageControlData() {
        return (batchNumber != 0 || sequenceNumber != 0);
    }

    public ResubmitBuilder withAuthCode(String value) {
        authCode = value;
        return this;
    }
    public ResubmitBuilder withBatchNumber(int batchNumber, int sequenceNumber) {
        this.batchNumber = batchNumber;
        this.sequenceNumber = sequenceNumber;
        return this;
    }
    public ResubmitBuilder withForceToHost(boolean value) {
        forceToHost = value;
        return this;
    }
    public ResubmitBuilder withNtsData(NtsData value) {
        ntsData = value;
        return this;
    }
    public ResubmitBuilder withSimulatedHostErrors(Host host, HostError... errors) {
        if(simulatedHostErrors == null) {
            simulatedHostErrors = new HashMap<Host, ArrayList<HostError>>();
        }

        if(!simulatedHostErrors.containsKey(host)) {
            simulatedHostErrors.put(host, new ArrayList<HostError>());
        }
        for(HostError error: errors) {
            simulatedHostErrors.get(host).add(error);
        }
        return this;
    }
    public ResubmitBuilder withSystemTraceAuditNumber(int value) {
        systemTraceAuditNumber = value;
        return this;
    }
    public ResubmitBuilder withTimestamp(String value) {
        timestamp = value;
        return this;
    }
    public ResubmitBuilder withTransactionToken(String token) {
        this.transactionToken = token;
        return this;
    }
    public ResubmitBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public ResubmitBuilder(TransactionType type) {
        super(type, null);
    }

    public ResubmitBuilder withPaymentMethod (IPaymentMethod value){
        this.paymentMethod = value;
        return this;
    }

    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);
        if(client instanceof VapsConnector) {
            return ((VapsConnector)client).resubmitTransaction(this);
        }
        if(client instanceof NtsConnector) {
            return ((NtsConnector)client).resubmitTransaction(this);
        }
        else {
            throw new UnsupportedTransactionException("Resubmissions are not allowed for the currently configured gateway.");
        }
    }

    public void setupValidations() {
        this.validations.of(TransactionType.Capture)
                .check("authCode").isNotNull()
                .check("ntsData").isNotNull();
    }
}
