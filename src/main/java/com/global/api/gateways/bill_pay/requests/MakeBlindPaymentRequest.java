package com.global.api.gateways.bill_pay.requests;

import java.math.BigDecimal;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.ICardData;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class MakeBlindPaymentRequest extends BillPayRequestBase {
    public MakeBlindPaymentRequest(ElementTree et) {
        super(et);
    }

    protected String getMethodElementTagName() {
        return "bil:MakeBlindPayment";
    }

    protected String getRequestElementTagName() {
        return "bil:MakeE3PaymentRequest";
    }

    public String build(Element envelope, AuthorizationBuilder builder, Credentials credentials) throws UnsupportedTransactionException, BuilderException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, getMethodElementTagName());
        Element requestElement = et.subElement(methodElement, getRequestElementTagName());

        boolean hasToken = (builder.getPaymentMethod() instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builder.getPaymentMethod()).getToken()));
        // Would EntryMethod.Manual be clear Swipe?
        boolean hasCardData = (builder.getPaymentMethod() instanceof ICardData && !StringUtils.isNullOrEmpty(((ICardData) builder.getPaymentMethod()).getNumber()));
        boolean hasACHData = (builder.getPaymentMethod() instanceof eCheck && !StringUtils.isNullOrEmpty(((eCheck) builder.getPaymentMethod()).getAccountNumber()));

        BigDecimal amount = builder.getAmount() != null ? builder.getAmount() : new BigDecimal(0);

        // Only allow token, card, and ACH data at thinstanceof time
        if (!hasToken && !hasCardData && !hasACHData) {
            throw new UnsupportedTransactionException("Payment method not accepted");
        }

        validateTransaction(builder);

        buildCredentials(requestElement, credentials);

        if (!hasToken && builder.getPaymentMethod() instanceof eCheck) {
            buildACHAccount(requestElement, (eCheck) builder.getPaymentMethod(), amount, builder.getConvenienceAmount());
        }

        Element billTransactions = et.subElement(requestElement, "bdms:BillTransactions");
        buildBillTransactions(billTransactions, builder.getBills(), "bdms:BillTransaction", "bdms:AmountToApplyToBill");
        // PLACEHOLDER: ClearSwipe

        // ClearTextCredit
        if (hasCardData && builder.getPaymentMethod() instanceof CreditCardData) {
            buildClearTextCredit(
                requestElement,
                (CreditCardData) builder.getPaymentMethod(),
                amount,
                builder.getConvenienceAmount(),
                builder.getEmvFallbackCondition(),
                builder.getEmvLastChipRead(),
                builder.getBillingAddress()
            );
        }

        // PLACEHOLDER: E3Credit
        // PLACEHOLDER: E3DebitWithPIN
        et.subElement(requestElement, "bdms:EndUserBrowserType", browserType);
        et.subElement(requestElement, "bdms:EndUserIPAddress", builder.getCustomerIpAddress());
        et.subElement(requestElement, "bdms:OrderID", builder.getOrderId());
        // PLACEHOLDER: PAXDevices
        // PLACEHOLDER: TimeoutInSeconds
        if (hasToken) {
            buildTokenToCharge(requestElement, builder.getPaymentMethod(), amount, builder.getConvenienceAmount());
        }

        buildTransaction(requestElement, builder);

        return et.toString(envelope);
    }
}
