package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Customer;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.Entities.Lodging;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.EnumSet;

public class TerminalManageBuilder extends TerminalBuilder<TerminalManageBuilder> {
    protected BigDecimal amount;
    protected CurrencyType currency;
    protected BigDecimal gratuity;
    protected String transactionId;
    protected String terminalRefNumber;

    protected String messageAuthCode;
    protected String reasonCode;
    protected String trackingId;

    protected String signatureImage;
    protected String signatureFormat;
    protected Boolean signatureLine;
    protected String softDescriptor;

    protected Customer customer;

    @Getter
    protected String clientTransactionId;
    @Getter
    private Lodging lodging;
    @Getter
    private BigDecimal preAuthAmount;
    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public BigDecimal getGratuity() {
        return gratuity;
    }

    public String getTerminalRefNumber() { return terminalRefNumber; }
    public String getMessageAuthCode() { return messageAuthCode; }
    public String getReasonCode() { return reasonCode; }
    public String getSoftDescriptor() { return softDescriptor; }
    public String getTrackingId() { return trackingId; }
    public String getSignatureImage() { return signatureImage; }
    public String getSignatureFormat() { return signatureFormat; }
    public Boolean getSignatureLine() { return signatureLine; }

    public Customer getCustomer() {
        return customer;
    }
    public String getTransactionId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getTransactionId();
        return null;
    }

    public TerminalManageBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }

    public TerminalManageBuilder withCustomer(Customer value) {
        this.customer = value;
        return this;
    }

    public TerminalManageBuilder withCurrency(CurrencyType value) {
        this.currency = value;
        return this;
    }

    public TerminalManageBuilder withGratuity(BigDecimal value) {
        this.gratuity = value;
        return this;
    }

    public TerminalManageBuilder withTerminalRefNumber(String value) {
        this.terminalRefNumber = value;
        return this;
    }

    public TerminalManageBuilder withSoftDescriptor(String value) {
        this.softDescriptor = value;
        return this;
    }

    public TerminalManageBuilder withMessageAuthCode(String value) {
        this.messageAuthCode = value;
        return this;
    }
    public TerminalManageBuilder withReasonCode(String value) {
        this.reasonCode = value;
        return this;
    }
    public TerminalManageBuilder withTrackingId(String value) {
        this.trackingId = value;
        return this;
    }
    public TerminalManageBuilder withSignatureImage(String value) {
        this.signatureImage = value;
        return this;
    }
    public TerminalManageBuilder withSignatureFormat(String value) {
        this.signatureFormat = value;
        return this;
    }
    public TerminalManageBuilder withSignatureLine(Boolean value) {
        this.signatureLine = value;
        return this;
    }
    public TerminalManageBuilder withTransactionId(String value) {
        if(paymentMethod == null || !(paymentMethod instanceof TransactionReference))
            paymentMethod = new TransactionReference();
        ((TransactionReference)paymentMethod).setTransactionId(value);
        this.transactionId = value;
        return this;
    }

    public TerminalManageBuilder withClientTransactionId(String value) {
        this.clientTransactionId = value;
        return this;
    }

    public TerminalManageBuilder withLodging(Lodging lodging){
        this.lodging = lodging;
        return this;
    }

    public TerminalManageBuilder withPreAuthAmount(BigDecimal preAuthAmount){
        this.preAuthAmount = preAuthAmount;
        return this;
    }
    public TerminalManageBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, paymentType);
    }

    public TerminalResponse execute(String configName) throws ApiException {
        super.execute(configName);

        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.manageTransaction(this);
    }

    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Void)).check("transactionId").isNotNull();
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
    }
}
