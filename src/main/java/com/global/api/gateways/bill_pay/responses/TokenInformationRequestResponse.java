package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Address;
import com.global.api.entities.Card;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.billing.TokenData;
import com.global.api.utils.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class TokenInformationRequestResponse extends BillPayResponseBase<Transaction> {
    @Override
    public Transaction map() {
        Transaction result = new Transaction();
        Address address = new Address();
        Card cardDetails = new Card();
        Customer customerData = new Customer();
        TokenData tokenData = new TokenData();

        Element tokenDetailsElement = response.get("a:TokenDetails");
        Element accountHolderDataElement = tokenDetailsElement.get(("a:AccountHolderData"));
        Element merchantsElement = tokenDetailsElement.get("a:Merchants");

        address.setStreetAddress1(accountHolderDataElement.getString("b:Address"));
        address.setCity(accountHolderDataElement.getString("b:City"));
        address.setState(accountHolderDataElement.getString("b:State"));
        address.setPostalCode(accountHolderDataElement.getString("b:Zip"));
        address.setCountry(accountHolderDataElement.getString("b:Country"));

        customerData.setCompany(accountHolderDataElement.getString("b:BusinessName"));
        customerData.setFirstName(accountHolderDataElement.getString("b:FirstName"));
        customerData.setLastName(accountHolderDataElement.getString("b:LastName"));
        customerData.setMiddleName(accountHolderDataElement.getString("b:MiddleName"));
        customerData.setHomePhone(accountHolderDataElement.getString("b:Phone"));

        cardDetails.setCardHolderName(accountHolderDataElement.getString("b:NameOnCard"));
        cardDetails.setCardExpMonth(tokenDetailsElement.getString("a:ExpirationMonth"));
        cardDetails.setCardExpYear(tokenDetailsElement.getString("a:ExpirationYear"));
        cardDetails.setMaskedNumberLast4(tokenDetailsElement.getString("a:Last4"));

        tokenData.setExpired(tokenDetailsElement.getBool("a:IsExpired"));
        tokenData.setLastUsedDateUTC(dateTimeXMLParser(tokenDetailsElement.getString("a:LastUsedDateUTC")));
        tokenData.setMerchants(populateMerchantListFromElement(merchantsElement));
        tokenData.setSharedTokenWithGroup(tokenDetailsElement.getBool("a:SharedTokenWithGroup"));

        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setAddress(address);
        result.setCustomerData(customerData);
        result.setCardDetails(cardDetails);
        result.setTokenData(tokenData);
        result.setPaymentMethodType(getPaymentMethodType(tokenDetailsElement.getString("a:PaymentMethod")));
        result.setCardType(getCardType(tokenDetailsElement.getString("a:PaymentMethod")));
        result.setToken(tokenDetailsElement.getString("a:Token"));

        return result;
    }
    private List<String> populateMerchantListFromElement(Element merchantsElement) {
        if(merchantsElement == null) {
           return null;
        }
        if(merchantsElement.getElement().getChildNodes().getLength() > 0) {
            List<String> merchantList = new ArrayList<>();
            NodeList childNodes = merchantsElement.getElement().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                merchantList.add(childNodes.item(i).getTextContent());
            }
            return merchantList;
        }

        return   null;
    }

    private DateTime dateTimeXMLParser(String xmlDateTimeStr) {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

        return formatter.parseDateTime(xmlDateTimeStr);
    }
}
