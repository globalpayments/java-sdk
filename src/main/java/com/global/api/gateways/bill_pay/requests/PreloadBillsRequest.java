package com.global.api.gateways.bill_pay.requests;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class PreloadBillsRequest extends BillPayRequestBase {
    public PreloadBillsRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, BillingBuilder builder, Credentials credentials) throws ApiException {
        Element body = et.subElement(envelope, "soapenv:Body");
        Element methodElement = et.subElement(body, "bil:PreloadBills");
        Element requestElement = et.subElement(methodElement, "bil:PreloadBillsRequest");

        validateBills(builder.getBills());
        buildCredentials(requestElement, credentials);
        Element bills = et.subElement(requestElement, "bdms:Bills");

        for (Bill bill : builder.getBills()) {
            Element billElement = et.subElement(bills, "bdms:Bill");
            Element billIdentifierExtended = et.subElement(billElement, "bdms:BillIdentifierExtended");

            et.subElement(billIdentifierExtended, "bdms:BillType", bill.getBillType());
            et.subElement(billIdentifierExtended, "bdms:ID1", bill.getIdentifier1());
            et.subElement(billIdentifierExtended, "bdms:ID2", bill.getIdentifier2());
            et.subElement(billIdentifierExtended, "bdms:ID3", bill.getIdentifier3());
            et.subElement(billIdentifierExtended, "bdms:ID4", bill.getIdentifier4());

            et.subElement(billIdentifierExtended, "bdms:DueDate", getDateFormatted(bill.getDueDate()));

            et.subElement(billElement, "bdms:BillPresentment", getBillPresentmentType(bill.getBillPresentment()));


            if (bill.getCustomer() != null) {
                Customer customer = bill.getCustomer();

                if (customer.getAddress() != null) {
                    Address address = customer.getAddress();
                    Element customerAddress = et.subElement(billElement, "bdms:CustomerAddress");
                    et.subElement(customerAddress, "bdms:AddressLineOne", address.getStreetAddress1());
                    et.subElement(customerAddress, "bdms:City", address.getCity());
                    et.subElement(customerAddress, "bdms:Country", address.getCountry());
                    et.subElement(customerAddress, "bdms:PostalCode", address.getPostalCode());
                    et.subElement(customerAddress, "bdms:State", address.getState());
                }
                
                et.subElement(billElement, "bdms:MerchantCustomerId", customer.getId());
                et.subElement(billElement, "bdms:ObligorEmailAddress", customer.getEmail());
                et.subElement(billElement, "bdms:ObligorFirstName", customer.getFirstName());
                et.subElement(billElement, "bdms:ObligorLastName", customer.getLastName());
                et.subElement(billElement, "bdms:ObligorPhoneNumber", customer.getHomePhone());
            }

            et.subElement(billElement, "bdms:RequiredAmount", bill.getAmount());
        }

        return et.toString(envelope);
    }
}
