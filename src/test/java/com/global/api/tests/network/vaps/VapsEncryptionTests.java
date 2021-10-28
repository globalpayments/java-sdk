package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.ResubmitBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.NetworkMessage;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import com.global.api.utils.MessageReader;
import com.global.api.utils.MessageWriter;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsEncryptionTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private DebitTrackData debit;

    public VapsEncryptionTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // AMEX
//        card = TestCards.AmexManualEncrypted();
//        cardWithCvn = TestCards.AmexManualEncrypted();
//        cardWithCvn.setCvn("9072488");
//        track = TestCards.AmexSwipeEncrypted();

        // DISCOVER
//        card = TestCards.DiscoverManualEncrypted();
//        cardWithCvn = TestCards.DiscoverManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.DiscoverSwipeEncryptedV2();

        // MASTERCARD
//        card = TestCards.MasterCardManualEncrypted();
//        cardWithCvn = TestCards.MasterCardManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.MasterCardSwipeEncryptedV2();

        track = TestCards.MasterCardSwipeEncryptedV2();
        track.setEncryptedPan("5473500844750014");

        // VISA
        card = TestCards.VisaManualEncrypted();
        cardWithCvn = TestCards.VisaManualEncrypted();
        cardWithCvn.setCvn("7803754");
        //cashCard = TestCards.VisaSwipeEncryptedV2();

        //track = TestCards.VisaSwipeEncryptedV2();
        //track.setEncryptedPan("4012005997950016");

        // DEBIT
        debit = new DebitTrackData();
        debit.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_credit_manual_auth_cvn() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_credit_manual_sale_cvn() throws ApiException {
        Transaction response = cardWithCvn.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction response = cardWithCvn.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_034_credit_swipe_auth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_036_credit_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_038_credit_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_039_debit_swipe_auth_capture() throws ApiException {
        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(6))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_040_debit_swipe_sale() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_041_visa_encrypted_follow_on() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                response.getNtsData(),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_042_visa_encrypted_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_043_credit_swipe_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_044_encrypted_forced_data_collect() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        assertNotNull(response.getTransactionToken());

        Transaction resubmit = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals(resubmit.getResponseMessage(), "000", resubmit.getResponseCode());
    }

    @Test @Ignore
    public void test_000_encryption_base64() {
        // initial value
        String initialString = "This is the initial value";

        // convert to byte array through MessageWriter class
        MessageWriter mw = new MessageWriter();
        mw.addRange(initialString.getBytes());
        byte[] initialBuffer = mw.toArray();

        // base64 encode the buffer & convert to encoded string
        byte[] encodedBuffer = Base64.encodeBase64(initialBuffer);
        String encodedString = new String(encodedBuffer);

        // encrypt the encoded string
        PayrollEncoder encoder = new PayrollEncoder("username", "apikey");
        String encryptedString = encoder.encode(encodedString);

        // add the STX/ETX and LRC
        mw = new MessageWriter();
        mw.add(ControlCodes.STX);
        mw.addRange(encryptedString.getBytes());
        mw.add(ControlCodes.ETX);
        mw.add(TerminalUtilities.calculateLRC(mw.toArray()));

        // final string
        String framedString = new String(mw.toArray());

        // validate
        assertTrue(TerminalUtilities.checkLRC(framedString));

        // check the outcome
        MessageReader mr = new MessageReader(framedString.getBytes());
        ControlCodes stx = mr.readCode();
        assertEquals(ControlCodes.STX, stx);

        String decryptedCheckString = mr.readToCode(ControlCodes.ETX, false);

        ControlCodes etx = mr.readCode();
        assertEquals(ControlCodes.ETX, etx);

        // decrypt the encrypted string
        String decryptedString = encoder.decode(decryptedCheckString);

        // decode the decrypted string
        byte[] decodedBuffer = Base64.decodeBase64(decryptedString);
        String decodedString = new String(decodedBuffer);

        // compare the initial and end values
        assertEquals(initialString, decodedString);
    }

    @Test @Ignore
    public void test_000_token_lrc_issue() throws ApiException {
        PayrollEncoder requestEncoder = new PayrollEncoder("0044", "0007169969911");

        //String encodedString = "MTEwMLIwRQAgwQAEAAAAAAAAAAAwMDMwMDAwMDAwMDAwMTAwMDAxMDA5MDQyNzEyMDA3MTIzMjAxMDA5MTIyNzEyNTU0MjIwMDIwMUIwMDE0QzEwMTIwMzcxNDQ5NjM1Mzk4NDMxPTI1MTIwMDQ0ICAgIDAwMDcxMDAwOTk5MTEgIDA5OUEgAACDAAAAUzIgIDEwMSAgICAgMTAxMTExMDAwMDgwMUU3NTA2M0FYICAwMyBZWTI2MDAxOTk5VlQgIDEyMDAwMDA5MDAwMDcxMjIyMTA0XFxcICAgNzUwNjMgICAgICAgIDAyNTAyTlBDMDlZWU5ZWVlZTk5JSUQwNDAyMDE=[TOKEN TRACE]: encryptedToken: NmgnRkOdVxn/Q3/QTrStkOTvOZ+DTPYH879KL8/OCd/q9j7x/pRVN60Zbp9dgaco1WjHQX2OalQTKLiwTQ6NAzGwPF5WsueNQYB7gHuC0pg4jbcQ2sVsIp1vIS3/wRCB6Tix6ECL+TDDRasuhMoIbLqE7i4YhKn/83/raok6w5h5yquKGNcWCHZMCeab1TeqL2Pr09xfpWyXm7bBB8CnnoxVZ904rTZL4WnG2c/WmHzqGh70QSctU5wvggNhcCGQgx5SDshQmaupuTEXf3V878uL5NhhMn5uiT4IhTLm/aFP+rRJiPllJTz58acCcg6egv9/c8XyzkSV6CduC1LERIl8T5JOCmDLk2omTwP2fGhsSZtpzv47lXroBDFDEpQ0kcQZljZDrnxyg2lcLbjDyMaMvEKvHelim1Ga3HwB4tLKtkS7vs3JyHGtZsbtBcH56WrvR0h5/joS6dZpFRT4yA==[TOKEN TRACE]: framedRequest: [1]NmgnRkOdVxn/Q3/QTrStkOTvOZ+DTPYH879KL8/OCd/q9j7x/pRVN60Zbp9dgaco1WjHQX2OalQTKLiwTQ6NAzGwPF5WsueNQYB7gHuC0pg4jbcQ2sVsIp1vIS3/wRCB6Tix6ECL+TDDRasuhMoIbLqE7i4YhKn/83/raok6w5h5yquKGNcWCHZMCeab1TeqL2Pr09xfpWyXm7bBB8CnnoxVZ904rTZL4WnG2c/WmHzqGh70QSctU5wvggNhcCGQgx5SDshQmaupuTEXf3V878uL5NhhMn5uiT4IhTLm/aFP+rRJiPllJTz58acCcg6egv9/c8XyzkSV6CduC1LERIl8T5JOCmDLk2omTwP2fGhsSZtpzv47lXroBDFDEpQ0kcQZljZDrnxyg2lcLbjDyMaMvEKvHelim1Ga3HwB4tLKtkS7vs3JyHGtZsbtBcH56WrvR0h5/joS6dZpFRT4yA==";
        String encodedString = "2QZH7AwLg3hFryDOw5E8ICBfMkng1KKlRY4PoLRxc/mEo7id7/O1ca1yP/QM01bOroTMLt6Luv56teTzhlihagwe8G5M6ylorGKbCYXCZp5sLpTqik5dNcSreIG0WW7W2uw0D13d952OLdRkKAaiyw0fnZcMShIq7aCCXqpi8pmyrXYeRfOnQUExvFMe4FY2IQIMFbLYrhvNrCOsCsLnv58NTDR4c4TrP4JFtGZaey1tyc0Nqr8qTtTsOzczMOTVaTlrNBGUm+oxuAipG+SOs7MWkMUpfuUTUt6xxf6Eh5DdwYKDzN5V6gojLUXH+9cuAaupb0U0Ju9jpEJ5IKQNZjpsBQ/RAJgNtWO9OzXR/SRxrKt8O0kWGWEZXv9GDSJelS2GsoFosJESpYXB9vHiOdf4TnxibJXvru2Tf+znpPjOyZKJdCtw5LBywgC4slsnNQ950yKBsWcrG9ZKxDB7tCLHL73wQeM4gV6Vt7UJtqqNPw9KtcqwkAZWJDkuhxfVqO78JWcnDCEh6ZeFlWlPcrGYmz6dXJOhLoR4srg8Va00vIBUkh7ZYTHhbX9GjIvdoTpy2VKzyZ/BNvDpD6/neNQXaYmBXnKawOmhhpFy24iUeSs03H2xb0fVlv1wsHu79WQAQK/NJ0oRPPXEOo3eDEO9AjA6Mv5MVQigVXLnag8=";

        /* Does the message decode properly */
        byte[] encodedBuffer = encodedString.getBytes();
        MessageReader mr = new MessageReader(encodedBuffer);

        String valueToDecrypt = encodedString;
        if(mr.peek() == ControlCodes.STX.getByte()) {
            mr.readCode(); // pop the STX off
            valueToDecrypt = mr.readToCode(ControlCodes.ETX);

            byte crc = mr.readByte();
            if(crc != TerminalUtilities.calculateLRC(encodedBuffer)) {
                // invalid token
            }
        }

        String requestStr = requestEncoder.decode(valueToDecrypt);
        byte[] decoded = Base64.decodeBase64(requestStr);

        mr = new MessageReader(decoded);
        String mti = mr.readString(4);
        byte[] buffer = mr.readBytes(decoded.length);
        NetworkMessage request = NetworkMessage.parse(buffer, Iso8583MessageType.CompleteMessage);
        request.setMessageTypeIndicator(mti);
        System.out.println(request.toString());

        /* RE-ENCODE THE MESSAGE */
        byte[] encoded = Base64.encodeBase64(request.buildMessage());
        encodedString = new String(encoded);

        // encrypt it
        String token = requestEncoder.encode(encodedString);

        // build final token
        MessageWriter mw = new MessageWriter();
        mw.add(ControlCodes.STX);
        mw.addRange(token.getBytes());
        mw.add(ControlCodes.ETX);

        // generate the CRC
        byte requestLrc = TerminalUtilities.calculateLRC(mw.toArray());
        mw.add(requestLrc);
        String encodedRequest = new String(mw.toArray());
        System.out.println(encodedRequest);

        String checkValue = encodedRequest.substring(0, encodedRequest.length() - 1);
        boolean lrcMatches = TerminalUtilities.checkLRC(checkValue);
        assertTrue(lrcMatches);
    }

    @Test
    public void test_000_token_test() throws ApiException {
        String terminalString = "000%s9911";

        int storeNumber = 726720;
        while(storeNumber++ < 999999) {
            if(storeNumber % 1000 == 0) {
                System.out.println(storeNumber);
            }

            String terminalId = String.format(terminalString, storeNumber);
            PayrollEncoder encoder = new PayrollEncoder("0044", terminalId);

            //String framedString = "bE1KW+xLqbgPdVMyoZ+Fa94zGXBYywYOiAxFyIK5SrBdjt3DQcxthFfOnRN2eZMCJik3p9D18SBTywBdqa3CBIv8lYhKt+iJ9D3ywuHc4z1GPEG8krq8BVO5aOpgXPXQwcfIpE4QRA7/c3LUm5hNTYYrFh/v7DKmAeorB+mH79aQv0sfEMHpJfsnaSRFwI+v2l7xuBo2oIDGyzSNRYju8idZA0WNmLs8A/lAP3sQp34Xr+2ILH5ayzrZ93Z9ie5gNN6oYTHmbXQ6nlr92VUOO7I8cWDQv+dSTOAtXcKjIkztOvpWAl2Rvvs7ponw2pCCgCzXc3OaOcj8F35pkuXvTA+2SuMu1ztabkGCyhzG12X0crRMpFEgeG+EhHaCT66+NVwrgLcbvXHi6vaCUZT2pEjJ6K0aGWErE3Fo1qrBqE3WF8ltRqX9HmM9D9pUn0x5nZlDzVRT7iO77r9+WnaJ5h3ONJFk6OUlVL1sY0NNp8VZY1J3hJPwR/+SUxfBR+vVsXFZt+IXoZxkIalLnBLufv/10sprQRboGsYia/VMQC72lp7ODHmWntnYqza22uRuW1ymjd377IZ/T7vH6pzLjUkVx/SfFy37icoXA4QlTaI=";
            //String framedString = "9b4sO7nSybc2q3aCq6jtCtvd9d0Nc7jZsIsk2Y63COIXVvY6q1ZG4ADjCxN7+mL1PdZMGntFwN5XRu16tKM0tmU4Xu2AU8EEdF8Br1Lp6sIFXCBcG7mU7c5ee/H8L4PY/EpmvIyzyZvNm2VoRi114qiGY32l7G0SnOZ7GfUz0nTVg8k8Rawcix33/5K+6Tb2R96TwD3G/yxhSpKnEB7ypIH/+P0wq5ANpTcdnMo0meBuIqNPfbmGgwKVrsHKygEjE73LnognfxYc6tOMDzsik+UQJMI/jXN3xCB1IpSg9S3ZoQN+gc46wuoXzKY/5IsiudwDn7+OatuVPb9OnNUaCN2HCRQRwhUnyzN7clcm6nL30ZTWtydNGH37pDuHegadhYpL8CyzYlAm/alZloHY/wVe2NCfKgnmTuABGLfpg6TMJUa4hwRc4iFnGyVW/Yi25oFMVYlX70BRLTJFKxMVY28hoyoWD2eG2UjoNxqufdY4SjeKgj6waarnAkWO8q9iB6Uncu4CVOzEYBsjbUchTg==";
            //String framedString = "2QZH7AwLg3hFryDOw5E8ICBfMkng1KKlRY4PoLRxc/mEo7id7/O1ca1yP/QM01bOroTMLt6Luv56teTzhlihagwe8G5M6ylorGKbCYXCZp5sLpTqik5dNcSreIG0WW7W2uw0D13d952OLdRkKAaiyw0fnZcMShIq7aCCXqpi8pmyrXYeRfOnQUExvFMe4FY2IQIMFbLYrhvNrCOsCsLnv58NTDR4c4TrP4JFtGZaey1tyc0Nqr8qTtTsOzczMOTVaTlrNBGUm+oxuAipG+SOs7MWkMUpfuUTUt6xxf6Eh5DdwYKDzN5V6gojLUXH+9cuAaupb0U0Ju9jpEJ5IKQNZjpsBQ/RAJgNtWO9OzXR/SRxrKt8O0kWGWEZXv9GDSJelS2GsoFosJESpYXB9vHiOdf4TnxibJXvru2Tf+znpPjOyZKJdCtw5LBywgC4slsnNQ950yKBsWcrG9ZKxDB7tCLHL73wQeM4gV6Vt7UJtqqNPw9KtcqwkAZWJDkuhxfVqO78JWcnDCEh6ZeFlWlPcrGYmz6dXJOhLoR4srg8Va00vIBUkh7ZYTHhbX9GjIvdoTpy2VKzyZ/BNvDpD6/neNQXaYmBXnKawOmhhpFy24iUeSs03H2xb0fVlv1wsHu79WQAQK/NJ0oRPPXEOo3eDEO9AjA6Mv5MVQigVXLnag8=";
            String framedString = "jtgt1q1FfC5dUPnmTUKe4GBDlqbUsyKYQFUVz9NzRve9b32B/GEQgd2EQNZbErnEXYbQwW74JZ3DWBqylUJCBy5B+90hW3+ze06dk7j+wHt8P+JW7LzDzNoCGd4LWuYMDvwJB4dziGOd4NcGqJIufj0jTqM6GoPoFBAEUXMnSwDCvG/1lPRXJs176NXvezMfARG9oxWl9O54xLwoMEU/8hg5yoIYAGh/vRiN2Mbn5JyaGV+7sp3042Q1zd53djm0tbzU42A2oIbnikcoCi5HnBDZFxbMkIFf67yXo9KVGMCd2gHkPFUIUidGWZC5vMQpHDOdTxdQLxoqukVlJMMvwYro1gFdWVSIq6P+siBhu5Uga50xbfUvOiuicxNn6tH8DHAP1m6tcDbfI3J1V8rhfkafs/GdscjAhW1p0xl+9PY53LoxqjUwMbsQXE1SBG0kBBeYGEYOeimoQexBOyPsLh2bAXhglIzpafKro6fwouZiKaVQcQIObBNIxtQGtvN3Mc99MveNmifk/MOKiF4WkEVVuzRwoqA0eW98Vl/LoZYyQBeKe6b5Qy96xtmwHKotRkV+ZcncsHkR1/B8RDYaShQp3PpDNCf/4mwO5gTK9QnhuGDwSuiZ8x5StI7RQSLHr+8RNyXatXrXNaRS/7iHVune5rnukn354xzVe4PuELgK/rY+dBpAHBmz6emfUJh9oaGd24mF8KNvtHmtFt8L+Ip3spd6AI501b1E1df3tZ/Aw8g5fx8wx6DXPX+Er332OhIBfYalvguqDjbXkB2ka1yae8nw4zZSihkA8X8fB/NRWMn+moWxxeuFx8aVJH46+qqYw4zeabiSQ3mj9MPPSOeMcdLHLPenJ+pE0nHw69pMxthF76ObDL/ixGQcbnaA+Ir1QRfYYjEfE0/XMG2dHZM6QAhLZqNGx7EdnArUXZsVxJf5weAOuz4l3XoB8SOBkGz+cxXQU495BX5u4C6LlgcXfkMlRK6E16JUKEn80zC3EP+9dz0DhvQTVhwKPwEEJqoDRnk90CDq4RevDe4SImy6T3EbYqodVeUd8MnzxZ8kcGtrA+2DpV1PTx9nPxzUkn1O1q0O8BTyldlO4EFJzGOB5C9UzggDBHRqDpA9SsmZulFS0KNCjJ3KcBgRKuFoq2YMh/3DT98up50kjzLW5Hb1RbWOj7TL8QX961VgKcaeU0aycJMZA5ID8pHpfC8K9zEHF/sIpKjIF3OU7Kh/HGROxB8gQllOfeP3Wcxf34g=";
            MessageReader mr = new MessageReader(framedString.getBytes());

            String decryptedCheckString = mr.readToCode(ControlCodes.ETX, false);

            // decrypt the encrypted string
            String decryptedString = encoder.decode(decryptedCheckString);

            // decode the decrypted string
            byte[] decodedBuffer = Base64.decodeBase64(decryptedString);
            String decodedString = new String(decodedBuffer);
            if(decodedString.startsWith("1220")) {
                System.out.print(storeNumber);
                assertTrue(decodedString.startsWith("1220"));
                break;
            }
        }
    }
}
