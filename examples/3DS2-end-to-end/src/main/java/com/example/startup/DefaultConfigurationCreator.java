/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.startup;

import com.example.applicationparameters.ApplicationParameters;
import com.global.api.ServicesContainer;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.serviceConfigs.GpApiConfig;

class DefaultConfigurationCreator {


    void setDefaultConfig() throws ConfigurationException {

        System.out.println();
        System.out.println("*** 00 *** RunOnStartUp");
        System.out.println();

        String baseUrl = ApplicationParameters.getBaseUrl();

        GpApiConfig config = new GpApiConfig();
        config.setAppId(ApplicationParameters.getAppId());
        config.setAppKey(ApplicationParameters.getAppKey());
        config.setEnvironment(Environment.TEST);
        config.setCountry("GB");
        config.setChannel(Channel.CardNotPresent);
        config.setMethodNotificationUrl(baseUrl + "/methodNotificationUrl");
        config.setMerchantContactUrl("https://www.example.com/contact-us");
        config.setChallengeNotificationUrl(baseUrl + "/challengeNotificationUrl");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());

        ServicesContainer.configureService(config);
    }

}
