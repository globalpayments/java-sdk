package com.global.api.tests.propay;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.propay.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.ProPayConfig;
import com.global.api.services.ProPayService;
import com.global.api.tests.testdata.TestAccountData;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProPayAccountTests {
    private ProPayService _service;
    private String documentPath=System.getProperty("user.dir")+"\\src\\test\\java\\com\\global\\api\\tests\\propay\\TestData\\TestDocChargeback.docx";
    private String x509CertificatePath=System.getProperty("user.dir")+"\\src\\test\\java\\com\\global\\api\\tests\\propay\\TestData\\testCertificate.crt";
    public ProPayAccountTests() {
        _service = new ProPayService();
        ProPayConfig config = new ProPayConfig();
        config.setCertificationStr("5dbacb0fc504dd7bdc2eadeb7039dd");
        config.setTerminalID("7039dd");
        config.setX509CertificatePath(x509CertificatePath);
        config.setEnvironment(Environment.TEST);
        config.setProPayUS(true);
        config.setEnableLogging(true);
        try {
            ServicesContainer.configureService(config);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void CreateAccount() throws ApiException {
        BankAccountData bankAccountInfo = TestAccountData.getBankAccountData();
        BusinessData userBusinessInfo = TestAccountData.getBusinessData();
        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        ThreatRiskData threatRiskData = TestAccountData.getThreatRiskData();
        SignificantOwnerData significantOwnerData = TestAccountData.getSignificantOwnerData();
        BeneficialOwnerData beneficialOwnerData = TestAccountData.getBeneficialOwnerData();
        CreditCardData creditCardData = TestAccountData.getCreditCardData();
        BankAccountData achData = TestAccountData.getACHData();
        Address mailingAddress = TestAccountData.getMailingAddress();
        BankAccountData secondaryBankAccountData = TestAccountData.getSecondaryBankAccountData();
        DeviceData deviceData = TestAccountData.getDeviceData(1, false);

        Transaction response = _service.createAccount()
                .withBankAccountData(bankAccountInfo)
                .withBusinessData(userBusinessInfo)
                .withUserPersonalData(userPersonalInfo)
                .withThreatRiskData(threatRiskData)
                .withSignificantOwnerData(significantOwnerData)
                .withBeneficialOwnerData(beneficialOwnerData)
                .withCreditCardData(creditCardData)
                .withACHData(achData)
                .withMailingAddress(mailingAddress)
                .withSecondaryBankAccountData(secondaryBankAccountData)
                .withDeviceData(deviceData)
                .withTimeZone("ET")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void EditAccountInformation() throws ApiException {
        UserPersonalData accountPersonalInfo = new UserPersonalData();
        accountPersonalInfo.setDayPhone("4464464464");
        accountPersonalInfo.setEveningPhone("4464464464");
        //accountPersonalInfo.setExternalID("123456789");
        accountPersonalInfo.setFirstName("John");
        accountPersonalInfo.setLastName("Doe");
        accountPersonalInfo.setMiddleInitial("A");
        accountPersonalInfo.setSourceEmail("user2496@user.com");

        Transaction response = _service.editAccount()
                .withAccountNumber("718554713")
                .withUserPersonalData(accountPersonalInfo)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    //working
    public void ResetPassword() throws ApiException {
        Transaction response = _service.resetPassword()
                .withAccountNumber("718567269")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getPassword());
    }

    @Test
    //working
    public void RenewAccount() throws ApiException {
        Transaction response = _service.renewAccount()
                .withAccountNumber("718567269")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void RenewAccountByCreditCard() throws ApiException {
        Transaction response = _service.renewAccount()
                .withAccountNumber("718567269")
                .withRenewalAccountData(TestAccountData.getRenewAccountData(true))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void RenewAccountByBankAccount() throws ApiException {
        Transaction response = _service.renewAccount()
                .withAccountNumber("718567269")
                .withRenewalAccountData(TestAccountData.getRenewAccountData(false))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Ignore
    @Test
    public void UpdateAccountBeneficialOwnership() throws ApiException {
        BeneficialOwnerData beneficialOwners = TestAccountData.getBeneficialOwnerData();

        Transaction response = _service.updateBeneficialOwnershipInfo()
                .withAccountNumber("718567424") // This account must have been created with a beneficial owner count specified, but no owner details passed
                .withBeneficialOwnerData(beneficialOwners)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getBeneficialOwnerDataResults());
    }

    @Ignore
    @Test
    public void DisownAccount() throws ApiException {
        Transaction response = _service.disownAccount()
                .withAccountNumber("718553721") // The account being "disowned" needs to have another affiliation set. Contact propayimplementations@tsys.com and they will set one if necessary
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadDocumentChargeback() throws Exception {
        String documentPath=System.getProperty("user.dir")+"\\src\\test\\java\\com\\global\\api\\tests\\propay\\TestData\\TestDocChargeback.docx";
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("Chargeback 456");
        docUploadData.setTransactionReference("345");
        docUploadData.setDocumentPath(documentPath);

        Transaction response = _service.uploadDocumentChargeback()
                .withAccountNumber("718134204")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadDocument() throws Exception {
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("TestDocCB_12345");
        docUploadData.setDocCategory("Verification");
        docUploadData.setDocumentPath(documentPath);

        Transaction response = _service.uploadDocument()
                .withAccountNumber("718134204")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadDocumentChargebackByDocumentString() throws Exception {
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("TestDocCB_12345");
        docUploadData.setTransactionReference("2");
        docUploadData.setDocumentPath(documentPath);
        docUploadData.setDocType("docx");

        Transaction response = _service.uploadDocumentChargeback()
                .withAccountNumber("718134204")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadDocumentByDocumentString() throws Exception {
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("TestDocCB_12345");
        docUploadData.setDocCategory("Verification");
        docUploadData.setDocumentPath(documentPath);
        docUploadData.setDocType("docx");

        Transaction response = _service.uploadDocument()
                .withAccountNumber("718134204")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ObtainSSOKey() throws ApiException {
        SSORequestData ssoRequestData = new SSORequestData();
        ssoRequestData.setReferrerURL("https://www.globalpaymentsinc.com/");
        ssoRequestData.setIpAddress("40.81.11.219");
        ssoRequestData.setIpSubnetMask("255.255.255.0");

        Transaction response = _service.obtainSSOKey()
                .withAccountNumber("718567269")
                .withSSORequestData(ssoRequestData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getAuthToken());
    }

    // This ProPay method only works with Canadian affiliations
    @Test
    public void UpdateBankAccountOwnershipInfo() throws ApiException {
        //configuration for Canadian affiliations
        ProPayConfig config = new ProPayConfig();
        config.setCertificationStr("7c4ddcba7054a1d9e00bcac4743b98");
        config.setTerminalID("3b98");
        config.setX509CertificateBase64String("MIICpDCCAYygAwIBAgIIfzbbTvSWYSEwDQYJKoZIhvcNAQENBQAwETEPMA0GA1UEAwwGUFJPUEFZMB4XDTIxMDgyMzAwMDAwMFoXDTMxMDgyMzAwMDAwMFowEzERMA8GA1UEAwwIMTI3LjAuMDEwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCpKFpE1GY7YtcFuDFlUbdrHRaAFQWPt+XKFQ8oSpjhHRueL2JRsN0Rq1+eJjx0TYCLNVrmNSSEfYfiVdns2qo7JrZD1XJp7tafomY5t/SK7Q8m5KiEfR3ae+VFD1JpJC8ExZiguBvHITU5ltIAshnqM/GyCwSj6GyLkEMaH8ehRu4rPCpOGuI3YoaB/1gEDsWNhkjyL7B2Wxe+InwVqaI1idfgTUOHMXvrH90DpbWGr6E7GHsha7zh2mdORhy2lVKtO0u7cTQqqIXFFDzxDhfCM+Vx4eT22u4hlfquAi3n3ihqQ2AWgFga+MiJBUWg1J+3OiUZYwCBvNFysCIsBtL5AgMBAAEwDQYJKoZIhvcNAQENBQADggEBAHNGhfegh2Tfjy9/hVcTzDHszdu980M8+nWGGxC+RHkSf8AYtwXLDgX7BTwUVkrs99qAV7U3tTBpIQMBWZBsHiQWYFNgwtmDdEsAsO9A6o4u1XJdlA/ggMkoYD57OcTdhds+77QnUUOVGKVn5pJN0E2OPE2xbBp+KOsavCYmd7NNm/I3d73maGKqNjQqoc6KHVgdtuE+YE4MRTVclAtalbvi7s4mFSi7d3Q+P2LTWKqS5vJWBHSiDCx6bt8htbD+jR1nb+upOecTy4+IN/LzKXJBGTANd4Q4qQQCU8sPp0piC5ZNvnokAgq4SiE0ycZwnU8A5P/OfDetTEK62YLcn8I=");
        config.setProPayUS(false);
        ServicesContainer.configureService(config);

        BankAccountOwnershipData primaryOwner = new BankAccountOwnershipData();
        primaryOwner.setFirstName("Style");
        primaryOwner.setLastName("Stallone");
        Address address = new Address();
        address.setStreetAddress1("3400 N Ashton Blvd");
        address.setStreetAddress2("3401 M Ashton Clad");
        address.setStreetAddress3("3402 L Ashton Blvd");
        address.setCity("Orlando");
        address.setState("FL");
        address.setPostalCode("X0A 0H0");
        address.setCountry("CAN");
        primaryOwner.setOwnerAddress(address);
        primaryOwner.setPhoneNumber("123456789");

        Transaction response = _service.updateBankAccountOwnershipInfo()
                .withAccountNumber("716016890")
                .withPrimaryBankAccountOwner(primaryOwner)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void orderNewDevice() throws ApiException {
        OrderDevice orderDevice = TestAccountData.getOrderNewDeviceData();
        DeviceData deviceData = TestAccountData.getDeviceData(1,false);

        Transaction response = _service.orderDevice()
                .withAccountNumber("718554713")
                .withOrderDevice(orderDevice)
                .withDeviceData(deviceData)
                .execute();

        assertNotNull(response);
        assertEquals("00",response.getResponseCode());
    }
}
