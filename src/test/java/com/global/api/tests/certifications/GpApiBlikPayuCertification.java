package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.BankList;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.tests.gpEcom.BaseGpEComTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.global.api.entities.enums.AlternativePaymentType.BLIK;
import static com.global.api.entities.enums.AlternativePaymentType.OB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GpApiBlikPayuCertification extends BaseGpEComTest {

    static final BigDecimal amount = new BigDecimal(0.02);
    static final String currency = "PLN";
    static final String returnUrl = "https://www.example.com/returnUrl";
    static final String statusUpdateUrl = "https://www.example.com/statusUrl";
    static final String cancelUrl = "https://www.example.com/cancelUrl";
    static final String descriptor = "Test Transaction";
    static final String accountName = "Jane";
    static final String chargeDescription = "New APM";

    @BeforeEach
    public void Init() throws ConfigurationException {
        //BLIK APM configuration
        String APP_ID_BLIK = "ZbFY1jAz6sqq0GAyIPZe1raLCC7cUlpD";
        String APP_KEY_BLIK = "4NpIQJDCIDzfTKhA";
        GpApiConfig gpApiConfigBlik = new GpApiConfig()
                .setAppId(APP_ID_BLIK)
                .setAppKey(APP_KEY_BLIK);
        gpApiConfigBlik.setChannel(Channel.CardNotPresent);
        gpApiConfigBlik.setServiceUrl("https://apis.globalpay.com/ucp");
        gpApiConfigBlik.setEnableLogging(true);
        gpApiConfigBlik.setRequestLogger(new RequestConsoleLogger());
        gpApiConfigBlik.setCountry("PL");

        AccessTokenInfo accessTokenInfoBlik = new AccessTokenInfo();
        accessTokenInfoBlik.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfoBlik.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfigBlik.setAccessTokenInfo(accessTokenInfoBlik);
        ServicesContainer.configureService(gpApiConfigBlik,"BLIK");

        //Payu APM configuration
        String APP_ID_Payu = "ZbFY1jAz6sqq0GAyIPZe1raLCC7cUlpD";
        String APP_KEY_Payu = "4NpIQJDCIDzfTKhA";
        GpApiConfig gpApiConfigPayu = new GpApiConfig()
                .setAppId(APP_ID_Payu)
                .setAppKey(APP_KEY_Payu);
        gpApiConfigPayu.setChannel(Channel.CardNotPresent);
        gpApiConfigPayu.setServiceUrl("https://apis.globalpay.com/ucp");
        gpApiConfigPayu.setEnableLogging(true);
        gpApiConfigPayu.setRequestLogger(new RequestConsoleLogger());
        gpApiConfigPayu.setCountry("PL");

        AccessTokenInfo accessTokenInfoPayu = new AccessTokenInfo();
        accessTokenInfoPayu.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfoPayu.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfigPayu.setAccessTokenInfo(accessTokenInfoPayu);
        ServicesContainer.configureService(gpApiConfigPayu,"PAYU");
    }


    // Sale for Blik APM
    @Test
    public void testBlikApmForSale() throws ApiException {
        AlternativePaymentMethod paymentMethodDetails =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(BLIK)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setCancelUrl(cancelUrl)
                        .setDescriptor(descriptor)
                        .setCountry("PL")
                        .setAccountHolderName(accountName);

        Transaction response =
                paymentMethodDetails
                        .charge(amount)
                        .withCurrency(currency)
                        .withDescription(chargeDescription)
                        .execute("BLIK");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("BLIK",response.getAlternativePaymentResponse().getProviderName().toUpperCase());

    }

    @Test
    public void testBlikApmForRefund_Full() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_zWqOpwd36jzNyELDbynP0O81BlRr3C_0cb0b8116b82";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency("PLN")
                        .execute("BLIK");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    public void testBlikApmForRefund_Partial() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_VdwgW1lpYUCo0fokqYozPJv6cTFsVy_6b20cbc825c3";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(new BigDecimal(0.01))
                        .withCurrency("PLN")
                        .execute("BLIK");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    public void testBlikApmForRefund_OverAmount() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_8z5rgYy4hOofLWigcwAHjpzrJ1tNuY_9925cabac541";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(new BigDecimal(0.05))
                        .withCurrency("PLN")
                        .execute("BLIK");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    public void testPayuApmForSale() throws ApiException {
        AlternativePaymentMethod paymentMethodDetails =
                new AlternativePaymentMethod()
                        .setAlternativePaymentMethodType(OB)
                        .setReturnUrl(returnUrl)
                        .setStatusUpdateUrl(statusUpdateUrl)
                        .setCancelUrl(cancelUrl)
                        .setDescriptor(descriptor)
                        .setCountry("PL")
                        .setAccountHolderName(accountName)
                        .setBank(BankList.MILLENIUM);

        Transaction response =
                paymentMethodDetails
                        .charge(amount)
                        .withCurrency("PLN")
                        .withDescription(chargeDescription)
                        .execute("PAYU");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertNotNull(response.getAlternativePaymentResponse());
        assertNotNull(response.getAlternativePaymentResponse().getRedirectUrl());
        assertEquals("BANK_PAYMENT",response.getAlternativePaymentResponse().getProviderName().toUpperCase());
    }


    @Test
    public void testPayuApmForRefund_Full() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_AdTvvSbWJ3vItVstySSKYl9JN59eUD_371900f48ebd";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency("PLN")
                        .execute("PAYU");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    public void testPayuApmForRefund_Partial() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_XZPuSoRVxSBWGT1diWOiWRBZlwPeT8_ff1ebf5045da";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(new BigDecimal(0.01))
                        .withCurrency("PLN")
                        .execute("PAYU");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    public void testPayuApmForRefund_OverAmount() throws ApiException {
        // For refund we have to run sale test and get Transaction ID from that response and paste here in transactionId.
        // Also go to redirect_url from response of sale and approve by entering the code.
        // After some time when status changed to "Captured" run the refund test.
        String transactionId = "TRN_TZp5ZvK81V7MwSPIhFl7oNI4myyMdz_3e1d14a1271a";

        // create the rebate transaction object
        Transaction transaction = Transaction.fromId(transactionId);

        Transaction response =
                transaction
                        .refund(new BigDecimal(0.05))
                        .withCurrency("PLN")
                        .execute("PAYU");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }
}
