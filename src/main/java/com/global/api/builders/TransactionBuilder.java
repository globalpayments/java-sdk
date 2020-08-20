package com.global.api.builders;

import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.paymentMethods.IPaymentMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class TransactionBuilder<TResult> extends BaseBuilder<TResult> {
    protected TransactionType transactionType;
    protected TransactionModifier transactionModifier = TransactionModifier.None;
    protected IPaymentMethod paymentMethod;
    protected HashMap<Host, ArrayList<HostError>> simulatedHostErrors;

    // network fields
    protected int batchNumber;
    protected String companyId;
    protected FleetData fleetData;
    protected LinkedHashMap<CardIssuerEntryTag, String> issuerData;
    protected Integer followOnStan;
    protected PriorMessageInformation priorMessageInformation;
    protected ProductData productData;
    protected int sequenceNumber;
    protected int systemTraceAuditNumber;
    protected String uniqueDeviceId;
    protected TransactionMatchingData transactionMatchingData;
    protected boolean terminalError;

    public TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    public TransactionModifier getTransactionModifier() {
        return transactionModifier;
    }
    public void setTransactionModifier(TransactionModifier transactionModifier) {
        this.transactionModifier = transactionModifier;
    }
    public IPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(IPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public HashMap<Host, ArrayList<HostError>> getSimulatedHostErrors() {
        return simulatedHostErrors;
    }

    // network fields
    public int getBatchNumber() { return batchNumber; }
    public String getCompanyId() {
        return companyId;
    }
    public FleetData getFleetData() {
        return fleetData;
    }
    public LinkedHashMap<CardIssuerEntryTag, String> getIssuerData() {
        return issuerData;
    }
    public Integer getFollowOnStan() {
        return followOnStan;
    }
    public PriorMessageInformation getPriorMessageInformation() {
        return priorMessageInformation;
    }
    public void setPriorMessageInformation(PriorMessageInformation priorMessageInformation) {
        this.priorMessageInformation = priorMessageInformation;
    }
    public ProductData getProductData() {
        return productData;
    }
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    public int getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }
    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }
    public TransactionMatchingData getTransactionMatchingData() {
        return transactionMatchingData;
    }
    public boolean isTerminalError() {
        return terminalError;
    }

    public TransactionBuilder(TransactionType type) {
        this(type, null);
    }
    public TransactionBuilder(TransactionType type, IPaymentMethod paymentMethod){
        super();
        this.transactionType = type;
        this.paymentMethod = paymentMethod;
    }
}
