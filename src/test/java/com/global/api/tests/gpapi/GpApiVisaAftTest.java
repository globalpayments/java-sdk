package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Product;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.GenerationUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GpApiVisaAftTest extends BaseGpApiTest {


    private CreditCardData card;
    private final BigDecimal amount = new BigDecimal("2.02");
    private final String currency = "GBP";
    private Address shippingAddress;
    private Product product;

    public GpApiVisaAftTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setCountry("UK");
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);

        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Birmingham");
        shippingAddress.setPostalCode("50001");
        shippingAddress.setState("England");
        shippingAddress.setCountryCode("UK");

        product = getProduct();
    }

    //Added new object as SupplementaryData.
    @Test
    void creditSaleVisaAft() throws ApiException {

        ArrayList<Product> newProducts = getProductList();
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withMiscProductData(newProducts)
                        .withSupplementaryData("VISA_DIRECT_AFT", "John Smith", "10 High Street", "Nottingham", "GBR", "02", "123456789")
                        .withSupplementaryData("VISA_DIRECT", "Philip White", "High Street", "Nottingham", "GBR", "02", "123333789")
                        .withOrderId("12345")
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    //credit sale without order object
    @Test
    void creditSaleWithoutOrderObject() throws ApiException {

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(shippingAddress)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    //credit sale with order object and without SupplementaryData.
    @Test
    void creditSaleWithOrderObjectAndWithoutSupplementaryData() throws ApiException {

        ArrayList<Product> newProducts = getProductList();
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withMiscProductData(newProducts)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    //credit sale with order object and SupplementaryData list as null.
    @Test
    void creditSaleWithNullSupplementaryData() throws ApiException {

        ArrayList<Product> newProducts = getProductList();
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withMiscProductData(newProducts)
                        .withSupplementaryData(null, (String) null)
                        .withOrderId("12345")
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    //credit sale with order object and empty SupplementaryData list.
    @Test
    void creditSaleWithEmptySupplementaryDataList() {

        ArrayList<Product> newProducts = getProductList();
        ApiException exception = assertThrows(ApiException.class, () -> {

            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withMiscProductData(newProducts)
                    .withSupplementaryData("VISA_DIRECT_AFT", "", "", "", "", "", "")
                    .withOrderId("12345")
                    .execute();
        });

        assertEquals("Status Code: 400 - order.supplementary_data.fields value is invalid. Please check the format and data provided is correct.", exception.getMessage());
    }

    //credit sale with order object and invalid SupplementaryData list.
    @Test
    void creditSaleWithInvalidSupplementaryDataList() throws ApiException {

        ArrayList<Product> newProducts = getProductList();
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withMiscProductData(newProducts)
                        .withSupplementaryData("", "", "", "", "", "", "")
                        .withOrderId("12345")
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
    }

    private static Product getProduct() {
        Product product = new Product();
        product.setProductId(GenerationUtils.generateOrderId());
        product.setProductName("iPhone 13");
        product.setDescription("iPhone 13");
        product.setQuantity(1);
        product.setUnitCurrency("EUR");
        product.setUnitPrice(BigDecimal.valueOf(550));
        product.setNetUnitPrice(BigDecimal.valueOf(550));
        product.setTaxAmount(BigDecimal.valueOf(0));
        product.setDiscountAmount(BigDecimal.valueOf(0));
        product.setTaxPercentage(BigDecimal.valueOf(0));
        product.setUrl("https://www.example.com/iphone.html");
        product.setImageUrl("https://www.example.com/iphone.png");
        return product;
    }

    private ArrayList<Product> getProductList() {
        List<Product> products = Collections.singletonList(product);
        return new ArrayList<>(products);
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }
}
