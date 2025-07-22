package com.global.api.tests.utils;


import com.global.api.network.Iso8583Bitmap;
import com.global.api.network.NetworkMessage;
import com.global.api.network.elements.*;
import com.global.api.network.enums.*;
import com.global.api.utils.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NetworkMessageTests {
    @Test
    public void bitmap_parse_tests() {
        String original = "9c00000020000000";
        Iso8583Bitmap bitmap = new Iso8583Bitmap(byteFromHex(original));
        assertEquals("1001110000000000000000000000000000100000000000000000000000000000", bitmap.toBinaryString());
        assertEquals(original, bitmap.toHexString());
        assertTrue(bitmap.isPresent(DataElementId.DE_001));
        assertTrue(bitmap.isPresent(DataElementId.DE_004));
        assertTrue(bitmap.isPresent(DataElementId.DE_005));
        assertTrue(bitmap.isPresent(DataElementId.DE_006));
        assertTrue(bitmap.isPresent(DataElementId.DE_035));

        original = "b230450008c90024";
        bitmap = new Iso8583Bitmap(byteFromHex(original));
        assertEquals("1011001000110000010001010000000000001000110010010000000000100100", bitmap.toBinaryString());
        assertEquals(original, bitmap.toHexString());
        assertTrue(bitmap.isPresent(DataElementId.DE_001));
        assertTrue(bitmap.isPresent(DataElementId.DE_003));
        assertTrue(bitmap.isPresent(DataElementId.DE_004));
        assertTrue(bitmap.isPresent(DataElementId.DE_007));
        assertTrue(bitmap.isPresent(DataElementId.DE_011));
        assertTrue(bitmap.isPresent(DataElementId.DE_012));
        assertTrue(bitmap.isPresent(DataElementId.DE_018));
        assertTrue(bitmap.isPresent(DataElementId.DE_022));
        assertTrue(bitmap.isPresent(DataElementId.DE_024));
        assertTrue(bitmap.isPresent(DataElementId.DE_037));
        assertTrue(bitmap.isPresent(DataElementId.DE_041));
        assertTrue(bitmap.isPresent(DataElementId.DE_042));
        assertTrue(bitmap.isPresent(DataElementId.DE_045));
        assertTrue(bitmap.isPresent(DataElementId.DE_048));
        assertTrue(bitmap.isPresent(DataElementId.DE_059));
        assertTrue(bitmap.isPresent(DataElementId.DE_062));

        original = "3030054020c00002";
        bitmap = new Iso8583Bitmap(byteFromHex(original));
        assertEquals("0011000000110000000001010100000000100000110000000000000000000010", bitmap.toBinaryString());
        assertEquals(original, bitmap.toHexString());
        assertTrue(bitmap.isPresent(DataElementId.DE_003));
        assertTrue(bitmap.isPresent(DataElementId.DE_004));
        assertTrue(bitmap.isPresent(DataElementId.DE_011));
        assertTrue(bitmap.isPresent(DataElementId.DE_012));
        assertTrue(bitmap.isPresent(DataElementId.DE_022));
        assertTrue(bitmap.isPresent(DataElementId.DE_024));
        assertTrue(bitmap.isPresent(DataElementId.DE_026));
        assertTrue(bitmap.isPresent(DataElementId.DE_035));
        assertTrue(bitmap.isPresent(DataElementId.DE_041));
        assertTrue(bitmap.isPresent(DataElementId.DE_042));
        assertTrue(bitmap.isPresent(DataElementId.DE_063));

        original = "3230000006c00000";
        bitmap = new Iso8583Bitmap(byteFromHex(original));
        assertEquals("0011001000110000000000000000000000000110110000000000000000000000", bitmap.toBinaryString());
        assertEquals(original, bitmap.toHexString());
        assertTrue(bitmap.isPresent(DataElementId.DE_003));
        assertTrue(bitmap.isPresent(DataElementId.DE_004));
        assertTrue(bitmap.isPresent(DataElementId.DE_007));
        assertTrue(bitmap.isPresent(DataElementId.DE_011));
        assertTrue(bitmap.isPresent(DataElementId.DE_012));
        assertTrue(bitmap.isPresent(DataElementId.DE_038));
        assertTrue(bitmap.isPresent(DataElementId.DE_039));
        assertTrue(bitmap.isPresent(DataElementId.DE_041));
        assertTrue(bitmap.isPresent(DataElementId.DE_042));

        original = "0200000100000000";
        bitmap = new Iso8583Bitmap(byteFromHex(original), 64);
        assertEquals("0000001000000000000000000000000100000000000000000000000000000000", bitmap.toBinaryString());
        assertEquals(original, bitmap.toHexString());
        assertTrue(bitmap.isPresent(DataElementId.DE_071));
        assertTrue(bitmap.isPresent(DataElementId.DE_096));
    }

    @Test
    public void bitmap_build_test() {
        NetworkMessage doc = new NetworkMessage();

        // primary values
        doc.set(DataElementId.DE_003, "");
        doc.set(DataElementId.DE_004, "");
        doc.set(DataElementId.DE_011, "");
        doc.set(DataElementId.DE_012, "");
        doc.set(DataElementId.DE_022, "");
        doc.set(DataElementId.DE_024, "");
        doc.set(DataElementId.DE_025, "");
        doc.set(DataElementId.DE_035, "");
        doc.set(DataElementId.DE_041, "");
        doc.set(DataElementId.DE_042, "");
        doc.set(DataElementId.DE_063, "");
        doc.buildMessage();

        Iso8583Bitmap pbmp = doc.getBitmap();
        assertEquals("0011000000110000000001011000000000100000110000000000000000000010", pbmp.toBinaryString());
        assertEquals("3030058020c00002", pbmp.toHexString());
    }

    @Test
    public void networkMessage_parse_test() {
        String bexString = "b230450028e11824000000000000000030303038303030303030303030303130383930343137313334333530303030313533313830343137303934333439353534314c31303130314231303134433230303234343030353535313132323333343435303d3230313231303130303030303130313135383030303434202020203030303733323635333939303820203535372d454c4556454e5c3132353220464f52455354204156455c53544154454e2049534c414e445c313033303220202020204e59205553413038305824000082000000333420203231323035373130202020202020202030303030303330303031303032444220203331303320594e32363939393939392020202030303030303030303030303030303030492728e8cb6dc5833136413530343031303032363830303434323037313030303033393030313030303132303030303030303030303130383930303031303030303230353932303030313138303431373039343335304442202030303038303030354e323031313031494944303430303031";
        String hexString = "b230450028e11824000000000000000030303038303030303030303030303130383931303130303233303437303030313533313831303130303233303437353534314c31303130314231303134433230303234343030353535313132323333343435303d3230313231303130303030303130313135383030303434202020203030303131323634393833202020203535372d454c4556454e5c3132353220464f52455354204156455c53544154454e2049534c414e445c313033303220202020204e59205553413038305824000082000000333420203231323035373130202020202020202030303030303330303031303032444220203331303320594e32363939393939392020202030303030303030303030303030303030492728e8cb6dc5833136413530343031303032363830303434323037313030303033393030313030303132303030303030303030303130383930303031303030303230353932303030313138303431373039343335304442202030303038303030354e323031313031494944303430303031";

        byte[] data = byteFromHex(hexString);
        NetworkMessage doc = NetworkMessage.parse(data, Iso8583MessageType.CompleteMessage);

        // Secondary bitmap

        // DE3
        DE3_ProcessingCode processingCode = doc.getDataElement(DataElementId.DE_003, DE3_ProcessingCode.class);
        assertNotNull(processingCode);
        assertEquals(DE3_TransactionType.GoodsAndService, processingCode.getTransactionType());
        assertEquals(DE3_AccountType.PinDebitAccount, processingCode.getFromAccount());
        assertEquals(DE3_AccountType.Unspecified, processingCode.getToAccount());
        assertEquals("000800", new String(processingCode.toByteArray()));

        // DE4 - Transaction Amount
        assertEquals(new BigDecimal("10.89"), doc.getAmount(DataElementId.DE_004));

        // DE7 - Transmission DateTime
        //assertEquals("0417134350", doc.getString(DataElementId.DE_007));

        // DE11 - STAN (System Trace Audit Number)
        assertEquals("000153", doc.getString(DataElementId.DE_011));

        // DE12 - Transaction DateTime
        //assertEquals("180417094349", doc.getString(DataElementId.DE_012));

        // DE18 - Merchant Type
        assertEquals("5541", doc.getString(DataElementId.DE_018));

        // DE22 - PosDataCode
        DE22_PosDataCode posDataCode = doc.getDataElement(DataElementId.DE_022, DE22_PosDataCode.class);
        assertEquals("L10101B1014C", new String(posDataCode.toByteArray()));

        // DE24 - Function Code
        assertEquals("200", doc.getString(DataElementId.DE_024));

        // DE35 - Track 2 Data
        assertEquals("4005551122334450=2012101", doc.getString(DataElementId.DE_035));

        // DE37 - Retrieval Reference Number
        assertEquals("000001011580", doc.getString(DataElementId.DE_037));

        // DE41 - Card  Acceptor Terminal Identification Code
        assertEquals("0044    ", doc.getString(DataElementId.DE_041));

        // DE42 - Card Acceptor Identification Code
        //assertEquals("0007326539908  ", doc.getString(DataElementId.DE_042));

        // DE43 - Card Acceptor Name/Location
        DE43_CardAcceptorData cad = doc.getDataElement(DataElementId.DE_043, DE43_CardAcceptorData.class);
        assertEquals("7-ELEVEN\\1252 FOREST AVE\\STATEN ISLAND\\10302     NY USA", new String(cad.toByteArray()));

        // DE48 - Message Control
        DE48_MessageControl messageControl = doc.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);

        // DE 48-2 - Hardware/Software Config
        DE48_2_HardwareSoftwareConfig hsc = messageControl.getHardwareSoftwareConfig();
        assertEquals("34  21205710        ", new String(hsc.toByteArray()));

        // DE48-4 - Batch Number
        assertEquals("0000030001", messageControl.getBatchNumber());

        // DE48-5 - Shift Number
        assertEquals("002", messageControl.getShiftNumber());

        // DE48-11 - Card Type
        assertEquals(DE48_CardType.PINDebitCard, messageControl.getCardType());

        // DE48-14 - PIN Encryption Method
        DE48_14_PinEncryptionMethodology pem = messageControl.getPinEncryptionMethodology();
        assertEquals("31", new String(pem.toByteArray()));

        // DE48-33 - POS Configuration
        DE48_33_PosConfiguration posConfiguration = messageControl.getPosConfiguration();
        assertEquals(" YN", new String(posConfiguration.toByteArray()));

        // DE48-39 - Prior Message Information
        DE48_39_PriorMessageInformation priorMessageInformation = messageControl.getPriorMessageInformation();
        assertEquals("999999    0000000000000000", new String(priorMessageInformation.toByteArray()));

        // DE52
        assertNotNull(doc.getByteArray(DataElementId.DE_052));

        // DE53
        assertEquals("A504010026800442", doc.getString(DataElementId.DE_053));

        // DE59
        assertEquals("000039001000120000000000108900010000205920001180417094350DB  00080005N2", doc.getString(DataElementId.DE_059));

        // DE62
        DE62_CardIssuerData cardIssuerData = doc.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
        assertEquals(1, cardIssuerData.getNumEntries());
        assertEquals("01IID040001", new String(cardIssuerData.toByteArray()));

        // DE62 - First Entry
        DE62_2_CardIssuerEntry cardIssuerEntry = cardIssuerData.getCardIssuerEntries().get(0);
        assertEquals(CardIssuerEntryTag.UniqueDeviceId, cardIssuerEntry.getIssuerTag());
        assertEquals("0001", cardIssuerEntry.getIssuerEntry());
    }

    @Test
    public void DE3_ProcessingCode_test() {
        String original = "013000";

        DE3_ProcessingCode element = new DE3_ProcessingCode().fromByteArray(original.getBytes());
        assertEquals(DE3_TransactionType.Cash, element.getTransactionType());
        assertEquals(DE3_AccountType.CreditAccount, element.getFromAccount());
        assertEquals(DE3_AccountType.Unspecified, element.getToAccount());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "200065";
        element = new DE3_ProcessingCode().fromByteArray(original.getBytes());
        assertEquals(DE3_TransactionType.Return, element.getTransactionType());
        assertEquals(DE3_AccountType.Unspecified, element.getFromAccount());
        assertEquals(DE3_AccountType.CashCard_CashAccount, element.getToAccount());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE22_PosDataCode_test() {
        String original = "B10101210041"; // Swipe, pin
        DE22_PosDataCode element = new DE22_PosDataCode().fromByteArray(original.getBytes());
        assertEquals(DE22_CardDataInputMode.MagStripe, element.getCardDataInputMode());
        assertEquals(DE22_CardHolderAuthenticationMethod.PIN, element.getCardHolderAuthenticationMethod());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "B10101200041"; // Swipe, no pin
        element = new DE22_PosDataCode().fromByteArray(original.getBytes());
        assertEquals(DE22_CardDataInputMode.MagStripe, element.getCardDataInputMode());
        assertEquals(DE22_CardHolderAuthenticationMethod.NotAuthenticated, element.getCardHolderAuthenticationMethod());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "B10101D50041"; // Manual, access code
        element = new DE22_PosDataCode().fromByteArray(original.getBytes());
        assertEquals(DE22_CardDataInputMode.ContactlessEmv, element.getCardDataInputMode());
        assertEquals(DE22_CardHolderAuthenticationMethod.ManualSignatureVerification, element.getCardHolderAuthenticationMethod());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "B10101650041"; // Manual, no access code
        element = new DE22_PosDataCode().fromByteArray(original.getBytes());
        assertEquals(DE22_CardDataInputMode.KeyEntry, element.getCardDataInputMode());
        assertEquals(DE22_CardHolderAuthenticationMethod.ManualSignatureVerification, element.getCardHolderAuthenticationMethod());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE30_OriginalAmounts_tests() {
        String original = "000000001234000000000000";
        DE30_OriginalAmounts amounts = new DE30_OriginalAmounts().fromByteArray(original.getBytes());
        assertEquals(new BigDecimal("12.34"), amounts.getOriginalTransactionAmount());
        assertEquals(new BigDecimal("0"), amounts.getOriginalReconciliationAmount());

        byte[] buffer = amounts.toByteArray();
        assertEquals(original, new String(buffer));

        original = "000000010243000000000000";
        amounts = new DE30_OriginalAmounts().fromByteArray(original.getBytes());
        assertEquals(new BigDecimal("102.43"), amounts.getOriginalTransactionAmount());
        assertEquals(new BigDecimal("0"), amounts.getOriginalReconciliationAmount());

        buffer = amounts.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE43_CardAcceptorData_tests() {
        String original = "Heartland\\701 4TH AVE S\\Minneapolis\\55415     MN USA";

        DE43_CardAcceptorData element = new DE43_CardAcceptorData().fromByteArray(original.getBytes());
        assertEquals("Heartland", element.getAddress().getName());
        assertEquals("701 4TH AVE S", element.getAddress().getStreetAddress1());
        assertEquals("Minneapolis", element.getAddress().getCity());
        assertEquals("55415", element.getAddress().getPostalCode());
        assertEquals("MN", element.getAddress().getState());
        assertEquals("USA", element.getAddress().getCountry());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE44_AdditionalResponseData_tests() {
        String original = "0000APPROVAL 123456";
        DE44_AdditionalResponseData element = new DE44_AdditionalResponseData().fromByteArray(original.getBytes());
        assertEquals(DE44_ActionReasonCode.NoActionReason, element.getActionReasonCode());
        assertEquals("APPROVAL 123456", element.getTextMessage());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "0511";
        element = new DE44_AdditionalResponseData().fromByteArray(original.getBytes());
        assertEquals(DE44_ActionReasonCode.VehicleNotOnFile, element.getActionReasonCode());
        assertEquals("", element.getTextMessage());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE46_FeeAmount_tests() {
        String original = "00840D0000005000000000D00000000840";
        DE46_FeeAmounts element = new DE46_FeeAmounts().fromByteArray(original.getBytes());
        assertEquals(FeeType.TransactionFee, element.getFeeTypeCode());
        assertEquals(Iso4217_CurrencyCode.USD, element.getCurrencyCode());
        assertEquals(new BigDecimal(".5"), element.getAmount());
        assertEquals(new BigDecimal("0"), element.getConversionRate());
        assertEquals(new BigDecimal("0"), element.getReconciliationAmount());
        assertEquals(Iso4217_CurrencyCode.USD, element.getReconciliationCurrencyCode());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_MessageControl_tests() {
        String original = "5824000082000000333420203231323035373130202020202020202030303030303330303031303032444220203331303320594e32363939393939392020202030303030303030303030303030303030";

        DE48_MessageControl element = new DE48_MessageControl().fromByteArray(byteFromHex(original));

        // DE 48-2 - Hardware/Software Config
        DE48_2_HardwareSoftwareConfig hsc = element.getHardwareSoftwareConfig();
        assertEquals("34  21205710        ", new String(hsc.toByteArray()));

        // DE48-4 - Batch Number
        assertEquals("0000030001", element.getBatchNumber());

        // DE48-5 - Shift Number
        assertEquals("002", element.getShiftNumber());

        // DE48-11 - Card Type
        assertEquals(DE48_CardType.PINDebitCard, element.getCardType());

        // DE48-14 - PIN Encryption Method
        DE48_14_PinEncryptionMethodology pem = element.getPinEncryptionMethodology();
        assertEquals("31", new String(pem.toByteArray()));

        // DE48-33 - POS Configuration
        DE48_33_PosConfiguration posConfiguration = element.getPosConfiguration();
        assertEquals(" YN", new String(posConfiguration.toByteArray()));

        // DE48-39 - Prior Message Information
        DE48_39_PriorMessageInformation priorMessageInformation = element.getPriorMessageInformation();
        assertEquals("999999    0000000000000000", new String(priorMessageInformation.toByteArray()));
    }

    @Test
    public void DE48_1_CommDiagnostic_tests() {
        String original = "2181";

        DE48_1_CommunicationDiagnostics element = new DE48_1_CommunicationDiagnostics().fromByteArray(original.getBytes());
        assertEquals(2, element.getCommunicationAttempts());
        assertEquals(DE48_ConnectionResult.LostCarrierAwaitingResponse, element.getConnectionResult());
        assertEquals(DE48_HostConnected.PrimaryHost, element.getHostConnected());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_2_HardwareConfig_tests() {
        String original = "375004010031Q50016A6";

        DE48_2_HardwareSoftwareConfig element = new DE48_2_HardwareSoftwareConfig().fromByteArray(original.getBytes());
        assertEquals("3750", element.getHardwareLevel());
        assertEquals("04010031", element.getSoftwareLevel());
        assertEquals("Q50016A6", element.getOperatingSystemLevel());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_8_CustomerData_tests() {
        String original = "022VEHTAG\\3DRIVERID";

        DE48_8_CustomerData element = new DE48_8_CustomerData().fromByteArray(original.getBytes());
        assertEquals(2, element.getFieldCount());

        String field1 = element.get(DE48_CustomerDataType.VehicleTag);
        assertNotNull(field1);
        assertEquals("VEHTAG", field1);

        String field2 = element.get(DE48_CustomerDataType.DriverId_EmployeeNumber);
        assertNotNull(field2);
        assertEquals("DRIVERID", field2);

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_14_PinEncryptionMethodology_tests() {
        String original = "32";

        DE48_14_PinEncryptionMethodology element = new DE48_14_PinEncryptionMethodology().fromByteArray(original.getBytes());
        assertEquals(DE48_KeyManagementDataCode.DerivedUniqueKeyPerTransaction_DUKPT, element.getKeyManagementDataCode());
        assertEquals(DE48_EncryptionAlgorithmDataCode.TripleDES_2Keys, element.getEncryptionAlgorithmDataCode());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_33_PosConfiguration_tests() {
        String original = "ZY";

        DE48_33_PosConfiguration element = new DE48_33_PosConfiguration().fromByteArray(original.getBytes());
        assertEquals("Z", element.getTimezone());
        assertEquals(true, element.getSupportsPartialApproval());
        assertNull(element.getSupportsReturnBalance());
        assertNull(element.getSupportsCashAtCheckOut());
        assertNull(element.getMobileDevice());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "ZNY";

        element = new DE48_33_PosConfiguration().fromByteArray(original.getBytes());
        assertEquals("Z", element.getTimezone());
        assertEquals(false, element.getSupportsPartialApproval());
        assertEquals(true, element.getSupportsReturnBalance());
        assertNull(element.getSupportsCashAtCheckOut());
        assertNull(element.getMobileDevice());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_34_MessageConfig_tests() {
        String original = "";

        DE48_34_MessageConfiguration element = new DE48_34_MessageConfiguration().fromByteArray(original.getBytes());
        // TODO: needs format testing

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_35_Name_tests() {
        String original = "00John Q Public";

        DE48_Name element = new DE48_Name().fromByteArray(original.getBytes());
        assertEquals(DE48_NameType.CardHolderName, element.getNameType());
        assertEquals(DE48_NameFormat.FreeFormat, element.getNameFormat());
        assertEquals("John Q Public", element.getName());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "01Jane\\\\Doe";

        element = new DE48_Name().fromByteArray(original.getBytes());
        assertEquals(DE48_NameType.CardHolderName, element.getNameType());
        assertEquals(DE48_NameFormat.Delimited_FirstMiddleLast, element.getNameFormat());
        assertEquals("Jane", element.getFirstName());
        assertEquals("", element.getMiddleName());
        assertEquals("Doe", element.getLastName());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_39_PriorMessageInformation_tests() {
        String original = "005005VISA1200000000123456";

        DE48_39_PriorMessageInformation element = new DE48_39_PriorMessageInformation().fromByteArray(original.getBytes());
        assertEquals("005", element.getResponseTime());
        assertEquals("005", element.getConnectTime());
        assertEquals("VISA", element.getCardType());
        assertEquals("1200", element.getMessageTransactionIndicator());
        assertEquals("000000", element.getProcessingCode());
        assertEquals("123456", element.getStan());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE48_40_Address_tests() {
        // STREET ADDRESS FOR BUSINESS
        String original = "00701 FOURTH AVE S Ste 1600\\\\Minneapolis\\MN 55415^^^^^^^^";

        DE48_Address element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.StreetAddress, element.getAddressType());
        assertEquals(DE48_AddressUsage.Business, element.getAddressUsage());

        assertNotNull(element.getAddress());
        assertEquals("701 FOURTH AVE S Ste 1600", element.getAddress().getStreetAddress1());
        assertEquals("Minneapolis", element.getAddress().getCity());
        assertEquals("MN", element.getAddress().getState());
        assertEquals("55415", element.getAddress().getPostalCode());
        assertEquals("", element.getAddress().getCountry());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));


        // ADDRESS VERIFICATION FOR BUSINESS
        original = "1055415    701 4TH AVE S";

        element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.AddressVerification, element.getAddressType());
        assertEquals(DE48_AddressUsage.Business, element.getAddressUsage());

        assertNotNull(element.getAddress());
        assertEquals("701 4TH AVE S", element.getAddress().getStreetAddress1());
        assertEquals("55415", element.getAddress().getPostalCode());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));


        // ADDRESS VERIFICATION FOR HOME
        original = "1155415    123";

        element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.AddressVerification, element.getAddressType());
        assertEquals(DE48_AddressUsage.Home, element.getAddressUsage());

        assertNotNull(element.getAddress());
        assertEquals("123", element.getAddress().getStreetAddress1());
        assertEquals("55415", element.getAddress().getPostalCode());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));


        // ADDRESS VERIFICATION FOR HOME WITH PADDING CHAR
        original = "1175252^^^^";

        element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.AddressVerification, element.getAddressType());
        assertEquals(DE48_AddressUsage.Home, element.getAddressUsage());

        assertNotNull(element.getAddress());
        assertEquals("75252", element.getAddress().getPostalCode());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));


        // HOME PHONE NUMBER
        original = "21\\999\\5550123\\";

        element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.PhoneNumber, element.getAddressType());
        assertEquals(DE48_AddressUsage.Home, element.getAddressUsage());

        assertNotNull(element.getPhoneNumber());
        assertTrue(StringUtils.isNullOrEmpty(element.getPhoneNumber().getCountryCode()));
        assertEquals("999", element.getPhoneNumber().getAreaCode());
        assertEquals("5550123", element.getPhoneNumber().getNumber());
        assertEquals("", element.getPhoneNumber().getExtension());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));


        // HOME EMAIL ADDRESS
        original = "31myname@some_place_some_where.com";

        element = new DE48_Address().fromByteArray(original.getBytes());
        assertEquals(DE48_AddressType.Email, element.getAddressType());
        assertEquals(DE48_AddressUsage.Home, element.getAddressUsage());

        assertNotNull(element.getEmail());
        assertEquals("myname@some_place_some_where.com", element.getEmail());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE54_AmountsAdditional_tests() {
        String original = "0056840D000000000027";

        DE54_AmountsAdditional element = new DE54_AmountsAdditional().fromByteArray(original.getBytes());

        DE54_AdditionalAmount amount = element.get(DE3_AccountType.Unspecified, DE54_AmountTypeCode.AmountTax);
        assertNotNull(amount);
        assertEquals(DE3_AccountType.Unspecified, amount.getAccountType());
        assertEquals(DE54_AmountTypeCode.AmountTax, amount.getAmountType());
        assertEquals(Iso4217_CurrencyCode.USD, amount.getCurrencyCode());
        assertEquals(new BigDecimal("0.27"), amount.getAmount());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE56_OriginalDataElements_tests() {
        String original = "1100123456131211100908";

        DE56_OriginalDataElements element = new DE56_OriginalDataElements().fromByteArray(original.getBytes());
        assertEquals("1100", element.getMessageTypeIdentifier());
        assertEquals("123456", element.getSystemTraceAuditNumber());
        assertEquals("131211100908", element.getTransactionDateTime());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE62_CardIssuerData_tests() {
        String original = "02IRA0205ITX08APPROVED";

        DE62_CardIssuerData element = new DE62_CardIssuerData().fromByteArray(original.getBytes());
        assertEquals(2, element.getNumEntries());
        assertEquals(2, element.getCardIssuerEntries().size());

        DE62_2_CardIssuerEntry entry = element.getCardIssuerEntries().get(CardIssuerEntryTag.OriginalResponse_ActionCode);
        assertEquals(CardIssuerEntryTag.OriginalResponse_ActionCode, entry.getIssuerTag());
        assertEquals("05", entry.getIssuerEntry());

        entry = element.getCardIssuerEntries().get(CardIssuerEntryTag.DisplayText);
        assertEquals(CardIssuerEntryTag.DisplayText, entry.getIssuerTag());
        assertEquals("APPROVED", entry.getIssuerEntry());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE63_ProductData_HeartlandStandardFormat_tests() {
        String original = "00O00182\\\\\\75\\";

        DE63_ProductData element = new DE63_ProductData().fromByteArray(original.getBytes());
        assertEquals(ProductDataFormat.HeartlandStandardFormat, element.getProductDataFormat());
        assertEquals(ProductCodeSet.Heartland, element.getProductCodeSet());
        assertEquals(ServiceLevel.Other_NonFuel, element.getServiceLevel());
        assertEquals(1, element.getProductCount());

        DE63_ProductDataEntry entry = element.getProductDataEntries().get("82");
        assertNotNull(entry);
        assertNull(entry.getUnitOfMeasure());
        assertNull(entry.getQuantity());
        assertNull(entry.getPrice());
        assertEquals(new BigDecimal("0.75"), entry.getAmount());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE63_ProductData_AnsiX9_tests() {
        String original = "13S05001G2417\\31199\\500\\411U010\\2179\\1790\\461O\\\\99\\905O\\\\179\\950O\\\\97\\";

        DE63_ProductData element = new DE63_ProductData().fromByteArray(original.getBytes());
        assertEquals(ProductDataFormat.ANSI_X9_TG23_Format, element.getProductDataFormat());
        assertEquals(ProductCodeSet.Conexxus_3_Digit, element.getProductCodeSet());
        assertEquals(ServiceLevel.SelfServe, element.getServiceLevel());
        assertEquals(5, element.getProductCount());

        // GAS ENTRY
        DE63_ProductDataEntry entry = element.getProductDataEntries().get("001");
        assertNotNull(entry);
        assertEquals(UnitOfMeasure.Gallons, entry.getUnitOfMeasure());
        assertEquals(new BigDecimal("4.17"), entry.getQuantity());
        assertEquals(new BigDecimal("1.199"), entry.getPrice());
        assertEquals(new BigDecimal("5"), entry.getAmount());

        // CIGARETTES
        entry = element.getProductDataEntries().get("411");
        assertNotNull(entry);
        assertEquals(UnitOfMeasure.Units, entry.getUnitOfMeasure());
        assertEquals(new BigDecimal("10"), entry.getQuantity());
        assertEquals(new BigDecimal("1.79"), entry.getPrice());
        assertEquals(new BigDecimal("17.9"), entry.getAmount());

        // MILK
        entry = element.getProductDataEntries().get("461");
        assertNotNull(entry);
        assertEquals(UnitOfMeasure.OtherOrUnknown, entry.getUnitOfMeasure());
        assertNull(entry.getQuantity());
        assertNull(entry.getPrice());
        assertEquals(new BigDecimal("0.99"), entry.getAmount());

        // COUPON
        entry = element.getProductDataEntries().get("905");
        assertNotNull(entry);
        assertEquals(UnitOfMeasure.OtherOrUnknown, entry.getUnitOfMeasure());
        assertNull(entry.getQuantity());
        assertNull(entry.getPrice());
        assertEquals(new BigDecimal("1.79"), entry.getAmount());

        // TAX
        entry = element.getProductDataEntries().get("950");
        assertNotNull(entry);
        assertEquals(UnitOfMeasure.OtherOrUnknown, entry.getUnitOfMeasure());
        assertNull(entry.getQuantity());
        assertNull(entry.getPrice());
        assertEquals(new BigDecimal("0.97"), entry.getAmount());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE103_Check_MICR_Data_test() {
        // account and routing
        String original = "222222222\\333444555666";

        DE103_Check_MICR_Data element = new DE103_Check_MICR_Data().fromByteArray(original.getBytes());
        assertEquals("222222222", element.getTransitNumber());
        assertEquals("333444555666", element.getAccountNumber());
        assertEquals("", element.getSequenceNumber());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        // account, routing and sequence number
        original = "222222222\\333444555666\\101";

        element = new DE103_Check_MICR_Data().fromByteArray(original.getBytes());
        assertEquals("222222222", element.getTransitNumber());
        assertEquals("333444555666", element.getAccountNumber());
        assertEquals("101", element.getSequenceNumber());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        // account number only
        original = "\\333444555666";

        element = new DE103_Check_MICR_Data().fromByteArray(original.getBytes());
        assertEquals("", element.getTransitNumber());
        assertEquals("333444555666", element.getAccountNumber());
        assertEquals("", element.getSequenceNumber());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE123_ReconciliationTotals_StandardFormat_tests() {
        String original = "0010002   PL  2\\4206\\002   VI  9\\64527\\002   MC  8\\54682\\002   OH  12\\74432\\002   CT  31\\197847\\002   DB  5\\17539\\299   CT  1\\1732\\299   DB  1\\2249\\007   CT  2\\5706\\007   DB  1\\2159\\";

        DE123_ReconciliationTotals element = new DE123_ReconciliationTotals().fromByteArray(original.getBytes());
        assertEquals("00", element.getEntryFormat());
        assertEquals(10, element.getEntryCount());

        int index = 0;
        DE123_ReconciliationTotal total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("PL", total.getCardType());
        assertEquals(2, total.getTransactionCount());
        assertEquals(new BigDecimal("42.06"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("VI", total.getCardType());
        assertEquals(9, total.getTransactionCount());
        assertEquals(new BigDecimal("645.27"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("MC", total.getCardType());
        assertEquals(8, total.getTransactionCount());
        assertEquals(new BigDecimal("546.82"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("OH", total.getCardType());
        assertEquals(12, total.getTransactionCount());
        assertEquals(new BigDecimal("744.32"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("CT", total.getCardType());
        assertEquals(31, total.getTransactionCount());
        assertEquals(new BigDecimal("1978.47"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.DebitLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("DB", total.getCardType());
        assertEquals(5, total.getTransactionCount());
        assertEquals(new BigDecimal("175.39"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.AllVoids_Voids, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("CT", total.getCardType());
        assertEquals(1, total.getTransactionCount());
        assertEquals(new BigDecimal("17.32"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.AllVoids_Voids, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("DB", total.getCardType());
        assertEquals(1, total.getTransactionCount());
        assertEquals(new BigDecimal("22.49"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.CreditLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("CT", total.getCardType());
        assertEquals(2, total.getTransactionCount());
        assertEquals(new BigDecimal("57.06"), total.getTotalAmount());

        total = element.getTotals().get(index++);
        assertEquals(DE123_TransactionType.CreditLessReversals, total.getTransactionType());
        assertEquals(DE123_TotalType.NotSpecific, total.getTotalType());
        assertEquals("DB", total.getCardType());
        assertEquals(1, total.getTransactionCount());
        assertEquals(new BigDecimal("21.59"), total.getTotalAmount());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE124_SundryData_AuthCaptureData_tests() {
        String original = "0102040001234TA99990000000092551234567890123456";

        DE124_SundryData element = new DE124_SundryData().fromByteArray(original.getBytes());
        assertEquals(1, element.getEntryCount());

        DE124_SundryEntry entry = element.getEntries().get(0);
        assertEquals(DE124_SundryDataTag.PiggyBack_AuthCaptureData, entry.getTag());
        assertEquals("001234", entry.getSystemTraceAuditNumber());
        assertEquals("TA9999", entry.getApprovalCode());
        assertEquals(new BigDecimal("92.55"), entry.getTransactionAmount());
        assertEquals("1234567890123456", entry.getCustomerData());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE124_SundryData_AuthCaptureData_CustomerData_tests() {
        String original = "0200022123456789012345678901202024001234TA9999000000009255";

        DE124_SundryData element = new DE124_SundryData().fromByteArray(original.getBytes());
        assertEquals(2, element.getEntryCount());

        DE124_SundryEntry entry = element.getEntries().get(0);
        assertEquals(DE124_SundryDataTag.ClientSuppliedData, entry.getTag());
        assertEquals("1234567890123456789012", entry.getCustomerData());

        entry = element.getEntries().get(1);
        assertEquals(DE124_SundryDataTag.PiggyBack_AuthCaptureData, entry.getTag());
        assertEquals("001234", entry.getSystemTraceAuditNumber());
        assertEquals("TA9999", entry.getApprovalCode());
        assertEquals(new BigDecimal("92.55"), entry.getTransactionAmount());
        assertEquals("", entry.getCustomerData());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void DE127_ForwardingData_tests() {
        String original = "00";

        DE127_ForwardingData element = new DE127_ForwardingData().fromByteArray(original.getBytes());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    private byte[] byteFromHex(String s) {
        return StringUtils.bytesFromHex(s);
    }
}
