package com.global.api.tests.terminals.hpa.vrf;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HpaVerificationTests {
    private IDeviceInterface _device;

    public HpaVerificationTests() throws ApiException {
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setDeviceType(DeviceType.HPA_ISC250);
        connectionConfig.setConnectionMode(ConnectionModes.TCP_IP);
        connectionConfig.setIpAddress("10.12.220.39");
        connectionConfig.setPort(12345);
        connectionConfig.setRequestIdProvider(new RandomIdProvider());

        _device = DeviceService.create(connectionConfig);
        assertNotNull(_device);
        _device.openLane();
    }

    private void PrintReceipt(TerminalResponse response) throws ApiException {
        String receipt = "x_trans_type=" + response.getTransactionType();
        receipt += "&x_application_label=" + response.getApplicationLabel();
        receipt += "&x_masked_card=" + response.getMaskedCardNumber();
        receipt += "&x_application_id=" + response.getApplicationId();
        receipt += "&x_cryptogram_type=" + response.getApplicationCryptogramType();
        receipt += "&x_application_cryptogram=" + response.getApplicationCryptogram();
        receipt += "&x_expiration_date=" + response.getExpirationDate();
        receipt += "&x_entry_method=" + response.getEntryMethod();
        receipt += "&x_approval=" + response.getApprovalCode();
        receipt += "&x_transaction_amount=" + response.getTransactionAmount();
        receipt += "&x_amount_due=" + response.getAmountDue();
        receipt += "&x_customer_verification_method=" + response.getCustomerVerificationMethod();
        receipt += "&x_signature_status=" + response.getSignatureStatus();
        receipt += "&x_response_text=" + response.getResponseText();
        System.out.println(receipt);
    }

    @After
    public void waitAndReset() throws Exception {
        Thread.sleep(3000);
        _device.reset();
    }

    /*
        TEST CASE #1 – Contact Chip and Signature – Offline
        Objective Process a contact transaction where the CVM’s supported are offline chip and signature
        Test Card Card #1 - MasterCard EMV
        Procedure Perform a complete transaction without error..
        Enter transaction amount $23.00.
    */
    @Test
    public void testCase01() throws ApiException {
        TerminalResponse response = _device.creditSale(new BigDecimal("23"))
                .withSignatureCapture(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
        PrintReceipt(response);
    }

    /*
        TEST CASE #2 - EMV Receipts 
        Objective	1. Verify receipt image conforms to EMV Receipt Requirements.
        2. Verify that signature capture functionality works.
        Test Card	Any card brand – Visa, MC, Discover, AMEX.
        Procedure	Run an EMV insert sale using any card brand.
        The device should get an Approval.
        Cardholder is prompted to sign on the device.
    */
    @Test
    public void testCase02() throws ApiException {
        // print receipt for TestCase01
    }

    /*
        TEST CASE #3 - Approved Sale with Offline PIN
        Objective	Process an EMV contact sale with offline PIN.
        Test Card	Card #1 - MasterCard EMV
        Procedure	Insert the card in the chip reader and follow the instructions on the device.
        Enter transaction amount $25.00.
        When prompted for PIN, enter 4315.
        If no PIN prompt, device could be in QPS mode with limit above transaction amount.
    */
    @Test
    public void testCase03() throws ApiException {
        TerminalResponse response = _device.creditSale(new BigDecimal("25"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
        PrintReceipt(response);
    }

    /*
        TEST CASE #4 -  Manually Entered Sale with AVS & CVV2/CID 
        (If AVS is supported)
        Objective	Process a keyed sale, with PAN & exp date, along with Address Verification and Card Security Code to confirm the application can support any or all of these.
        Test Card	Card #5 – MSD only MasterCard
        Procedure	1. Select sale function and manually key Test Card #5 for the amount of $90.08.
        a.	Enter PAN & expiration date.
        b.	Enter 321 for Card Security Code (CVV2, CID), if supporting this feature.
        Enter 76321 for AVS, if supporting this feature.
    */
    @Test
    public void testCase04() throws ApiException {
        TerminalResponse response = _device.creditSale(new BigDecimal("90.08"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        //assertEquals("Y", response.getAvsResponseCode());
        //assertEquals("M", response.getCvvResponseCode());
        assertEquals("Zip and address match.", response.getAvsResponseText());
        assertEquals("M", response.getAvsResponseCode());
        assertEquals("Not Processed.", response.getCvvResponseText());
        assertEquals("U", response.getAvsResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
        PrintReceipt(response);
    }
    
    /*
        TEST CASE #5 - Partial Approval
        Objective	1. Ensure application can handle non-EMV swiped transactions.
        2. Validate partial approval support.
        Test Card	Card #4 – MSD only Visa
        Procedure	Run a credit sale and follow the instructions on the device to complete the transaction.
        Enter transaction amount $155.00 to receive a partial approval.
        Transaction is partially approved online with an amount due remaining.
    */
    @Test
    public void testCase05() throws ApiException {
        TerminalResponse response = _device.creditSale(new BigDecimal("155"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("55"), response.getAmountDue());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
        PrintReceipt(response);
    }

    /*
        TEST CASE #6 - Online Void
        Objective	Process an online void.
        Test Card	Card #3 – EMV Visa w/ Signature CVM
        Procedure	Enter the Transaction ID to void.
        Transaction has been voided.
    */
    @Test
    public void testCase06() throws ApiException {
        TerminalResponse response = _device.creditSale(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse voidResponse = _device.creditVoid()
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());

        System.out.println("Response: " + voidResponse);
        System.out.println("Gateway Txn ID: " + voidResponse.getTransactionId());
    }

    /*
        TEST CASE  #8 – Process Lane Open on SIP
        Objective	Display line items on the SIP.
        Test Card	NA
        Procedure	Start the process to open a lane on the POS.
    */
    @Test
    public void testCase08() throws ApiException {
        IDeviceResponse response = _device.openLane();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    /*
        TEST CASE #9 – Credit Return
        Objective	Confirm support of a Return transaction for credit.
        Test Card	Card #4 – MSD only Visa
        Procedure	1.	Select return function for the amount of $9.00
        2.	Swipe or Key Test card #4 through the MSR
        3.	Select credit on the device
    */
    @Test
    public void testCase09() throws ApiException {
        TerminalResponse response = _device.creditRefund(new BigDecimal("9"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    /*
        TEST CASE #10 – HMS Gift
        Objective	Transactions: Gift Balance Inquiry,  Gift Load,  Gift Sale/Redeem, Gift Replace
        Test Card	Gift Card (Card Present/Card Swipe)
        Procedure	Test System is a Stateless Environment, the responses are Static.
        1.	Gift Balance Inquiry (GiftCardBalance):
        a.	Should respond with a BalanceAmt of $10
        2.	Gift Load (GiftCardAddValue):
        a.	Initiate a Sale and swipe
        b.	Enter $8.00 as the amount
        3.	Gift Sale/Redeem (GiftCardSale):
        a.	Initiate a Sale and swipe
        b.	Enter $1.00 as the amount
        4.	Gift Card Replace (GiftCardReplace)
        a.	Initiate a Gift Card Replace
        b.	Swipe Card #1 – (Acct #: 5022440000000000098)
        c.	Manually enter  Card #2 –  (Acct #: “5022440000000000007”)
    */
    @Test
    public void testCase10a() throws ApiException {
        TerminalResponse response = _device.giftBalance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getBalanceAmount());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase10b() throws ApiException {
        TerminalResponse response = _device.giftAddValue(new BigDecimal("8")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase10c() throws ApiException {
        TerminalResponse response = _device.giftSale(new BigDecimal("1"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    /*
        TEST CASE #11 – EBT Food Stamp
        Objective Transactions: Food Stamp Purchase, Food Stamp Return and Food Stamp Balance Inquiry
        Test Card   Card #4 – MSD only Visa
        1.Food Stamp Purchase (EBTFSPurchase):
        a.  Initiate an EBT sale transaction and swipe Test Card #4
        b.  Select EBT Food Stamp if prompted.
        c.  Enter $101.01 as the amount
        2.Food Stamp Return (EBTFSReturn):
        a.  Intitiate an EBT return and manually enter Test Card #4
        b.  Select EBT Food Stamp if prompted
        c.  Enter $104.01 as the amount
        3.Food Stamp Balance Inquiry (EBTBalanceInquiry):
        a.  Initiate an EBT blance inquiry transaction and swipe Test Card #4 Settle all transactions.
     */
    @Test
    public void testCase11a() throws ApiException {
        TerminalResponse response = _device.ebtPurchase(new BigDecimal("101.01"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase11b() throws ApiException {
        TerminalResponse response = _device.ebtRefund(new BigDecimal("104.01")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase11c() throws ApiException {
        TerminalResponse response = _device.ebtBalance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    /*
        TEST CASE #12 – EBT Cash Benefits
        Objective   Transactions: EBT Cash Benefits with Cash Back, EBT Cash Benefits Balance Inquiry and EBT Cash Benefits Withdraw
        Test Card   Card #4 – MSD only Visa
        EBT Cash Benefits w Cash Back (EBTCashBackPurchase):
        a.  Initiate an EBT sale transaction and swipe Test Card #4
        b.  Select EBT Cash Benefits if prompted
        c.  Enter $101.01 as the amount
        d.  Enter $5.00 as the cash back amount
        e.  The settlement amount is $106.01
        2. EBT Cash Benefits Balance Inquiry (EBTBalanceInquiry):
        a.    Initiate an EBT cash benefit balance inquiry transaction and
        swipe Test Card #4
    */
    @Test
    public void testCase12a() throws ApiException {
        TerminalResponse response = _device.ebtPurchase(new BigDecimal("101.01"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase12b() throws ApiException {
        TerminalResponse response = _device.ebtBalance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void testCase12c() throws ApiException {
        TerminalResponse response = _device.ebtWithdrawal(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response);
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    /*
        TEST CASE #13 – Batch Close
       Objective End of Day, ensuring all approved transactions (offline or online) are settled. 
          Supported transactions: Reversal, Offline Decline, Transaction Certificate, Add Attachment, SendSAF, Batch Close, EMV PDL and Heartbeat
          Procedure	Initiate a End of Day command
          Pass Criteria	EOD submission must be successful.
          References		HPA Specifications.    */
    @Test
    public void testCase13() throws ApiException {
        _device.closeLane();
        _device.reset();

        IEODResponse response = _device.endOfDay();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());

        System.out.println("Reversal Response: " + response.getReversalResponse().toString());
        System.out.println("Offline Decline Response: " + response.getEmvOfflineDeclineResponse().toString());
        System.out.println("TransactionCertificate Response: " + response.getEmvTransactionCertificateResponse().toString());
        System.out.println("Add Attachment Response: " + response.getAttachmentResponse().toString());
        System.out.println("SendSAF Response: " + response.getSAFResponse().toString());
        System.out.println("Batch Close Response: " + response.getBatchCloseResponse().toString());
        System.out.println("EmvPDL Response: " + response.getEmvPDLResponse().toString());
        System.out.println("Heartbeat Response:" + response.getHeartBeatResponse().toString());
    }
}
