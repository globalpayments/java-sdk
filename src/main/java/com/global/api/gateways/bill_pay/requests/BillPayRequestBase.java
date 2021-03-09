package com.global.api.gateways.bill_pay.requests;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.HostedPaymentData;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.BillPresentment;
import com.global.api.entities.enums.CheckType;
import com.global.api.entities.enums.EmvFallbackCondition;
import com.global.api.entities.enums.EmvLastChipRead;
import com.global.api.entities.enums.HostedPaymentType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public abstract class BillPayRequestBase {
    private int version = 3092;
    private int applicationId = 3;
    protected String browserType = "Java SDK";
    protected final ElementTree et;

    public BillPayRequestBase(ElementTree et) {
        this.et = et;
    }

    /// <summary>
    /// Builds the credentials element
    /// </summary>
    /// <param name="parent">The element to add children elements under</param>
    /// <param name="credentials">The credential object containing merchant credentials to authenticate the request</param>
    protected void buildCredentials(Element parent, Credentials credentials) {
        et.subElement(parent, "bdms:BollettaVersion", version);
        Element credential = et.subElement(parent, "bdms:Credential");
        et.subElement(credential, "bdms:ApiKey", credentials.getApiKey());
        et.subElement(credential, "bdms:ApplicationID", applicationId);
        et.subElement(credential, "bdms:Password", credentials.getPassword());
        et.subElement(credential, "bdms:UserName", credentials.getUserName());
        et.subElement(credential, "bdms:MerchantName", credentials.getMerchantName());
    }

    /// <summary>
    /// Builds the ACH Account section of the request
    /// </summary>
    /// <param name="parent"></param>
    /// <param name="eCheck"></param>
    /// <param name="amountToCharge"></param>
    /// <param name="feeAmount"></param>
    protected void buildACHAccount(Element parent, eCheck eCheck, BigDecimal amountToCharge) throws UnsupportedTransactionException {
        buildACHAccount(parent, eCheck, amountToCharge, null);
    }
    protected void buildACHAccount(
        Element parent,
        eCheck eCheck,
        BigDecimal amountToCharge,
        BigDecimal feeAmount
    ) throws UnsupportedTransactionException {
        Element achAccounts = et.subElement(parent, "bdms:ACHAccountsToCharge");
        Element achAccount = et.subElement(achAccounts, "bdms:ACHAccountToCharge");
        et.subElement(achAccount, "bdms:Amount", amountToCharge);
        et.subElement(achAccount, "bdms:ExpectedFeeAmount", feeAmount == null ? new BigDecimal(0) : feeAmount);
        // PLACEHOLDER: ACHReturnEmailAddress
        et.subElement(achAccount, "bdms:ACHStandardEntryClass", eCheck.getSecCode());
        et.subElement(achAccount, "bdms:AccountNumber", eCheck.getAccountNumber());
        if (eCheck.getCheckType() != null) {
            et.subElement(achAccount, "bdms:AccountType", getDepositType(eCheck.getCheckType()));
        }
        if (eCheck.getAccountType() != null) {
            et.subElement(achAccount, "bdms:DepositType", getACHAccountType(eCheck.getAccountType()));
        }
        // PLACEHOLDER: DocumentID
        // PLACEHOLDER: InternalAccountNumber
        et.subElement(achAccount, "bdms:PayorName", eCheck.getCheckHolderName());
        et.subElement(achAccount, "bdms:RoutingNumber", eCheck.getRoutingNumber());
        // PLACEHOLDER: SendEmailOnReturn
        // PLACEHOLDER: SubmitDate
        // PLACEHOLDER: TrackingNumber
    }

    /// <summary>
    /// Builds a list of BillPay Bill Transactions from a list of Bills
    /// </summary>
    /// <param name="parent"></param>
    /// <param name="bills"></param>
    protected void buildBillTransactions(Element parent, List<Bill> bills, String billLabel, String amountLabel) {
        for (Bill bill : bills) {
            Element billTransaction = et.subElement(parent, billLabel);
            et.subElement(billTransaction, "bdms:BillType", bill.getBillType());
            et.subElement(billTransaction, "bdms:ID1", bill.getIdentifier1());
            et.subElement(billTransaction, "bdms:ID2", bill.getIdentifier2());
            et.subElement(billTransaction, "bdms:ID3", bill.getIdentifier3());
            et.subElement(billTransaction, "bdms:ID4", bill.getIdentifier4());
            et.subElement(billTransaction, amountLabel, bill.getAmount());
        }
    }

    /// <summary>
    /// Builds a BillPay ClearTextCredit card from CreditCardData
    /// </summary>
    /// <param name="et"></param>
    /// <param name="parent"></param>
    /// <param name="card"></param>
    /// <param name="amountToCharge"></param>
    protected void buildClearTextCredit(Element parent, CreditCardData card, BigDecimal amountToCharge) {
        buildClearTextCredit(parent, card, amountToCharge, null, null, null, null);
    }
    protected void buildClearTextCredit(Element parent, CreditCardData card, BigDecimal amountToCharge, BigDecimal feeAmount) {
        buildClearTextCredit(parent, card, amountToCharge, feeAmount, null, null, null);
    }
    protected void buildClearTextCredit(Element parent, CreditCardData card, BigDecimal amountToCharge, BigDecimal feeAmount, EmvFallbackCondition condition) {
        buildClearTextCredit(parent, card, amountToCharge, feeAmount, condition, null, null);
    }
    protected void buildClearTextCredit(Element parent, CreditCardData card, BigDecimal amountToCharge, BigDecimal feeAmount, EmvFallbackCondition condition, EmvLastChipRead lastRead) {
        buildClearTextCredit(parent, card, amountToCharge, feeAmount, condition, lastRead, null);
    }
    protected void buildClearTextCredit(Element parent, CreditCardData card, BigDecimal amountToCharge, BigDecimal feeAmount, EmvFallbackCondition condition, EmvLastChipRead lastRead, Address address) {
        boolean isEmvFallback = condition != null && condition.equals(EmvFallbackCondition.ChipReadFailure);
        boolean isPreviousEmvFallback = lastRead != null && lastRead.equals(EmvLastChipRead.FAILED);

        Element clearTextCards = et.subElement(parent, "bdms:ClearTextCreditCardsToCharge");
        Element clearTextCard = et.subElement(clearTextCards, "bdms:ClearTextCardToCharge");
        et.subElement(clearTextCard, "bdms:Amount", amountToCharge);
        et.subElement(clearTextCard, "bdms:CardProcessingMethod", "Credit");
        et.subElement(clearTextCard, "bdms:ExpectedFeeAmount", feeAmount == null ? new BigDecimal(0) : feeAmount);

        Element clearTextCredit = et.subElement(clearTextCard, "bdms:ClearTextCreditCard");

        Element cardHolder = et.subElement(clearTextCredit, "pos:CardHolderData");
        buildAccountHolderData(cardHolder,
            address,
            card.getCardHolderName());

        et.subElement(clearTextCredit, "pos:CardNumber", card.getNumber());
        et.subElement(clearTextCredit, "pos:ExpirationMonth", card.getExpMonth());
        et.subElement(clearTextCredit, "pos:ExpirationYear", card.getExpYear());
        et.subElement(clearTextCredit, "pos:IsEmvFallback", serializeBooleanValues(isEmvFallback));
        et.subElement(clearTextCredit, "pos:PreviousEmvAlsoFallback", serializeBooleanValues(isPreviousEmvFallback));
        et.subElement(clearTextCredit, "pos:VerificationCode", card.getCvn());
    }

    /// <summary>
    /// Builds the account billing information
    /// </summary>
    /// <param name="parent">The XML element to attatch to</param>
    /// <param name="address">The billing address of the customer</param>
    /// <param name="nameOnAccount">The name on the payment account</param>
    // private void BuildAccountHolderData(Element parent, RecurringPaymentMethod recurringPaymentMethod)
    protected void buildAccountHolderData(Element parent, Address address, String nameOnAccount) {
        et.subElement(parent, "pos:NameOnCard", nameOnAccount);
        if (address != null) {
            et.subElement(parent, "pos:City", address.getCity());
            et.subElement(parent, "pos:Address", address.getStreetAddress1());
            et.subElement(parent, "pos:State", address.getState());
            et.subElement(parent, "pos:Zip", address.getPostalCode());
        }
    }

    /// <summary>
    /// Builds a BillPay token to charge from any payment method
    /// </summary>
    /// <param name="parent">The parent XML element to attatch to</param>
    /// <param name="paymentMethod">The token to pay</param>
    /// <param name="amount">The amount to charge</param>
    /// <param name="feeAmount">The expected fee amount to charge</param>
    protected void buildTokenToCharge(Element parent, IPaymentMethod paymentMethod, BigDecimal amount) {
        buildTokenToCharge(parent, paymentMethod, amount, null);
    }
    protected void buildTokenToCharge(Element parent, IPaymentMethod paymentMethod, BigDecimal amount, BigDecimal feeAmount) {
        Element tokensToCharge = et.subElement(parent, "bdms:TokensToCharge");
        Element tokenToCharge = et.subElement(tokensToCharge, "bdms:TokenToCharge");

        et.subElement(tokenToCharge, "bdms:Amount", amount);
        et.subElement(tokenToCharge, "bdms:CardProcessingMethod", getCardProcessingMethod(paymentMethod.getPaymentMethodType()));
        et.subElement(tokenToCharge, "bdms:ExpectedFeeAmount", feeAmount);
        if (paymentMethod instanceof eCheck) {
            et.subElement(tokenToCharge, "bdms:ACHStandardEntryClass", ((eCheck) paymentMethod).getSecCode());
        }
        et.subElement(tokenToCharge, "bdms:Token", ((ITokenizable) paymentMethod).getToken());
    }

    /// <summary>
    /// Builds the BillPay transaction object
    /// </summary>
    /// <param name="parent"></param>
    protected void buildTransaction(Element parent, AuthorizationBuilder builder) {
        Element transaction = et.subElement(parent, "bdms:Transaction");
        et.subElement(transaction, "bdms:Amount", builder.getAmount());
        et.subElement(transaction, "bdms:FeeAmount", builder.getConvenienceAmount());
        et.subElement(transaction, "bdms:MerchantInvoiceNumber", builder.getInvoiceNumber());
        et.subElement(transaction, "bdms:MerchantTransactionDescription", builder.getDescription());
        et.subElement(transaction, "bdms:MerchantTransactionID", builder.getClientTransactionId());

        if (builder.getCustomer() == null) {
            return;
        }

        Customer customer = builder.getCustomer();
        et.subElement(transaction, "bdms:PayorEmailAddress", customer.getEmail());
        et.subElement(transaction, "bdms:PayorFirstName", customer.getFirstName());
        et.subElement(transaction, "bdms:PayorLastName", customer.getLastName());
        et.subElement(transaction, "bdms:PayorPhoneNumber", customer.getHomePhone());

        if (customer.getAddress() == null) {
            return;
        }

        Address address = customer.getAddress();
        et.subElement(transaction, "bdms:PayorAddress", address.getStreetAddress1());
        et.subElement(transaction, "bdms:PayorCity", address.getCity());
        et.subElement(transaction, "bdms:PayorCountry", address.getCountry());
        et.subElement(transaction, "bdms:PayorPostalCode", address.getPostalCode());
        et.subElement(transaction, "bdms:PayorState", address.getState());
    }

    protected void buildCustomer(Element parent, Customer customer) {
        et.subElement(parent, "bdms:EmailAddress", customer.getEmail());
        et.subElement(parent, "bdms:FirstName", customer.getFirstName());
        et.subElement(parent, "bdms:LastName", customer.getLastName());
        et.subElement(parent, "bdms:MerchantCustomerID", customer.getId()); // Should we create the guid or throw an error?
        et.subElement(parent, "bdms:MobilePhone", customer.getMobilePhone());
        et.subElement(parent, "bdms:Phone", customer.getHomePhone());

        if (customer.getAddress() == null) {
            return;
        }

        Address address = customer.getAddress();
        et.subElement(parent, "bdms:Address", address.getStreetAddress1());
        et.subElement(parent, "bdms:City", address.getCity());
        et.subElement(parent, "bdms:Country", address.getCountry());
        et.subElement(parent, "bdms:Postal", address.getPostalCode());
        et.subElement(parent, "bdms:State", address.getState());
    }

    /// <summary>
    /// Validates that the AuthorizationBuilder is configured correctly for a Bill Payment
    /// </summary>
    protected void validateTransaction(AuthorizationBuilder builder) throws BuilderException {
        ArrayList<String> validationErrors = new ArrayList<>();

        if (builder.getBills() == null || builder.getBills().isEmpty()) {
            validationErrors.add("Bill Payments must have at least one bill to pay.");
        } else {
            BigDecimal billSum = new BigDecimal(0);
            for (Bill bill : builder.getBills()) {
                billSum = billSum.add(bill.getAmount());
            }

            if (!builder.getAmount().equals(billSum)) {
                validationErrors.add("The sum of the bill amounts must match the amount charged.");
            }
        }

        if (!builder.getCurrency().equals("USD")) {
            validationErrors.add("Bill Pay only supports currency USD.");
        }

        if (!validationErrors.isEmpty()) {
            throwBuilderException(validationErrors);
        }
    }

    protected void validateBills(List<Bill> bills) throws BuilderException {
        ArrayList<String> validationErrors = new ArrayList<>();

        if (bills == null || bills.isEmpty()) {
            validationErrors.add("At least one Bill required to Load Bills.");
        } else {
            for (Bill bill : bills) {
                int comparison = bill.getAmount().compareTo(new BigDecimal(0));
                if (comparison <= 0) {
                    validationErrors.add("Bills require an amount greater than zero.");
                    break;
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throwBuilderException(validationErrors);
        }
    }

    protected void validateReversal(ManagementBuilder builder) throws BuilderException {
        ArrayList<String> validationErrors = new ArrayList<>();

        if (!(builder.getPaymentMethod() instanceof TransactionReference) || StringUtils.isNullOrEmpty(((TransactionReference) builder.getPaymentMethod()).getTransactionId())) {
            validationErrors.add("A transaction to reverse must be provided.");
        } else {
            if (Integer.parseInt(((TransactionReference) builder.getPaymentMethod()).getTransactionId()) < 1) {
                validationErrors.add("The transaction id to reverse must be a positive integer.");
            }
        }

        if (builder.getBills() != null && !builder.getBills().isEmpty()) {
            BigDecimal billSum = new BigDecimal(0);
            for (Bill bill : builder.getBills()) {
                billSum = billSum.add(bill.getAmount());
            }

            if (!builder.getAmount().equals(billSum)) {
                validationErrors.add("The sum of the bill amounts must match the amount to reverse.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throwBuilderException(validationErrors);
        }
    }

    protected void validateLoadSecurePay(HostedPaymentData hostedPaymentData) throws BuilderException {
        ArrayList<String> validationErrors = new ArrayList<>();

        if (hostedPaymentData == null) {
            validationErrors.add("HostedPaymentData Required");
        } else {
            if (hostedPaymentData.getBills() == null || hostedPaymentData.getBills().isEmpty()) {
                validationErrors.add("At least one Bill required to Load Bills.");
            } else {
                for (Bill bill : hostedPaymentData.getBills()) {
                    int comparison = bill.getAmount().compareTo(new BigDecimal(0));
                    if (comparison <= 0) {
                        validationErrors.add("Bills require an amount greater than zero.");
                        break;
                    }
                }
            }

            if (hostedPaymentData.getHostedPaymentType() == null || hostedPaymentData.getHostedPaymentType().equals(HostedPaymentType.NONE)) {
                validationErrors.add("You must set a valid HostedPaymentType.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throwBuilderException(validationErrors);
        }
    }

    protected String getDateFormatted(Date date) {
        // Override format/parse methods to handle differences in `X` and `Z` format identifiers
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ") {
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
                StringBuffer rfcFormat = super.format(date, toAppendTo, pos);
                return rfcFormat.insert(rfcFormat.length() - 2, ":");
            }

            @Override
            public Date parse(String text, ParsePosition pos) {
                if (text.length() > 3) {
                    text = text.substring(0, text.length() - 3) + text.substring(text.length() - 2);
                }
                return super.parse(text, pos);
            }
        };

        return dateFormat.format(date).replace("::", ":");
    }

    /// <summary>
    /// These methods are here to convert SDK enums
    /// Into values that BillPay will recognize
    /// </summary>
    /// <param name="account"></param>
    /// <returns></returns>
    protected String getBillPresentmentType(BillPresentment billPresentment) throws UnsupportedTransactionException {
        switch (billPresentment) {
            case FULL:
                return "Full";
            default:
                throw new UnsupportedTransactionException(String.format("Bill Presentment Type of %s is not supported", billPresentment.getValue()));
        }
    }

    protected String getDepositType(CheckType deposit) throws UnsupportedTransactionException {
        switch (deposit) {
            case Business:
                return "Business";
            case Personal:
                return "Personal";
            case Payroll:
            default:
                throw new UnsupportedTransactionException(String.format("eCheck Deposit Type of %s is not supported.", deposit.getValue()));
        }
    }

    protected String getACHAccountType(AccountType accountType) throws UnsupportedTransactionException {
        switch (accountType) {
            case Checking:
                return "Checking";
            case Savings:
                return "Savings";
            default:
                throw new UnsupportedTransactionException(String.format("eCheck Account Type of %s is not supported", accountType.getValue()));
        }
    }

    protected String getCardProcessingMethod(PaymentMethodType paymentMethodType) {
        switch (paymentMethodType) {
            case Credit:
                return "Credit";
            case Debit:
                return "Debit";
            // Need to differentiate PINDebit
            default:
                return "Unassigned";
        }
    }

    protected String getPaymentMethodType(PaymentMethodType paymentMethodType) throws UnsupportedTransactionException {
        switch (paymentMethodType) {
            case Credit:
                return "Credit";
            case Debit:
                return "Debit";
            case ACH:
                return "ACH";
            default:
                throw new UnsupportedTransactionException();
        }
    }

    protected String serializeBooleanValues(boolean value) {
        return value ? "true" : "false";
    }

    protected void throwBuilderException(ArrayList<String> messages) throws BuilderException {
        StringBuilder messageBuilder = new StringBuilder();

        for (String m:  messages) {
            messageBuilder.append(m + " ");
        }

        throw new BuilderException(messageBuilder.toString().trim());
    }
}
