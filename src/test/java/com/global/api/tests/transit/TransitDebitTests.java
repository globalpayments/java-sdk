package com.global.api.tests.transit;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.TransitConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class TransitDebitTests {
    private final DebitTrackData track;

    public TransitDebitTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        

        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Attended);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Unknown);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);
        acceptorConfig.setCardCaptureCapability(false);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.None);

        ServicesContainer.configureService(getConfig());

        track = new DebitTrackData();

        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setVersion("01");
        encryptionData.setKsn("000000000000000");

        track.setEncryptionData(encryptionData);
        track.setPinBlock("0000");
        track.setValue("<E1050711%4012002000060016^VI TEST CREDIT^25121011803939600000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012002000060016=25121011803939600000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
    }

    @Test
    public void saleSwipe() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
    }

    protected TransitConfig getConfig() {
        TransitConfig config = new TransitConfig();
        config.setMerchantId("884000003531");
        config.setUsername("TA5876503");
        config.setPassword("HRQATest!000");
        config.setDeviceId("88400000353102");
        config.setTransactionKey("7WDYEC6LE9T5Q8EER5CWRPN3P4O5BZH8");
        config.setDeveloperId("003226G001");
        config.setAcceptorConfig(new AcceptorConfig());
        return config;
    }
}