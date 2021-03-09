package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class CreateCustomerAccountRequest extends BillPayRequestBase {
    public CreateCustomerAccountRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, RecurringPaymentMethod paymentMethod) throws UnsupportedTransactionException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:SaveCustomerAccount");
        Element requestElement = et.subElement(methodElement, "bil:SaveCustomerAccountRequest");

        buildCredentials(requestElement, credentials);

        Element customerAccountElement = et.subElement(requestElement, "bdms:CustomerAccount");

        String accountNumber = "";
        String routingNumber = "";
        String bankName = "";
        int expMonth = 0;
        int expYear = 0;

        if (paymentMethod.getPaymentMethod() instanceof eCheck) {
            eCheck check = (eCheck) paymentMethod.getPaymentMethod();
            et.subElement(customerAccountElement, "bdms:ACHAccountType", getDepositType(check.getCheckType()));
            et.subElement(customerAccountElement, "bdms:ACHDepositType", getACHAccountType(check.getAccountType()));
            accountNumber = check.getAccountNumber();
            routingNumber = check.getRoutingNumber();
            bankName = check.getBankName();
        }

        Element accountHolder = et.subElement(customerAccountElement, "bdms:AccountHolderData");
        buildAccountHolderData(accountHolder, paymentMethod.getAddress(), paymentMethod.getNameOnAccount());

        if (paymentMethod.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData credit = (CreditCardData) paymentMethod.getPaymentMethod();
            accountNumber = credit.getNumber();
            expMonth = credit.getExpMonth();
            expYear = credit.getExpYear();
            bankName = credit.getBankName();
        }

        et.subElement(customerAccountElement, "bdms:AccountNumber", accountNumber);

        if (StringUtils.isNullOrEmpty(bankName)) {
            et.subElement(customerAccountElement, "bdms:BankName");
        } else {
            et.subElement(customerAccountElement, "bdms:BankName", bankName);
        }

        et.subElement(customerAccountElement, "bdms:CustomerAccountName", paymentMethod.getId());

        if (expMonth > 0) {
            et.subElement(customerAccountElement, "bdms:ExpirationMonth", expMonth);
        }

        if (expYear > 0) {
            et.subElement(customerAccountElement, "bdms:ExpirationYear", expYear);
        }
        et.subElement(customerAccountElement, "bdms:IsCustomerDefaultAccount", serializeBooleanValues(paymentMethod.isPreferredPayment()));
        et.subElement(customerAccountElement, "bdms:RoutingNumber", routingNumber);
        
        if (paymentMethod.getPaymentMethod() != null) {
            et.subElement(customerAccountElement, "bdms:TokenPaymentMethod", getPaymentMethodType(paymentMethod.getPaymentMethod().getPaymentMethodType()));
        }

        et.subElement(requestElement, "bdms:MerchantCustomerID", paymentMethod.getCustomerKey());
        return et.toString(envelope);
    }
}
