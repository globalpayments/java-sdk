package com.global.api.tests.propay;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.serviceConfigs.ProPayConfig;
import com.global.api.services.ProPayService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProPayInNetworkTransactionTests {
    private ProPayService service;
    private ProPayConfig config;

    public ProPayInNetworkTransactionTests() {
        service=new ProPayService();
        config=new ProPayConfig();
        config.setCertificationStr("5dbacb0fc504dd7bdc2eadeb7039dd");
        config.setTerminalID("7039dd");
        config.setX509CertificateBase64String("MIICpDCCAYygAwIBAgIIS7Y5fijJytIwDQYJKoZIhvcNAQENBQAwETEPMA0GA1UEAwwGUFJPUEFZMB4XDTE5MDkxOTAwMDAwMFoXDTI5MDkxOTAwMDAwMFowEzERMA8GA1UEAwwIMTI3LjAuMDEwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCCwvq2ho43oeeGX3L9+2aD7bna7qjdLwWumeIpwhPZLa44MeQ5100wy4W2hKk3pOb5yaHqyhzoHDriveQnq/EpZJk9m7sizXsxZtBHtt+wghSZjdNhnon3R54SH5J7oEPybRSAKXSEzHjN+kCu7W3TmXSLve6YuODnjUpbOcAsHG2wE+zpCoEbe8toH5Tt7g8HzEc5mJYkkILTq6j9pwDE50r2NVbV3SXwmQ1ifxf54Z9EFB5bQv5cI3+GL/VwlQeJdiKMGj1rs8zTR8TjbAjVlJbz6bBkFItUsqexgwAHIJZAaU7an8ZamGRlPjf6dp3mOEu4B47igNj5KOSgCNdRAgMBAAEwDQYJKoZIhvcNAQENBQADggEBAF88u367yrduqd3PfEIo2ClaI2QPRIIWKKACMcZDl3z1BzVzNFOZNG2vLcSuKnGRH89tJPCjyxdJa0RyDTkXMSLqb5FgUseEjmj3ULAvFqLZNW35PY9mmlmCY+S3CC/bQR4iyPLo8lsRq0Nl6hlvB440+9zS8UQjtc2957QgcXfD427UJb698gXzsfQcNeaQWy8pNm7FzDfHTJbo/t6FOpmfR+RMZky9FrlWabInkrkf3w2XJL0uUAYU9jGQa+l/vnZD2KNzs1mO1EqkS6yB/fsn85mkgGe4Vfbo9GQ/S+KmDujewFA0ma7O03fy1W5v6Amn/nAcFTCddVL3BDNEtOM=");
        config.setEnvironment(Environment.TEST);
        config.setProPayUS(true);
        try {
            ServicesContainer.configureService(config);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    //this method uses a certStr directly tied to the source account for funds disbursement
    @Test
    public void DisburseFunds() throws ApiException {
        config.setCertificationStr("C7277D317D1840F5ADEBE600CF47B9");
        ServicesContainer.configureService(config);

        Transaction response = service.disburseFunds()
                .withAmount("10")
                .withReceivingAccountNumber("718136438")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void SpendBackTransaction() throws ApiException{

        Transaction response = service.spendBack()
                .withAmount("10")
                .withAccountNumber("718136438")
                .withReceivingAccountNumber("718567303")
                .withAllowPending(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    // prerequisite -run transactionType 04,use the transNum from the response of transactionType 04 in splitFunds
    @Test
    public void SplitFunds() throws ApiException {

        Transaction response = service.splitFunds()
                .withAccountNumber("718567304")
                .withAmount("10")
                .withReceivingAccountNumber("718136438")
                .withTransNum("28")
                .execute();

        assertNotNull(response);
        assertEquals("00",response.getResponseCode());
    }

    //use the transNum from the previous i.e split funds response
    @Test
    public void ReverseSplitPay() throws ApiException {

        Transaction response = service.reverseSplitPay()
                .withAccountNumber("718037672")
                .withAmount("100")
                .withCCAmount("100")
                .withRequireCCRefund(false)
                .withTransNum("68")
                .execute();

        assertNotNull(response);
        assertEquals("00",response.getResponseCode());
    }
}
