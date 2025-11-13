package com.global.api.tests.testdata;

import com.global.api.entities.Address;
import com.global.api.entities.propay.*;
import com.global.api.paymentMethods.CreditCardData;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestAccountData {
    public static BankAccountData getBankAccountData() {
        BankAccountData bankAccountInformation = new BankAccountData();
         bankAccountInformation.setAccountCountryCode("USA");
         bankAccountInformation.setAccountName("MyBankAccount");
         bankAccountInformation.setAccountNumber("123456789");
         bankAccountInformation.setAccountOwnershipType("C");
         bankAccountInformation.setRoutingNumber("102000076");
         bankAccountInformation.setAccountType("Checking");
         bankAccountInformation.setBankName("MyBank");
        return bankAccountInformation;
    }

    public static BusinessData getBusinessData(){
        BusinessData businessData =new BusinessData();
        businessData.setBusinessLegalName("LegalName");
        businessData.setDoingBusinessAs("PPA");
        businessData.setEmployerIdentificationNumber(String.valueOf(((long)Math.floor(Math.random() * 900000000L))));
        businessData.setBusinessDescription("Accounting Services");
        businessData.setWebsiteURL("https://www.propay.com");
        businessData.setMerchantCategoryCode("5399");
        businessData.setMonthlyBankCardVolume("50000");
        businessData.setAverageTicket("100");
        businessData.setHighestTicket("300");
        businessData.setHighestTicket("50000");

        Address address=new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");
        address.setCountryCode("USA");

        businessData.setBusinessAddress(address);
        return businessData;
    }

    public static UserPersonalData getUserPersonalData(){
        UserPersonalData userPersonalData = new UserPersonalData();
        userPersonalData.setDayPhone("4464464464");
        userPersonalData.setEveningPhone("4464464464");
        userPersonalData.setExternalID(String.valueOf(((long)Math.floor(Math.random() * 9000000000L))));
        userPersonalData.setFirstName("John");
        userPersonalData.setMiddleInitial("");
        userPersonalData.setLastName("Doe");
        userPersonalData.setPhonePIN("1234");
        userPersonalData.setSourceEmail(String.format("user"+(new Random()).nextInt(10000)+"@user.com"));
        userPersonalData.setSsn("123456789");
        userPersonalData.setDateOfBirth("01-01-1981");
        userPersonalData.setTier("TestEIN");

        Address address=new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");
        address.setCountryCode("USA");
        userPersonalData.setPersonalAddress(address);

        return userPersonalData;
    }

    public static ThreatRiskData getThreatRiskData(){
        ThreatRiskData threatRiskData = new ThreatRiskData();
        threatRiskData.setMerchantSourceIP("8.8.8.8");
        threatRiskData.setThreatMetrixPolicy("Default");
        threatRiskData.setThreatMetrixSessionID("dad889c1-1ca4-4fq71-8f6f-807eb4408bc7");
        return threatRiskData;
    }

    public static SignificantOwnerData getSignificantOwnerData(){
        SignificantOwnerData significantOwnerData = new SignificantOwnerData();
        significantOwnerData.setAuthorizedSignerFirstName("John");
        significantOwnerData.setAuthorizedSignerLastName("Doe");
        significantOwnerData.setAuthorizedSignerTitle("Director");
        return significantOwnerData;
    }

    public static BeneficialOwnerData getBeneficialOwnerData(){
        BeneficialOwnerData beneficialOwnerData = new BeneficialOwnerData();
        List<OwnersData> ownersList=new ArrayList<>();
        OwnersData owner1=new OwnersData();
        owner1.setFirstName("First1");
        owner1.setLastName("Last1");
        owner1.setTitle("CEO");
        owner1.setEmail("abc@qamail.com");
        owner1.setDateOfBirth("11-11-1988");
        owner1.setSsn("123545677");
        Address address1=new Address();
        address1.setStreetAddress1("123 Main St.");
        address1.setCity("Downtown");
        address1.setState("NJ");
        address1.setPostalCode("12345");
        address1.setCountryCode("USA");
        owner1.setOwnerAddress(address1);

        OwnersData owner2=new OwnersData();
        owner2.setFirstName("First2");
        owner2.setLastName("Last2");
        owner2.setTitle("Director");
        owner2.setEmail("abc2@qamail.com");
        owner2.setDateOfBirth("11-11-1989");
        owner2.setSsn("123545677");
        Address address2=new Address();
        address2.setStreetAddress1("123 Main St.");
        address2.setCity("Downtown");
        address2.setState("NJ");
        address2.setPostalCode("12345");
        address2.setCountryCode("USA");
        owner2.setOwnerAddress(address2);

        ownersList.add(owner1);
        ownersList.add(owner2);
        beneficialOwnerData.setOwnersList(ownersList);
        beneficialOwnerData.setOwnersCount(ownersList.size());
        return beneficialOwnerData;
    }

    public static CreditCardData getCreditCardData(){
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("4111111111111111");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("123");
        creditCardData.setCardHolderName("Joe Smith");
        return creditCardData;
    }

    public static BankAccountData getACHData(){
        BankAccountData bankAccountData = new BankAccountData();
        bankAccountData.setAccountNumber("123456789");
        bankAccountData.setAccountType("C");
        bankAccountData.setRoutingNumber("102000076");
        return bankAccountData;
    }

    public static Address getMailingAddress(){
        Address mailingAddress = new Address();
        mailingAddress.setStreetAddress1("123 Main Street");
        mailingAddress.setCity("Downtown");
        mailingAddress.setState("NJ");
        mailingAddress.setPostalCode("12345");
        mailingAddress.setCountryCode("USA");
        return mailingAddress;
    }

    public static BankAccountData getSecondaryBankAccountData(){
        BankAccountData bankAccountData = new BankAccountData();
        bankAccountData.setAccountCountryCode("USA");
        bankAccountData.setAccountName("MyBankAccount");
        bankAccountData.setAccountNumber("123456788");
        bankAccountData.setAccountOwnershipType("Personal");
        bankAccountData.setAccountType("C");
        bankAccountData.setRoutingNumber("102000076");
        return bankAccountData;
    }

    public static GrossBillingInformation getGrossBillingInformation(){
        GrossBillingInformation grossBillingInformation = new GrossBillingInformation();
        BankAccountData grossSettleBankData=new BankAccountData();
        grossSettleBankData.setAccountCountryCode("USA");
        grossSettleBankData.setAccountName("MyBankAccount");
        grossSettleBankData.setAccountName("123456788");
        grossSettleBankData.setAccountOwnershipType("Personal");
        grossSettleBankData.setAccountType("C");
        grossSettleBankData.setRoutingNumber("102000076");
        grossSettleBankData.setAccountName("John");

        Address grossSettleAddress = new Address();
        grossSettleAddress.setStreetAddress1("123 Main St.");
        grossSettleAddress.setCity("Downtown");
        grossSettleAddress.setState("NJ");
        grossSettleAddress.setPostalCode("12345");
        grossSettleAddress.setCountryCode("USA");

        CreditCardData creditCardData=new CreditCardData();
        creditCardData.setNumber("4111111111111111");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("123");
        creditCardData.setCardHolderName("Joe Smith");

        grossBillingInformation.setGrossSettleBankData(grossSettleBankData);
        grossBillingInformation.setGrossSettleAddress(grossSettleAddress);
        grossBillingInformation.setGrossSettleCreditCardData(creditCardData);

        return grossBillingInformation;
    }

    public static AccountPermissions getAccountPermission(){
        AccountPermissions accountPermissions = new AccountPermissions();
        accountPermissions.setCCProcessing(true);
        return accountPermissions;
    }

    public static RenewAccountData getRenewAccountData(boolean payByCC){
        RenewAccountData renewAccountData = new RenewAccountData();
        renewAccountData.setTier("TestEIN");

        if (payByCC) {
            renewAccountData.setZipCode("12345");

            CreditCardData creditCardData=new CreditCardData();
            creditCardData.setNumber("4111111111111111");
            creditCardData.setExpMonth(12);
            creditCardData.setExpYear(2025);
            creditCardData.setCvn("123");
            renewAccountData.setCreditCard(creditCardData);
        }
        else {
            renewAccountData.setPaymentBankAccountNumber("123456789");
            renewAccountData.setPaymentBankRoutingNumber("102000076");
            renewAccountData.setPaymentBankAccountType("Checking");
        }
        return renewAccountData;
    }

    public static DeviceData getDeviceData(int numDeviceTypes,boolean withAttributes){
        DeviceData deviceData = new DeviceData();
        List<String> deviceTypes = new ArrayList<>();
        //Certification Credentials
        deviceTypes.add("Secure Submit");
        deviceTypes.add("TestDevice");

        for (int i = 0; i < numDeviceTypes; i++) {
            DeviceInfo deviceInfo = new DeviceInfo();
            if (i >= deviceTypes.size())
                break;
            deviceInfo.setName(deviceTypes.get(i));
            deviceInfo.setQuantity(1);
            if (withAttributes)
            {
                List<DeviceAttributeInfo> deviceAttributeInfo=new ArrayList<>();

                DeviceAttributeInfo info1=new DeviceAttributeInfo();
                info1.setName("GlobalPayments.AMD.OfficeKey");
                info1.setValue("123456");

                deviceAttributeInfo.add(info1);
            }
            deviceData.getDevices().add(deviceInfo);
        }

        return deviceData;
    }

    public static OrderDevice getOrderNewDeviceData(){
        OrderDevice orderDevice = new OrderDevice();
        orderDevice.setShipTo("Test Company");
        orderDevice.setShipToContact("John Q. Public");
        orderDevice.setShipToAddress("2675 W 600 N");
        orderDevice.setShipToAddress2("Apt G");
        orderDevice.setShipToCity("Lindon");
        orderDevice.setShipToState("UT");
        orderDevice.setShipToZip("84042");
        orderDevice.setShipToPhone("801-555-1212");
        orderDevice.setCardholderName("Johnny Cage");
        orderDevice.setCcNum("4111111111111111");
        orderDevice.setExpDate("0427");
        orderDevice.setCvv2("999");
        orderDevice.setBillingZip("84003");

        return orderDevice;
    }

    public static String GetDocumentBase64String(String filepath) {
        return new String(Base64.encodeBase64(filepath.getBytes(StandardCharsets.UTF_8)));
    }
}
