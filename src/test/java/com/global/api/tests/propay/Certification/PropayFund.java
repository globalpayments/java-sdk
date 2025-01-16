package com.global.api.tests.propay.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.serviceConfigs.ProPayConfig;
import com.global.api.services.ProPayService;
import com.global.api.tests.testdata.TestFundsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropayFund {
    private ProPayService service;
    private final String x509CertificatePath = System.getProperty("user.dir") + "\\src\\test\\java\\com\\global\\api\\tests\\propay\\Certification\\testCertificate.crt";

    public PropayFund() {
        service = new ProPayService();
        ProPayConfig config = new ProPayConfig();

        // Certification Credentials
        config.setCertificationStr("4ee64cbd706400fb4a34e65aab6f48");
        config.setTerminalID("ab6f48");

        config.setX509CertificatePath(x509CertificatePath);
        config.setEnvironment(Environment.TEST);
        config.setProPayUS(true);

        try {
            ServicesContainer.configureService(config);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void AddFunds_37() throws ApiException {
        Transaction response = service.addFunds()
                .withAmount("10")
                .withAccountNumber("718570822")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void SweepFunds_38() throws ApiException {
        Transaction response = service.sweepFunds()
                .withAccountNumber("718570822")
                .withAmount("10")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Can only be tested in production
    @Test
    public void AddCardForFlashFunds_209() throws ApiException {
        Transaction response = service.addCardFlashFunds()
                .withAccountNumber("718571149")
                .withFlashFundsPaymentCardData(TestFundsData.getFlashFundsPaymentCardData())
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Can only be tested in production
    @Test
    public void MoveMoneyOutOfFlashFunds_45() throws ApiException {
        Transaction response = service.pushMoneyToFlashFundsCard()
                .withAccountNumber("718571149")
                .withAmount("100")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
