package com.global.api.tests.transactionapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.transactionApi.entities.TransactionApiRegion;
import com.global.api.entities.transactionApi.enums.TransactionAPIEcomIndicator;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.TransactionApiConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionCreditTest extends BaseTransactionApiTest {

    CreditCardData card;

    TransactionApiConfig config;

    public TransactionCreditTest() throws ConfigurationException {
        config = new TransactionApiConfig();

        config
                .setAppKey("qeG6EWZOiAwk4jsiHzsh2BN8VkN2rdAs")
                .setAppSecret("lucQKkwz3W3RGzABkSWUVZj1Mb0Yx3E9chAA8ESUVAv")
                .setAccountCredential("800000052925:80039923:eWcWNJhfxiJ7QyEHSHndWk4VHKbSmSue")
                .setRegion(TransactionApiRegion.US);


        config.setEnvironment(Environment.TEST);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setCardHolderName("Joe Doe");
        card.setNumber("4761739001010010");
        card.setExpMonth(12);
        card.setExpYear(22);
        card.setCvn("201");
        card.setCardPresent(true);


    }

    @Test
    public void testCreditSales() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesCARegion() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withEcommerceAuthIndicator(TransactionAPIEcomIndicator.ECOM_INDICATOR_1.getValue())
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithCustomerInfo() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithAVSandReceipt() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withGenerateReceipt(true)
                        .withAvs(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithInvoiceNumber() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("12")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithDescription() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withDescription("ABC Grocery, Lindon")
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithClerkID() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withClientTransactionId(getTransactionID())
                        .withClerkId("Al090-John Doe")
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesWithCountry() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withCountry("USA")
                        .withAllowDuplicates(true)
                        .withClientTransactionId(getTransactionID())
                        .withClerkId("Al090-John Doe")
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditAuthUS() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditAuthCARegion() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditRefundWithCustomerInfo() throws ApiException {
        Transaction transaction =
                card
                        .refund(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
        assertEquals(BigDecimal.valueOf(14.55), transaction.getOrigionalAmount());
    }

    @Test
    public void testCreditRefundWithCustomerInfoCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);
        Transaction transaction =
                card
                        .refund(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAllowDuplicates(true)
                        .withCustomer(customerCA)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditRefundWithCreditReturnId() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromId(
                                "000000000241",
                                PaymentMethodType.Credit
                        );

        Transaction refund =
                transaction
                        .refund(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withDescription("ABC Grocery, Lin")
                        .withCustomer(customer)
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(refund);
        assertEquals("approved", refund.getResponseCode());
    }

    @Test
    public void testCreditRefundWithCreditReturnIdCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromId(
                                "000000000241",
                                PaymentMethodType.Credit
                        );

        Transaction refund =
                transaction
                        .refund(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_USA)
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withDescription("ABC Grocery, Lin")
                        .withCustomer(customerCA)
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(refund);
        assertEquals("approved", refund.getResponseCode());
    }

    @Test
    public void testCreditRefundWithReferanceID() throws ApiException {
        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1652705845220",
                                PaymentMethodType.Credit
                        );

        Transaction refund =
                transaction
                        .refund(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withDescription("ABC Grocery, Lin")
                        .withCustomer(customer)
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(refund);
        assertEquals("approved", refund.getResponseCode());
    }

    @Test
    public void testCreditRefundWithReferanceIDCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                "REF-1652705845220",
                                PaymentMethodType.Credit
                        );

        Transaction refund =
                transaction
                        .refund(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_CAD)
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withDescription("ABC Grocery, Lin")
                        .withCustomer(customerCA)
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(refund);
        assertEquals("approved", refund.getResponseCode());
    }

    @Test
    public void testEditSaleWithTransactionIdUS() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        Transaction editTransaction =
                Transaction
                        .fromId(
                                transaction.getTransactionId(),
                                PaymentMethodType.Credit
                        );

        Transaction edit =
                editTransaction
                        .edit(23.09)
                        .withCurrency("USD")
                        .withGratuity(new BigDecimal(10.00))
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(edit);
        assertEquals("approved", edit.getResponseCode());
    }

    @Test
    public void testCreditSaleWithCreditReferenceIdUS() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        Transaction editTransaction =
                Transaction
                        .fromClientTransactionId(
                                transaction.getClientTransactionId(),
                                PaymentMethodType.Credit
                        );

        Transaction edit =
                editTransaction
                        .edit(23.09)
                        .withCurrency("USD")
                        .withGratuity(new BigDecimal(10.00))
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(edit);
        assertEquals("approved", edit.getResponseCode());
    }

    @Test
    public void testCreditSaleWithTransactionIdCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAllowDuplicates(true)
                        .withCustomer(customerCA)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        Transaction editTransaction =
                Transaction
                        .fromId(
                                transaction.getTransactionId(),
                                PaymentMethodType.Credit
                        );

        Transaction edit =
                editTransaction
                        .edit(23.09)
                        .withCurrency(CURRENCY_CAD)
                        .withGratuity(new BigDecimal(10.00))
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(edit);
        assertEquals("approved", edit.getResponseCode());
    }

    @Test
    public void testCreditSaleWithCreditReferenceIdCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .charge(new BigDecimal(14.55))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withAllowDuplicates(true)
                        .withCustomer(customerCA)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        Transaction editTransaction =
                Transaction
                        .fromClientTransactionId(
                                transaction.getClientTransactionId(),
                                PaymentMethodType.Credit
                        );

        Transaction edit =
                editTransaction
                        .edit(23.09)
                        .withCurrency(CURRENCY_CAD)
                        .withGratuity(new BigDecimal(10.00))
                        .withAllowDuplicates(true)
                        .withInvoiceNumber("239087")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(edit);
        assertEquals("approved", edit.getResponseCode());
    }

    @Test
    public void testVoidWithCreditSaleIdUS() throws ApiException {
        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        Transaction transaction =
                Transaction
                        .fromId(
                                sale.getTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Sale
                        );

        Transaction response =
                transaction
                        .voidTransaction(new BigDecimal(14))
                        .withCurrency("USD")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

    @Test
    public void testVoidWithCreditSaleIdCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        Transaction transaction =
                Transaction
                        .fromId(
                                sale.getTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Sale
                        );

        Transaction response =
                transaction
                        .voidTransaction(new BigDecimal(14))
                        .withCurrency(CURRENCY_CAD)
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

    @Test
    public void testVoidWithCreditSaleReferenceIdUS() throws ApiException {
        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                sale.getClientTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Refund
                        );

        Transaction response =
                transaction
                        .voidTransaction(new BigDecimal(14))
                        .withCurrency("USD")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

    @Test
    public void testVoidWithCreditSaleReferenceIdCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        Transaction transaction =
                Transaction
                        .fromClientTransactionId(
                                sale.getClientTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Refund
                        );

        Transaction response =
                transaction
                        .voidTransaction(new BigDecimal(14))
                        .withCurrency("CA")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

    @Test
    public void testCreditVoidReturnWithReturnIdUS() throws ApiException {
        Transaction refund =
                card
                        .refund(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        Transaction transaction =
                Transaction
                        .fromId(
                                refund.getTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Refund
                        );

        Transaction response =
                transaction
                        .voidTransaction(new BigDecimal(14.55))
                        .withCurrency("USD")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }
        //CreditSale get
        @Test
        public void testCreditSaleWithTransactionIdUSGet () throws ApiException {
            Transaction transaction =
                    Transaction
                            .fromId(
                                    "000000000722",
                                    PaymentMethodType.Credit, TransactionType.Sale
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }

        @Test
        public void testCreditSaleWithCreditReferenceIdUSGet () throws ApiException {
            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    "REF-1654065729331",
                                    PaymentMethodType.Credit, TransactionType.Sale
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }
        @Test
        public void testCreditSaleWithTransactionIdCAGet () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction transaction =
                    Transaction
                            .fromId(
                                    "000000000722",
                                    PaymentMethodType.Credit, TransactionType.Sale
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }

        @Test
        public void testCreditSaleWithCreditReferenceIdCAGet () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    "REF-1654065729331",
                                    PaymentMethodType.Credit, TransactionType.Sale
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }

        @Test
        public void testCreditVoidReturnWithReturnIdCA () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction refundResponse =
                    card
                            .refund(new BigDecimal(14))
                            .withCurrency(CURRENCY_CAD)
                            .withCardHolderLanguage(LANGUAGE_CANADA)
                            .withAllowDuplicates(true)
                            .withCustomer(customerCA)
                            .withClientTransactionId(getTransactionID())
                            .execute();


            Transaction transaction =
                    Transaction
                            .fromId(
                                    refundResponse.getTransactionId(),
                                    PaymentMethodType.Credit,
                                    TransactionType.Refund
                            );

            Transaction response =
                    transaction
                            .voidTransaction(new BigDecimal(14))
                            .withCurrency(CURRENCY_CAD)
                            .withAllowDuplicates(true)
                            .withGenerateReceipt(true)
                            .execute();

            assertNotNull(response);
            assertEquals("voided", response.getResponseCode());
        }

        @Test
        public void testCreditVoidWithReturnReferenceIDUS () throws ApiException {
            Transaction refundResponse =
                    card
                            .refund(new BigDecimal(14))
                            .withCurrency("USD")
                            .withCardHolderLanguage("en-US")
                            .withAllowDuplicates(true)
                            .withCustomer(customer)
                            .withClientTransactionId(getTransactionID())
                            .execute();

            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    refundResponse.getClientTransactionId(),
                                    PaymentMethodType.Credit,
                                    TransactionType.Refund
                            );

            Transaction response =
                    transaction
                            .voidTransaction(new BigDecimal(14))
                            .withCurrency("USD")
                            .withGenerateReceipt(true)
                            .execute();

            assertNotNull(response);
            assertEquals("voided", response.getResponseCode());
        }

        @Test
        public void testCreditVoidWithReturnReferenceIDCA () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction refundResponse =
                    card
                            .refund(new BigDecimal(14))
                            .withCurrency(CURRENCY_CAD)
                            .withCardHolderLanguage(LANGUAGE_CANADA)
                            .withAllowDuplicates(true)
                            .withCustomer(customerCA)
                            .withClientTransactionId(getTransactionID())
                            .execute();

            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    refundResponse.getClientTransactionId(),
                                    PaymentMethodType.Credit,
                                    TransactionType.Refund
                            );

            Transaction response =
                    transaction
                            .voidTransaction(new BigDecimal(14))
                            .withCurrency(CURRENCY_CAD)
                            .withAllowDuplicates(true)
                            .withGenerateReceipt(true)
                            .execute();

            assertNotNull(response);
            assertEquals("voided", response.getResponseCode());
        }

    @Test
    public void testCreditReturnWithTransactionIdGet () throws ApiException {

        Transaction transaction =
                Transaction
                        .fromId(
                                "000000013591",
                                PaymentMethodType.Credit, TransactionType.Refund
                        );

        Transaction getTransaction =
                transaction
                        .fetch()
                        .execute();

        assertNotNull(getTransaction);
        assertEquals("closed", getTransaction.getResponseCode());
    }

        @Test
        public void testCreditReturnWithCreditReferenceIdUSGet () throws ApiException {
            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    "REF-1654160449085",
                                    PaymentMethodType.Credit, TransactionType.Refund
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }
        @Test
        public void testCreditReturnWithTransactionIdCAGet () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction transaction =
                    Transaction
                            .fromId(
                                    "000000000815",
                                    PaymentMethodType.Credit, TransactionType.Refund
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }

        @Test
        public void testCreditReturnWithCreditReferenceIdCAGet () throws ApiException {
            config.setRegion(TransactionApiRegion.CA);
            ServicesContainer.configureService(config);
            Transaction transaction =
                    Transaction
                            .fromClientTransactionId(
                                    "REF-1654160449085",
                                    PaymentMethodType.Credit, TransactionType.Refund
                            );

            Transaction getTransaction =
                    transaction
                            .fetch()
                            .execute();

            assertNotNull(getTransaction);
            assertEquals("closed", getTransaction.getResponseCode());
        }

    @Test
    public void testCreditSalesMultiUseToken() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(sale);
        assertEquals("approved", sale.getResponseCode());
    }

    @Test
    public void testCreditSalesMultiUseTokenCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction sale =
                card
                        .charge(new BigDecimal(14))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withCustomer(customerCA)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(sale);
        assertEquals("approved", sale.getResponseCode());
    }

    @Test
    public void testCreditReturnMultiUseToken() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction refundResponse =
                card
                        .refund(new BigDecimal(14))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withAllowDuplicates(true)
                        .withCustomer(customer)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(refundResponse);
        assertEquals("approved", refundResponse.getResponseCode());
    }

    @Test
    public void testCreditReturnMultiUseTokenCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withClientTransactionId(getTransactionID())
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction refundResponse =
                card
                        .refund(new BigDecimal(14))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCountry(COUNTRY_CAD)
                        .withAllowDuplicates(true)
                        .withCustomer(customerCA)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(refundResponse);
        assertEquals("approved", refundResponse.getResponseCode());
    }

    @Test
    public void testCreditAuthMultiUseToken() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction authResponse =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .execute();

        assertNotNull(authResponse);
        assertEquals("approved", authResponse.getResponseCode());
    }

    @Test
    public void testCreditAuthMultiUseTokenCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withClientTransactionId(getTransactionID())
                        .withCountry(COUNTRY_CAD)
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(transaction);
        assertEquals("approved", transaction.getResponseCode());

        card.setToken(transaction.getToken());

        Transaction authResponse =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withClientTransactionId(getTransactionID())
                        .withCountry(COUNTRY_CAD)
                        .execute();

        assertNotNull(authResponse);
        assertEquals("approved", authResponse.getResponseCode());
    }

    @Test
    @Ignore("This will work only when you generate new single use token from the globalpayment.js and add in test case")
    public void testCreditAuthSingleUseToken() throws ApiException {

        card.setToken("a474ecc3-8176-4287-bffe-92236d508692");

        Transaction authResponse =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency("USD")
                        .withCardHolderLanguage("en-US")
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                        .withClientTransactionId(getTransactionID())
                        .withCountry("USA")
                        .execute();

        assertNotNull(authResponse);
        assertEquals("approved", authResponse.getResponseCode());
    }

    @Test
    @Ignore("This will work only when you generate new single use token from the globalpayment.js and add in test case")
    public void testCreditAuthSingleUseTokenCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        card.setToken("286e22a1-7609-4fd6-a25e-3e2381f521f7");

        Transaction authResponse =
                card
                        .authorize(new BigDecimal(0))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                        .withClientTransactionId(getTransactionID())
                        .withCountry(COUNTRY_CAD)
                        .execute();

        assertNotNull(authResponse);
        assertEquals("approved", authResponse.getResponseCode());
    }

    @Test
    public void testCreditSalesPartialApproval() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(13.17))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("partially_approved", transaction.getResponseCode());
    }


    @Test
    public void testCreditSalesPartialApprovalCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction transaction =
                card
                        .charge(new BigDecimal(13.17))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCustomer(customerCA)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(transaction);
        assertEquals("partially_approved", transaction.getResponseCode());
    }

    @Test
    public void testCreditSalesPartialVoid() throws ApiException {
        Transaction sale =
                card
                        .charge(new BigDecimal(13.17))
                        .withCurrency(CURRENCY_USA)
                        .withCardHolderLanguage(LANGUAGE_USA)
                        .withCustomer(customer)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(sale);
        assertEquals("partially_approved", sale.getResponseCode());

        Transaction transaction =
                Transaction
                        .fromId(
                                sale.getTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Sale
                        );

        Transaction response =
                transaction
                        .voidTransaction(sale.getTransactionSummary().getAmount())
                        .withCurrency("USD")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

    @Test
    public void testCreditSalesPartialVoidCA() throws ApiException {
        config.setRegion(TransactionApiRegion.CA);
        ServicesContainer.configureService(config);

        Transaction sale =
                card
                        .charge(new BigDecimal(13.17))
                        .withCurrency(CURRENCY_CAD)
                        .withCardHolderLanguage(LANGUAGE_CANADA)
                        .withCustomer(customerCA)
                        .withAllowDuplicates(true)
                        .withAllowPartialAuth(true)
                        .withClientTransactionId(getTransactionID())
                        .execute();

        assertNotNull(sale);
        assertEquals("partially_approved", sale.getResponseCode());

        Transaction transaction =
                Transaction
                        .fromId(
                                sale.getTransactionId(),
                                PaymentMethodType.Credit,
                                TransactionType.Sale
                        );

        Transaction response =
                transaction
                        .voidTransaction(sale.getTransactionSummary().getAmount())
                        .withCurrency("USD")
                        .withGenerateReceipt(true)
                        .execute();

        assertNotNull(response);
        assertEquals("voided", response.getResponseCode());
    }

}
