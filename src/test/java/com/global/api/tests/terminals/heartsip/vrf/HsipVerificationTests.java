package com.global.api.tests.terminals.heartsip.vrf;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HsipVerificationTests {
    private IDeviceInterface _device;

    public HsipVerificationTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setDeviceType(DeviceType.HSIP_ISC250);
        connectionConfig.setConnectionMode(ConnectionModes.TCP_IP);
        connectionConfig.setIpAddress("10.12.220.130");
        connectionConfig.setPort(12345);
        config.setDeviceConnectionConfig(connectionConfig);

        _device = DeviceService.create(config);
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
        TerminalResponse response = _device.creditSale(1, new BigDecimal("23"))
                .withSignatureCapture(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response.toString());
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
        TerminalResponse response = _device.creditSale(2, new BigDecimal("25"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response.toString());
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
        TerminalResponse response = _device.creditSale(2, new BigDecimal("90.08"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("Y", response.getAvsResponseCode());
        assertEquals("M", response.getCvvResponseCode());

        System.out.println("Response: " + response.toString());
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
        TerminalResponse response = _device.creditSale(2, new BigDecimal("155"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("55"), response.getAmountDue());

        System.out.println("Response: " + response.toString());
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
        TerminalResponse response = _device.creditSale(2, new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse voidResponse = _device.creditVoid(2)
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());

        System.out.println("Response: " + voidResponse.toString());
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
        TerminalResponse response = _device.creditRefund(1, new BigDecimal("9"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response.toString());
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
        TerminalResponse response = _device.giftBalance(1).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getBalanceAmount());

        System.out.println("Response: " + response.toString());
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase10b() throws ApiException {
        TerminalResponse response = _device.giftAddValue(1, new BigDecimal("8")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response.toString());
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    @Test
    public void testCase10c() throws ApiException {
        TerminalResponse response = _device.giftSale(1, new BigDecimal("1"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        System.out.println("Response: " + response.toString());
        System.out.println("Gateway Txn ID: " + response.getTransactionId());
    }

    /*
        TEST CASE #13 – Batch Close
        (Mandatory if Conditional Test Cases are ran)
        Objective	Close the batch, ensuring all approved transactions (offline or online) are settled.
        Integrators are automatically provided accounts with auto-close enabled, so if manual batch transmission will not be performed in the production environment then it does not need to be tested.
        Test Card	N/A
        Procedure	Initiate a Batch Close command
        Pass Criteria	Batch submission must be successful.
        Batch Sequence #:
        References	HeartSIP Specifications.
    */
    @Test
    public void testCase13() throws ApiException {
        _device.closeLane();
        _device.reset();

        IBatchCloseResponse response = _device.batchClose();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());

        System.out.println("Response: " + response.toString());
        System.out.println("Sequence #: " + response.getSequenceNumber());
    }
}
