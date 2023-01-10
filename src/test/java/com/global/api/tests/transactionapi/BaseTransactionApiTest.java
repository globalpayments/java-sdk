package com.global.api.tests.transactionapi;

import com.global.api.entities.Address;
import com.global.api.entities.Customer;

import java.util.Date;

public abstract class BaseTransactionApiTest {


    final static protected String LANGUAGE_CANADA = "en-CA";
    final static protected String LANGUAGE_USA = "en-US";

    final static protected String CURRENCY_USA = "USD";
    final static protected String CURRENCY_CAD = "CAD";

    final static protected String COUNTRY_USA = "USA";
    final static protected String COUNTRY_CAD = "Canada";

    Customer customer, customerCA;

    public BaseTransactionApiTest(){
       // Customer Details for US region
        customer = new Customer();
        customer.setId("2e39a948-2a9e-4b4a-9c59-0b96765343b7");
        customer.setTitle("Mr.");
        customer.setFirstName("Joe");
        customer.setMiddleName("Henry");
        customer.setLastName("Doe");
        customer.setCompany("ABC Company LLC.");
        customer.setEmail("joe.doe@gmail.com");
        customer.setMobilePhone("345-090-2334");
        customer.setNote("This is a sample note");
        Address billingAddress = new Address();
        billingAddress.setStreetAddress1("2600 NW");
        billingAddress.setStreetAddress2("23th Street");
        billingAddress.setCity("Lindon");
        billingAddress.setState("Utah");
        billingAddress.setCountry("USA");
        billingAddress.setPostalCode("84042");
        customer.setAddress(billingAddress);

        // Customer Details for CA region
        customerCA = new Customer();
        customerCA.setId("2e39a948-2a9e-4b4a-9c59-0b96765343b7");
        customerCA.setTitle("Mr.");
        customerCA.setFirstName("Joe");
        customerCA.setMiddleName("Henry");
        customerCA.setLastName("Doe");
        customerCA.setCompany("ABC Company LLC.");
        customerCA.setEmail("joe.doe@gmail.com");
        customerCA.setMobilePhone("345-090-2334");
        customerCA.setNote("This is a sample note");
        Address billingAddressCA = new Address();
        billingAddressCA.setStreetAddress1("2600 NW");
        billingAddressCA.setStreetAddress2("23th Street");
        billingAddressCA.setCity("Lindon");
        billingAddressCA.setState("Utah");
        billingAddressCA.setCountry("Canada");
        billingAddressCA.setPostalCode("84042");
        customerCA.setAddress(billingAddressCA);
    }


    public String getTransactionID(){
        return "REF-" + new Date().getTime();
    }

    public String getTransactionCheckNumber(){
        return String.valueOf(new Date().getTime());
    }
}
