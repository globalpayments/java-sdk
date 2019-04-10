package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;

import java.awt.font.MultipleMaster;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public class AuthorizationBuilder extends TransactionBuilder<Transaction> {
    private AccountType accountType;
    private String alias;
    private AliasAction aliasAction;
    private boolean allowDuplicates;
    private boolean allowPartialAuth;
    private BigDecimal amount;
    private BigDecimal authAmount;
    private AutoSubstantiation autoSubstantiation;
    private InquiryType balanceInquiryType;
    private Address billingAddress;
    private BigDecimal cashBackAmount;
    private String clientTransactionId;
    private BigDecimal convinienceAmount;
    private String currency;
    private String customerId;
    private Customer customerData;
    private ArrayList<String[]> customData;
    private String customerIpAddress;
    private String cvn;
    private DccRateData dccRateData;
    private DccProcessor dccProcessor;
    private DccRateType dccRateType;
    private String dccType;
    private String description;
    private DecisionManager decisionManager;
    private String dynamicDescriptor;
    private EcommerceInfo ecommerceInfo;
    private EmvChipCondition emvChipCondition;
    private FraudFilterMode fraudFilterMode;
    private BigDecimal gratuity;
    private HostedPaymentData hostedPaymentData;
    private String invoiceNumber;
    private boolean level2Request;
    private LodgingData lodgingData;
    private String messageAuthenticationCode;
    private boolean multiCapture;
    private String offlineAuthCode;
    private boolean oneTimePayment;
    private String orderId;
    private String posSequenceNumber;
    private String productId;
    private ArrayList<String[]> productData;
    private RecurringSequence recurringSequence;
    private RecurringType recurringType;
    private boolean requestMultiUseToken;
    private GiftCard replacementCard;
    private ReversalReasonCode reversalReasonCode;
    private String scheduleId;
    private Address shippingAddress;
    private BigDecimal shippingAmount;
    private MultiValuedMap<String, String[]> supplementaryData;
    private String tagData;
    private String timestamp;

    public String getAlias() {
        return alias;
    }
    public AliasAction getAliasAction() {
        return aliasAction;
    }
    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }
    public boolean isAllowPartialAuth() {
        return allowPartialAuth;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public BigDecimal getAuthAmount() {
        return authAmount;
    }
    public AutoSubstantiation getAutoSubstantiation() {
        return autoSubstantiation;
    }
    public InquiryType getBalanceInquiryType() {
        return balanceInquiryType;
    }
    public Address getBillingAddress() {
        return billingAddress;
    }
    public BigDecimal getCashBackAmount() {
        return cashBackAmount;
    }
    public String getClientTransactionId() {
        return clientTransactionId;
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
    public Customer getCustomerData() {
        return customerData;
    }
    public ArrayList<String[]> getCustomData() {
        return customData;
    }
    public String getCvn() {
        return cvn;
    }
    public DccRateData getDccRateData() {
        return dccRateData;
    }
    public DccProcessor getDccProcessor() {
        return dccProcessor;
    }
    public DccRateType getDccRateType() {
        return dccRateType;
    }
    public String getDccType() {
        return dccType;
    }
    public String getDescription() {
        return description;
    }
    public DecisionManager getDecisionManager() {
        return decisionManager;
    }
    public String getDynamicDescriptor() {
        return dynamicDescriptor;
    }
    public EcommerceInfo getEcommerceInfo() {
        return ecommerceInfo;
    }
    public FraudFilterMode getFraudFilterMode() {
        return fraudFilterMode;
    }
    public BigDecimal getGratuity() {
        return gratuity;
    }
    public HostedPaymentData getHostedPaymentData() {
        return hostedPaymentData;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public boolean isLevel2Request() {
        return level2Request;
    }
    public LodgingData getLodgingData() {
        return lodgingData;
    }
    public String getOfflineAuthCode() {
        return offlineAuthCode;
    }
    public boolean isOneTimePayment() {
        return oneTimePayment;
    }
    public String getOrderId() {
        return orderId;
    }
    public String getProductId() {
        return productId;
    }
    public boolean isRequestMultiUseToken() {
        return requestMultiUseToken;
    }
    public RecurringSequence getRecurringSequence() { return recurringSequence; }
    public RecurringType getRecurringType() {
        return recurringType;
    }
    public GiftCard getReplacementCard() {
        return replacementCard;
    }
    public ReversalReasonCode getReversalReasonCode() {
        return reversalReasonCode;
    }
    public String getScheduleId() { return scheduleId; }
    public Address getShippingAddress() {
        return shippingAddress;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public BigDecimal getConvinienceAmount() {
        return convinienceAmount;
    }
    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }
    public MultiValuedMap<String, String[]> getSupplementaryData() {
        return supplementaryData;
    }
    public AccountType getAccountType() {
        return accountType;
    }
    public EmvChipCondition getEmvChipCondition() {
        return emvChipCondition;
    }
    public String getMessageAuthenticationCode() {
        return messageAuthenticationCode;
    }
    public boolean isMultiCapture() {
        return multiCapture;
    }
    public String getPosSequenceNumber() {
        return posSequenceNumber;
    }
    public ArrayList<String[]> getProductData() {
        return productData;
    }
    public String getTagData() {
        return tagData;
    }

    public AuthorizationBuilder withAccountType(AccountType value) {
        this.accountType = value;
        return this;
    }
    public AuthorizationBuilder withAddress(Address value) {
        return withAddress(value, AddressType.Billing);
    }
    public AuthorizationBuilder withAddress(Address value, AddressType type) {
        if(value != null) {
            value.setType(type);
            if (type == AddressType.Billing)
                this.billingAddress = value;
            else this.shippingAddress = value;
        }
        return this;
    }
    public AuthorizationBuilder withAlias(AliasAction action, String value) {
        this.alias = value;
        this.aliasAction = action;
        return this;
    }
    public AuthorizationBuilder withAllowDuplicates(boolean value) {
        this.allowDuplicates = value;
        return this;
    }
    public AuthorizationBuilder withAllowPartialAuth(boolean value) {
        this.allowPartialAuth = value;
        return this;
    }
    public AuthorizationBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }
    public AuthorizationBuilder withAuthAmount(BigDecimal value) {
        this.authAmount = value;
        return this;
    }
    public AuthorizationBuilder withAutoSubstantiation(AutoSubstantiation value) {
        this.autoSubstantiation = value;
        return this;
    }
    public AuthorizationBuilder withBalanceInquiryType(InquiryType value) {
        this.balanceInquiryType = value;
        return this;
    }
    public AuthorizationBuilder withCashBack(BigDecimal value) {
        this.cashBackAmount = value;
        this.transactionModifier = TransactionModifier.CashBack;
        return this;
    }
    public AuthorizationBuilder withChipCondition(EmvChipCondition value) {
        this.emvChipCondition = value;
        return this;
    }
    public AuthorizationBuilder withClientTransactionId(String value) {
        if(transactionType == TransactionType.Reversal || transactionType == TransactionType.Refund) {
            if(paymentMethod instanceof TransactionReference)
                ((TransactionReference)paymentMethod).setClientTransactionId(value);
            else {
                TransactionReference ref = new TransactionReference();
                ref.setClientTransactionId(value);
                if(this.paymentMethod != null) {
                    ref.setPaymentMethodType(this.paymentMethod.getPaymentMethodType());
                }

                this.paymentMethod = ref;
            }
        }
        else clientTransactionId = value;
        return this;
    }
    public AuthorizationBuilder withCommercialRequest(boolean value) {
        this.level2Request = value;
        return this;
    }
    public AuthorizationBuilder withConvenienceAmt(BigDecimal value) {
        this.convinienceAmount = value;
        return this;
    }
    public AuthorizationBuilder withCurrency(String value) {
        this.currency = value;
        return this;
    }
    public AuthorizationBuilder withCustomerId(String value) {
        this.customerId = value;
        return this;
    }
    public AuthorizationBuilder withCustomerIpAddress(String value) {
        this.customerIpAddress = value;
        return this;
    }
    public AuthorizationBuilder withCustomerData(Customer value) {
        this.customerData = value;
        return this;
    }
    public AuthorizationBuilder withCustomData(String... value) {
        if (this.customData == null) {
            customData = new ArrayList<String[]>();
        }
        this.customData.add(value);
        return this;
    }
    public AuthorizationBuilder withCvn(String value) {
        this.cvn = value;
        return this;
    }
    public AuthorizationBuilder withDccRateData(DccRateData value) {
        this.dccRateData = value;
        return this;
    }
    public AuthorizationBuilder withDccProcessor(DccProcessor value) {
        this.dccProcessor = value;
        return this;
    }
    public AuthorizationBuilder withDccRateType(DccRateType value) {
        this.dccRateType = value;
        return this;
    }
    public AuthorizationBuilder withDccType(String value) {
        this.dccType = value;
        return this;
    }
    public AuthorizationBuilder withDescription(String value) {
        this.description = value;
        return this;
    }
    public AuthorizationBuilder withDecisionManager(DecisionManager value) {
        this.decisionManager = value;
        return this;
    }
    public AuthorizationBuilder withDynamicDescriptor(String value) {
        this.dynamicDescriptor = value;
        return this;
    }
    public AuthorizationBuilder withEcommerceInfo(EcommerceInfo value) {
        this.ecommerceInfo = value;
        return this;
    }
    public AuthorizationBuilder withFraudFilter(FraudFilterMode value) {
        this.fraudFilterMode = value;
        return this;
    }
    public AuthorizationBuilder withGratuity(BigDecimal value) {
        this.gratuity = value;
        return this;
    }
    public AuthorizationBuilder withHostedPaymentData(HostedPaymentData value) throws ApiException {
        this.hostedPaymentData = value;
        return this;
    }
    public AuthorizationBuilder withInvoiceNumber(String value) {
        this.invoiceNumber = value;
        return this;
    }
    public AuthorizationBuilder withLodgingData(LodgingData value) {
        this.lodgingData = value;
        return this;
    }
    public AuthorizationBuilder withMessageAuthenticationCode(String value) {
        this.messageAuthenticationCode = value;
        return this;
    }
    public AuthorizationBuilder withMultiCapture(Boolean value) {
        this.multiCapture = value;
        return this;
    }
    public AuthorizationBuilder withOfflineAuthCode(String value) {
        this.offlineAuthCode = value;
        this.transactionModifier = TransactionModifier.Offline;
        return this;
    }
    public AuthorizationBuilder withOneTimePayment(boolean value) {
        this.oneTimePayment = value;
        this.transactionModifier = TransactionModifier.Recurring;
        return this;
    }
    public AuthorizationBuilder withOrderId(String value) {
        this.orderId = value;
        return this;
    }
    public AuthorizationBuilder withPosSequenceNumber(String value) {
        this.posSequenceNumber = value;
        return this;
    }
    public AuthorizationBuilder withProductData(String ... value) {
        if (this.productData == null) {
            productData = new ArrayList<String[]>();
        }
        this.productData.add(value);
        return this;
    }
    public AuthorizationBuilder withProductId(String value) {
        this.productId = value;
        return this;
    }
    public AuthorizationBuilder withPaymentMethod(IPaymentMethod value) {
        this.paymentMethod = value;
        if (value instanceof EBTCardData && ((EBTCardData)value).getSerialNumber() != null)
            this.transactionModifier = TransactionModifier.Voucher;
        if (value instanceof CreditCardData && ((CreditCardData) value).getMobileType() != null)
            this.transactionModifier = TransactionModifier.EncryptedMobile;
        return this;
    }
    public AuthorizationBuilder withRecurringInfo(RecurringType type, RecurringSequence sequence) {
        this.recurringType = type;
        this.recurringSequence = sequence;
        return this;
    }
    public AuthorizationBuilder withRequestMultiUseToken(boolean value) {
        this.requestMultiUseToken = value;
        return this;
    }
    public AuthorizationBuilder withReplacementCard(GiftCard value) {
        this.replacementCard = value;
        return this;
    }
    public AuthorizationBuilder withReversalReasonCode(ReversalReasonCode value) {
        this.reversalReasonCode = value;
        return this;
    }
    public AuthorizationBuilder withTransactionId(String value) {
        if(paymentMethod instanceof TransactionReference) {
            ((TransactionReference)paymentMethod).setTransactionId(value);
        }
        else {
            TransactionReference ref = new TransactionReference();
            ref.setTransactionId(value);
            this.paymentMethod = ref;
        }
        return this;
    }
    public AuthorizationBuilder withModifier(TransactionModifier value) {
        this.transactionModifier = value;
        return this;
    }
    public AuthorizationBuilder withScheduleId(String value) {
        this.scheduleId = value;
        return this;
    }
    public AuthorizationBuilder withShippingAmt(BigDecimal value) {
        this.shippingAmount = value;
        return this;
    }
    public AuthorizationBuilder withSupplementaryData(String type, String... value) {
        if (this.supplementaryData == null) {
            supplementaryData = new ArrayListValuedHashMap<String, String[]>();
        }
        this.supplementaryData.put(type, value);
        return this;
    }
    public AuthorizationBuilder withTimestamp(String value) {
        this.timestamp = value;
        return this;
    }
    public AuthorizationBuilder withTagData(String value) {
        this.tagData = value;
        return this;
    }

    public AuthorizationBuilder(TransactionType type) {
        this(type, null);
    }
    public AuthorizationBuilder(TransactionType type, IPaymentMethod paymentMethod) {
        super(type);
        withPaymentMethod(paymentMethod);
    }

    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);
        return client.processAuthorization(this);
    }

    public String serialize() throws ApiException {
        return serialize("default");
    }
    public String serialize(String configName) throws ApiException {
        transactionModifier = TransactionModifier.HostedRequest;
        super.execute(configName);

        IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);
        if(client.supportsHostedPayments())
            return client.serializeRequest(this);
        throw new UnsupportedTransactionException("Your current gateway does not support hosted payments.");
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Auth, TransactionType.Sale, TransactionType.Refund, TransactionType.AddValue))
                .with(TransactionModifier.None)
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("paymentMethod").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.Auth, TransactionType.Sale))
                .with(TransactionModifier.HostedRequest)
                .check("amount").isNotNull()
                .check("currency").isNotNull();

        this.validations.of(TransactionType.Verify)
                .with(TransactionModifier.HostedRequest)
                .check("currency").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.Auth, TransactionType.Sale))
                .with(TransactionModifier.Offline)
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("offlineAuthCode").isNotNull();

        this.validations.of(TransactionType.BenefitWithdrawal)
                .with(TransactionModifier.CashBack)
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("paymentMethod").isNotNull();

        this.validations.of(TransactionType.Balance).check("paymentMethod").isNotNull();

        this.validations.of(TransactionType.Alias)
                .check("aliasAction").isNotNull()
                .check("alias").isNotNull();

        this.validations.of(TransactionType.Replace).check("replacementCard").isNotNull();

        this.validations.of(PaymentMethodType.ACH).check("billingAddress").isNotNull();

        this.validations.of(PaymentMethodType.Debit)
                .when("reversalReasonCode").isNotNull()
                .check("transactionType").isEqualTo(TransactionType.Reversal);

        this.validations.of(EnumSet.of(PaymentMethodType.Debit, PaymentMethodType.Credit))
                .when("emvChipCondition").isNotNull().check("tagData").isNull();
        this.validations.of(EnumSet.of(PaymentMethodType.Debit, PaymentMethodType.Credit))
                .when("tagData").isNotNull().check("emvChipCondition").isNull();
        this.validations.of(EnumSet.of(TransactionType.Auth, TransactionType.Sale))
                .with(TransactionModifier.EncryptedMobile).check("paymentMethod").isNotNull();

    }
}