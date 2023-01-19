package com.global.api.tests.transactionapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.transactionApi.entities.TransactionApiRegion;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.TransactionApiConfig;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionApiACHTest extends BaseTransactionApiTest {

    private eCheck eCheck;
    Customer customer;
    TransactionApiConfig config;
    Address billingAddress;

    public TransactionApiACHTest() throws ConfigurationException {
        config = new TransactionApiConfig();

        config
                .setAppKey("qeG6EWZOiAwk4jsiHzsh2BN8VkN2rdAs")
                .setAppSecret("lucQKkwz3W3RGzABkSWUVZj1Mb0Yx3E9chAA8ESUVAv")
                .setAccountCredential("800000052925:80039990:n7j9rGFUml1Du7rcRs7XGYdJdVMmZKzh")
                .setRegion(TransactionApiRegion.US);


        config.setEnvironment(Environment.TEST);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        customer = new Customer();
//        customer.setId("2e39a948-2a9e-4b4a-9c59-0b96765343b7");
        customer.setTitle("Mr.");
        customer.setFirstName("Joe");
        customer.setMiddleName("Henry");
        customer.setLastName("Doe");
//        customer.setCompany("ABC Company LLC.");
//        customer.setEmail("joe.doe@gmail.com");
//        customer.setMobilePhone("345-090-2334");
//        customer.setNote("This is a sample note");

        billingAddress = new Address();
        billingAddress.setStreetAddress1("2600 NW");
        billingAddress.setStreetAddress2("23th Street");
        billingAddress.setCity("Lindon");
        billingAddress.setState("Utah");
        billingAddress.setCountry("USA");
        billingAddress.setPostalCode("84042");

        customer.setAddress(billingAddress);

        eCheck = new eCheck();
        eCheck.setAccountNumber("12121");
        eCheck.setRoutingNumber("112000066");
        eCheck.setAccountType(AccountType.Checking);
        eCheck.setSecCode(SecCode.Ppd);
//        eCheck.setCheckReference("121002048");
        eCheck.setCheckNumber(getTransactionCheckNumber());
        eCheck.setTransitNumber("00257");
        eCheck.setFinancialInstituteNumber("596");
//        eCheck.setMerchantNotes("123");
//        eCheck.setBankName("First Union");
//        eCheck.setCheckHolderName("Jane Doe");
//        eCheck.setBankAddress(bankAddress);
    }

    @Test
    public void testCheckSales() throws ApiException {
        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());
    }

    @Test
    public void testCheckSalesCARegion() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        billingAddress.setCountry("CA");
        eCheck.setAccountNumber("001221111221");
        eCheck.setCheckNumber(getTransactionCheckNumber());
        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());
    }

    @Test
    public void CheckRefund() throws ApiException {
        Transaction response =
                eCheck
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(response);
        assertEquals("check_submitted", response.getResponseCode());
    }

    @Test
    public void testCheckRefundCARegion() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        eCheck.setAccountNumber("001221111221");
        eCheck.setCheckNumber(getTransactionCheckNumber());
        billingAddress.setCountry("CA");
        Transaction transaction =
                eCheck
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());
    }

    @Test
    public void testCheckRefundWithCheckSaleId() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromId(
                                "000000000394",
                                PaymentMethodType.ACH
                        );
        Transaction response =
                transaction
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_USA)
                        .withBankTransferDetails(eCheck)
                        .execute();

        assertNotNull(response);
        assertEquals("check_submitted", response.getResponseCode());
    }

    @Test
    public void testCheckRefundWithCheckSaleIdCA() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        eCheck.setAccountNumber("001221111221");
        eCheck.setCheckNumber(getTransactionCheckNumber());
        billingAddress.setCountry("CA");
        Transaction transaction =
                Transaction
                        .fromId(
                                "000000000696",
                                PaymentMethodType.ACH

                        );
        Transaction response =
                transaction
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_CAD)
                        .withPaymentPurposeCode("150")
                        .withBankTransferDetails(eCheck)
                        .execute();

        assertNotNull(response);
        assertEquals("check_submitted", response.getResponseCode());
    }

    @Test
    public void testCheckRefundWithReferanceID() throws ApiException {
        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        Transaction refundTransaction =
                Transaction
                        .fromClientTransactionId(
                                transaction.getClientTransactionId(),
                                PaymentMethodType.ACH
                        );

        Transaction refund =
                refundTransaction
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_USA)
                        .withBankTransferDetails(eCheck)
                        .execute();

        assertNotNull(refund);
        assertEquals("check_submitted", refund.getResponseCode());
    }

    @Test
    public void testCheckRefundWithReferanceIDCA() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        eCheck.setAccountNumber("001221111221");
        eCheck.setCheckNumber(getTransactionCheckNumber());
        billingAddress.setCountry("CA");

        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        eCheck.setCheckNumber(getTransactionCheckNumber());

        Transaction refundTransaction =
                Transaction
                        .fromClientTransactionId(
                                transaction.getClientTransactionId(),
                                PaymentMethodType.ACH
                        );

        Transaction refund =
                refundTransaction
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_CAD)
                        .withBankTransferDetails(eCheck)
                        .withPaymentPurposeCode("150")
                        .execute();

        assertNotNull(refund);
        assertEquals("check_submitted", refund.getResponseCode());
    }


    @Test
    public void testCheckRefundWithCheckSaleIdGetUS() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromId(
                                "000000001399",
                                PaymentMethodType.ACH,TransactionType.Refund

                        );

        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckRefundWithReferanceIDGetUS() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1654868377616",
                                PaymentMethodType.ACH,TransactionType.Refund
                        );


        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckRefundWithCheckSaleIdGetCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromId(
                                "000000001399",
                                PaymentMethodType.ACH,TransactionType.Refund

                        );

        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckRefundWithReferanceIDGetCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1654868377616",
                                PaymentMethodType.ACH,TransactionType.Refund
                        );


        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesWithCheckSaleIdGetUS() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromId(
                                "000000001739",
                                PaymentMethodType.ACH,TransactionType.Sale

                        );

        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesWithReferanceIDGetUS() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1655282319210",
                                PaymentMethodType.ACH,TransactionType.Sale
                        );


        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesWithCheckSaleIdGetCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromId(
                                "000000001739",
                                PaymentMethodType.ACH,TransactionType.Sale

                        );

        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesWithReferanceIDGetCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1655282319210",
                                PaymentMethodType.ACH,TransactionType.Sale
                        );


        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("approved", getTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesMultiUseToken() throws ApiException {
        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        eCheck.setToken(transaction.getToken());

        Transaction salesTransaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(salesTransaction);
        assertEquals("check_submitted", salesTransaction.getResponseCode());
    }

    @Test
    public void testCheckSalesMultiUseTokenCA() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);
        customer.getAddress().setCountry("CA");

        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        eCheck.setToken(transaction.getToken());
        eCheck.setCheckNumber(getTransactionCheckNumber());

        Transaction salesTransaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(salesTransaction);
        assertEquals("check_submitted", salesTransaction.getResponseCode());
    }

    @Test
    public void testCheckRefundMultiUseToken() throws ApiException {
        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        eCheck.setToken(transaction.getToken());

        Transaction response =
                eCheck
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(response);
        assertEquals("check_submitted", response.getResponseCode());
    }

    @Test
    public void testCheckRefundMultiUseTokenCA() throws ApiException {
        config.setAccountCredential("800000052925:80039996:58xcGM3pbTtzcidVPY65XBqbB1EzWoD3");
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);
        customer.getAddress().setCountry("CA");

        Transaction transaction =
                eCheck
                        .charge(new BigDecimal(23.09))
                        .withInvoiceNumber("239087")
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("check_submitted", transaction.getResponseCode());

        eCheck.setToken(transaction.getToken());
        eCheck.setCheckNumber(getTransactionCheckNumber());

        Transaction response =
                eCheck
                        .refund(new BigDecimal(23.09))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAddress(new Address(), AddressType.Billing)
                        .withCustomer(customer)
                        .withPaymentPurposeCode("150")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(response);
        assertEquals("check_submitted", response.getResponseCode());
    }
}


