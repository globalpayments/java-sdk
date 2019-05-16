package com.global.api.builders;

import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.network.enums.DE62_CardIssuerEntryTag;
import com.global.api.paymentMethods.IPaymentMethod;

import java.util.LinkedHashMap;

public abstract class TransactionBuilder<TResult> extends BaseBuilder<TResult> {
    protected boolean forceGatewayTimeout;
    protected TransactionType transactionType;
    protected TransactionModifier transactionModifier = TransactionModifier.None;
    protected IPaymentMethod paymentMethod;

    // network fields
    protected int batchNumber;
    protected String companyId;
    protected FleetData fleetData;
    protected LinkedHashMap<DE62_CardIssuerEntryTag, String> issuerData;
    protected PriorMessageInformation priorMessageInformation;
    protected ProductData productData;
    protected int sequenceNumber;
    protected int systemTraceAuditNumber;
    protected String uniqueDeviceId;
    protected TransactionMatchingData transactionMatchingData;

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
    public boolean isForceGatewayTimeout() {
        return forceGatewayTimeout;
    }

    // network fields
    public int getBatchNumber() { return batchNumber; }
    public String getCompanyId() {
        return companyId;
    }
    public FleetData getFleetData() {
        return fleetData;
    }
    public LinkedHashMap<DE62_CardIssuerEntryTag, String> getIssuerData() {
        return issuerData;
    }
    public PriorMessageInformation getPriorMessageInformation() {
        return priorMessageInformation;
    }
    public ProductData getProductData() {
        return productData;
    }
    public void setPriorMessageInformation(PriorMessageInformation priorMessageInformation) {
        this.priorMessageInformation = priorMessageInformation;
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

    public TransactionBuilder(TransactionType type) {
        this(type, null);
    }
    public TransactionBuilder(TransactionType type, IPaymentMethod paymentMethod){
        super();
        this.transactionType = type;
        this.paymentMethod = paymentMethod;
    }
}
