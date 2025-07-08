package com.global.api.tests.terminals.upa;

import java.math.BigDecimal;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.terminals.upa.UpaInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import com.global.api.utils.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class UpaVerificationTests {
    private final UpaInterface device;

    public UpaVerificationTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("10.253.146.225");
        config.setTimeout(100000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setEnableLogging(true);

        device = (UpaInterface) DeviceService.create(config);
        assertNotNull(device);
    }

    private void PrintReceiptEmv(TerminalResponse response) {
        String receipt = "Transaction Type = " + response.getTransactionType() + "\r\n";
        receipt += "Last four digits of the card number = " + response.getMaskedCardNumber() + "\r\n";
        receipt += "Application Preferred Name or Application Label = " + response.getApplicationPreferredName() + "\r\n";
        receipt += "Application Identifier (AID) = " + response.getApplicationId() + "\r\n";
        receipt += "Application Cryptogram type (ARQC or TC, as applicable)* = " + response.getApplicationCryptogramType().toString() + "\r\n";
        receipt += "Application Cryptogram = " + response.getApplicationCryptogram() + "\r\n";
        receipt += "Terminal Status Indicator = " + response.getTerminalStatusIndicator() + "\r\n";
        receipt += "Terminal verification Result = " + response.getTerminalVerificationResult() + "\r\n";
        receipt += "Entry method = " + response.getEntryMethod() + "\r\n";
        receipt += "Approval code = " + response.getApprovalCode() + "\r\n";
        receipt += "Transaction amount = " + response.getTransactionAmount() + "\r\n";

        if (response.getAmountDue() != null) {
            receipt += "Remaining balance = " + response.getAmountDue() + "\r\n";
        }

        receipt += "Cardholder Name as it appears in track data = " + response.getCardHolderName() + "\r\n";
        System.out.println(receipt);
    }

    private void PrintReceiptMsr(TerminalResponse response) {
        String receipt = "Transaction Type = " + response.getTransactionType() + "\r\n";
        receipt += "Payment type (card brand) = " + response.getCardType().toString() + "\r\n";
        receipt += "Last four digits of the card number = " + response.getMaskedCardNumber() + "\r\n";
        receipt += "Entry method = " + response.getEntryMethod() + "\r\n";
        receipt += "Approval code = " + response.getApprovalCode() + "\r\n";
        receipt += "Transaction amount = " + response.getTransactionAmount() + "\r\n";

        if (response.getAmountDue() != null) {
            receipt += "Remaining balance = " + response.getAmountDue() + "\r\n";
        }

        receipt += "Cardholder Name as it appears in track data = " + response.getCardHolderName() + "\r\n";
        System.out.println(receipt);
    }

    /**
     * Objective    Process an EMV contact sale with offline PIN.
     * Test Card    EMV Mastercard
     * Procedure
     *      1. Select Sale function for an amount of $4.00.
     *          a. Insert Test Card and select application if prompted.
     *          b. Terminal will respond approved.
     * Pass Criteria
     *      1. Transaction must be approved. Receipt must conform to EMV Receipt Requirements
     */
    @Test
    public void test001EMVContactSale() throws ApiException {
        device.ping();
        TerminalResponse response = device.sale(new BigDecimal("4.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // emv receipt requirements
        assertEquals(new BigDecimal("4.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationPreferredName() + response.getApplicationLabel()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationId()));
        assertNotNull(response.getApplicationCryptogramType());
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogramType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogram()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        PrintReceiptEmv(response);
    }

    /**
     * Objective    Ensure application can handle non-EMV swiped transactions.
     * Test Card    Magnetic stripe Mastercard
     * Procedure
     *      1. Select Sale function and swipe for the amount of $7.00.
     *          a. Insert Test Card and select application if prompted.
     *          b. Terminal will respond approved.
     * Pass Criteria
     *      1. Transaction must be approved. Receipt must conform to non-EMV Receipt Requirements
     */
    @Test
    public void test002MSRContactSale() throws ApiException
    {
        TerminalResponse response = device.sale(new BigDecimal("7.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // msr receipt requirements
        assertEquals(new BigDecimal("7.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        PrintReceiptMsr(response);

        // test004TransactionVoid
        TerminalResponse voidResponse = device.voidTransaction()
                .withTerminalRefNumber(response.getTerminalRefNumber())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Objective    Process a keyed sale, with PAN & exp date, along this Address Verification and Card
     *              Security Code to confirm the application can support any or all of these.
     * Test Card    Magnetic stripe Mastercard
     * Procedure
     *      1. Select Sale function and manually key for the amount of $118.00.
     *          a. Enter PAN & expiration date.
     *          b. Enter 321 for Card Security Code (CVV2, CID), if supporting this feature. Enter 76321
     *             for AVS, if supporting this feature
     * Pass Criteria
     *      1. Transaction must be approved online. AVS Result Code: CVV Result Code: M
     */
    @Test
    public void test003ManualSale() throws ApiException
    {
        TerminalResponse response = device.sale(new BigDecimal("7.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // msr receipt requirements
        assertEquals(new BigDecimal("7.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
    }

    /**
     * Objective    Ensure application can handle Partial Approval from our host.
     * Test Card    Magnetic stripe Mastercard
     * Procedure
     *      1. Select Sale function and swipe for the amount of $155.00.
     *          a. Insert Test Card and select application if prompted.
     *          b. Receive an approved amount less than requested. Finalize open ticket with remaining
     *             balance using a different card or tender.
     * Pass Criteria
     *      1. Transaction must be approved. Receipt must conform to non-EMV Receipt Requirements
     */
    @Test
    public void test005PartialApproval() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("155.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("10", response.getResponseCode());

        // msr receipt requirements
        assertEquals(new BigDecimal("100.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        PrintReceiptMsr(response);
    }

    /**
     * Objective    Complete Sale request and then attempt a duplicate Sale transaction
     * Test Card    MSR credit card
     * Procedure
     *      1. Process a Credit Sale for $2.00 using any ECRRefNum
     *      2. Reprocess the Credit Sale using same amount and the same ECRRefNum
     * Pass Criteria
     *      1. Provide Debug logs showing the two Credit Sales for $2.00. Both must be using the same
     *         ECRRefNum. The Log should reflect the Credit Sale failure due to a duplicate transaction.
     */
    @Test
    public void test006DuplicateTransaction() throws ApiException { // you're here
        TerminalResponse response = device.sale(new BigDecimal("2.00"))
                .withGratuity(new BigDecimal("0.00"))
                .withRequestId(22)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // msr receipt requirements
        assertEquals(new BigDecimal("2.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        TerminalResponse duplicateResponse = device.sale(new BigDecimal("2.00"))
                .withGratuity(new BigDecimal("0.00"))
                .withRequestId(22)
                .execute();

        assertNotNull(duplicateResponse);
        assertEquals("2", duplicateResponse.getResponseCode()); // if cancelled via device prompt
    }

    /**
     * Objective    Confirm support of a Return transaction for credit/debit using the gateway TxnId
     * Test Card    Magnetic stripe Visa
     * Procedure
     *      1. Select sale function for the amount of $4.00
     *      2. Swipe or key test card #4 through the MSR, record the TxnId
     *      3. Select Refund function to refund the previous sale of $4.00, use the TxnId from the previous sale
     * Pass Criteria
     *      1. Transaction must be approved using the TxnId
     */
    @Test
    public void test007CreditReturn() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("4.00"))
                .withGratuity(new BigDecimal("0.00"))
                .withRequestId(22)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // msr receipt requirements
        assertEquals(new BigDecimal("4.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        // TODO: perform refund with terminal ref number. dependent on SDK update
        TerminalResponse refundResponse = device.refund(new BigDecimal("4.00"))
                .withReferenceNumber(response.getTransactionId())
                .execute();

        assertFalse(StringUtils.isNullOrEmpty(refundResponse.getTransactionType()));
        assertFalse(StringUtils.isNullOrEmpty(refundResponse.getEntryMethod()));
        assertEquals(refundResponse.getResponseCode(), "00");
    }

    /**
     * Objective    Complete Token request and then use that token for a Sale transaction
     * Test Card    MSR credit card
     * Procedure
     *      1. Perform a Verify transaction
     *          a. Review the response back from our host and locate the token value that is returned.
     *             Store this within your localized token value and be able to retrieve it for the next
     *             transaction
     *      2. Perform a Sale transaction
     *          a. Use the token value that you received and process a transaction for $15.01
     * Pass Criteria
     *      1. TokenValue returned in response
     *      2. Transaction #2 receives response code of 00
     */
    @Test
    public void test008TokenPayment() throws ApiException {
        TerminalResponse response = device.verify()
                .withRequestMultiUseToken(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .execute();

        assertNotNull(response);
        assertNotNull(response.getCardBrandTransactionId());
        assertFalse(StringUtils.isNullOrEmpty(response.getToken()));

        TerminalResponse tokenSaleResponse = device.sale(new BigDecimal("15.01"))
                .withToken(response.getToken())
                .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                .execute();

        assertNotNull(tokenSaleResponse);
        assertEquals(tokenSaleResponse.getResponseCode(), "00");
    }

    /**
     * Objective    Confirm support of EMV PIN Debit sale
     * Test Card    EMV PIN Debit Card (not provided by Heartland)
     * Procedure
     *      1. Select Sale function and Select Debit for the card type OR select DEBIT function. For the test
     *         amount, use $10.00
     * Pass Criteria
     *      1. Transaction must be approved online
     */
    @Test
    public void test009DebitSale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10.00"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseCode(), "96"); // seems to be the expected response here due to test gateway limitations
    }

    /**
     * Objective    Complete Sale with Tip Adjustment
     * Pick One Gratuity Approach
     *      1. Credit Auth + Credit Capture
     *      2. Credit Sale + Tip Adjust // this one
     * Test Card    Mastercard
     * Procedure
     *      1. Select Sale function and process for the amount of $15.12
     *      2. Add a $3.00 tip at settlement
     * Pass Criteria
     *      1. Transaction must be approved.
     * Tony note - Confirmed there is a bug in V1.30 regarding how the tip adjust amount is handled; that amount
     * isn't correctly added to the total transaction amount; earlier software versions did not have this bug
     */
    @Test
    public void test010aAdjustment() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("15.12"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertEquals("Success", response.getStatus());
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse tipAdjustResponse = device.tipAdjust(new BigDecimal("3.00"))
                .withTerminalRefNumber(response.getTerminalRefNumber())
                .execute();

        assertNotNull(tipAdjustResponse);
        assertEquals("00", tipAdjustResponse.getResponseCode());
        assertEquals("18.12", tipAdjustResponse.getTransactionAmount().toString());
    }

    /**
     * Objective    Complete Sale with Tip Adjustment
     * Pick One Gratuity Approach
     *      1. Credit Auth + Credit Capture // this one
     *      2. Credit Sale + Tip Adjust
     * Test Card    Mastercard
     * Procedure
     *      1. Select Sale function and process for the amount of $15.12
     *      2. Add a $3.00 tip at settlement
     * Pass Criteria
     *      1. Transaction must be approved.
     */
    @Test
    public void test010bAuthCapture() throws ApiException {
        TerminalResponse response = device.authorize(new BigDecimal("15.12"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse captureResponse = device.capture(new BigDecimal("18.12"))
                .withTransactionId(response.getTransactionId())
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        // emv receipt requirements
        assertEquals(new BigDecimal("18.12"), captureResponse.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationPreferredName() + response.getApplicationLabel()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationId()));
        assertNotNull(response.getApplicationCryptogramType());
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogramType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogram()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));
    }

    /**
     * Objective    Transactions: Gift Balance Inquiry, Gift Load, Gift Sale/Redeem, Gift Replace
     * Test Card    Heartland Test Gift Cards
     * Procedure
     *      1. Gift Balance Inquiry
     *          a. Should respond with a balance amount of $10
     *      2. Gift Add Value
     *          a. Initiate a load and swipe
     *          b. Enter $8.00 as the amount
     *      3. Gift Sale
     *          a. Initiate a Sale and swipe
     *          b. Enter $1.00 as the amount
     *      4. Gift card replace
     *          a. Initiate a gift card replace
     * Pass Criteria
     *      1. All transactions must be approved.
     */
    @Test
    public void test011GiftCard()  {}

    /**
     * Objective    Transactions: Food Stamp Purchase, Food Stamp Return, Food Stamp Balance Inquiry
     * Test Card    MSD only Visa
     * Procedure
     *      1. Food Stamp Purchase
     *          a. Initiate an EBT Sale and swipe
     *          b. Select EBT Food Stamp if prompted
     *          c. Enter $101.01 as the amount
     *      2. Food Stamp Return
     *          a. Initiate an EBT return and manually enter
     *          b. Select EBT Food Stamp if prompted
     *          c. Enter $104.01 as the amount
     *      4. Food Stamp Balance Inquiry
     *          a. Initiate an EBT balance inquiry transaction
     * Pass Criteria
     *      1. All transactions must be approved.
     */
    @Test
    public void test012EBT() {}

    /**
     * Objective    Transactions: Food Stamp Purchase, Food Stamp Return, Food Stamp Balance Inquiry
     * Test Card    MSD only Visa
     * Procedure
     *      1. Food Stamp Purchase
     *          a. Initiate an EBT Sale and swipe
     *          b. Select EBT Food Stamp if prompted
     *          c. Enter $101.01 as the amount
     *      2. Food Stamp Return
     *          a. Initiate an EBT return and manually enter
     *          b. Select EBT Food Stamp if prompted
     *          c. Enter $104.01 as the amount
     *      3. Food Stamp Balance Inquiry
     *          a. Initiate an EBT balance inquiry transaction
     * Pass Criteria
     *      1. All transactions must be approved.
     */
    @Test
    public void test013EBTCash() {}

    /**
     * Objective    Send extended healthcare (Rx, Vision, Dental, Clinical)
     * Test Card    Visa
     * Procedure
     *      1. Process a Sale for $100.00, with $50 being qualified for healthcare. Choose any of the groups
     *         (Rx, Vision, Dental, Clinical)
     * Pass Criteria
     *      1. Transaction must be approved online
     */
    @Test
    public void test014Healthcare() throws ApiException {
        AutoSubstantiation healthcare = new AutoSubstantiation();
        healthcare.setPrescriptionSubTotal(new BigDecimal("12.50"));
        healthcare.setVisionSubTotal(new BigDecimal("12.50"));
        healthcare.setDentalSubTotal(new BigDecimal("25.00"));

        TerminalResponse response = device.sale(new BigDecimal("100.00"))
                .withAutoSubstantiation(healthcare)
                .execute();

        // msr receipt requirements
        assertEquals(new BigDecimal("100.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getTransactionType()));
        assertNotNull(response.getCardType());
        assertFalse(StringUtils.isNullOrEmpty(response.getCardType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));
    }

    /**
     * Objective    Process the 3 types of Corporate Card transactions: No Tax, Tax Amount, and Tax
     *              Exempt, including the passing of PO Number
     * Test Card    Magnetic stripe Visa
     * Procedure
     *      1. Select Sale function for the amount of $112.34
     *          a. Receive CPC Indicator of B
     *          b. Continue with CPC Edit transaction to account for Tax Type of Not Used
     *          c. Enter the PO Number of 98765432101234567 on the device
     *      2. Select Sale function for the amount of $123.45
     *          a. Receive CPC Indicator of R
     *          b. Continue with CPC Edit transaction to account for Tax Type of Sales Tax, Tax Amount for $1.00
     *      3. Select Sale function for the amount of $134.56
     *          a. Receive CPC Indicator of S
     *          b. Continue with CPC Edit transaction to account for Tax Type of Tax Exempt
     *          c. Enter the PO Number of 98765432101234567 on the device
     * Pass Criteria
     *      1. Transactions must be approved online
     * Tony note - Lvl2 doesn't seem to be fully supported as of V1.30
     */
    @Test
    public void test015Level2() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("112.34"))
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * Objective    Process credit sale in Store and Forward, upload transaction, close batch
     * Test Card    EMV Visa
     * Procedure
     *      1. Select Sale function for an amount of $4.00
     *          a. Response approved
     *      2. Send SAF command
     *          a. SAF Indicator = 2
     *          b. Result OK
     *      3. Initiate a Batch Close
     * Pass Criteria
     *      1. Transaction must approve in SAF and settles in a batch
     */
    @Test
    public void test016StoreAndForwardWithApproval() {}

    /**
     * Objective    Process credit sale in Store and Forward, upload transaction, delete declined
     *              transaction from terminal
     * Procedure
     *      1. Select Sale function for an amount of $10.25
     *          a. Response approved
     *      2. Send SAF command
     *          a. SAF Indicator = 2
     *          b. Transaction will decline
     *      3. Perform delete SAF file
     *          a. SAF Indicator = 2
     * Pass Criteria
     *      1. Transaction must approve in SAF and settles in a batch
     */
    @Test
    public void test017StoreAndForwardWithDecline() {}

    /**
     * Objective    Apply a surcharge to a transaction. You will need to make sure that you have worked with
     *              the Heartland team to set a surcharge amount for all qualifying transactions.
     * Test Card    EMV Mastercard // I used and configured EMV Amex for surcharge
     * Procedure
     *      1. Process a Credit Sale transaction for $50.00 with a 3.5% surcharge
     * Pass Criteria
     *      1. Printed receipt shows that a surcharge was added to the total amount and that the total amount
     *         processed matches the principal amount plus the surcharge
     * Tony note: current device programming doesn't seem to be setup for Surcharging; I tried configuring for
     * Surcharging in the Device Manager, but it did not work     *
     */
    @Test
    public void test018Surcharge() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("50.00"))
                .execute();

        // emv receipt requirements
        assertEquals(new BigDecimal("51.75"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationPreferredName() + response.getApplicationLabel()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationId()));
        assertNotNull(response.getApplicationCryptogramType());
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogramType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogram()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        PrintReceiptEmv(response);
    }

    /**
     * Objective    Close the batch, ensuring all approved transactions (offline or online) are settled.
     *              Integrators are automatically provided accounts with auto-close enabled, so if manual batch transmission
     *              will not be performed in the production environment, it does not need to be tested.
     * Test Card    N/A
     * Procedure
     *      1. Initiate a Batch Close request
     * Pass Criteria
     *      1. Batch submission must be successful
     */
    @Test
    public void test019BatchClose() throws ApiException {
        IEODResponse response = device.endOfDay();

        assertNotNull(response);
        assertFalse(StringUtils.isNullOrEmpty(response.getBatchId()));
    }
    // emv TSI
    // cover negative scenario as well. if TSI is not present in the response test case will fail in assert statement
    @Test
    public void test001EMVContactSale_TSI() throws ApiException
    {
        device.ping();
        TerminalResponse response = device.sale(new BigDecimal("4.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // emv receipt requirements
        assertEquals(new BigDecimal("4.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationPreferredName() + response.getApplicationLabel()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationId()));
        assertNotNull(response.getApplicationCryptogramType());
        assertNotNull(response.getTerminalStatusIndicator());
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogramType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogram()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));
        assertFalse(StringUtils.isNullOrEmpty(response.getCardHolderName()));

        PrintReceiptEmv(response);
    }
    // emv TVR
    // cover negative scenario as well. if TSI is not present in the response test case will fail in assert statement
    @Test
    public void test001EMVContactSale_TVR() throws ApiException {
        device.ping();
        TerminalResponse response = device.sale(new BigDecimal("4.00"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // emv receipt requirements
        assertEquals(new BigDecimal("4.00"), response.getTransactionAmount());
        assertFalse(StringUtils.isNullOrEmpty(response.getMaskedCardNumber()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationPreferredName() + response.getApplicationLabel()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationId()));
        assertNotNull(response.getApplicationCryptogramType());
        assertNotNull(response.getTerminalStatusIndicator());
        assertNotNull(response.getTerminalVerificationResult());
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogramType().toString()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApplicationCryptogram()));
        assertFalse(StringUtils.isNullOrEmpty(response.getEntryMethod()));
        assertFalse(StringUtils.isNullOrEmpty(response.getApprovalCode()));

        PrintReceiptEmv(response);
    }
}