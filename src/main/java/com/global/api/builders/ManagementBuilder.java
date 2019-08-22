package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.DccRateData;
import com.global.api.entities.LodgingData;
import com.global.api.entities.enums.*;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ManagementBuilder extends TransactionBuilder<Transaction> {
	private AlternativePaymentType alternativePaymentType;
    private BigDecimal amount;
    private BigDecimal authAmount;
    private BatchCloseType batchCloseType;
    private BigDecimal cashBackAmount;
    private String clerkId;
    private String clientTransactionId;
    private String currency;
    private String customerId;
    private boolean customerInitiated;
    private DccRateData dccRateData;
    private String description;
    private boolean forcedReversal;
    private BigDecimal gratuity;
    private String invoiceNumber;
    private LodgingData lodgingData;
    private String orderId;
    private String payerAuthenticationResponse;
    private String poNumber;
    private String productId;
    private ReasonCode reasonCode;
    private String referenceNumber;
    private String shiftNumber;
    private HashMap<String, ArrayList<String[]>> supplementaryData;
    private BigDecimal taxAmount;
    private TaxType taxType;
    private boolean terminalError;
    private String timestamp;
    private Integer transactionCount;
    private String transportData;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;

    public AlternativePaymentType getAlternativePaymentType() {
		return alternativePaymentType;
	}
	public BigDecimal getAmount() {
        return amount;
    }
    public BigDecimal getAuthAmount() {
        return authAmount;
    }
    public String getAuthorizationCode() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getAuthCode();
        return null;
    }
    public BatchCloseType getBatchCloseType() {
        return batchCloseType;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public BigDecimal getCashBackAmount() {
        return cashBackAmount;
    }
    public String getClerkId() {
        return clerkId;
    }
    public String getClientTransactionId() {
        if(!StringUtils.isNullOrEmpty(clientTransactionId)) {
            return clientTransactionId;
        }
        else if(paymentMethod instanceof TransactionReference) {
            return ((TransactionReference) paymentMethod).getClientTransactionId();
        }
        return null;
    }
    public String getCurrency() {
        return currency;
    }
    public String getCustomerId() {
        return customerId;
    }
    public boolean isCustomerInitiated() {
        return customerInitiated;
    }
    public DccRateData getDccRateData() {
        return dccRateData;
    }
    public String getDescription() {
        return description;
    }
    public boolean isForcedReversal() {
        return forcedReversal;
    }
    public BigDecimal getGratuity() {
        return gratuity;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public LodgingData getLodgingData() {
        return lodgingData;
    }
    public String getOrderId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getOrderId();
        return null;
    }
    public String getPayerAuthenticationResponse() {
        return payerAuthenticationResponse;
    }
    public String getPoNumber() {
        return poNumber;
    }
    public String getProductId() {
        return productId;
    }
    public ReasonCode getReasonCode() {
        return reasonCode;
    }
    public String getShiftNumber() {
        return shiftNumber;
    }
    public HashMap<String, ArrayList<String[]>> getSupplementaryData() { return supplementaryData; }
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    public TaxType getTaxType() {
        return taxType;
    }
    public boolean isTerminalError() {
        return terminalError;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getTransactionId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getTransactionId();
        return null;
    }
    public String getTransportData() {
        return transportData;
    }
    public Integer getTransactionCount() {
        return transactionCount;
    }
    public BigDecimal getTotalCredits() {
        return totalCredits;
    }
    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public ManagementBuilder withAlternativePaymentType(AlternativePaymentType value) {
		this.alternativePaymentType = value;
		return this;
	}
    public ManagementBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }
    public ManagementBuilder withAuthAmount(BigDecimal value) {
        this.authAmount = value;
        return this;
    }
    public ManagementBuilder withBatchCloseType(BatchCloseType value) {
        batchCloseType = value;
        return this;
    }
    public ManagementBuilder withBatchNumber(int batchNumber) {
        return withBatchNumber(batchNumber, 0);
    }
    public ManagementBuilder withBatchNumber(int batchNumber, int sequenceNumber) {
        this.batchNumber = batchNumber;
        this.sequenceNumber = sequenceNumber;
        return this;
    }
    public ManagementBuilder withBatchTotals(int transactionCount, BigDecimal totalDebits, BigDecimal totalCredits) {
        this.transactionCount = transactionCount;
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;

        return this;
    }
    public ManagementBuilder withCashBackAmount(BigDecimal value) {
        cashBackAmount = value;
        return this;
    }
    public ManagementBuilder withClerkId(String value) {
        clerkId = value;
        return this;
    }
    public ManagementBuilder withClientTransactionId(String value) {
        clientTransactionId = value;
        return this;
    }
    public ManagementBuilder withCurrency(String value) {
        this.currency = value;
        return this;
    }
    public ManagementBuilder withCustomerId(String value) {
        this.customerId = value;
        return this;
    }
    public ManagementBuilder withCustomerInitiated(boolean value) {
        customerInitiated = value;
        return this;
    }
    public ManagementBuilder withDccRateData(DccRateData value) {
        this.dccRateData = value;
        return this;
    }
    public ManagementBuilder withDescription(String value) {
        this.description = value;
        return this;
    }
    public ManagementBuilder withFleetData(FleetData value) {
        fleetData = value;
        return this;
    }
    public ManagementBuilder withForceGatewayTimeout(boolean value) {
        this.forceGatewayTimeout = value;
        return this;
    }
    public ManagementBuilder withForcedReversal(boolean value) {
        forcedReversal = value;
        return this;
    }
    public ManagementBuilder withGratuity(BigDecimal value) {
        this.gratuity = value;
        return this;
    }
    public ManagementBuilder withInvoiceNumber(String value) {
        this.invoiceNumber = value;
        return this;
    }
    public ManagementBuilder withIssuerData(CardIssuerEntryTag tag, String value) {
        if(issuerData == null) {
            issuerData = new LinkedHashMap<CardIssuerEntryTag, String>();
        }

        issuerData.put(tag, value);
        return this;
    }
    public ManagementBuilder withLodgingData(LodgingData value) {
        this.lodgingData = value;
        return this;
    }
    public ManagementBuilder withPayerAuthenticationResponse(String value) {
        payerAuthenticationResponse = value;
        return this;
    }
    public ManagementBuilder withPaymentMethod(IPaymentMethod value) {
        this.paymentMethod = value;
        if(paymentMethod instanceof TransactionReference)
            this.orderId = ((TransactionReference) paymentMethod).getOrderId();
        return this;
    }
    public ManagementBuilder withPoNumber(String value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.poNumber = value;
        return this;
    }
    public ManagementBuilder withPriorMessageInformation(PriorMessageInformation value) {
        this.priorMessageInformation = value;
        return this;
    }
    public ManagementBuilder withProductData(ProductData value) {
        productData = value;
        return this;
    }
    public ManagementBuilder withProductId(String value) {
        this.productId = value;
        return this;
    }
    public ManagementBuilder withReasonCode(ReasonCode value) {
        this.reasonCode = value;
        return this;
    }
    public ManagementBuilder withReferenceNumber(String value) {
        this.referenceNumber = value;
        return this;
    }
    public ManagementBuilder withShiftNumber(String value) {
        shiftNumber = value;
        return this;
    }
    public ManagementBuilder withSupplementaryData(String type, String... values) {
        // create the dictionary if needed
        if (supplementaryData == null) {
            supplementaryData = new HashMap<String, ArrayList<String[]>>();
        }

        // add the type if needed
        if(!supplementaryData.containsKey(type)) {
            supplementaryData.put(type, new ArrayList<String[]>());
        }

        // add the values to it
        supplementaryData.get(type).add(values);
        return this;
    }
    public ManagementBuilder withTaxAmount(BigDecimal value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.taxAmount = value;
        return this;
    }
    public ManagementBuilder withTaxType(TaxType value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.taxType = value;
        return this;
    }
    public ManagementBuilder withTimestamp(String value) {
        timestamp = value;
        return this;
    }
    private ManagementBuilder withModifier(TransactionModifier value) {
        this.transactionModifier = value;
        return this;
    }
    public ManagementBuilder withTransportData(String value) {
        transportData = value;
        return this;
    }
    public ManagementBuilder withUniqueDeviceId(String value) {
        uniqueDeviceId = value;
        return this;
    }

    // network fields
    public ManagementBuilder withCompanyId(String companyId) {
        this.companyId = companyId;
        return this;
    }
    public ManagementBuilder withSystemTraceAuditNumber(int value) {
        systemTraceAuditNumber = value;
        return this;
    }
    public ManagementBuilder withTerminalError(boolean value) {
        terminalError = value;
        return this;
    }
    public ManagementBuilder withTransactionMatchingData(TransactionMatchingData value) {
        transactionMatchingData = value;
        return this;
    }

    public ManagementBuilder(TransactionType type) {
        super(type, null);
    }

    @Override
    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway gateway = ServicesContainer.getInstance().getGateway(configName);
        return gateway.manageTransaction(this);
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Edit, TransactionType.Hold, TransactionType.Release))
                .check("paymentMethod").isClass(TransactionReference.class);

        this.validations.of(TransactionType.Edit).with(TransactionModifier.LevelII)
                .check("taxType").isNotNull();

        this.validations.of(TransactionType.Refund)
                .when("amount").isNotNull()
                .check("currency").isNotNull();

        this.validations.of(TransactionType.VerifySignature)
                .check("payerAuthenticationResponse").isNotNull()
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("orderId").isNotNull();
    }
}
