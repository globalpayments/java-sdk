package com.global.api.tests.terminals.genius;

import com.global.api.entities.Address;
import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.genius.serviceConfigs.MitcConfig;
import com.global.api.terminals.genius.enums.TransactionIdType;
import lombok.var;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeniusCreditTests {
    IDeviceInterface device;

    public GeniusCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setDeviceType(DeviceType.GENIUS_VERIFONE_P400);
        config.setConnectionMode(ConnectionModes.MEET_IN_THE_CLOUD);

        MitcConfig gatewayConfig = new MitcConfig(
                "800000052964",
                "80040205",
                "LHgD5tP1KeUhIdTp1hW8gwiEliUdoUZz",
                "u1cYb2xoGONWkGfSxp8js1BGgMOkO0tyMUP732qbAWM",
                "uITbt4dHj0f6Q2EVDwuWWA9cGiDAQnyD",
                "cedevice::at64t3"
        );
        config.setGeniusMitcConfig(gatewayConfig);
        config.setEnvironment(Environment.TEST);
        config.setEnableLogging(true);

        device = DeviceService.create(config);

        /*
        MitcConfig gatewayConfigTony = new MitcConfig(
                "800000052971",
                "80040245",
                "kBySHIAkhL4UBFkVokFEUmDKWY1WGWUv",
                "u1cYb2xoGONWkGfSxp8js1BGgMOkO0tyMUP732qbAWM",
                "uITbt4dHj0f6Q2EVDwuWWA9cGiDAQnyD",
                "cedevice::at63jh"
        );
        */
    }

    @Test
    public void testSale() throws ApiException {
        AutoSubstantiation auto = new AutoSubstantiation();
        auto.setRealTimeSubstantiation(true);
        auto.setDentalSubTotal(BigDecimal.valueOf(5.00));
        auto.setClinicSubTotal(BigDecimal.valueOf(5.00));
        auto.setVisionSubTotal(BigDecimal.valueOf(5.00));
        auto.setCopaySubTotal(BigDecimal.valueOf(5.00));

        Address address = new Address();
        address.setPostalCode("84042");

        TerminalResponse response = device.sale(BigDecimal.valueOf(28526))
                .withClientTransactionId(getRandomNumber(6))
                .withInvoiceNumber(getRandomNumber(8))
                .withAutoSubstantiation(auto)
                .withAddress(address)
                .withAllowPartialAuth(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*Refund linked to a sale*/
    @Test
    public void testRefundPrevSale() throws ApiException {
        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse saleResponse = device.sale(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse refundResponse = device.refundById(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .execute();

        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }
    /*Refund a card directly; doesn't target a previous transaction*/
    @Test
    public void testIndependentRefund() throws ApiException {
        TerminalResponse refundResponse = device.refund(BigDecimal.valueOf(100.28))
                .withClientTransactionId(getRandomNumber(20))
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }
    /*Voids a sale that is in an open batch*/
    @Test
    public void testVoidPrevSale() throws ApiException {
        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse saleResponse = device.sale(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse refundResponse = device.voidTransaction()
                .withClientTransactionId(clientTransId)
                .execute();

        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }
    /*Voids a debit sale that is in an open batch. */
    @Test
    public void testVoidPrevDebitSale() throws ApiException {
        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse saleResponse = device.sale(BigDecimal.valueOf(100.00))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse voidResponse = device.voidTransaction()
                .withClientTransactionId(clientTransId)
                .execute();

        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
    /*Cancels a credit refund */
    @Test
    public void testVoidPrevRefund() throws ApiException {
        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse response = device.refund(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse voidResponse = device.voidTransaction()
                .withClientTransactionId(clientTransId)
                .execute();

        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
    /*Gets details of a credit sale */
    @Test
    public void testSaleReport() throws ApiException {

        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse saleResponse = device.sale(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse transactionReport =
                device.getTransactionDetails(
                        TransactionType.Sale,
                        clientTransId,
                        TransactionIdType.CLIENT_TRANSACTION_ID
                );

        assertNotNull(transactionReport);
        assertEquals("00", transactionReport.getResponseCode());
    }
    /*Gets details of a refund */
    @Test
    public void testRefundReport() throws ApiException {

        var clientTransId = "mapsToReference_id" + getRandomNumber(6);

        TerminalResponse saleResponse = device.refund(BigDecimal.valueOf(100.00))
                .withClientTransactionId(clientTransId)
                .withInvoiceNumber(getRandomNumber(8))
                .execute();

        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse response =
                device.getTransactionDetails(
                        TransactionType.Refund,
                        clientTransId,
                        TransactionIdType.CLIENT_TRANSACTION_ID
                );

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    static String getRandomNumber(int length) {
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString().replace("_","").substring(0,length);
    }

}
