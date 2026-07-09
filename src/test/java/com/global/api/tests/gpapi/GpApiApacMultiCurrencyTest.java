package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.ManualEntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.CurrencyExponentUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test suite for APAC multi\-currency card processing using GP\-API.
 *
 * <p>This class validates end\-to\-end card transaction flows for VISA and MASTERCARD
 * across multiple currencies grouped by exponent \(0, 2, and 3\). Covered scenarios include:
 * sale, authorization, pre\-auth capture, partial capture with gratuity, void, auth reversal,
 * linked refund, standalone refund, and MOTO variants.</p>
 *
 * <p>It also includes amount\-encoding verification to ensure currency exponent handling
 * is consistent for SDK amount conversion to/from minor units.</p>
 *
 * <p>Test configuration is initialized for card\-not\-present APAC processing with country set to SG.</p>
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GpApiApacMultiCurrencyTest extends BaseGpApiTest {

    private static final String APAC_APP_ID = "16Br1RfjChBrsFnWlu7NGIp9LKm2MWWFyGg3SU3UfEl3voA2";
    private static final String APAC_APP_KEY = "xV9wnRLmi8qPqvMZoxAH9S0RtoQlodCYuvCboVYUohW6DObtcrYL1uj4YOZilKyu";

    private static final BigDecimal AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal PARTIAL_CAPTURE_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal GRATUITY_AMOUNT = new BigDecimal("1.00");

    private static final String VISA_CARD_NUMBER = "4263970000005262";
    private static final String MASTERCARD_CARD_NUMBER = "5425230000004415";

    private CreditCardData visaCard;
    private CreditCardData masterCard;
    private CreditCardData motoVisaCard;
    private CreditCardData motoMasterCard;

    public GpApiApacMultiCurrencyTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APAC_APP_ID, APAC_APP_KEY, Channel.CardNotPresent);
        config.setCountry("SG");
        ServicesContainer.configureService(config);
    }

    @BeforeEach
    public void setupCards() {
        visaCard = createCard(VISA_CARD_NUMBER, "John Smith", false);
        masterCard = createCard(MASTERCARD_CARD_NUMBER, "Jane Doe", false);

        motoVisaCard = createCard(VISA_CARD_NUMBER, "John Smith", true);
        motoMasterCard = createCard(MASTERCARD_CARD_NUMBER, "Jane Doe", true);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditSale_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;

        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditSale_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditSale_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditAuthorization_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditAuthorization_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditAuthorization_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditPreAuthAndCapture_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(AMOUNT).execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
        assertTransactionAmount(captureResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditPreAuthAndCapture_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(AMOUNT).execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
        assertTransactionAmount(captureResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditPreAuthAndCapture_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(AMOUNT).execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
        assertTransactionAmount(captureResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditVoid_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction saleResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(saleResponse, TransactionStatus.Captured);

        Transaction voidResponse = saleResponse.voidTransaction(AMOUNT).execute();
        assertTransactionResponse(voidResponse, TransactionStatus.Reversed);
        assertTransactionAmount(voidResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditVoid_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction saleResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(saleResponse, TransactionStatus.Captured);

        Transaction voidResponse = saleResponse.voidTransaction(AMOUNT).execute();
        assertTransactionResponse(voidResponse, TransactionStatus.Reversed);
        assertTransactionAmount(voidResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditVoid_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction saleResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(saleResponse, TransactionStatus.Captured);

        Transaction voidResponse = saleResponse.voidTransaction(AMOUNT).execute();
        assertTransactionResponse(voidResponse, TransactionStatus.Reversed);
        assertTransactionAmount(voidResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditAuthReversal_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction reversalResponse = authResponse.reverse(AMOUNT).execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditAuthReversal_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction reversalResponse = authResponse.reverse(AMOUNT).execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Reversed);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditAuthReversal_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction reversalResponse = authResponse.reverse(AMOUNT).execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Reversed);
        assertTransactionAmount(reversalResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditRefund_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Captured);

        Transaction reversalResponse = authResponse.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Captured);
        assertTransactionAmount(reversalResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditRefund_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Captured);

        Transaction reversalResponse = authResponse.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Captured);
        assertTransactionAmount(reversalResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditRefund_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Captured);

        Transaction reversalResponse = authResponse.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(reversalResponse, TransactionStatus.Captured);
        assertTransactionAmount(reversalResponse);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditRefund_StandaloneRefund_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction refundResponse = card.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(refundResponse, TransactionStatus.Captured);
        assertTransactionAmount(refundResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditRefund_StandaloneRefund_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction refundResponse = card.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(refundResponse, TransactionStatus.Captured);
        assertTransactionAmount(refundResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditRefund_StandaloneRefund_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction refundResponse = card.refund(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(refundResponse, TransactionStatus.Captured);
        assertTransactionAmount(refundResponse);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditPartialCapture_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(PARTIAL_CAPTURE_AMOUNT)
                .withGratuity(GRATUITY_AMOUNT)
                .execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditPartialCapture_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(PARTIAL_CAPTURE_AMOUNT)
                .withGratuity(GRATUITY_AMOUNT)
                .execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditPartialCapture_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? visaCard : masterCard;
        Transaction authResponse = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(authResponse, TransactionStatus.Preauthorized);

        Transaction captureResponse = authResponse.capture(PARTIAL_CAPTURE_AMOUNT)
                .withGratuity(GRATUITY_AMOUNT)
                .execute();
        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditMotoSale_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditMotoSale_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditMotoSale_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.charge(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertTransactionAmount(response);
    }


    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,ISK", "MASTERCARD,ISK",
            "VISA,KRW", "MASTERCARD,KRW",
            "VISA,VND", "MASTERCARD,VND"
    })
    public void CreditMoto_Authorization_Exponent0(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,AED", "MASTERCARD,AED",
            "VISA,AUD", "MASTERCARD,AUD",
            "VISA,BDT", "MASTERCARD,BDT",
            "VISA,BND", "MASTERCARD,BND",
            "VISA,BRL", "MASTERCARD,BRL",
            "VISA,CAD", "MASTERCARD,CAD",
            "VISA,CHF", "MASTERCARD,CHF",
            "VISA,CLP", "MASTERCARD,CLP",
            "VISA,CNY", "MASTERCARD,CNY",
            "VISA,DKK", "MASTERCARD,DKK",
            "VISA,EGP", "MASTERCARD,EGP",
            "VISA,EUR", "MASTERCARD,EUR",
            "VISA,GBP", "MASTERCARD,GBP",
            "VISA,HKD", "MASTERCARD,HKD",
            "VISA,IDR", "MASTERCARD,IDR",
            "VISA,ILS", "MASTERCARD,ILS",
            "VISA,INR", "MASTERCARD,INR",
            "VISA,JPY", "MASTERCARD,JPY",
            "VISA,LKR", "MASTERCARD,LKR",
            "VISA,MOP", "MASTERCARD,MOP",
            "VISA,MUR", "MASTERCARD,MUR",
            "VISA,MVR", "MASTERCARD,MVR",
            "VISA,MXN", "MASTERCARD,MXN",
            "VISA,MYR", "MASTERCARD,MYR",
            "VISA,NOK", "MASTERCARD,NOK",
            "VISA,NZD", "MASTERCARD,NZD",
            "VISA,PGK", "MASTERCARD,PGK",
            "VISA,PHP", "MASTERCARD,PHP",
            "VISA,PKR", "MASTERCARD,PKR",
            "VISA,QAR", "MASTERCARD,QAR",
            "VISA,RUB", "MASTERCARD,RUB",
            "VISA,SAR", "MASTERCARD,SAR",
            "VISA,SEK", "MASTERCARD,SEK",
            "VISA,SGD", "MASTERCARD,SGD",
            "VISA,THB", "MASTERCARD,THB",
            "VISA,TRY", "MASTERCARD,TRY",
            "VISA,TWD", "MASTERCARD,TWD",
            "VISA,USD", "MASTERCARD,USD",
            "VISA,ZAR", "MASTERCARD,ZAR"
    })
    public void CreditMoto_Authorization_Exponent2(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }

    @ParameterizedTest(name = "{index} => card={0}, currency={1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "VISA,BHD", "MASTERCARD,BHD",
            "VISA,KWD", "MASTERCARD,KWD",
            "VISA,OMR", "MASTERCARD,OMR"
    })
    public void CreditMoto_Authorization_Exponent3(String cardType, String currency) throws ApiException {
        CreditCardData card = "VISA".equals(cardType) ? motoVisaCard : motoMasterCard;
        Transaction response = card.authorize(AMOUNT)
                .withCurrency(currency)
                .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);
        assertTransactionAmount(response);
    }

    private void assertTransactionAmount(Transaction transaction) {
        assertNotNull(transaction);
        assertEquals(0, AMOUNT.compareTo(transaction.getBalanceAmount()));
    }


    private CreditCardData createCard(String cardNumber, String cardHolderName, boolean moto) {
        CreditCardData card = new CreditCardData();
        card.setNumber(cardNumber);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardHolderName(cardHolderName);
        if (moto) {
            card.setEntryMethod(ManualEntryMethod.Moto);
        }
        return card;
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertTrue(SUCCESS.equals(transaction.getResponseCode()) || "00".equals(transaction.getResponseCode()));
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void AmountEncoding_RoundsAndScalesPerExponent() {
        assertAmountEncoding("JPY", "1235.876", "123588", "1235.88");
        assertAmountEncoding("KRW", "1235.876", "1236", "1236");
        assertAmountEncoding("VND", "1235.876", "1236", "1236");
        assertAmountEncoding("ISK", "1235.876", "1236", "1236");
        assertAmountEncoding("CLP", "1235.876", "123588", "1235.88");
        assertAmountEncoding("USD", "1235.876", "123588", "1235.88");
        assertAmountEncoding("BHD", "1235.876", "1235876", "1235.876");
        assertAmountEncoding("KWD", "1235.876", "1235876", "1235.876");
        assertAmountEncoding("OMR", "1235.876", "1235876", "1235.876");
    }

    /**
     * Verifies that `CurrencyExponentUtils.getExponent(String)` returns expected exponents
     * for known currencies and default fallback scenarios.
     *
     * <p>Covered cases:</p>
     * <ul>
     *   <li>`USD` \-> `2`</li>
     *   <li>`KRW` \-> `0`</li>
     *   <li>`BHD` \-> `3`</li>
     *   <li>`null`, empty, unknown, and lowercase inputs \-> default `2`</li>
     * </ul>
     */
    @Test
    public void getExponent_ShouldReturnExpectedExponent_ForKnownAndFallbackInputs() throws ApiException {
        assertEquals(2, CurrencyExponentUtils.getExponent("USD"));
        assertEquals(0, CurrencyExponentUtils.getExponent("KRW"));
        assertEquals(3, CurrencyExponentUtils.getExponent("BHD"));
        assertEquals(2, CurrencyExponentUtils.getExponent(null));
        assertEquals(2, CurrencyExponentUtils.getExponent(""));
        assertEquals(2, CurrencyExponentUtils.getExponent("ABC"));
        assertEquals(2, CurrencyExponentUtils.getExponent("usd"));
    }


    private static void assertAmountEncoding(String currency, String merchantInput, String expectedWire, String expectedDecoded) {
        BigDecimal typed = new BigDecimal(merchantInput);
        String wire = toNumericCurrencyString(typed, currency);
        assertEquals(expectedWire, wire, currency + ": SDK -> GP-API wire string for merchant input " + merchantInput + ".");

        BigDecimal decoded = fromMinorUnits(wire, currency);
        assertEquals(new BigDecimal(expectedDecoded), decoded,
                currency + ": GP-API -> SDK decoded amount for wire string " + wire + ".");
    }
    private static String toNumericCurrencyString(BigDecimal amount, String currency) {
        return amount.movePointRight(currencyExponent(currency))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private static BigDecimal fromMinorUnits(String wireAmount, String currency) {
        return new BigDecimal(wireAmount).movePointLeft(currencyExponent(currency));
    }
    private static int currencyExponent(String currency) {
        switch (currency) {
            case "BHD":
            case "KWD":
            case "OMR":
                return 3;
            case "ISK":
            case "KRW":
            case "VND":
                return 0;
            case "JPY":
            case "CLP":
            default:
                return 2;
        }
    }
}

