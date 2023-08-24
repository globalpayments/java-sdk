package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.CheckService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestChecks;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NWSCheckTest {
    private CheckService service;
    private eCheck check;
    private Address address;


    public NWSCheckTest() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA03");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");

        address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");

        check = TestChecks.certification();

    }

    @Test
    public void Test_CheckGuarantee_FormattedMICR() throws ApiException {
       check.setAccountNumber("333444555666");
       check.setRoutingNumber("222222222");
       check.setCheckGuarantee(true) ;

        Transaction response = check.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("032000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }
    @Test
    public void Test_CheckVerification_FormattedMICR() throws ApiException {
        check.setAccountNumber("333444555666");
        check.setRoutingNumber("222222222");
        check.setCheckVerify(true) ;

        Transaction response = check.authorize(new BigDecimal(1),true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("042000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }
    @Test
    public void Test_CheckGuarantee_FormattedMICR_extended() throws ApiException {
        check.setAccountNumber("333444555666");
        check.setRoutingNumber("222222222");
        check.setCheckNumber("11111");
        check.setCheckGuarantee(true) ;

        Transaction response = check.authorize(new BigDecimal(1) ,true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("032000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }
    @Test
    public void Test_CheckVerify_FormattedMICR_extended() throws ApiException {
        check.setAccountNumber("333444555666");
        check.setRoutingNumber("222222222");
        check.setCheckNumber("11111");
        check.setCheckVerify(true) ;

        Transaction response = check.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("042000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }
    @Test
    public void Test_CheckGuarantee_RawMICR() throws ApiException {
        check.setAccountNumber("");
        check.setRoutingNumber("");
        check.setCheckNumber("");
        check.setCheckGuarantee(true) ;

        Transaction response = check.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withRawMICRData("⑆111111111⑆11111111⑈00101")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("032000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }
    @Test
    public void Test_CheckVerification_RawMICR() throws ApiException {
        check.setAccountNumber("");
        check.setRoutingNumber("");
        check.setCheckNumber("");
        check.setCheckVerify(true) ;

        Transaction response = check.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withCheckCustomerId("0873629115")
                .withRawMICRData("⑆111111111⑆11111111⑈00101")
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100" , pmi.getMessageTransactionIndicator());
        assertEquals("042000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
    }

}
