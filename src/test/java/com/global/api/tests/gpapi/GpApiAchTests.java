package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiAchTests extends BaseGpApiTest {

    eCheck eCheck;
    Address address;
    Customer customer;

    private final String CURRENCY = "USD";
    private final BigDecimal AMOUNT = new BigDecimal(10);

    public GpApiAchTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll")
                .setAppKey("QDsW1ETQKHX6Y4TA")
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);
    }

    @Before
    public void TestInitialize() {
        address = new Address();
        address.setStreetAddress1("Apartment 852");
        address.setStreetAddress2("Complex 741");
        address.setStreetAddress3("no");
        address.setCity("Chicago");
        address.setPostalCode("5001");
        address.setState("IL");
        address.setCountryCode("US");

        Address bankAddress = new Address();
        bankAddress.setStreetAddress1("12000 Smoketown Rd");
        bankAddress.setStreetAddress2("Apt 3B");
        bankAddress.setStreetAddress3("no");
        bankAddress.setCity("Mesa");
        bankAddress.setPostalCode("22192");
        bankAddress.setState("AZ");
        bankAddress.setCountryCode("US");

        eCheck = new eCheck();
        eCheck.setAccountNumber("1234567890");
        eCheck.setRoutingNumber("122000030");
        eCheck.setAccountType(AccountType.Savings);
        eCheck.setSecCode(SecCode.Web);
        eCheck.setCheckReference("123");
        eCheck.setMerchantNotes("123");
        eCheck.setBankName("First Union");
        eCheck.setCheckHolderName("Jane Doe");
        eCheck.setBankAddress(bankAddress);

        customer = new Customer();
        customer.setId("e193c21a-ce64-4820-b5b6-8f46715de931");
        customer.setFirstName("James");
        customer.setLastName("Mason");
        customer.setDateOfBirth("1980-01-01");
        customer.setMobilePhone("+35312345678");
        customer.setHomePhone("+11234589");
    }

    @Test
    public void CheckSale() throws ApiException {
        var response =
                eCheck
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .withAddress(address)
                        .withCustomer(customer)
                        .execute(GP_API_CONFIG_NAME);

        assertResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CheckRefund() throws ApiException {
        var response =
                eCheck
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .withAddress(address)
                        .withCustomer(customer)
                        .execute(GP_API_CONFIG_NAME);

        assertResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Ignore("GP-API sandbox limitation")
    public void CheckRefundExistingSale() throws ApiException {
        var amount = new BigDecimal(1.29);

        var response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, DateUtils.addDays(DateTime.now().toDate(), -1))
                        .and(SearchCriteria.EndDate, DateUtils.addDays(DateTime.now().toDate(), -2))
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .and(SearchCriteria.PaymentMethod, PaymentMethodName.BankTransfer)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertNotNull(response.getResults());
        var transactionSummary = response.getResults().get(0);
        assertNotNull(transactionSummary);
        assertEquals(amount, transactionSummary.getAmount());
        var transaction = new Transaction();
        transaction.setTransactionId(transactionSummary.getTransactionId());
        transaction.setPaymentMethodType(PaymentMethodType.ACH);

        var resp =
                transaction
                        .refund()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertResponse(resp, TransactionStatus.Captured);
    }

    @Test
    public void CheckReauthorize() throws ApiException {
        var eCheckReauth = new eCheck();
        eCheckReauth.setSecCode(SecCode.Ppd);
        eCheckReauth.setAccountNumber("051904524");
        eCheckReauth.setRoutingNumber("123456780");

        var amount = new BigDecimal("1.29");

        var response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, DateUtils.addDays(DateTime.now().toDate(), -365))
                        .and(SearchCriteria.EndDate, DateUtils.addDays(DateTime.now().toDate(), -2))
                        .and(SearchCriteria.PaymentType, PaymentType.Sale)
                        .and(SearchCriteria.PaymentMethod, PaymentMethodName.BankTransfer)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertNotNull(response.getResults());

        var transactionSummary = response.getResults().get(0);
        assertNotNull(transactionSummary);
        assertEquals(amount, transactionSummary.getAmount());

        var transaction = new Transaction();
        transaction.setTransactionId(transactionSummary.getTransactionId());
        transaction.setPaymentMethodType(PaymentMethodType.ACH);

        var resp =
                transaction
                        .reauthorize()
                        .withDescription("Resubmitting " + transaction.getReferenceNumber())
                        .withBankTransferDetails(eCheckReauth)
                        .execute(GP_API_CONFIG_NAME);

        assertResponse(resp, TransactionStatus.Captured);
    }

    private void assertResponse(Transaction response, TransactionStatus transactionStatus) {
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(transactionStatus.getValue(), response.getResponseMessage());
    }

}