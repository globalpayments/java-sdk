package com.global.api.tests.propay.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.DocumentCategory;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.FileType;
import com.global.api.entities.enums.TermsVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.propay.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.ProPayConfig;
import com.global.api.services.ProPayService;
import com.global.api.tests.testdata.TestAccountData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PropayAccount {
    private ProPayService _service;
    private String documentPath = System.getProperty("user.dir") + "\\src\\test\\java\\com\\global\\api\\tests\\propay\\TestData\\TestDocChargeback.docx";
    private String x509CertificatePath = System.getProperty("user.dir") + "\\src\\test\\java\\com\\global\\api\\tests\\propay\\Certification\\testCertificate.crt";

    public PropayAccount() {
        _service = new ProPayService();
        ProPayConfig config = new ProPayConfig();

        // Certification Credentials
        config.setCertificationStr("4ee64cbd706400fb4a34e65aab6f48");
        config.setTerminalID("ab6f48");

        config.setX509CertificatePath(x509CertificatePath);
        // config.setX509CertificateBase64String("MIICpDCCAYygAwIBAgIIS7Y5fijJytIwDQYJKoZIhvcNAQENBQAwETEPMA0GA1UEAwwGUFJPUEFZMB4XDTE5MDkxOTAwMDAwMFoXDTI5MDkxOTAwMDAwMFowEzERMA8GA1UEAwwIMTI3LjAuMDEwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCCwvq2ho43oeeGX3L9+2aD7bna7qjdLwWumeIpwhPZLa44MeQ5100wy4W2hKk3pOb5yaHqyhzoHDriveQnq/EpZJk9m7sizXsxZtBHtt+wghSZjdNhnon3R54SH5J7oEPybRSAKXSEzHjN+kCu7W3TmXSLve6YuODnjUpbOcAsHG2wE+zpCoEbe8toH5Tt7g8HzEc5mJYkkILTq6j9pwDE50r2NVbV3SXwmQ1ifxf54Z9EFB5bQv5cI3+GL/VwlQeJdiKMGj1rs8zTR8TjbAjVlJbz6bBkFItUsqexgwAHIJZAaU7an8ZamGRlPjf6dp3mOEu4B47igNj5KOSgCNdRAgMBAAEwDQYJKoZIhvcNAQENBQADggEBAF88u367yrduqd3PfEIo2ClaI2QPRIIWKKACMcZDl3z1BzVzNFOZNG2vLcSuKnGRH89tJPCjyxdJa0RyDTkXMSLqb5FgUseEjmj3ULAvFqLZNW35PY9mmlmCY+S3CC/bQR4iyPLo8lsRq0Nl6hlvB440+9zS8UQjtc2957QgcXfD427UJb698gXzsfQcNeaQWy8pNm7FzDfHTJbo/t6FOpmfR+RMZky9FrlWabInkrkf3w2XJL0uUAYU9jGQa+l/vnZD2KNzs1mO1EqkS6yB/fsn85mkgGe4Vfbo9GQ/S+KmDujewFA0ma7O03fy1W5v6Amn/nAcFTCddVL3BDNEtOM=");

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
    public void Disbursement_Account_Boarding_01() throws ApiException {
        BeneficialOwnerData beneficialOwnerData = new BeneficialOwnerData();
        List<OwnersData> ownersList = new ArrayList<>();
        OwnersData owner1 = new OwnersData();
        owner1.setFirstName("First1");
        owner1.setLastName("Last1");
        owner1.setTitle("CEO");
        owner1.setEmail("abc@qamail.com");
        owner1.setDateOfBirth("11-11-1988");
        owner1.setSsn("123545677");
        Address address1 = new Address();
        address1.setStreetAddress1("123 Main St.");
        address1.setCity("Downtown");
        address1.setState("NJ");
        address1.setPostalCode("12345");
        address1.setCountryCode("USA");
        owner1.setOwnerAddress(address1);
        ownersList.add(owner1);
        beneficialOwnerData.setOwnersList(ownersList);
        beneficialOwnerData.setOwnersCount(ownersList.size());

        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        userPersonalInfo.setTier("CardOnly");

        Transaction response = _service.createAccount()
                .withUserPersonalData(userPersonalInfo)
                .withBeneficialOwnerData(beneficialOwnerData)
                .withTimeZone("ET")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void MerchantBoarding_EcomDeviceOrder_01() throws ApiException {
        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        userPersonalInfo.setTier("test");
        userPersonalInfo.setIpSignup("4.14.150.14");
        userPersonalInfo.setUsCitizen(true);
        userPersonalInfo.setBOAttestation(true);
        userPersonalInfo.setTermsAcceptanceIP("4.14.150.145");
        userPersonalInfo.setTermsVersion(TermsVersion.merchant_US);
        userPersonalInfo.setNotificationEmail("Partner@partner.com");

        BusinessData businessData = TestAccountData.getBusinessData();
        BankAccountData bankAccountData = TestAccountData.getBankAccountData();
        CreditCardData data = TestAccountData.getCreditCardData();
        BeneficialOwnerData beneficialOwnerData = TestAccountData.getBeneficialOwnerData();

        DeviceData deviceData = new DeviceData();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setName("Secure Submit");
        deviceInfo.setQuantity(1);
        deviceData.getDevices().add(deviceInfo);

        Transaction response = _service.createAccount()
                .withUserPersonalData(userPersonalInfo)
                .withBeneficialOwnerData(beneficialOwnerData)
                .withTimeZone("UTC")
                .withBankAccountData(bankAccountData)
                .withDeviceData(deviceData)
                .withCreditCardData(data)
                .withBusinessData(businessData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void MerchantBoarding_EcomAndPhysicalDevice_01() throws ApiException {
        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        userPersonalInfo.setTier("test");
        userPersonalInfo.setIpSignup("4.14.150.14");
        userPersonalInfo.setUsCitizen(true);
        userPersonalInfo.setBOAttestation(true);
        userPersonalInfo.setTermsAcceptanceIP("4.14.150.145");
        userPersonalInfo.setTermsVersion(TermsVersion.merchant_US);
        userPersonalInfo.setNotificationEmail("Partner@partner.com");
        userPersonalInfo.setDayPhone("9860668923");
        userPersonalInfo.setEveningPhone("9860668923");
        userPersonalInfo.setDateOfBirth("07-15-1989");

        BusinessData businessData = TestAccountData.getBusinessData();

        BankAccountData bankAccountInformation = new BankAccountData();
        bankAccountInformation.setAccountCountryCode("USA");
        bankAccountInformation.setAccountName("Account Name");
        bankAccountInformation.setAccountNumber("123456");
        bankAccountInformation.setAccountOwnershipType("Personal");
        bankAccountInformation.setRoutingNumber("083908420");
        bankAccountInformation.setAccountType("Checking");
        bankAccountInformation.setBankName("First Union");

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("4111111111111111");
        creditCardData.setExpMonth(9);
        creditCardData.setExpYear(2023);
        creditCardData.setCvn("999");
        creditCardData.setCardHolderName("Sylvester Stallone");

        BeneficialOwnerData beneficialOwnerData = TestAccountData.getBeneficialOwnerData();

        DeviceData deviceData = TestAccountData.getDeviceData(2, false);

        Transaction response = _service.createAccount()
                .withUserPersonalData(userPersonalInfo)
                .withBeneficialOwnerData(beneficialOwnerData)
                .withTimeZone("UTC")
                .withBankAccountData(bankAccountInformation)
                .withDeviceData(deviceData)
                .withCreditCardData(creditCardData)
                .withBusinessData(businessData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void MerchantBoarding_KYCStatus66_01() throws ApiException {
        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        userPersonalInfo.setTier("test");
        userPersonalInfo.setIpSignup("4.14.150.14");
        userPersonalInfo.setUsCitizen(true);
        userPersonalInfo.setBOAttestation(true);
        userPersonalInfo.setTermsAcceptanceIP("4.14.150.145");
        userPersonalInfo.setTermsVersion(TermsVersion.merchant_US);
        userPersonalInfo.setNotificationEmail("Partner@partner.com");
        userPersonalInfo.setDateOfBirth("01-01-1971");

        BusinessData businessData = TestAccountData.getBusinessData();
        BankAccountData bankAccountData = TestAccountData.getBankAccountData();
        CreditCardData data = TestAccountData.getCreditCardData();
        BeneficialOwnerData beneficialOwnerData = TestAccountData.getBeneficialOwnerData();

        DeviceData deviceData = new DeviceData();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setName("Secure Submit");
        deviceInfo.setQuantity(1);
        deviceData.getDevices().add(deviceInfo);

        Transaction response = _service.createAccount()
                .withUserPersonalData(userPersonalInfo)
                .withBeneficialOwnerData(beneficialOwnerData)
                .withTimeZone("UTC")
                .withBankAccountData(bankAccountData)
                .withDeviceData(deviceData)
                .withCreditCardData(data)
                .withBusinessData(businessData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void MerchantAccountEdit_MinimumBankEdit_42() throws ApiException {
        BankAccountData bankAccountInformation = new BankAccountData();
        bankAccountInformation.setAccountCountryCode("USA");
        bankAccountInformation.setBankName("MyBank");
        bankAccountInformation.setAccountType("Checking");
        bankAccountInformation.setAccountNumber("123456789");
        bankAccountInformation.setAccountOwnershipType("C");
        bankAccountInformation.setRoutingNumber("102000076");

        Transaction response = _service.editAccount()
                .withAccountNumber("718570872")
                .withBankAccountData(bankAccountInformation)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Renew_Account_39() throws ApiException {
        Transaction response = _service.renewAccount()
                .withAccountNumber("718570870")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Reset_Password_32() throws ApiException {
        Transaction response = _service.resetPassword()
                .withAccountNumber("718570870")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getPassword());
    }

    @Test
    public void MoveAcccountOffAffiliation_41() throws ApiException {
        Transaction response = _service.disownAccount()
                .withAccountNumber("718570965")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void CreateAccount_01_for_UpdateBeneficialOwnerData() throws ApiException {
        BeneficialOwnerData beneficialOwnerData = new BeneficialOwnerData();
        beneficialOwnerData.setOwnersCount(2); // beneficial owner data with owner's count only

        UserPersonalData userPersonalInfo = TestAccountData.getUserPersonalData();
        userPersonalInfo.setTier("CardOnly");

        Transaction response = _service.createAccount()
                .withBeneficialOwnerData(beneficialOwnerData)
                .withUserPersonalData(userPersonalInfo)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UpdateBeneficialOwnerData_44() throws ApiException {
        BeneficialOwnerData beneficialOwnerData = TestAccountData.getBeneficialOwnerData();

        Transaction response = _service.updateBeneficialOwnershipInfo()
                .withAccountNumber("718576576") // This account must have been created with a beneficial owner count specified, but no owner details passed
                .withBeneficialOwnerData(beneficialOwnerData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getBeneficialOwnerDataResults());
    }

    @Test
    public void UploadRegularDoc_47() throws Exception {
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("TestDocCB_12345");
        docUploadData.setDocCategory(DocumentCategory.VERIFICATION);
        docUploadData.setDocumentPath(documentPath);

        Transaction response = _service.uploadDocument()
                .withAccountNumber("718570870")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadChargebackDoc_46() throws Exception {
        String documentPath = System.getProperty("user.dir") + "\\src\\test\\java\\com\\global\\api\\tests\\propay\\TestData\\TestDocChargeback.docx";
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("Chargeback 456");
        docUploadData.setTransactionReference("1");
        docUploadData.setDocumentPath(documentPath);

        Transaction response = _service.uploadDocumentChargeback()
                .withAccountNumber("718569966")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void UploadChargebackDoc_ByDocumentString_46() throws Exception {
        DocumentUploadData docUploadData = new DocumentUploadData();
        docUploadData.setDocumentName("TestDocCB_12345");
        docUploadData.setTransactionReference("1");
        docUploadData.setDocumentPath(documentPath);
        docUploadData.setDocType(FileType.DOCX);

        Transaction response = _service.uploadDocumentChargeback()
                .withAccountNumber("718569966")
                .withDocumentUploadData(docUploadData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void SSOToken_300() throws ApiException {
        SSORequestData ssoRequestData = new SSORequestData();
        ssoRequestData.setReferrerURL("https://www.globalpaymentsinc.com/");
        ssoRequestData.setIpAddress("40.81.11.219");
        ssoRequestData.setIpSubnetMask("255.255.255.0");

        Transaction response = _service.obtainSSOKey()
                .withAccountNumber("718570870")
                .withSSORequestData(ssoRequestData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getProPayResponseData().getAuthToken());
    }

    @Test
    public void orderNewDevice() throws ApiException {
        OrderDevice orderDevice = TestAccountData.getOrderNewDeviceData();
        DeviceData deviceData = TestAccountData.getDeviceData(1, false);

        Transaction response = _service.orderDevice()
                .withAccountNumber("718579267")
                .withOrderDevice(orderDevice)
                .withDeviceData(deviceData)
                .execute();

        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

}
