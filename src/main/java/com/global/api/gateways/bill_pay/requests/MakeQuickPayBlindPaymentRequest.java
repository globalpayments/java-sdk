package com.global.api.gateways.bill_pay.requests;

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
import java.math.BigDecimal;

public class MakeQuickPayBlindPaymentRequest extends BillPayRequestBase{
    protected static final String SOAPENV_BODY= "soapenv:Body";
    protected static final String BDMS_BILLTRANSACTIONS= "bdms:BillTransactions";
    protected static final String BDMS_BILLTRANSACTION= "bdms:BillTransaction";
    protected static final String BDMS_AMOUNTTOAPPLYTOBILL= "bdms:AmountToApplyToBill";
    protected static final String BDMS_ENDUSERBROWSERTYPE= "bdms:EndUserBrowserType";
    protected static final String BDMS_ENDUSERIPADDRESS= "bdms:EndUserIPAddress";
    protected static final String BDMS_ORDERID= "bdms:OrderID";

    public MakeQuickPayBlindPaymentRequest(ElementTree et) {
        super(et);
    }

    protected String getMethodElementTagName() {
        return "bil:MakeQuickPayBlindPayment";
    }

    protected String getRequestElementTagName() {
        return "bil:request";
    }

    public String build(Element envelope, AuthorizationBuilder builder, Credentials credentials) throws UnsupportedTransactionException, BuilderException {
        Element body = et.subElement(envelope, SOAPENV_BODY);
        Element methodElement = et.subElement(body, getMethodElementTagName());
        Element requestElement = et.subElement(methodElement, getRequestElementTagName());

        boolean hasToken = hasToken(builder);
        boolean hasCardData = hasCardData(builder);
        boolean hasACHData = hasACHData(builder);

        BigDecimal amount = builder.getAmount() != null ? builder.getAmount() : new BigDecimal(0);

        if (!hasToken && !hasCardData && !hasACHData) {
            throw new UnsupportedTransactionException("Payment method not accepted");
        }

        validateTransaction(builder);

        buildCredentials(requestElement, credentials);

        Element billTransactions = et.subElement(requestElement, BDMS_BILLTRANSACTIONS);
        buildBillTransactions(billTransactions, builder.getBills(), BDMS_BILLTRANSACTION, BDMS_AMOUNTTOAPPLYTOBILL);

        // QuickPayACHAccountToCharge
        if (builder.getPaymentMethod() instanceof eCheck){
            if (!StringUtils.isNullOrEmpty(((eCheck) builder.getPaymentMethod()).getToken())) {
                buildQuickPayACHAccountToCharge(requestElement, (eCheck) builder.getPaymentMethod(), builder.getAmount(), builder.getConvenienceAmount());
            }
            else {
                throw new UnsupportedTransactionException("Quick Pay token must be provided for this transaction");
            }
        }

        // QuickPayToCharge
        if (builder.getPaymentMethod() instanceof CreditCardData) {
            if (hasToken) {
                buildQuickPayCardToCharge(requestElement, (CreditCardData) builder.getPaymentMethod(), amount, builder.getConvenienceAmount(), builder.getBillingAddress());
            }
            else {
                throw new UnsupportedTransactionException("Quick Pay token must be provided for this transaction");
            }
        }

        et.subElement(requestElement, BDMS_ENDUSERBROWSERTYPE, browserType);
        et.subElement(requestElement, BDMS_ENDUSERIPADDRESS, builder.getCustomerIpAddress());
        et.subElement(requestElement, BDMS_ORDERID, builder.getOrderId());

        buildTransaction(requestElement, builder);

        return et.toString(envelope);
    }

    private static boolean hasToken(AuthorizationBuilder builder) {
        return builder.getPaymentMethod() instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builder.getPaymentMethod()).getToken());
    }

    private static boolean hasCardData(AuthorizationBuilder builder) {
        return builder.getPaymentMethod() instanceof ICardData && !StringUtils.isNullOrEmpty(((ICardData) builder.getPaymentMethod()).getNumber());
    }

    private static boolean hasACHData(AuthorizationBuilder builder) {
        return builder.getPaymentMethod() instanceof eCheck && !StringUtils.isNullOrEmpty(((eCheck) builder.getPaymentMethod()).getAccountNumber());
    }
}

