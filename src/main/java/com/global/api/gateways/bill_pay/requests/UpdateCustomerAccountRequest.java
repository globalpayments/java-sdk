package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class UpdateCustomerAccountRequest extends BillPayRequestBase {
    public UpdateCustomerAccountRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, RecurringPaymentMethod paymentMethod) throws UnsupportedTransactionException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:UpdateCustomerAccount");
        Element requestElement = et.subElement(methodElement, "bil:UpdateCustomerAccountRequest");

        buildCredentials(requestElement, credentials);

        String bankName = "";
        int expMonth = 0;
        int expYear = 0;

        if (paymentMethod.getPaymentMethod() instanceof eCheck) {
            eCheck check = (eCheck) paymentMethod.getPaymentMethod();
            et.subElement(requestElement, "bdms:ACHAccountType", getDepositType(check.getCheckType()));
            et.subElement(requestElement, "bdms:ACHDepositType", getACHAccountType(check.getAccountType()));
            bankName = check.getBankName();
        }

        Element accountHolderElement = et.subElement(requestElement, "bdms:AccountHolderData");
        buildAccountHolderData(accountHolderElement, paymentMethod.getAddress(), paymentMethod.getNameOnAccount());

        if (paymentMethod.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData credit = (CreditCardData) paymentMethod.getPaymentMethod();
            expMonth = credit.getExpMonth();
            expYear = credit.getExpYear();
            bankName = credit.getBankName();
        }

        if (StringUtils.isNullOrEmpty(bankName)) {
            // Need to explicity set the empty value
            et.subElement(requestElement, "bdms:BankName");
        } else {
            et.subElement(requestElement, "bdms:BankName", bankName);
        }

        if (expMonth > 0) {
            et.subElement(requestElement, "bdms:ExpirationMonth", expMonth);
        }

        if (expYear > 0) {
            et.subElement(requestElement, "bdms:ExpirationYear", expYear);
        }
        
        et.subElement(requestElement, "bdms:IsCustomerDefaultAccount", serializeBooleanValues(paymentMethod.isPreferredPayment()));
        et.subElement(requestElement, "bdms:MerchantCustomerID", paymentMethod.getCustomerKey());
        et.subElement(requestElement, "bdms:NewCustomerAccountName", paymentMethod.getId());
        et.subElement(requestElement, "bdms:OldCustomerAccountName", paymentMethod.getId());
        et.subElement(requestElement, "bdms:PaymentMethod", getPaymentMethodType(paymentMethod.getPaymentMethod().getPaymentMethodType()));

        return et.toString(envelope);
    }
}
