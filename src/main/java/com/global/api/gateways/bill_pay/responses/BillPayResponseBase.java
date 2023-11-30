package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.enums.CardType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.bill_pay.IBillPayResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

import java.util.HashMap;

public abstract class BillPayResponseBase<T> implements IBillPayResponse<T> {
    protected Element response;
    protected String responseTagName;

    public IBillPayResponse<T> withResponseTagName(String tagName) {
        this.responseTagName = tagName;
        return this;
    }

    public IBillPayResponse<T> withResponse(String response) throws ApiException {
        HashMap<String, String> namespaces = new HashMap<>();

        namespaces.put("s", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("", "https://test.heartlandpaymentservices.net/BillingDataManagement/v3/BillingDataManagementService");
        namespaces.put("a", "http://schemas.datacontract.org/2004/07/BDMS.NewModel");
        namespaces.put("i", "http://www.w3.org/2001/XMLSchema-instance");

        this.response = ElementTree.parse(response, namespaces).get(responseTagName);

        return this;
    }

    protected String getFirstResponseCode(Element response) {
        Element message = response.get("a:Messages");
        return message.getString("a:Code");
    }

    protected String getFirstResponseMessage(Element response) {
        Element message = response.get("a:Messages");
        return message.getString("a:MessageDescription");
    }

    protected PaymentMethodType getPaymentMethodType(String paymentMethod) {
        PaymentMethodType paymentMethodType = null;
        if(paymentMethod.contains("Credit"))
            paymentMethodType = PaymentMethodType.Credit;
        else if (paymentMethod.contains("Debit"))
            paymentMethodType = PaymentMethodType.Debit;
        else if (paymentMethod.contains("ACH"))
            paymentMethodType = PaymentMethodType.ACH;

        return paymentMethodType;
    }
    protected String getCardType(String cardType)
    {
        if (cardType.contains("Visa"))
            return CardType.VISA.toString();
        else if (cardType.contains("Mastercard"))
            return CardType.MC.toString();
        else if (cardType.contains("Discover"))
            return CardType.DISC.toString();
        else if (cardType.contains("AmericanExpress"))
            return CardType.AMEX.toString();
        else
            return "";
    }
}
