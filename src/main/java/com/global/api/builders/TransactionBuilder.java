package com.global.api.builders;

import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.PayByLinkData;
import com.global.api.entities.enums.*;
import com.global.api.network.elements.EWICData;
import com.global.api.network.entities.*;
import com.global.api.network.entities.gnap.GnapRequestData;
import com.global.api.network.entities.nts.NtsNetworkMessageHeader;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    @Getter
    @Setter
    protected String description;
    protected FleetData fleetData;
    protected LinkedHashMap<CardIssuerEntryTag, String> issuerData;
    protected Integer followOnStan;
    protected PriorMessageInformation priorMessageInformation;
    protected ProductData productData;
    protected int sequenceNumber;
    protected int systemTraceAuditNumber;
    protected String uniqueDeviceId;
    // Entity specific to PayByLink service
    @Getter
    @Setter
    protected PayByLinkData payByLinkData;
    // A unique identifier generated by Global Payments to identify the payment link.
    @Getter
    @Setter
    protected String paymentLinkId;
    protected TransactionMatchingData transactionMatchingData;
    @Getter
    protected MasterCardCITMITIndicator citMitIndicator;
    protected boolean terminalError;
    @Getter
    protected GnapRequestData gnapRequestData;
    @Getter
    protected BigDecimal taxAmount;
    @Getter
    protected BigDecimal tipAmount;
    @Getter
    protected BigDecimal surchargeAmount;
    @Getter
    protected BigDecimal cashBackAmount;
    //Nts
    @Getter
    protected String invoiceNumber;
    @Getter
    protected String cvn;
    @Getter
    protected BigDecimal amount;
    @Getter
    protected String tagData;
    //Emv
    @Getter
    protected String emvMaxPinEntry;

    // P66 Tag 16
    @Getter
    protected NtsTag16 ntsTag16;
    @Getter
    @Setter
    private String posSequenceNumber;
    @Getter
    protected String serviceCode;
    @Getter
    protected String cardSequenceNumber;     // Card Sequence number.
    @Getter
    protected NtsProductData ntsProductData;
    @Getter
    protected String ecommerceAuthIndicator;
    @Getter
    protected String ecommerceData1;
    @Getter
    protected String ecommerceData2;
    @Getter
    protected String mcUCAF;
    @Getter
    protected String mcWalletId;
    @Getter
    protected String mcSLI;
    @Getter
    protected NtsNetworkMessageHeader ntsNetworkMessageHeader;
    @Getter
    protected NtsRequestMessageHeader ntsRequestMessageHeader;
    @Getter
    protected String transactionDate;
    @Getter
    protected String transactionTime;
    @Getter
    protected EWICData ewicData;
    protected String ewicIssuingEntity;
    @Getter
    private String zipCode;

    @Getter
    @Setter
    private String customerCode;
    @Getter
    @Setter
    private String hostResponseCode = "";
    @Getter
    @Setter
    private String offlineDeclineIndicator;
    @Getter
    private Boolean isSAFIndicator;
    @Getter
    private String safOrignDT;
    @Getter
    private int pdlTimeout;
    @Getter @Setter
    protected EcommerceInfo ecommerceInfo;
    @Getter @Setter
    protected String merchantOrCustomerInitiatedFlag;



    public void setNtsRequestMessageHeader(NtsRequestMessageHeader ntsRequestMessageHeader) {
        this.ntsRequestMessageHeader = ntsRequestMessageHeader;
    }

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
    public int getBatchNumber() {
        return batchNumber;
    }

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

    protected TransactionBuilder(TransactionType type) {
        this(type, null);
    }

    protected TransactionBuilder(TransactionType type, IPaymentMethod paymentMethod) {
        super();
        this.transactionType = type;
        this.paymentMethod = paymentMethod;
    }

    public TransactionBuilder<TResult> withServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
        return this;
    }

    public TransactionBuilder<TResult> withNtsProductData(NtsProductData ntsProductData) {
        this.ntsProductData = ntsProductData;
        return this;
    }

    public TransactionBuilder<TResult> withEcommerceAuthIndicator(String ecommerceAuthIndicator) {
        this.ecommerceAuthIndicator = ecommerceAuthIndicator;
        return this;
    }

    public TransactionBuilder<TResult> withEcommerceData1(String ecommerceData1) {
        this.ecommerceData1 = ecommerceData1;
        return this;
    }

    public TransactionBuilder<TResult> withEcommerceData2(String ecommerceData2) {
        this.ecommerceData2 = ecommerceData2;
        return this;
    }

    public TransactionBuilder<TResult> withEWICData(EWICData ewicData) {
        this.ewicData = ewicData;
        return this;
    }

    public String getEwicIssuingEntity() {
        return ewicIssuingEntity;
    }

    public void setEwicIssuingEntity(String ewicIssuingEntity) {
        this.ewicIssuingEntity = ewicIssuingEntity;
    }

    public TransactionBuilder<TResult> withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public TransactionBuilder<TResult> withCustomerCode(String customerCode) {
        this.customerCode = customerCode;
        return this;
    }

    public TransactionBuilder<TResult> withHostResponseCode(String hostResponseCode) {
        this.hostResponseCode = hostResponseCode;
        return this;
    }

    public TransactionBuilder<TResult> withOfflineDeclineIndicator(String offlineDeclineIndicator) {
        this.offlineDeclineIndicator = offlineDeclineIndicator;
        return this;
    }

    public TransactionBuilder<TResult> withSAFIndicator(Boolean value) {
        this.isSAFIndicator = value;
        return this;
    }

    public TransactionBuilder<TResult> withSAFOrigDT(String value) {
        this.safOrignDT = value;
        return this;
    }
    public TransactionBuilder<TResult> withMerchantOrCustomerInitiatedFlag(String merchantOrCustomerInitiatedFlag){
        this.merchantOrCustomerInitiatedFlag = merchantOrCustomerInitiatedFlag;
        return this;
    }

    public TransactionBuilder<TResult> withPDLTimeout(int value){
        this.pdlTimeout = value;
        return this;
    }
}