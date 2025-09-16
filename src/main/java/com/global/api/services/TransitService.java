package com.global.api.services;

import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.TransitConnector;

public class TransitService {

    /**
     * Generates a transaction key for Transit configuration
     *
     * @param environment The target environment (TEST or PRODUCTION)
     * @param merchantId  The merchant ID
     * @param username    The username
     * @param password    The password
     * @return The generated transaction key
     * @throws ApiException If key generation fails
     */
    public static String generateTransactionKey(Environment environment, String merchantId, String username, String password) throws ApiException {
        return generateTransactionKey(environment, merchantId, username, password, null);
    }

    /**
     * Generates a transaction key for Transit configuration
     *
     * @param environment    The target environment (TEST or PRODUCTION)
     * @param merchantId     The merchant ID
     * @param username       The username
     * @param password       The password
     * @param transactionKey Optional existing transaction key
     * @return The generated transaction key
     * @throws ApiException If key generation fails
     */
    public static String generateTransactionKey(Environment environment, String merchantId, String username, String password, String transactionKey) throws ApiException {
        TransitConnector connector = new TransitConnector();
        connector.setMerchantId(merchantId);
        connector.setTransactionKey(transactionKey);
        connector.setServiceUrl(environment.equals(Environment.PRODUCTION) ?
                ServiceEndpoints.TRANSIT_MULTIPASS_PRODUCTION.getValue() :
                ServiceEndpoints.TRANSIT_MULTIPASS_TEST.getValue());
        connector.setTimeout(10000);

        return connector.generateKey(username, password);
    }
}