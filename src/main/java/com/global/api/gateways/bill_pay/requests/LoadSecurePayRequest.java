package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.HostedPaymentData;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

public class LoadSecurePayRequest extends BillPayRequestBase {
    public LoadSecurePayRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, BillingBuilder builder, Credentials credentials) throws BuilderException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:LoadSecurePayDataExtended");
        Element requestElement = et.subElement(methodElement, "bil:request");
        HostedPaymentData hostedPaymentData = builder.getHostedPaymentData();

        validateLoadSecurePay(hostedPaymentData);

        buildCredentials(requestElement, credentials);

        Element billsElement = et.subElement(requestElement, "bdms:BillData");

        if (hostedPaymentData != null) {
            String customerIsEditable = serializeBooleanValues(hostedPaymentData.isCustomerEditable());

            for (Bill bill : hostedPaymentData.getBills())
            {
                Element billElement = et.subElement(billsElement, "bdms:SecurePayBill");

                et.subElement(billElement, "bdms:Amount", bill.getAmount());
                et.subElement(billElement, "bdms:BillTypeName", bill.getBillType());
                et.subElement(billElement, "bdms:Identifier1", bill.getIdentifier1());
                et.subElement(billElement, "bdms:Identifier2", bill.getIdentifier2());
                et.subElement(billElement, "bdms:Identifier3", bill.getIdentifier3());
                et.subElement(billElement, "bdms:Identifier4", bill.getIdentifier4());
            }

            et.subElement(requestElement, "bdms:SecurePayPaymentType_ID", hostedPaymentData.getHostedPaymentType().ordinal());
            et.subElement(requestElement, "bdms:ReturnURL", hostedPaymentData.getReturnUrl());
            et.subElement(requestElement, "bdms:CancelURL", hostedPaymentData.getCancelUrl());

            String merchantCustomerId = null;
            if (!StringUtils.isNullOrEmpty(hostedPaymentData.getCustomerKey())) {
                merchantCustomerId = hostedPaymentData.getCustomerKey();
            } else if (!StringUtils.isNullOrEmpty(hostedPaymentData.getCustomerNumber())) {
                merchantCustomerId = hostedPaymentData.getCustomerNumber();
            }

            et.subElement(requestElement, "bdms:MerchantCustomerID", merchantCustomerId);
            et.subElement(requestElement, "bdms:OrderID", builder.getOrderId());
            et.subElement(requestElement, "bdms:PayorEmailAddress", hostedPaymentData.getCustomerEmail());
            et.subElement(requestElement, "bdms:PayorEmailAddressIsEditable", customerIsEditable);
            et.subElement(requestElement, "bdms:PayorFirstName", hostedPaymentData.getCustomerFirstName());
            et.subElement(requestElement, "bdms:PayorFirstNameIsEditable", customerIsEditable);
            et.subElement(requestElement, "bdms:PayorLastName", hostedPaymentData.getCustomerLastName());
            et.subElement(requestElement, "bdms:PayorLastNameIsEditable", customerIsEditable);
            et.subElement(requestElement, "bdms:PayorMiddleNameIsEditable", customerIsEditable);
            et.subElement(requestElement, "bdms:PayorPhoneNumber", hostedPaymentData.getCustomerPhoneMobile());
            et.subElement(requestElement, "bdms:PayorPhoneNumberIsEditable", customerIsEditable);

            if (hostedPaymentData.getCustomerAddress() != null) {
                Address address = hostedPaymentData.getCustomerAddress();
                et.subElement(requestElement, "bdms:PayorAddress", address.getStreetAddress1());
                et.subElement(requestElement, "bdms:PayorAddressIsEditable", customerIsEditable);
                et.subElement(requestElement, "bdms:PayorBusinessNameIsEditable", customerIsEditable);
                et.subElement(requestElement, "bdms:PayorCity", address.getCity());
                et.subElement(requestElement, "bdms:PayorCityIsEditable", customerIsEditable);
                et.subElement(requestElement, "bdms:PayorCountry", address.getCountryCode());
                et.subElement(requestElement, "bdms:PayorCountryIsEditable", customerIsEditable);
                et.subElement(requestElement, "bdms:PayorPostalCode", address.getPostalCode());
                et.subElement(requestElement, "bdms:PayorPostalCodeIsEditable", customerIsEditable);
                et.subElement(requestElement, "bdms:PayorState", address.getState());
                et.subElement(requestElement, "bdms:PayorStateIsEditable", customerIsEditable);
            }
        }

        return et.toString(envelope);
    }
}
