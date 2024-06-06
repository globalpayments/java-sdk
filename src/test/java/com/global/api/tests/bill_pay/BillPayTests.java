package com.global.api.tests.bill_pay;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.HostedPaymentData;
import com.global.api.entities.Transaction;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.LoadHostedPaymentResponse;
import com.global.api.entities.billing.LoadSecurePayResponse;
import com.global.api.entities.Schedule;
import com.global.api.entities.billing.enums.InitialPaymentMethod;
import com.global.api.entities.billing.enums.RecurringAuthorizationType;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.serviceConfigs.BillPayConfig;
import com.global.api.services.BillPayService;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import com.global.api.entities.TransactionSummary;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class BillPayTests {
    eCheck ach;
    CreditCardData clearTextCredit;
    Address address;
    Customer customer;
    Bill bill;
    List<Bill> bills;
    Bill billLoad;
    Bill blindBill;
    private static final String SECOND_INSTANCE_DATE_EXCEPTION = "Second Instance Date is required for the semi-monthly schedule.";
    private static final String PRIMARY_ACCOUNT_TOKEN_REQUIRED_EXCEPTION = "Primary token is required to perform recurring transaction.";
    private static final String SCHEDULE_TYPE_REQUIRED_EXCEPTION = "Schedule Type is required to perform recurring transaction.";
    private static final String QUICK_PAY_TOKEN_EXCEPTION = "Quick Pay token must be provided for this transaction";
    private static final String ORDER_ID_EXCEPTION = "The search criteria resulted in no transactions found. Please verify search criteria.";
    private static final String FIRST_NAME = "Account";
    private static final String LAST_NAME = "Update";
    private static final String EMAIL_ID = "account.update@test.com";

    public BillPayTests() throws ConfigurationException {

        BillPayConfig config = new BillPayConfig();
        config.setMerchantName("Dev_Exp_Team_Merchant");
        config.setUsername("DevExpTeam");
        config.setPassword("devexpteam_R0cks!");
        config.setServiceUrl(ServiceEndpoints.BILLPAY_CERTIFICATION.getValue());
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);

        ach = new eCheck();
        ach.setAccountNumber("12345");
        ach.setRoutingNumber("064000017");
        ach.setAccountType(AccountType.Checking);
        ach.setCheckType(CheckType.Business);
        ach.setSecCode(SecCode.Web);
        ach.setCheckHolderName("Tester");
        ach.setBankName("Regions");

        clearTextCredit = new CreditCardData();
        clearTextCredit.setNumber("4444444444444448");
        clearTextCredit.setExpMonth(12);
        clearTextCredit.setExpYear(2025);
        clearTextCredit.setCvn("123");
        clearTextCredit.setCardHolderName("Test Tester");

        address = new Address();
        address.setStreetAddress1("1234 Test St");
        address.setStreetAddress2("Apt 201");
        address.setCity("Auburn");
        address.setState("AL");
        address.setCountry("US");
        address.setPostalCode("12345");

        customer = new Customer();
        customer.setAddress(address);
        customer.setEmail("testemailaddress@e-hps.com");
        customer.setFirstName("Test");
        customer.setLastName("Tester");
        customer.setHomePhone("555-555-4444");

        bill = new Bill();
        bill.setAmount(new BigDecimal(50));
        bill.setIdentifier1("12345");

        Bill bill1 = new Bill();
        bill1.setBillType("Tax Payments");
        bill1.setIdentifier1("123");
        bill1.setAmount(new BigDecimal(10));
        Bill bill2 = new Bill();
        bill2.setBillType("Tax Payments");
        bill2.setIdentifier1("321");
        bill2.setAmount(new BigDecimal(10));
        bills = new ArrayList<>();
        bills.add(bill1);
        bills.add(bill2);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + 3);

        billLoad = new Bill();
        billLoad.setAmount(new BigDecimal(50));
        billLoad.setBillType("Tax Payments");
        billLoad.setIdentifier1("12345");
        billLoad.setIdentifier2("23456");
        billLoad.setBillPresentment(BillPresentment.FULL);
        billLoad.setDueDate(now.getTime());
        billLoad.setCustomer(customer);

        now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + 1);

        blindBill = new Bill();
        blindBill.setAmount(new BigDecimal(50));
        blindBill.setBillType("Tax Payments");
        blindBill.setIdentifier1("12345");
        blindBill.setIdentifier2("23456");
        blindBill.setBillPresentment(BillPresentment.FULL);
        blindBill.setDueDate(now.getTime());
        blindBill.setCustomer(customer);
    }

    // #region Authorization Builder Cases

    @Test
    public void Charge_WithSingleBill_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        Transaction result = clearTextCredit.charge(bill.getAmount())
            .withAddress(address)
            .withBills(bill)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();

        validateSuccesfulTransaction(result);
    }
    @Test
    public void Charge_WithMultipleBills_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal totalAmount = new BigDecimal(0);
        
        for (Bill bill : bills) {
            totalAmount = totalAmount.add(bill.getAmount());
        }
        
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, totalAmount);

        Transaction result = clearTextCredit
            .charge(totalAmount)
            .withAddress(address)
            .withBills(bills)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();

        validateSuccesfulTransaction(result);
    }
    @Test
    public void Tokenize_UsingCreditCard_ReturnsToken() throws ApiException {
        Address address = new Address();
        address.setPostalCode("12345");

        Transaction result = clearTextCredit.verify()
            .withAddress(address)
            .withRequestMultiUseToken(true)
            .execute();

        assertFalse(StringUtils.isNullOrEmpty(result.getToken()));
    }
    @Test
    public void UpdateTokenExpiry_UsingCreditCardToken_DoesNotThrow() throws ApiException {
        Address address = new Address();
        address.setPostalCode("12345");

        Transaction result = clearTextCredit.verify()
            .withAddress(address)
            .withRequestMultiUseToken(true)
            .execute();

        assertFalse(StringUtils.isNullOrEmpty(result.getToken()));

        try {
            clearTextCredit.setToken(result.getToken());
            clearTextCredit.setExpMonth(12);
            clearTextCredit.setExpYear(2022);

            clearTextCredit.updateTokenExpiry();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Tokenize_UsingACH_ReturnsToken() throws ApiException {
        Address address = new Address();
        address.setPostalCode("12345");

        Transaction result = ach.verify()
            .withAddress(address)
            .execute();

        assertFalse(StringUtils.isNullOrEmpty(result.getToken()));
    }
    @Test
    public void Charge_UsingTokenizedCreditCard_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        Address address = new Address();
        address.setPostalCode("12345");

        Transaction result = clearTextCredit.verify()
            .withAddress(address)
            .withRequestMultiUseToken(true)
            .execute();

        String token = result.getToken();
        assertFalse(StringUtils.isNullOrEmpty(token));

        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        CreditCardData paymentMethod = new CreditCardData();
        paymentMethod.setToken(token);
        paymentMethod.setExpMonth(clearTextCredit.getExpMonth());
        paymentMethod.setExpYear(clearTextCredit.getExpYear());

        assertFalse(StringUtils.isNullOrEmpty(token));

        result = paymentMethod
            .charge(bill.getAmount())
            .withAddress(address)
            .withBills(bill)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();

        validateSuccesfulTransaction(result);
    }
     @Test
    public void Tokenize_UsingCreditCard_returns_TokenDetails_Along_with_cardBrand() throws ApiException {
        String cardTypeVisa = "VISA";
        String cardTypeDiscover = "DISC";
        String cardTypeMasterCard = "MC";
        String cardTypeAmericanExpress = "AMEX";

        String postalCode = address.getPostalCode();
        String custFirstName = customer.getFirstName();
        String custLastName = customer.getLastName();
        String cardHoldeName = "Test Tester";

        CreditCardData clearTextCreditVisa = new CreditCardData();
        clearTextCreditVisa.setNumber( "4444444444444448");
        clearTextCreditVisa.setExpMonth(DateTime.now().getMonthOfYear());
        clearTextCreditVisa.setExpYear(DateTime.now().getYear());
        clearTextCreditVisa.setCvn("123");
        clearTextCreditVisa.setCardHolderName(cardHoldeName);

        CreditCardData clearTextCreditDiscover = new CreditCardData();
        clearTextCreditDiscover.setNumber( "6011000000000087");
        clearTextCreditDiscover.setExpMonth(DateTime.now().getMonthOfYear());
        clearTextCreditDiscover.setExpYear(DateTime.now().getYear());
        clearTextCreditDiscover.setCvn("123");
        clearTextCreditDiscover.setCardHolderName(cardHoldeName);

        CreditCardData clearTextCreditMasterCard = new CreditCardData();
        clearTextCreditMasterCard.setNumber( "5425230000004415");
        clearTextCreditMasterCard.setExpMonth(DateTime.now().getMonthOfYear());
        clearTextCreditMasterCard.setExpYear(DateTime.now().getYear());
        clearTextCreditMasterCard.setCvn("123");
        clearTextCreditMasterCard.setCardHolderName(cardHoldeName);

        CreditCardData clearTextCreditAmericanExpress = new CreditCardData();
        clearTextCreditAmericanExpress.setNumber( "374101000000608");
        clearTextCreditAmericanExpress.setExpMonth(DateTime.now().getMonthOfYear());
        clearTextCreditAmericanExpress.setExpYear(DateTime.now().getYear());
        clearTextCreditAmericanExpress.setCvn("123");
        clearTextCreditAmericanExpress.setCardHolderName(cardHoldeName);

        // VISA
         Transaction tokenResponseVisa = clearTextCreditVisa.charge(bill.getAmount())
                 .withAddress(address)
                 .withCustomerData(customer)
                 .withBills(bill)
                 .withCurrency("USD")
                 .withRequestMultiUseToken(true)
                 .execute();

       assertNotNull(tokenResponseVisa);

       clearTextCreditVisa.setToken(tokenResponseVisa.getToken());
       Transaction tokenInfoResponseVisa = clearTextCreditVisa.getTokenInformation();

        assertNotNull(tokenInfoResponseVisa);
        assertEquals(cardTypeVisa, tokenInfoResponseVisa.getCardType());
        assertNotNull(tokenInfoResponseVisa.getAddress());
        assertEquals(postalCode, tokenInfoResponseVisa.getAddress().getPostalCode());
        assertEquals(custFirstName, tokenInfoResponseVisa.getCustomerData().getFirstName());
        assertEquals(custLastName, tokenInfoResponseVisa.getCustomerData().getLastName());
        assertNotNull(tokenInfoResponseVisa.getTokenData());
        assertNotNull(tokenInfoResponseVisa.getTokenData().getLastUsedDateUTC());
        assertTrue(tokenInfoResponseVisa.getTokenData().getLastUsedDateUTC() instanceof DateTime);
        assertFalse(tokenInfoResponseVisa.getTokenData().getMerchants().isEmpty());
        assertEquals("Dev_Exp_Team_Merchant", tokenInfoResponseVisa.getTokenData().getMerchants().get(0));

        //Discover
         Transaction tokenResponseDiscover = clearTextCreditDiscover.charge(bill.getAmount())
                 .withAddress(address)
                 .withCustomerData(customer)
                 .withBills(bill)
                 .withCurrency("USD")
                 .withRequestMultiUseToken(true)
                 .execute();
         assertNotNull(tokenResponseDiscover);

         clearTextCreditDiscover.setToken(tokenResponseDiscover.getToken());
         Transaction tokenInfoResponseDiscover = clearTextCreditDiscover.getTokenInformation();

         assertNotNull(tokenInfoResponseDiscover);
         assertEquals(cardTypeDiscover, tokenInfoResponseDiscover.getCardType());
         assertNotNull(tokenInfoResponseDiscover.getAddress());
         assertEquals(postalCode, tokenInfoResponseDiscover.getAddress().getPostalCode());
         assertEquals(custFirstName, tokenInfoResponseDiscover.getCustomerData().getFirstName());
         assertEquals(custLastName, tokenInfoResponseDiscover.getCustomerData().getLastName());
         assertNotNull(tokenInfoResponseDiscover.getTokenData());
         assertNotNull(tokenInfoResponseDiscover.getTokenData().getLastUsedDateUTC());
         assertTrue(tokenInfoResponseDiscover.getTokenData().getLastUsedDateUTC() instanceof DateTime);
         assertFalse(tokenInfoResponseDiscover.getTokenData().getMerchants().isEmpty());
         assertEquals("Dev_Exp_Team_Merchant", tokenInfoResponseDiscover.getTokenData().getMerchants().get(0));

       //Master Card
         Transaction tokenResponseMasterCard = clearTextCreditMasterCard.charge(bill.getAmount())
                 .withAddress(address)
                 .withCustomerData(customer)
                 .withBills(bill)
                 .withCurrency("USD")
                 .withRequestMultiUseToken(true)
                 .execute();
         assertNotNull(tokenResponseMasterCard);

         clearTextCreditMasterCard.setToken(tokenResponseMasterCard.getToken());
         Transaction tokenInfoResponseMasterCard = clearTextCreditMasterCard.getTokenInformation();

         assertNotNull(tokenInfoResponseMasterCard);
         assertEquals(cardTypeMasterCard, tokenInfoResponseMasterCard.getCardType());
         assertNotNull(tokenInfoResponseMasterCard.getAddress());
         assertEquals(postalCode, tokenInfoResponseMasterCard.getAddress().getPostalCode());
         assertEquals(custFirstName, tokenInfoResponseMasterCard.getCustomerData().getFirstName());
         assertEquals(custLastName, tokenInfoResponseMasterCard.getCustomerData().getLastName());
         assertNotNull(tokenInfoResponseMasterCard.getTokenData());
         assertNotNull(tokenInfoResponseMasterCard.getTokenData().getLastUsedDateUTC());
         assertTrue(tokenInfoResponseMasterCard.getTokenData().getLastUsedDateUTC() instanceof DateTime);
         assertFalse(tokenInfoResponseMasterCard.getTokenData().getMerchants().isEmpty());
         assertEquals("Dev_Exp_Team_Merchant", tokenInfoResponseMasterCard.getTokenData().getMerchants().get(0));

       //America Express
         Transaction tokenResponseAmericanExpress = clearTextCreditAmericanExpress.charge(bill.getAmount())
                 .withAddress(address)
                 .withCustomerData(customer)
                 .withBills(bill)
                 .withCurrency("USD")
                 .withRequestMultiUseToken(true)
                 .execute();
         assertNotNull(tokenResponseAmericanExpress);

         clearTextCreditAmericanExpress.setToken(tokenResponseAmericanExpress.getToken());
         Transaction tokenInfoResponseAmericanExpress = clearTextCreditAmericanExpress.getTokenInformation();

         assertNotNull(tokenInfoResponseAmericanExpress);
         assertEquals(cardTypeAmericanExpress, tokenInfoResponseAmericanExpress.getCardType());
         assertNotNull(tokenInfoResponseAmericanExpress.getAddress());
         assertEquals(postalCode, tokenInfoResponseAmericanExpress.getAddress().getPostalCode());
         assertEquals(custFirstName, tokenInfoResponseAmericanExpress.getCustomerData().getFirstName());
         assertEquals(custLastName, tokenInfoResponseAmericanExpress.getCustomerData().getLastName());
         assertNotNull(tokenInfoResponseAmericanExpress.getTokenData());
         assertNotNull(tokenInfoResponseAmericanExpress.getTokenData().getLastUsedDateUTC());
         assertTrue(tokenInfoResponseAmericanExpress.getTokenData().getLastUsedDateUTC() instanceof DateTime);
         assertFalse(tokenInfoResponseAmericanExpress.getTokenData().getMerchants().isEmpty());
         assertEquals("Dev_Exp_Team_Merchant", tokenInfoResponseAmericanExpress.getTokenData().getMerchants().get(0));

    }
    @Test
    public void Charge_UsingTokenizedACH_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        Address address = new Address();
        address.setPostalCode("12345");

        Transaction result = ach.verify()
            .withAddress(address)
            .execute();

        String token = result.getToken();
        assertFalse(StringUtils.isNullOrEmpty(token));
        BigDecimal fee = service.calculateConvenienceAmount(ach, bill.getAmount());

        eCheck paymentMethod = new eCheck();
        paymentMethod.setAccountType(AccountType.Checking);
        paymentMethod.setCheckType(CheckType.Business);
        paymentMethod.setSecCode(SecCode.Web);
        paymentMethod.setCheckHolderName("Tester");
        paymentMethod.setToken(token);

        assertFalse(StringUtils.isNullOrEmpty(token));

        result = paymentMethod
                .charge(bill.getAmount())
                .withBills(bill)
                .withConvenienceAmt(fee)
                .withCurrency("USD")
                .withAddress(address)
                .execute();

        validateSuccesfulTransaction(result);
    }
    @Test
    public void Charge_UsingTokenFromPreviousPayment_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        Transaction transaction = clearTextCredit
            .charge(bill.getAmount())
            .withAddress(address)
            .withBills(bill)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .withRequestMultiUseToken(true)
            .execute();

        validateSuccesfulTransaction(transaction);
        assertFalse(StringUtils.isNullOrEmpty(transaction.getToken()));

        CreditCardData token = new CreditCardData();
        token.setToken(transaction.getToken());
        token.setExpYear(clearTextCredit.getExpYear());
        token.setExpMonth(clearTextCredit.getExpMonth());

        try {
        Transaction result = token.charge(bill.getAmount())
            .withBills(bill)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();
        } catch (BuilderException ex) {
            System.out.println(ex);
        }

        // validateSuccesfulTransaction(result);
    }
    @Test(expected = BuilderException.class)
    public void Charge_WithoutAddingBills_ThrowsBuilderException() throws ApiException {
        clearTextCredit
            .charge(new BigDecimal(50))
            .withCurrency("USD")
            .withConvenienceAmt(new BigDecimal(3))
            .execute();
    }
    @Test(expected = BuilderException.class)
    public void Charge_WithMismatchingAmounts_ThrowsBuilderException() throws ApiException {
        clearTextCredit
            .charge(new BigDecimal(60))
            .withBills(bills)
            .withCurrency("USD")
            .execute();
    }

    // #endregion

    // #region Management Builder Cases
    @Test
    public void ReversePayment_WithPreviousTransaction_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        // Make transaction to reverse
        Transaction transaction = clearTextCredit
            .charge(bill.getAmount())
            .withAddress(address)
            .withBills(bill)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();

        validateSuccesfulTransaction(transaction);

        // Now reverse it
        Transaction reversal = Transaction.fromId(transaction.getTransactionId())
            .reverse(bill.getAmount())
            .withConvenienceAmt(fee)
            .execute();

        validateSuccesfulTransaction(reversal);
    }
    @Test
    public void ReversePayment_WithPreviousMultiBillTransaction_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal totalAmount = new BigDecimal(0);
        
        for (Bill bill : bills) {
            totalAmount = totalAmount.add(bill.getAmount());
        }
        
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, totalAmount);

        // Make transaction to reverse
        Transaction transaction = clearTextCredit
            .charge(totalAmount)
            .withAddress(address)
            .withBills(bills)
            .withConvenienceAmt(fee)
            .withCurrency("USD")
            .execute();

        validateSuccesfulTransaction(transaction);

        // Now reverse it
        Transaction reversal = Transaction.fromId(transaction.getTransactionId())
            .reverse(totalAmount)
            .withConvenienceAmt(fee)
            .execute();

        validateSuccesfulTransaction(reversal);
    }
    @Test
    public void PartialReversal_WithCreditCard_ReturnsSuccessfulTransaction() throws ApiException {
        BillPayService service = new BillPayService();
        BigDecimal totalAmount = new BigDecimal(0);
        
        for (Bill bill : bills) {
            totalAmount = totalAmount.add(bill.getAmount());
        }
        
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, totalAmount);

        // Make transaction to reverse
        Transaction transaction =  clearTextCredit
                .charge(totalAmount)
                .withAddress(address)
                .withBills(bills)
                .withPaymentMethod(clearTextCredit)
                .withConvenienceAmt(fee)
                .withCurrency("USD")
                .execute();

        validateSuccesfulTransaction(transaction);

        // Now reverse it
        List<Bill> billsToPariallyReverse = new ArrayList<>(); 
        for (Bill x : bills) {
            Bill bill = new Bill();
            bill.setBillType(x.getBillType());
            bill.setIdentifier1(x.getIdentifier1());
            bill.setAmount(x.getAmount().subtract(new BigDecimal(5)));

            billsToPariallyReverse.add(bill);
        }

        BigDecimal newTotalAmount = totalAmount.subtract(new BigDecimal(10));
        BigDecimal newFees = service.calculateConvenienceAmount(clearTextCredit, newTotalAmount);

        Transaction reversal = Transaction.fromId(transaction.getTransactionId())
            .reverse(newTotalAmount)
            .withBills(billsToPariallyReverse)
            .withConvenienceAmt(fee.subtract(newFees))
            .execute();

        validateSuccesfulTransaction(reversal);
    }

    // #endregion

    // #region Billing Builder Cases
    @Test
    public void LoadHostedPayment_WithMakePaymentType_ReturnsIdentifier() throws ApiException {
        BillPayService service = new BillPayService();
        HostedPaymentData data = new HostedPaymentData();
        
        List<Bill> bills = new ArrayList<>();
        bills.add(blindBill);

        Address address = new Address();
        address.setStreetAddress1("123 Drive");
        address.setPostalCode("12345");

        data.setBills(bills);
        data.setCustomerAddress(address);
        data.setCustomerEmail("test@tester.com");
        data.setCustomerFirstName("Test");
        data.setCustomerLastName("Tester");
        data.setHostedPaymentType(HostedPaymentType.MAKE_PAYMENT);

        LoadSecurePayResponse response = service.loadHostedPayment(data);

        assertTrue(!StringUtils.isNullOrEmpty(response.getPaymentIdentifier()));

    }
    @Test
    public void LoadHostedPayment_WithMakePaymentReturnToken_ReturnsIdentifier() throws ApiException {
        BillPayService service = new BillPayService();
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        
        List<Bill> bills = new ArrayList<>();
        bills.add(blindBill);

        Address address = new Address();
        address.setStreetAddress1("123 Drive");
        address.setCity("Auburn");
        address.setState("AL");
        address.setPostalCode("36830");
        address.setCountryCode("US");

        hostedPaymentData.setBills(bills);
        hostedPaymentData.setCustomerAddress(address);
        hostedPaymentData.setCustomerEmail("test@tester.com");
        hostedPaymentData.setCustomerFirstName("Test");
        hostedPaymentData.setCustomerLastName("Tester");
        hostedPaymentData.setCustomerPhoneMobile("800-555-5555");
        hostedPaymentData.setCustomerIsEditable(true);
        hostedPaymentData.setHostedPaymentType(HostedPaymentType.MAKE_PAYMENT_RETURN_TOKEN);

        LoadHostedPaymentResponse response = service.loadHostedPayment(hostedPaymentData);

        assertTrue(!StringUtils.isNullOrEmpty(response.getPaymentIdentifier()));
    }
    @Test(expected = BuilderException.class)
    public void LoadHostedPayment_WithoutBills_ThrowsBuilderException() throws ApiException {
        BillPayService service = new BillPayService();
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        
        Address address = new Address();
        address.setStreetAddress1("123 Drive");

        hostedPaymentData.setCustomerAddress(address);
        hostedPaymentData.setCustomerEmail("alexander.molbert@e-hps.com");
        hostedPaymentData.setCustomerFirstName("Alex");
        hostedPaymentData.setHostedPaymentType(HostedPaymentType.MAKE_PAYMENT);

        LoadHostedPaymentResponse response = service.loadHostedPayment(hostedPaymentData);
    }
    @Test(expected = BuilderException.class)
    public void LoadHostedPayment_WithoutPaymentType_ThrowsBuilderException() throws ApiException {
        BillPayService service = new BillPayService();
        HostedPaymentData hostedPaymentData = new HostedPaymentData();
        
        List<Bill> bills = new ArrayList<>();
        bills.add(blindBill);
        
        Address address = new Address();
        address.setStreetAddress1("123 Drive");

        hostedPaymentData.setBills(bills);
        hostedPaymentData.setCustomerAddress(address);
        hostedPaymentData.setCustomerEmail("alexander.molbert@e-hps.com");
        hostedPaymentData.setCustomerFirstName("Alex");

        service.loadHostedPayment(hostedPaymentData);
    }
    @Test
    public void Load_WithOneBill_DoesNotThrow() {
        try {
            BillPayService service = new BillPayService();
        
            List<Bill> bills = new ArrayList<>();
            bills.add(blindBill);

            service.loadBills(bills);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Load_WithOneThousandBills_DoesNotThrow() {
        try {
            BillPayService service = new BillPayService();

            service.loadBills(makeNumberOfBills(1000));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Load_WithFiveThousandBills_DoesNotThrow() {
        try {
            BillPayService service = new BillPayService();

            service.loadBills(makeNumberOfBills(5000));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test(expected = GatewayException.class)
    public void Load_WithDuplicateBills_ThrowsGatewayException() throws ApiException {
        BillPayService service = new BillPayService();
        List<Bill> bills = new ArrayList<>();
        bills.add(billLoad);
        bills.add(billLoad);

        service.loadBills(bills);
    }
    @Test(expected = GatewayException.class)
    public void Load_WithInvalidBillType_ThrowsGatewayException() throws ApiException {
        BillPayService service = new BillPayService();
        List<Bill> bills = new ArrayList<>();
        bills.add(billLoad);
        
        Bill newBill = new Bill();
        newBill.setAmount(billLoad.getAmount());
        newBill.setBillPresentment(billLoad.getBillPresentment());
        newBill.setBillType("InvalidBillType");
        newBill.setCustomer(billLoad.getCustomer());
        newBill.setDueDate(billLoad.getDueDate());
        newBill.setIdentifier1(billLoad.getIdentifier1());
        bills.add(newBill);

        service.loadBills(bills);
    }

    // #endregion

    // #region Recurring Builder Cases
    @Test
    public void Create_Customer_ReturnsCustomer() {
        try {
            customer = new Customer();
            customer.setFirstName("IntegrationCreate");
            customer.setLastName("Customer");
            customer.setEmail("test.test@test.com");
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            assertEquals("IntegrationCreate", customer.getFirstName());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Update_Customer_ReturnsCustomer() {
        try {
            customer = new Customer();
            customer.setFirstName("IntegrationUpdate");
            customer.setLastName("Customer");
            customer.setEmail("test.test@test.com");
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            assertEquals("IntegrationUpdate", customer.getFirstName());

            customer.setFirstName("Updated");

            customer.saveChanges();

            assertEquals("Updated", customer.getFirstName());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Delete_Customer_ReturnsCustomer() {
        String id = UUID.randomUUID().toString();

        try {
            customer = new Customer();
            customer.setFirstName("IntegrationDelete");
            customer.setLastName("Customer");
            customer.setEmail("test.test@test.com");
            customer.setId(id);
            customer = customer.create();

            assertEquals("IntegrationDelete", customer.getFirstName());

            customer.delete();

            // Bill pay currently does not support retrieval of customer, so there is no true
            // way to validate the customer was deleted other than no exception was thrown
            assertEquals("IntegrationDelete", customer.getFirstName());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    @Test
    public void Create_CustomerAccount_ReturnsPaymentMethod() {
        try {
            customer = new Customer();
            customer.setFirstName("Integration");
            customer.setLastName("Customer");
            customer.setEmail("test.test@test.com");
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

            assertFalse(StringUtils.isNullOrEmpty(paymentMethod.getKey()));
        } catch (Exception ex) {
            fail((ex.getCause() != null ? ex.getCause() : ex).getMessage());
        }
    }
    @Test
    public void Update_CustomerAccount_ReturnsSuccess() {
        try {
            customer = new Customer();
            customer.setFirstName("Account");
            customer.setLastName("Update");
            customer.setEmail("account.update@test.com");
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

            assertFalse(StringUtils.isNullOrEmpty(paymentMethod.getKey()));

            ((CreditCardData) paymentMethod.getPaymentMethod()).setExpYear(2026);

            paymentMethod.saveChanges();
        } catch (Exception ex) {
            fail((ex.getCause() != null ? ex.getCause() : ex).getMessage());
        }
    }
    @Test
    public void Delete_CustomerAccount_ReturnsSuccess() {
        try {
            customer = new Customer();
            customer.setFirstName("Account");
            customer.setLastName("Delete");
            customer.setEmail("account.delete@test.com");
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

            assertFalse(StringUtils.isNullOrEmpty(paymentMethod.getKey()));

            paymentMethod.delete();
        } catch (Exception ex) {
            fail((ex.getCause() != null ? ex.getCause() : ex).getMessage());
        }
    }
    @Test(expected = ApiException.class)
    public void Delete_NonexistingCustomer_ThrowsApiException() throws ApiException {
        Customer customer = new Customer();
        customer.setFirstName("Incog");
        customer.setLastName("Anony");
        customer.setId("DoesntExist");
        customer.delete();
    }

    @Test
    public void test_Create_RecurringPayment_Monthly_Positive() throws ApiException {
        try {
            customer = new Customer();
            customer.setFirstName(FIRST_NAME);
            customer.setLastName(LAST_NAME);
            customer.setEmail(EMAIL_ID);
            customer.setId(UUID.randomUUID().toString());
            customer = customer.create();

            RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

            customer.setAddress(address);
            Schedule recur = paymentMethod.addSchedule(UUID.randomUUID().toString())
                    .withAmount(new BigDecimal(50))
                    .withBills(blindBill)
                    .withCustomer(customer)
                    .withStartDate(DateTime.now().toDate())
                    .withEndDate(DateUtils.parse("12/21/2026"))
                    .withNumberOfPayments(27)
                    .withFrequency(ScheduleFrequency.Monthly)
                    .withToken(paymentMethod.getToken())
                    .withPrimaryConvenienceAmount(new BigDecimal(5))
                    .withLastPrimaryConvenienceAmount(new BigDecimal(4))
                    .withRecurringAuthorizationType(RecurringAuthorizationType.UNASSIGNED)
                    .withInitialPaymentMethod(InitialPaymentMethod.CARD)
                            .create();
            assertNotNull(recur);
            assertFalse(StringUtils.isNullOrEmpty(paymentMethod.getKey()));
        } catch (Exception ex) {
            fail((ex.getCause() != null ? ex.getCause() : ex).getMessage());
        }
    }
    @Test
    public void test_CreateRecurringSchedule_SemiMonthly_SecondInstanceDateRequired_Negative() throws UnsupportedOperationException, ApiException {
        customer = new Customer();
        customer.setFirstName(FIRST_NAME);
        customer.setLastName(LAST_NAME);
        customer.setEmail(EMAIL_ID);
        customer.setId(UUID.randomUUID().toString());
        customer = customer.create();

        RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

        customer.setAddress(address);
        bill.setBillType("Tax Payments");
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> paymentMethod.addSchedule(UUID.randomUUID().toString())
                    .withAmount(new BigDecimal(50))
                    .withBills(bill)
                    .withCustomer(customer)
                    .withStartDate(DateTime.now().toDate())
                    .withEndDate(DateUtils.parse("12/21/2026"))
                    .withNumberOfPayments(27)
                    .withFrequency(ScheduleFrequency.SemiMonthly)
                    .withToken(paymentMethod.getToken())
                    .withPrimaryConvenienceAmount(new BigDecimal(5))
                    .withLastPrimaryConvenienceAmount(new BigDecimal(4))
                    .withRecurringAuthorizationType(RecurringAuthorizationType.UNASSIGNED)
                    .withInitialPaymentMethod(InitialPaymentMethod.CARD)
                    .create());
        assertEquals(SECOND_INSTANCE_DATE_EXCEPTION,exception.getMessage());
    }
    @Test
    public void test_CreateRecurringSchedule_Monthly_PrimaryAccountTokenRequired_Negative() throws UnsupportedOperationException, ApiException {
        customer = new Customer();
        customer.setFirstName(FIRST_NAME);
        customer.setLastName(LAST_NAME);
        customer.setEmail(EMAIL_ID);
        customer.setId(UUID.randomUUID().toString());
        customer = customer.create();

        RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

        customer.setAddress(address);
        bill.setBillType("Tax Payments");
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> paymentMethod.addSchedule(UUID.randomUUID().toString())
                        .withAmount(new BigDecimal(50))
                        .withBills(bill)
                        .withCustomer(customer)
                        .withStartDate(DateTime.now().toDate())
                        .withEndDate(DateUtils.parse("12/21/2026"))
                        .withNumberOfPayments(27)
                        .withFrequency(ScheduleFrequency.Monthly)
                        .withPrimaryConvenienceAmount(new BigDecimal(5))
                        .withLastPrimaryConvenienceAmount(new BigDecimal(4))
                        .withRecurringAuthorizationType(RecurringAuthorizationType.UNASSIGNED)
                        .withInitialPaymentMethod(InitialPaymentMethod.CARD)
                        .create());
        assertEquals(PRIMARY_ACCOUNT_TOKEN_REQUIRED_EXCEPTION,exception.getMessage());
    }
    @Test
    public void test_CreateRecurringSchedule_Monthly_ScheduleTypeRequired_Negative() throws UnsupportedOperationException, ApiException {
        customer = new Customer();
        customer.setFirstName(FIRST_NAME);
        customer.setLastName(LAST_NAME);
        customer.setEmail(EMAIL_ID);
        customer.setId(UUID.randomUUID().toString());
        customer = customer.create();

        RecurringPaymentMethod paymentMethod = customer.addPaymentMethod(UUID.randomUUID().toString(), clearTextCredit).create();

        customer.setAddress(address);
        bill.setBillType("Tax Payments");
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> paymentMethod.addSchedule(UUID.randomUUID().toString())
                        .withAmount(new BigDecimal(50))
                        .withBills(bill)
                        .withCustomer(customer)
                        .withStartDate(DateTime.now().toDate())
                        .withEndDate(DateUtils.parse("12/21/2026"))
                        .withNumberOfPayments(27)
                        .withToken(paymentMethod.getToken())
                        .withPrimaryConvenienceAmount(new BigDecimal(5))
                        .withLastPrimaryConvenienceAmount(new BigDecimal(4))
                        .withRecurringAuthorizationType(RecurringAuthorizationType.UNASSIGNED)
                        .withInitialPaymentMethod(InitialPaymentMethod.CARD)
                        .create());
        assertEquals(SCHEDULE_TYPE_REQUIRED_EXCEPTION,exception.getMessage());
    }
    @Test
    public void Charge_MakeQuickPayBlindPayment_ACH() throws ApiException {
        getQuickPayConfig();

        Address address = new Address();
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setAddress(address);

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal(350));
        bill.setIdentifier1("12345");
        bill.setBillType("Tax Payments");

        eCheck ach = new eCheck();
        ach.setAccountNumber("987987987");
        ach.setRoutingNumber("062000080");
        ach.setAccountType(AccountType.Checking);
        ach.setCheckType(CheckType.Personal);
        ach.setSecCode(SecCode.Web);
        ach.setCheckHolderName("Hank Hill");
        ach.setBankName("Regions");
        // need to use diff token everytime
        ach.setToken("01094ED0-C1F6-4260-B9FE-3182A1B53495");

        Transaction result = ach.charge(bill.getAmount())
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(BigDecimal.valueOf(2.65))
                .withCurrency("USD")
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute();

        validateSuccesfulTransaction(result);
    }
    @Test
    public void Charge_MakeQuickPayBlindPayment_Credit() throws ApiException {
        getQuickPayConfig();

        Address address = new Address();
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setAddress(address);

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal(350));
        bill.setIdentifier1("12345");
        bill.setBillType("Tax Payments");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("5454545454545454");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setCvn("123");
        cardData.setCardHolderName("Hank Hill");
        // need to use diff token everytime
        cardData.setToken("3EACE97E-5166-4F3C-942D-888DFB0DB95C");

        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(cardData, bill.getAmount());

        Transaction result = cardData.charge(bill.getAmount())
                .withAddress(address)
                .withBills(bill)
                .withCustomer(customer)
                .withConvenienceAmt(fee)
                .withCurrency("USD")
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute();

        validateSuccesfulTransaction(result);
    }

    @Test
    public void Charge_MakeQuickPayBlindPaymentReturnToken_Credit() throws ApiException {
        getQuickPayConfig();

        Address address = new Address();
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setAddress(address);

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal(350));
        bill.setIdentifier1("12345");
        bill.setBillType("Tax Payments");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("5454545454545454");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setCvn("123");
        cardData.setCardHolderName("Hank Hill");
        // need to use diff token everytime
        cardData.setToken("1AA43CEB-76CA-4254-B640-F46F787677C4" );

        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(cardData, bill.getAmount());

        Transaction result = cardData.charge(bill.getAmount())
                .withCurrency("USD")
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(fee)
                .withRequestMultiUseToken(true)
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute();

        validateSuccesfulTransaction(result);
    }

    @Test
    public void Charge_MakeQuickPayBlindPaymentReturnToken_ACH() throws ApiException {
        getQuickPayConfig();

        Address address = new Address();
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setAddress(address);

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal(350));
        bill.setIdentifier1("12345");
        bill.setBillType("Tax Payments");

        eCheck ach = new eCheck();
        ach.setAccountNumber("987987987");
        ach.setRoutingNumber("062000080");
        ach.setAccountType(AccountType.Checking);
        ach.setCheckType(CheckType.Personal);
        ach.setSecCode(SecCode.Web);
        ach.setCheckHolderName("Hank Hill");
        ach.setBankName("Regions");
        // need to use diff token everytime
        ach.setToken("5CBC5301-D8AB-4212-855D-DB1704C65802");

        Transaction result = ach.charge(bill.getAmount())
                .withCurrency("USD")
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(BigDecimal.valueOf(2.65))
                .withRequestMultiUseToken(true)
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute();

        validateSuccesfulTransaction(result);
    }
    @Test
    public void Charge_MakeQuickPayBlindPayment_ACH_tokenNotPassed_exceptionCase() throws ApiException {
        getQuickPayConfig();

        UnsupportedTransactionException tokenException = assertThrows(UnsupportedTransactionException.class,
                () -> ach.charge(bill.getAmount())
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(BigDecimal.valueOf(2.65))
                .withCurrency("USD")
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute());
        assertEquals(QUICK_PAY_TOKEN_EXCEPTION,tokenException.getMessage());
    }
    @Test
    public void Charge_MakeQuickPayBlindPayment_Credit_tokenNotPassed_exceptionCase() throws ApiException {
        getQuickPayConfig();

        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        UnsupportedTransactionException tokenException = assertThrows(UnsupportedTransactionException.class,
                () -> clearTextCredit.charge(bill.getAmount())
                .withAddress(address)
                .withBills(bill)
                .withCustomer(customer)
                .withConvenienceAmt(fee)
                .withCurrency("USD")
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute());
        assertEquals(QUICK_PAY_TOKEN_EXCEPTION,tokenException.getMessage());
    }

    @Test
    public void Charge_MakeQuickPayBlindPaymentReturnToken_Credit_tokenNotPassed_exceptionCase() throws ApiException {
        getQuickPayConfig();

        BillPayService service = new BillPayService();
        BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

        UnsupportedTransactionException tokenException = assertThrows(UnsupportedTransactionException.class,
                () -> clearTextCredit.charge(bill.getAmount())
                .withCurrency("USD")
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(fee)
                .withRequestMultiUseToken(true)
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute());
        assertEquals(QUICK_PAY_TOKEN_EXCEPTION,tokenException.getMessage());
    }

    @Test
    public void Charge_MakeQuickPayBlindPaymentReturnToken_ACH_tokenNotPassed_exceptionCase() throws ApiException {
        getQuickPayConfig();

        UnsupportedTransactionException tokenException = assertThrows(UnsupportedTransactionException.class,
                () -> ach.charge(bill.getAmount())
                .withCurrency("USD")
                .withAddress(address)
                .withCustomer(customer)
                .withBills(bill)
                .withConvenienceAmt(BigDecimal.valueOf(2.65))
                .withRequestMultiUseToken(true)
                .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                .execute());
        assertEquals(QUICK_PAY_TOKEN_EXCEPTION,tokenException.getMessage());
    }
    // #endregion


    @Test
    public void test_GetTransactionByOrderId_Validation_Positive() throws ApiException {

        try {
            Transaction response = clearTextCredit.verify()
                    .withAddress(address)
                    .withRequestMultiUseToken(true)
                    .execute();

            String token = response.getToken();
            BillPayService service = new BillPayService();
            BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

            CreditCardData paymentMethod = new CreditCardData();
            paymentMethod.setToken(token);
            paymentMethod.setExpMonth(clearTextCredit.getExpMonth());
            paymentMethod.setExpYear(clearTextCredit.getExpYear());

            UUID uuid = UUID. randomUUID();
            String orderID = uuid.toString();

            Transaction transactionResponse = paymentMethod.charge(bill.getAmount())
                    .withCurrency("USD")
                    .withAddress(address)
                    .withBills(bill)
                    .withConvenienceAmt(fee)
                    .withOrderId(orderID)
                    .execute();
            assertNotNull(transactionResponse);

            TransactionSummary summary = ReportingService.transactionDetail(orderID)
                    .execute();
           assertNotNull(summary);
           assertNotNull(summary.getBillTransactions());
           assertEquals("0",summary.getGatewayResponseCode());
           assertEquals(transactionResponse.getTransactionId(),summary.getTransactionId());
           assertNotNull(summary.getAmount());

        } catch(ApiException exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }
    @Test
    public void test_GetTransactionByOrderId_twoDiffOrderId_exception_Negative() throws ApiException {

            Transaction response = clearTextCredit.verify()
                    .withAddress(address)
                    .withRequestMultiUseToken(true)
                    .execute();

            String token = response.getToken();
            BillPayService service = new BillPayService();
            BigDecimal fee = service.calculateConvenienceAmount(clearTextCredit, bill.getAmount());

            CreditCardData paymentMethod = new CreditCardData();
            paymentMethod.setToken(token);
            paymentMethod.setExpMonth(clearTextCredit.getExpMonth());
            paymentMethod.setExpYear(clearTextCredit.getExpYear());

            UUID uuid = UUID. randomUUID();
            String orderID = uuid.toString();

            Transaction transactionResponse = paymentMethod.charge(bill.getAmount())
                    .withCurrency("USD")
                    .withAddress(address)
                    .withBills(bill)
                    .withConvenienceAmt(fee)
                    .withOrderId(orderID)
                    .execute();
            assertNotNull(transactionResponse);

            UUID uuid1 = UUID. randomUUID();
            String orderID1 = uuid1.toString();

        GatewayException tokenException = assertThrows(GatewayException.class,
                () -> ReportingService.transactionDetail(orderID1)
                    .execute());
            assertEquals(ORDER_ID_EXCEPTION,tokenException.getResponseText());

    }

    // #region Helpers
    private void validateSuccesfulTransaction(Transaction transaction) {
        int transactionId = Integer.parseInt(transaction.getTransactionId());

        assertNotEquals(transaction.getResponseMessage(), transactionId, 0);
    }
    private List<Bill> makeNumberOfBills(int number) {
        List<Bill> bills = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            Bill bill = new Bill();
            bill.setAmount(billLoad.getAmount());
            bill.setBillPresentment(billLoad.getBillPresentment());
            bill.setBillType(billLoad.getBillType());
            bill.setCustomer(billLoad.getCustomer());
            bill.setDueDate(billLoad.getDueDate());
            bill.setIdentifier1(String.format("%s", i));
            bill.setIdentifier2(String.format("%s", i));
            bills.add(bill);
        }

        return bills;
    }
    // #endregion
    private static void getQuickPayConfig() throws ConfigurationException {
        BillPayConfig config = new BillPayConfig();
        config.setMerchantName("LeeCo");
        config.setUsername("sdktest");
        config.setPassword("$Test1234");
        config.setServiceUrl(ServiceEndpoints.BILLPAY_CERTIFICATION.getValue());
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);
    }
}
