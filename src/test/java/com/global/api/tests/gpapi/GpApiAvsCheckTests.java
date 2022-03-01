package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Collection;

import static com.global.api.tests.gpapi.BaseGpApiTest.AvsCheckTestCards.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Enclosed.class)
public class GpApiAvsCheckTests extends BaseGpApiTest {

    private static final BigDecimal amount = new BigDecimal("1.01");
    private static final String currency = "GBP";

    private static CreditCardData card;
    private static Address address;

    abstract public static class SharedSetUp {
        @BeforeClass
        @SneakyThrows
        public static void init() {
            GpApiConfig config = new GpApiConfig();

            // GP-API settings
            config.setAppId(APP_ID);
            config.setAppKey(APP_KEY);
            config.setChannel(Channel.CardNotPresent.getValue());

            config.setEnableLogging(true);

            ServicesContainer.configureService(config);

            card = new CreditCardData();
            card.setExpMonth(expMonth);
            card.setExpYear(expYear);
            card.setCardHolderName("John Smith");

            address = new Address();
            address.setStreetAddress1("Apartment 852");
            address.setStreetAddress2("Complex 741");
            address.setStreetAddress3("no");
            address.setCity("Chicago");
            address.setPostalCode("5001");
            address.setState("IL");
            address.setCountryCode("840");
        }
    }

    @RunWith(Parameterized.class)
    public static class AvsTestCardTests extends SharedSetUp {
        @Parameterized.Parameters(name = "AvsTestCardTests :: {index} :: card [{0}], cvnResponseMessage [{1}], avsResponseCode [{2}], avsAddressResponse [{3}], status [{4}], transactionStatus [{5}]")
        public static Collection input() {
            return asList(new Object[][]{
                    {AVS_MasterCard_1.avsCardNumber, "MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_2.avsCardNumber, "MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_3.avsCardNumber, "MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_4.avsCardNumber, "MATCHED", "MATCHED", "MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_5.avsCardNumber, "MATCHED", "MATCHED", "NOT_MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_6.avsCardNumber, "MATCHED", "NOT_MATCHED", "MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_7.avsCardNumber, "MATCHED", "NOT_MATCHED", "NOT_MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_8.avsCardNumber, "NOT_MATCHED", "NOT_MATCHED", "MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_9.avsCardNumber, "NOT_MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_10.avsCardNumber, "NOT_MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_11.avsCardNumber, "NOT_MATCHED", "NOT_CHECKED", "NOT_CHECKED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_12.avsCardNumber, "NOT_MATCHED", "MATCHED", "MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_13.avsCardNumber, "NOT_MATCHED", "MATCHED", "NOT_MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_MasterCard_14.avsCardNumber, "NOT_MATCHED", "NOT_MATCHED", "NOT_MATCHED", SUCCESS, TransactionStatus.Captured},
                    {AVS_Visa_1.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_2.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_3.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_4.avsCardNumber, "NOT_CHECKED", "MATCHED", "MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_5.avsCardNumber, "NOT_CHECKED", "MATCHED", "NOT_MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_6.avsCardNumber, "NOT_CHECKED", "NOT_MATCHED", "MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_7.avsCardNumber, "NOT_CHECKED", "NOT_MATCHED", "NOT_MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_8.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_9.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_10.avsCardNumber, "NOT_CHECKED", "NOT_CHECKED", "NOT_CHECKED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_11.avsCardNumber, "NOT_CHECKED", "MATCHED", "MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_12.avsCardNumber, "NOT_CHECKED", "MATCHED", "NOT_MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_13.avsCardNumber, "NOT_CHECKED", "NOT_MATCHED", "MATCHED", DECLINED, TransactionStatus.Declined},
                    {AVS_Visa_14.avsCardNumber, "NOT_CHECKED", "NOT_MATCHED", "NOT_MATCHED", DECLINED, TransactionStatus.Declined}
            });
        }

        @Parameterized.Parameter()
        public String cardNumber;
        @Parameterized.Parameter(1)
        public String cvnResponseMessage;
        @Parameterized.Parameter(2)
        public String avsResponseCode;
        @Parameterized.Parameter(3)
        public String avsAddressResponse;
        @Parameterized.Parameter(4)
        public String status;
        @Parameterized.Parameter(5)
        public TransactionStatus transactionStatus;

        @SneakyThrows
        @Test
        public void CreditSale_CvvResult() {
            card.setNumber(cardNumber);

            Transaction response =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .withAddress(address)
                            .execute();

            assertNotNull(response);
            assertEquals(status, response.getResponseCode());
            assertEquals(transactionStatus.getValue(), response.getResponseMessage());

            assertEquals(cvnResponseMessage, response.getCvnResponseMessage());
            assertEquals(avsResponseCode, response.getAvsResponseCode());
            assertEquals(avsAddressResponse, response.getAvsAddressResponse());
        }
    }

}
