package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.propay.*;
import com.global.api.gateways.IProPayProvider;
import com.global.api.paymentMethods.CreditCardData;
import lombok.Getter;

import java.util.EnumSet;

@Getter
public class ProPayBuilder extends BaseBuilder<Transaction>{
    private TransactionType transactionType;
    private TransactionModifier transactionModifier;

    // Primary Bank Account Inofmation - Optional. Used to add a bank account to which funds can be settled
    private BankAccountData bankAccountData;

    // Merchant Beneficiary Owner Inofmation - Required of all merchants validating KYC based off of personal data
    private BeneficialOwnerData beneficialOwnerData;

    // User of Portico Device Ordering. Must set TimeZone property as well when ordering Portico devices
    private DeviceData deviceData;

    // Required of partners ordering Portico devices. Valid values: [ UTC, PT, MST, MT, CT, ET, HST, AT, AST, AKST, ACT, EET, EAT, MET, NET, PLT, IST, BST, VST, CTT, JST, ACT, AET, SST, NST, MIT, CNT, AGT, CAT ]
    private String timeZone;

    // Business Data - Required of business validated accounts. May also be required of personal validated accounts
    private BusinessData businessData;

    // Significant Owner Inofmation - May be required of some partners based on ProPay Risk decision
    public SignificantOwnerData significantOwnerData;

    // Threat Risk Assessment Inofmation - May be required based on ProPay Risk decision
    public ThreatRiskData threatRiskData;

    // User/Merchant Personal Data
    public UserPersonalData userPersonalData;

    public CreditCardData creditCardData;
    public BankAccountData aCHInofmation;
    public Address mailingAddressInofmation;
    public BankAccountData secondaryBankInofmation; 
    public GrossBillingInformation grossBillingInformation;
    public String negativeLimit; 
    public RenewAccountData renewalAccountData; 
    public String accountNumber; 
    public String password; 
    public AccountPermissions accountPermissions; 
    public BankAccountOwnershipData primaryBankAccountOwner; 
    public BankAccountOwnershipData secondaryBankAccountOwner; 
    public DocumentUploadData documentUploadData;
    public SSORequestData sSORequestData;
    public OrderDevice orderDevice;

    public String amount; 
    public String receivingAccountNumber; 
    public Boolean allowPending;
    public String cCAmount; 
    public Boolean requireCCRefund;
    public String transNum; 

    public String externalID; 
    public String sourceEmail; 

    public FlashFundsPaymentCardData flashFundsPaymentCardData; 

    public String gatewayTransactionId;
    public String cardBrandTransactionId;
    public String globalTransId;
    public String globalTransSource;

    public ProPayBuilder(TransactionType type){
        transactionType=type;

    }
    public ProPayBuilder(TransactionType type, TransactionModifier modifer) {
        transactionType = type;
        transactionModifier = modifer;
    }

    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);
        IProPayProvider client = ServicesContainer.getInstance().getProPay(configName);
        return client.processProPay(this);
    }

    public void SetupValidations() {
        // Account Management Methods
        this.validations.of(EnumSet.of(TransactionType.CreateAccount))
                .with(TransactionModifier.None)
                .check("beneficialOwnerData").isNotNull()
                .check("businessData").isNotNull()
                .check("userPersonalData").isNotNull()
                .check("creditCardData").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.EditAccount))
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.ResetPassword))
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.RenewAccount))
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();

        this.validations.of(EnumSet.of(TransactionType.UpdateBeneficialOwnership))
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("BeneficialOwnerData").isNotNull();

        this.validations.of(TransactionType.DisownAccount)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();

        this.validations.of(TransactionType.UploadDocumentChargeback)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("DocumentUploadData").isNotNull();

        this.validations.of(TransactionType.UploadDocument)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("DocumentUploadData").isNotNull();

        this.validations.of(TransactionType.ObtainSSOKey)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("SSORequestData").isNotNull();

        this.validations.of(TransactionType.UpdateBankAccountOwnership)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();

        // Funds Management Methods
        this.validations.of(TransactionType.AddFunds)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("Amount").isNotNull();

        this.validations.of(TransactionType.SweepFunds)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("Amount").isNotNull();

        this.validations.of(TransactionType.AddCardFlashFunds)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("FlashFundsPaymentCardData").isNotNull();

        this.validations.of(TransactionType.PushMoneyFlashFunds)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("Amount").isNotNull();

        // In-Network Transaction Methods
        this.validations.of(TransactionType.DisburseFunds)
                .with(TransactionModifier.None)
                .check("Amount").isNotNull()
                .check("ReceivingAccountNumber").isNotNull();

        this.validations.of(TransactionType.SpendBack)
                .with(TransactionModifier.None)
                .check("Amount").isNotNull()
                .check("AccountNumber").isNotNull()
                .check("ReceivingAccountNumber").isNotNull()
                .check("AllowPending").isNotNull();

        this.validations.of(TransactionType.ReverseSplitPay)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("Amount").isNotNull()
                .check("CCAmount").isNotNull()
                .check("RequireCCRefund").isNotNull()
                .check("TransNum").isNotNull();

        this.validations.of(TransactionType.SplitFunds)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull()
                .check("ReceivingAccountNumber").isNotNull()
                .check("Amount").isNotNull()
                .check("TransNum").isNotNull();

        // Get Inofmation Methods

        // of GetAccountDetails, there are "required" 3 parameter types
        // But we can only send one type per request. If we send more than one, the method will fail
        // As a result, we need to do some more thorough validation work
        // Each .when must be paired with a .check, hence the duplicate .when calls in each section below

        // check if AccountNumber is passed in (and ExternalID/SourceEmail have not)
        this.validations.of(TransactionType.GetAccountDetails)
                .with(TransactionModifier.None)
                .when("AccountNumber").isNotNull()
                .check("ExternalID").isNull()
                .when("AccountNumber").isNotNull()
                .check("SourceEmail").isNull();
        // check if ExternalID has been passed in (and AccountNumber/SourceEmail have not)
        this.validations.of(TransactionType.GetAccountDetails)
                .with(TransactionModifier.None)
                .when("ExternalID").isNotNull()
                .check("SourceEmail").isNull()
                .when("ExternalID").isNotNull()
                .check("AccountNumber").isNull();
        // check if SourceEmail has been passed in (and AccountNumber/ExternalID have not)
        this.validations.of(TransactionType.GetAccountDetails)
                .with(TransactionModifier.None)
                .when("SourceEmail").isNotNull()
                .check("ExternalID").isNull()
                .when("SourceEmail").isNotNull()
                .check("AccountNumber").isNull();

        this.validations.of(TransactionType.GetAccountDetails)
                .with(TransactionModifier.Additional)
                .check("AccountNumber").isNotNull();

        this.validations.of(TransactionType.GetAccountBalance)
                .with(TransactionModifier.None)
                .check("AccountNumber").isNotNull();
    }

    public ProPayBuilder withBankAccountData(BankAccountData bankAccountData) {
        this.bankAccountData = bankAccountData;
        return this;
    }

    public ProPayBuilder withBeneficialOwnerData(BeneficialOwnerData beneficialOwnerData) {
        this.beneficialOwnerData = beneficialOwnerData;
        return this;
    }

    public ProPayBuilder withDeviceData(DeviceData deviceData) {
        this.deviceData = deviceData;
        return this;
    }

    // Required of partners ordering Portico devices. Valid values: [ UTC, PT, MST, MT, CT, ET, HST, AT, AST, AKST, ACT, EET, EAT, MET, NET, PLT, IST, BST, VST, CTT, JST, ACT, AET, SST, NST, MIT, CNT, AGT, CAT ]
    public ProPayBuilder withTimeZone(String timezone) {
        timeZone = timezone;
        return this;
    }

    public ProPayBuilder withBusinessData(BusinessData businessData) {
        this.businessData = businessData;
        return this;
    }

    public ProPayBuilder withSignificantOwnerData(SignificantOwnerData significantOwnerData) {
        this.significantOwnerData = significantOwnerData;
        return this;
    }

    public ProPayBuilder withThreatRiskData(ThreatRiskData threatRiskData) {
        this.threatRiskData = threatRiskData;
        return this;
    }

    public ProPayBuilder withUserPersonalData(UserPersonalData userPersonalData) {
        this.userPersonalData = userPersonalData;
        return this;
    }

    public ProPayBuilder withCreditCardData(CreditCardData creditCardData) {
        this.creditCardData = creditCardData;
        return this;
    }

    public ProPayBuilder withACHData(BankAccountData achInofmation) {
        this.aCHInofmation = achInofmation;
        return this;
    }

    public ProPayBuilder withMailingAddress(Address mailingAddressInofmation) {
        this.mailingAddressInofmation = mailingAddressInofmation;
        return this;
    }

    public ProPayBuilder withSecondaryBankAccountData(BankAccountData secondaryBankInofmation) {
        this.secondaryBankInofmation = secondaryBankInofmation;
        return this;
    }

    public ProPayBuilder withGrossBillingSettleData(GrossBillingInformation grossBillingInformation) {
        this.grossBillingInformation = grossBillingInformation;
        return this;
    }

    public ProPayBuilder withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public ProPayBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public ProPayBuilder withAccountPermissions(AccountPermissions accountPermissions) {
        this.accountPermissions = accountPermissions;
        return this;
    }

    public ProPayBuilder withPrimaryBankAccountOwner(BankAccountOwnershipData primaryBankAccountOwner) {
        this.primaryBankAccountOwner = primaryBankAccountOwner;
        return this;
    }

    public ProPayBuilder withSecondaryBankAccountOwner(BankAccountOwnershipData secondaryBankAccountOwner) {
        this.secondaryBankAccountOwner = secondaryBankAccountOwner;
        return this;
    }

    public ProPayBuilder withDocumentUploadData(DocumentUploadData docUploadData) {
        this.documentUploadData = docUploadData;
        return this;
    }

    public ProPayBuilder withSSORequestData(SSORequestData ssoRequestData) {
        this.sSORequestData = ssoRequestData;
        return this;
    }

    public ProPayBuilder withNegativeLimit(String negativeLimit) {
        this.negativeLimit = negativeLimit;
        return this;
    }

    public ProPayBuilder withRenewalAccountData(RenewAccountData renewalAccountData) {
        this.renewalAccountData = renewalAccountData;
        return this;
    }

    public ProPayBuilder withAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public ProPayBuilder withFlashFundsPaymentCardData(FlashFundsPaymentCardData cardData) {
        this.flashFundsPaymentCardData = cardData;
        return this;
    }

    public ProPayBuilder withReceivingAccountNumber(String receivingAccountNumber) {
        this.receivingAccountNumber = receivingAccountNumber;
        return this;
    }

    public ProPayBuilder withAllowPending(Boolean allowPending) {
        this.allowPending = allowPending;
        return this;
    }

    public ProPayBuilder withCCAmount(String ccAmount) {
        this.cCAmount = ccAmount;
        return this;
    }

    public ProPayBuilder withRequireCCRefund(Boolean requireCCRefund) {
        this.requireCCRefund = requireCCRefund;
        return this;
    }

    public ProPayBuilder withTransNum(String transNum) {
        this.transNum = transNum;
        return this;
    }

    public ProPayBuilder withExternalID(String externalId) {
        this.externalID = externalId;
        return this;
    }

    public ProPayBuilder withSourceEmail(String sourceEmail) {
        this.sourceEmail = sourceEmail;
        return this;
    }

    public ProPayBuilder withGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
        return this;
    }

    public ProPayBuilder withCardBrandTransactionId(String cardBrandTransactionId) {
        this.cardBrandTransactionId = cardBrandTransactionId;
        return this;
    }

    public ProPayBuilder withGlobalTransId(String globalTransId) {
        this.globalTransId = globalTransId;
        return this;
    }

    public ProPayBuilder withGlobalTransSource(String globalTransSource) {
        this.globalTransSource = globalTransSource;
        return this;
    }

    public ProPayBuilder withOrderDevice(OrderDevice orderDevice) {
        this.orderDevice = orderDevice;
        return this;
    }

    @Override
    public void setupValidations() {

    }
}
