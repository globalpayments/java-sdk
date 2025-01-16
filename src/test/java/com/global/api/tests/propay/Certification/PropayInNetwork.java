package com.global.api.tests.propay.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.serviceConfigs.ProPayConfig;
import com.global.api.services.ProPayService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropayInNetwork {
    private ProPayService service;
    private ProPayConfig config;
    private String x509CertificatePath=System.getProperty("user.dir")+"\\src\\test\\java\\com\\global\\api\\tests\\propay\\Certification\\testCertificate.crt";
    public PropayInNetwork(){
        service=new ProPayService();
        config=new ProPayConfig();

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
    //this method uses a certStr directly tied to the source account for funds disbursement
    @Test
    public void DisburseFunds_02() throws ApiException {
        config.setCertificationStr("AE4A2DBDDF0D4FF8879E77DC9E4D60");
        config.setTerminalID("AE4A2DBDDF0D4FF8");
        ServicesContainer.configureService(config);

        Transaction response = service.disburseFunds()
                .withAmount("10")
                .withReceivingAccountNumber("718570870")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void SpendBackTransaction_11() throws ApiException{

        Transaction response = service.spendBack()
                .withAmount("10")
                .withAccountNumber("718571167")
                .withReceivingAccountNumber("718570870")
                .withAllowPending(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    // prerequisite -run transactionType 04,use the transNum or gatewayTransactionId or combo of globaltransId and globalTransSource from the response of transactionType 04 in splitFunds
    @Test
    public void SplitpayTimedPull_16() throws ApiException {

        Transaction response = service.splitFunds()
                .withAccountNumber("718575858")
                .withAmount("8500")
                .withReceivingAccountNumber("718576447")
                .withTransNum("5")
                .execute();

        assertNotNull(response);
        assertEquals("00",response.getResponseCode());
    }

    //use the transNum from the previous i.e split funds response
    @Test
    public void ReverseSplitPay_43() throws ApiException {

        Transaction response = service.reverseSplitPay()
                .withAccountNumber("718576447")
                .withAmount("100")
                .withCCAmount("0")
                .withRequireCCRefund(false)
                .withTransNum("9")
                .execute();

        assertNotNull(response);
        assertEquals("00",response.getResponseCode());
    }
}
