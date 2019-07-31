package MyApp;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewMain {

    public static void main(String[] args) {
        
        // configure client & request settings
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("gpsalesdemo");
        config.setAccountId("internet");
        config.setSharedSecret("secret");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");

        try {
            ServicesContainer.configureService(config);
        } catch (ConfigurationException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create the card object
        CreditCardData card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("131");
        card.setCvnPresenceIndicator(CvnPresenceIndicator.Present);
        card.setCardHolderName("James Mason");

        try {
            Transaction response = card.authorize(new BigDecimal("30.00"))
                    .withMultiCapture(true)
                    .withEstimatedTransactions(2)
                    .withCurrency("CAD")
                    .execute();

            String result = response.getResponseCode();
            String message = response.getResponseMessage();
            String orderId = response.getOrderId();
            String authCode = response.getAuthorizationCode();
            String paymentsReference = response.getTransactionId();
            
            System.out.println("Pre-Auth");
            System.out.println(result);
            System.out.println(message);
            System.out.println(orderId);
            System.out.println(authCode);
            System.out.println(paymentsReference);
            System.out.println("---------------");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Transaction settle = Transaction.fromId(paymentsReference, orderId);
            settle.setAuthorizationCode(authCode);
            response = settle.multicapture(new BigDecimal("5.00"))
                    //.withEstimatedTransactions(5)
                    //.isFinal(true)
                    .withCurrency("CAD")
                    .execute();

            result = response.getResponseCode();
            message = response.getResponseMessage();
            
            System.out.println("Capture");
            System.out.println(result);
            System.out.println(message);
            System.out.println(orderId);
            System.out.println(authCode);
            System.out.println(paymentsReference);
            System.out.println("---------------");
            
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            settle = Transaction.fromId(paymentsReference, orderId);
            settle.setAuthorizationCode(authCode);
            response = settle.multicapture(new BigDecimal("5.00"))
                    //.withEstimatedTransactions(5)
                    //.isFinal(true)
                    .withCurrency("CAD")
                    .execute();

            result = response.getResponseCode();
            message = response.getResponseMessage();
            
            System.out.println("Capture");
            System.out.println(result);
            System.out.println(message);
            System.out.println(orderId);
            System.out.println(authCode);
            System.out.println(paymentsReference);
            System.out.println("---------------");
            
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            settle = Transaction.fromId(paymentsReference, orderId);
            settle.setAuthorizationCode(authCode);
            response = settle.multicapture(new BigDecimal("5.00"))
                    //.withEstimatedTransactions(5)
                    //.isFinal(true)
                    .withCurrency("CAD")
                    .execute();

            result = response.getResponseCode();
            message = response.getResponseMessage();
            
            System.out.println("Capture");
            System.out.println(result);
            System.out.println(message);
            System.out.println(orderId);
            System.out.println(authCode);
            System.out.println(paymentsReference);
            System.out.println("---------------");


        } catch (ApiException exce) {
            System.out.println(exce);
            // TODO: add your error handling here
        }
    }
}
