package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.Entities.Lodging;
import com.global.api.terminals.upa.Entities.Enums.UpaCardTypeFilter;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.EnumSet;

@Getter
public class TerminalAuthBuilder extends TerminalBuilder<TerminalAuthBuilder> {
    private Address address;
    private boolean allowDuplicates;
    protected BigDecimal amount;
    private String authCode;
    private AutoSubstantiation autoSubstantiation;
    private StoredCredentialInitiator cardBrandStorage;
    private String cardBrandTransactionId;
    private BigDecimal cashBackAmount;
    private boolean commercialRequest;
    private CurrencyType currency;
    private String customerCode;
    private BigDecimal gratuity;
    private String invoiceNumber;
    private String poNumber;
    private boolean requestMultiUseToken;
    private boolean signatureCapture;
    private BigDecimal taxAmount;
    private String taxExempt;
    private String taxExemptId;
    private Integer tokenRequest;
    private String tokenValue;
    private boolean processCPC;
    private String transactionId;
    private boolean allowPartialAuth;
    private StoredCredentialInitiator storedCredentialInitiator;
    private String clientTransactionId;
    private TransactionType giftTransactionType;
    private String directMarketInvoiceNumber;
    private Integer directMarketShipMonth;
    private Integer directMarketShipDay;
    private Lodging lodging;
    private BigDecimal preAuthAmount;
    private EnumSet<UpaCardTypeFilter> cardTypeFilter;
    private String cardBrandTransId;
    private DateTime shippingDate;
	private boolean requireSecurityCode = true;

    public String getAuthCode() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getAuthCode();
        return null;
    }
    public String getTransactionId() {
        if(paymentMethod instanceof TransactionReference) {
            return ((TransactionReference) paymentMethod).getTransactionId();
        }
        return transactionId;
    }
    public String getTokenValue() {
        if (paymentMethod instanceof CreditCardData) {
            return ((CreditCardData)paymentMethod).getToken();
            }
        return null;
    }
    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = amount;

        // add tip
        if(gratuity != null) {
            totalAmount = totalAmount.add(gratuity);
        }

        // add tax
        if(taxAmount != null) {
            totalAmount = totalAmount.add(taxAmount);
        }

        // add surcharge
        if(surchargeAmount != null) {
            totalAmount = totalAmount.add(surchargeAmount);
        }

        return totalAmount;
    }

    public TerminalAuthBuilder withTokenRequest(Integer tokenRequest) {
        this.tokenRequest = tokenRequest;
        return this;
    }
    public TerminalAuthBuilder withTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
        return this;
    }
    public TerminalAuthBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }
    public TerminalAuthBuilder withAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        return this;
    }
    public TerminalAuthBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    public TerminalAuthBuilder withAuthCode(String value) {
        if (paymentMethod == null || !(paymentMethod instanceof TransactionReference))
            paymentMethod = new TransactionReference();
        ((TransactionReference)paymentMethod).setAuthCode(value);
        this.authCode = value;
        return this;
    }
    public TerminalAuthBuilder withAutoSubstantiation(AutoSubstantiation healthcare) {
        this.autoSubstantiation = healthcare;
        return this;
    }
    public TerminalAuthBuilder withCashBack(BigDecimal value) {
        this.cashBackAmount = value;
        return this;
    }
    public TerminalAuthBuilder withCardBrandStorage(StoredCredentialInitiator value) {
        this.cardBrandStorage = value;
        return this;
    }
    public TerminalAuthBuilder withCardBrandStorage(StoredCredentialInitiator initiatorValue, String cardBrandTransId) {
        this.cardBrandStorage = initiatorValue;
        this.cardBrandTransactionId = cardBrandTransId;
        return this;
    }
    public TerminalAuthBuilder withCommercialRequest(boolean value) {
        this.commercialRequest = value;
        return this;
    }
    public TerminalAuthBuilder withCurrency(CurrencyType value) {
        this.currency = value;
        return this;
    }
    public TerminalAuthBuilder withCustomerCode(String value) {
        this.customerCode = value;
        return this;
    }
    public TerminalAuthBuilder withGratuity(BigDecimal gratuity) {
        this.gratuity = gratuity;
        return this;
    }
    public TerminalAuthBuilder withInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }
    public TerminalAuthBuilder withPaymentMethod(IPaymentMethod method) {
        paymentMethod = method;
        return this;
    }
    public TerminalAuthBuilder withPoNumber(String value) {
        this.poNumber = value;
        return this;
    }
    public TerminalAuthBuilder withRequestMultiUseToken(boolean requestMultiUseToken) {
        this.requestMultiUseToken = requestMultiUseToken;
        return this;
    }
    public TerminalAuthBuilder withSignatureCapture(boolean signatureCapture) {
        this.signatureCapture = signatureCapture;
        return this;
    }
    public TerminalAuthBuilder withTaxAmount(BigDecimal value) {
        this.taxAmount = value;
        return this;
    }
    public TerminalAuthBuilder withTaxType(TaxType value) {
        return withTaxType(value, null);
    }
    public TerminalAuthBuilder withTaxType(TaxType value, String taxExemptId) {
        this.taxExempt = (value.equals(TaxType.TaxExempt)) ? "1" : "0";
        this.taxExemptId = taxExemptId;
        return this;
    }
    public TerminalAuthBuilder withToken(String value) {
        if (paymentMethod == null || !(paymentMethod instanceof CreditCardData))
            paymentMethod = new CreditCardData();
        ((CreditCardData)paymentMethod).setToken(value);
        return this;
    }
    public TerminalAuthBuilder withTransactionId(String value) {
        if (paymentMethod == null || !(paymentMethod instanceof TransactionReference))
            paymentMethod = new TransactionReference();
        ((TransactionReference)paymentMethod).setTransactionId(value);
        this.transactionId = value;
        return this;
    }
    public TerminalAuthBuilder withAllowPartialAuth(boolean value) {
        this.allowPartialAuth = value;
        return this;
    }
    public TerminalAuthBuilder withStoredCredentialInitiator(StoredCredentialInitiator value) {
        this.storedCredentialInitiator = value;
        return this;
    }
    public TerminalAuthBuilder withClientTransactionId(String value) {
        this.clientTransactionId = value;
        return this;
    }
    public TerminalAuthBuilder withCardTypeFilter(EnumSet<UpaCardTypeFilter> cardTypeFilter) {
        this.cardTypeFilter = cardTypeFilter;
        return this;
    }
    public TerminalAuthBuilder withGiftTransactionType(TransactionType value) {
        this.giftTransactionType = value;
        return this;
    }
    public TerminalAuthBuilder withDirectMarketInvoiceNumber(String directMarketInvoiceNumber) {
        this.directMarketInvoiceNumber = directMarketInvoiceNumber;
        return this;
    }
    public TerminalAuthBuilder withDirectMarketShipMonth(Integer directMarketShipMonth) {
        this.directMarketShipMonth = directMarketShipMonth;
        return this;
    }
    public TerminalAuthBuilder withDirectMarketShipDay(Integer directMarketShipDay) {
        this.directMarketShipDay = directMarketShipDay;
        return this;
    }
    public TerminalAuthBuilder withLodging(Lodging lodging){
        this.lodging = lodging;
        return this;
    }
    public TerminalAuthBuilder withPreAuthAmount(BigDecimal preAuthAmount){
        this.preAuthAmount = preAuthAmount;
        return this;
    }
    public TerminalAuthBuilder withProcessCPC(boolean processCPC) {
        this.processCPC = processCPC;
        return this;
    }
    public TerminalAuthBuilder withPaymentMethodType(PaymentMethodType value) {
        this.paymentMethodType = value;
        return this;
    }
    public TerminalAuthBuilder withRequireSecurityCode(boolean requireSecurityCode) {
        this.requireSecurityCode = requireSecurityCode;
        return this;
    }
    public TerminalAuthBuilder withCardBrandTransId(String value) {
        cardBrandTransId = value;
        return this;
    }
    public TerminalAuthBuilder withShippingDate(DateTime value) {
        shippingDate = value;
        return this;
    }

    public TerminalAuthBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, paymentType);
    }
    public TerminalAuthBuilder(TransactionType type) {
        super(type, null);
    }

    public TerminalResponse execute(String configName) throws ApiException {
        super.execute(configName);

        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.processTransaction(this);
    }

    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Sale, TransactionType.Auth)).check("amount").isNotNull();
        this.validations.of(TransactionType.Refund).check("amount").isNotNull();
        this.validations.of(TransactionType.Auth)
                .with(PaymentMethodType.Credit)
                .when("transactionId").isNotNull();
//                .check("authCode").isNotNull();
        this.validations.of(TransactionType.Refund)
                .with(PaymentMethodType.Credit)
                .when("transactionId").isNotNull();
//                .check("authCode").isNotNull();
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
        this.validations.of(TransactionType.AddValue).check("amount").isNotNull();

        this.validations.of(PaymentMethodType.EBT).with(TransactionType.Balance)
                .when("currency").isNotNull()
                .check("currency").isNotEqual(CurrencyType.Voucher);
        this.validations.of(TransactionType.BenefitWithdrawal)
                .when("currency").isNotNull()
                .check("currency").isEqualTo(CurrencyType.CashBenefits);
        this.validations.of(PaymentMethodType.EBT).with(TransactionType.Refund).check("allowDuplicates").isEqualTo(false);
        this.validations.of(PaymentMethodType.EBT).with(TransactionType.BenefitWithdrawal).check("allowDuplicates").isEqualTo(false);
    }
}
