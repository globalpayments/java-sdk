package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE123_ReconciliationTotals_nws;
import com.global.api.gateways.IOpenBankingProvider;
import com.global.api.network.entities.gnap.GnapRequestData;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.network.entities.*;
import com.global.api.network.entities.nts.*;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.paymentMethods.*;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

public class ManagementBuilder extends TransactionBuilder<Transaction> {
    @Getter private AccountType accountType;
    private AlternativePaymentType alternativePaymentType;
    private BigDecimal amount;
    private BigDecimal authAmount;
    private BatchCloseType batchCloseType;
    private BigDecimal cashBackAmount;
    private String clerkId;
    private String clientTransactionId;
    private BigDecimal convenienceAmount;
    private String currency;
    private String customerId;
    private String customerIpAddress;
    private boolean customerInitiated;
    @Getter @Setter private boolean multiCapture;
    private DccRateData dccRateData;
    private String description;
    @Getter private ArrayList<DisputeDocument> disputeDocuments;
    @Getter private String disputeId;
    @Getter private String dynamicDescriptor;
    @Getter @Setter private eCheck bankTransferDetails;
    private boolean forceToHost;
    private BigDecimal gratuity;
    @Getter private String idempotencyKey;
    private String invoiceNumber;
    private LodgingData lodgingData;
    @Getter @Setter private Integer multiCapturePaymentCount;
    @Getter @Setter private Integer multiCaptureSequence;
    private String orderId;
    private String payerAuthenticationResponse;
    private String poNumber;
    @Getter private String posSequenceNumber;
    private String productId;
    private ReasonCode reasonCode;
    private String referenceNumber;
    private ReversalReasonCode reversalReasonCode;
    private String shiftNumber;
    private HashMap<String, ArrayList<String[]>> supplementaryData;
    private BigDecimal surchargeAmount;
    private String tagData;
    @Getter private PaymentMethodUsageMode paymentMethodUsageMode;
    private BigDecimal taxAmount;
    private TaxType taxType;
    private String timestamp;
    private Integer transactionCount;
    private String transportData;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    @Getter private BigDecimal totalAmount;
    @Getter private String batchReference;
    private List<Bill> bills;
    @Getter
    private EcommerceInfo ecommerceInfo;
    @Getter
    private StoredCredential storedCredential;
    @Getter
    private String paymentPurposeCode;

    //Nts block
    @Getter
    private int dataCollectResponseCode;
    @Getter
    private String approvalcode;
    @Getter
    protected BigDecimal settlementAmount;
    @Getter
    private BigDecimal totalSales;
    @Getter
    private BigDecimal totalReturns;
    @Getter
    private NtsRequestsToBalanceRequest ntsRequestsToBalance;
    @Getter
    private NtsRequestToBalanceData ntsRequestsToBalanceData;
    @Getter private boolean allowDuplicates;
    @Getter private Customer customer;
    @Getter private String country;
    @Getter private boolean generateReceipt;
    // TODO: Remove these PayByLinkData members when a validation for subProperties is working in ValidationClause class
    @Getter @Setter protected PaymentMethodUsageMode usageMode;
    @Getter @Setter protected Integer usageLimit;
    @Getter @Setter protected PayByLinkType type;
    @Getter
    private ArrayList<Product> miscProductData;

    @Getter @Setter private String reference;
    @Getter @Setter private FundsData fundsData;
    @Getter @Setter private CommercialData commercialData;

    @Getter @Setter private DE123_ReconciliationTotals_nws reconciliationTotals;
    @Getter @Setter private CreditDebitIndicator creditDebitIndicator;

    public ManagementBuilder withMiscProductData(ArrayList<Product> values) {
        this.miscProductData = values;
        return this;
    }
    @Getter
    private EBTVoucherEntryData voucherEntryData;
    private String eWICIssuingEntity;

    public ManagementBuilder withCardSequenceNumber(String value) {
        this.cardSequenceNumber = value;
        return this;
    }
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
    public List<Bill> getBills() {
        return bills;
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
    public BigDecimal getConvenienceAmount() {
        return convenienceAmount;
    }
    public String getCurrency() {
        return currency;
    }
    public String getCustomerId() {
        return customerId;
    }
    public String getCustomerIpAddress() {
        return customerIpAddress;
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
    public EmvChipCondition getEmvChipCondition() {
        if(paymentMethod instanceof TransactionReference) {
            return ((TransactionReference) paymentMethod).getOriginalEmvChipCondition();
        }
        return null;
    }
    public boolean isForceToHost() {
        return forceToHost;
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
    public boolean isPartialApproval() {
        if(paymentMethod instanceof TransactionReference) {
            return ((TransactionReference) paymentMethod).isPartialApproval();
        }
        return false;
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
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public ReversalReasonCode getReversalReasonCode() {
        return reversalReasonCode;
    }
    public String getShiftNumber() {
        return shiftNumber;
    }
    public HashMap<String, ArrayList<String[]>> getSupplementaryData() { return supplementaryData; }
    public BigDecimal getSurchargeAmount() { return surchargeAmount; }
    public String getTagData() { return tagData; }
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    public TaxType getTaxType() {
        return taxType;
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

    public ManagementBuilder withSettlementAmount(BigDecimal value)
    {
        this.settlementAmount = value;
        return this;
    }
    public ManagementBuilder withTransactiontype(TransactionType type) {
        transactionType = type;
        return this;
    }

    public ManagementBuilder withPaymentPurposeCode(String paymentPurposeCode) {
        this.paymentPurposeCode = paymentPurposeCode;
        return this;
    }

    public ManagementBuilder withNtsNetworkMessageHeader(NtsNetworkMessageHeader value) {
        this.ntsNetworkMessageHeader = value;
        return this;
    }
    public ManagementBuilder withNtsRequestMessageHeader(NtsRequestMessageHeader value) {
        this.ntsRequestMessageHeader = value;
        return this;
    }
    public ManagementBuilder withNtsRequestsToBalance(NtsRequestsToBalanceRequest value) {
        ntsRequestsToBalance = value;
        return this;
    }

    public ManagementBuilder withNtsRequestsToBalanceData(NtsRequestToBalanceData value) {
        this.ntsRequestsToBalanceData = value;
        return this;
    }
    public ManagementBuilder withApprovalCode(String value) {
        approvalcode = value;
        return this;
    }
    public ManagementBuilder withDataCollectResponseCode(int value) {
        dataCollectResponseCode = value;
        return this;
    }
    public ManagementBuilder withTransactionDate(String value) {
        transactionDate = value;
        return this;
    }
    public ManagementBuilder withTransactionTime(String value) {
        transactionTime = value;
        return this;
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

    public ManagementBuilder withBatchTotals(BigDecimal totalAmount,BigDecimal totalDebits, BigDecimal totalCredits) {
        this.totalAmount = totalAmount;
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;

        return this;
    }
    public ManagementBuilder withBatchTotals(int transactionCount ,DE123_ReconciliationTotals_nws totals) {
        this.transactionCount = transactionCount;
        this.reconciliationTotals = totals;
        return this;
    }

    public ManagementBuilder withBatchTotalTransaction(int transactionCount, BigDecimal totalSales, BigDecimal totalRetuens) {
        this.transactionCount = transactionCount;
        this.totalSales = totalSales;
        this.totalReturns = totalRetuens;
        return this;
    }
    public ManagementBuilder withBatchReference(String value) {
        this.batchReference = value;
        return this;
    }
    public ManagementBuilder withBills(Bill ... bills) {
        this.bills = Arrays.asList(bills);
        return this;
    }
    public ManagementBuilder withBills(List<Bill> values) {
        this.bills = values;
        return this;
    }
    public ManagementBuilder withMultiCapture() {
        this.multiCapture = true;
        this.multiCaptureSequence = 1;
        this.multiCapturePaymentCount = 1;

        return this;
    }
    public ManagementBuilder withMultiCapture(Integer sequence, Integer paymentCount) {
        this.multiCapture = true;
        this.multiCaptureSequence = (sequence != null) ? sequence : 1;
        this.multiCapturePaymentCount = (paymentCount != null) ? paymentCount : 1;

        return this;
    }
    public ManagementBuilder withCashBackAmount(BigDecimal value) {
        cashBackAmount = value;
        return this;
    }
    public ManagementBuilder withChipCondition(EmvChipCondition value) {
        if(paymentMethod instanceof TransactionReference) {
            ((TransactionReference) paymentMethod).setOriginalEmvChipCondition(value);
        }
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
    public ManagementBuilder withConvenienceAmt(BigDecimal value) {
        this.convenienceAmount = value;
        return this;
    }
    public ManagementBuilder withCurrency(String value) {
        this.currency = value;
        return this;
    }
    public ManagementBuilder withGnapRequestData(GnapRequestData gnapRequestData) {
        this.gnapRequestData = gnapRequestData;
        return this;
    }
    public ManagementBuilder withCustomerId(String value) {
        this.customerId = value;
        return this;
    }
    public ManagementBuilder withCustomerIpAddress(String value) {
        this.customerIpAddress = value;
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
    public ManagementBuilder withDisputeDocuments(ArrayList<DisputeDocument> value) {
        disputeDocuments = value;
        return this;
    }
    public ManagementBuilder withDisputeId(String value) {
        disputeId = value;
        return this;
    }
    public ManagementBuilder withDynamicDescriptor(String value) {
        dynamicDescriptor = value;
        return this;
    }
    public ManagementBuilder withBankTransferDetails(eCheck value) {
        bankTransferDetails = value;
        return this;
    }
    public ManagementBuilder withFleetData(FleetData value) {
        fleetData = value;
        return this;
    }
    public ManagementBuilder withForceToHost(boolean value) {
        forceToHost = value;
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
    public ManagementBuilder withIdempotencyKey(String value) {
        this.idempotencyKey = value;
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
    public ManagementBuilder withPartialApproval(boolean value) {
        if(paymentMethod instanceof TransactionReference) {
            ((TransactionReference) paymentMethod).setPartialApproval(value);
        }
        return this;
    }
    public ManagementBuilder withPayerAuthenticationResponse(String value) {
        payerAuthenticationResponse = value;
        return this;
    }
    public ManagementBuilder withPaymentMethod(IPaymentMethod value) {
        this.paymentMethod = value;
        if(paymentMethod instanceof TransactionReference) {
            this.orderId = ((TransactionReference) paymentMethod).getOrderId();
        }
        return this;
    }
    public ManagementBuilder withPoNumber(String value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.poNumber = value;
        return this;
    }
    public ManagementBuilder withPosSequenceNumber(String value) {
        this.posSequenceNumber = value;
        return this;
    }

     public ManagementBuilder withCommercialData(CommercialData data) {
        this.commercialData = data;
    if (data.getCommercialIndicator().equals(TransactionModifier.LevelII)) {
        this.transactionModifier = TransactionModifier.LevelII;
    }
    else { this.transactionModifier = TransactionModifier.Level_III;}
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
    public ManagementBuilder withPaymentLinkId(String value)
    {
        this.paymentLinkId = value;
        return this;
    }
    public ManagementBuilder withPayByLinkData(PayByLinkData payByLinkData)
    {
        this.payByLinkData = payByLinkData;
        return this;
    }
    public ManagementBuilder withReasonCode(ReasonCode value) {
        this.reasonCode = value;
        return this;
    }
    public ManagementBuilder withReversalReasonCode(ReversalReasonCode value) {
        reversalReasonCode = value;
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

    public ManagementBuilder withNtsTag16(NtsTag16 tag16) {
        ntsTag16 = tag16;
        return this;
    }

    public ManagementBuilder withNtsProductData(NtsProductData ntsProductData) {
        this.ntsProductData = ntsProductData;
        return this;
    }
    public ManagementBuilder withEcommerceAuthIndicator(String ecommerceAuthIndicator) {
        this.ecommerceAuthIndicator = ecommerceAuthIndicator;
        return this;
    }
    public ManagementBuilder withEcommerceData1(String ecommerceData1) {
        this.ecommerceData1 = ecommerceData1;
        return  this;
    }

    public ManagementBuilder withEcommerceData2(String ecommerceData2) {
        this.ecommerceData2 = ecommerceData2;
        return this;
    }
    public ManagementBuilder withEcommerceInfo(EcommerceInfo value) {
        this.ecommerceInfo = value;
        return this;
    }
    public ManagementBuilder withStoredCredential(StoredCredential value) {
        this.storedCredential = value;
        return this;
    }
    public ManagementBuilder withSimulatedHostErrors(Host host, HostError... errors) {
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
    public ManagementBuilder withSurchargeAmount(BigDecimal value, CreditDebitIndicator creditDebitIndicator) {
        this.surchargeAmount = value;
        this.creditDebitIndicator = creditDebitIndicator;
        return this;
    }
    public ManagementBuilder withTagData(String value) {
        this.tagData = value;
        return this;
    }
    public ManagementBuilder withPaymentMethodUsageMode(PaymentMethodUsageMode value) {
        this.paymentMethodUsageMode = value;
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
    public ManagementBuilder withModifier(TransactionModifier value) {
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
    public ManagementBuilder withSystemTraceAuditNumber(int original) {
        return withSystemTraceAuditNumber(original, null);
    }
    public ManagementBuilder withSystemTraceAuditNumber(int original, Integer followOn) {
        systemTraceAuditNumber = original;
        followOnStan = followOn;
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
    public ManagementBuilder withEmvMaxPinEntry(String emvMaxPinEntry){
        this.emvMaxPinEntry = emvMaxPinEntry;
        return this;
    }
    public ManagementBuilder withAccountType(AccountType value) {
        this.accountType = value;
        return this;
    }
    public ManagementBuilder withReference(String value) {
        reference = value;
        return this;
    }
    public ManagementBuilder withFundsData(FundsData value) {
        fundsData = value;
        return this;
    }

    public ManagementBuilder(TransactionType type) {
        super(type, null);
    }

    public ManagementBuilder(TransactionType type, IPaymentMethod paymentMethod) {
        super(type, paymentMethod);
    }

    @Override
    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);

        if (client.supportsOpenBanking() &&
                paymentMethod instanceof TransactionReference &&
                paymentMethod.getPaymentMethodType() == PaymentMethodType.BankPayment) {
            IOpenBankingProvider obClient = ServicesContainer.getInstance().getOpenBankingClient(configName);

            if (obClient != null && obClient != client) {
                return obClient.manageOpenBanking(this);
            }
        }

        return client.manageTransaction(this);
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Edit, TransactionType.Hold, TransactionType.Release))
                // .check("paymentMethod").isClass(TransactionReference.class);
                // Validation extracted from .NET SDK to be aligned between different SDKs
                .check("paymentMethod").isNotNull();

        this.validations.of(TransactionType.Refund)
                .when("amount").isNotNull()
                .check("currency").isNotNull();

        this.validations.of(TransactionType.VerifySignature)
                .check("payerAuthenticationResponse").isNotNull()
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("orderId").isNotNull();

        // Validations extracted from .NET SDK to be aligned between different SDKs
        this.validations.of(EnumSet.of(TransactionType.TokenDelete, TransactionType.TokenUpdate))
                .check("paymentMethod").isNotNull()
                .check("paymentMethod").isInstanceOf(ITokenizable.class);

        // Validations extracted from .NET SDK to be aligned between different SDKs
        this.validations.of(EnumSet.of(TransactionType.TokenUpdate))
                .check("paymentMethod").isInstanceOf(CreditCardData.class);

        this.validations.of(TransactionType.PayByLinkUpdate)
                .check("amount").isNotNull()
                .check("payByLinkData").isNotNull()
                .check("payByLinkData").propertyOf(PaymentMethodUsageMode.class, "usageMode").isNotNull()
                .check("payByLinkData").propertyOf(Integer.class, "usageLimit").isNotNull()
                .check("payByLinkData").propertyOf(PayByLinkType.class, "type").isNotNull();

        this.validations.of(TransactionType.Reversal)
                .check("paymentMethod").isNotNull();

        this.validations.of(TransactionType.SplitFunds)
                .check("fundsData").isNotNull()
                .check("amount").isNotNull();
    }

    public ManagementBuilder withAllowDuplicates(boolean value) {
        this.allowDuplicates = value;
        return this;
    }
    public ManagementBuilder withCustomer(Customer value) {
        this.customer = value;
        return this;
    }
    public ManagementBuilder withCountry(String country) {
        this.country = country;
        return this;
    }
    public ManagementBuilder withGenerateReceipt(boolean value) {
        this.generateReceipt = value;
        return this;
    }
    public ManagementBuilder withVoucherEntryData(EBTVoucherEntryData data) {
        this.voucherEntryData = data;
        return this;
    }
    public ManagementBuilder WithEWICIssuingEntity(String value) {
        eWICIssuingEntity = value;
        return this;
    }
}