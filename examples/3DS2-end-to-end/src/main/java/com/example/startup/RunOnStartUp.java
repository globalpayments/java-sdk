/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.startup;

import com.global.api.entities.exceptions.ConfigurationException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RunOnStartUp implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {

        try {
            DefaultConfigurationCreator defaultConfigurationCreator = new DefaultConfigurationCreator();
            defaultConfigurationCreator.setDefaultConfig();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException("In this example, be loud about it.");
        }

    }

}
