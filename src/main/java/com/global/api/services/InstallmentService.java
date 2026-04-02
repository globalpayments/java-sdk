package com.global.api.services;

import com.global.api.builders.InstallmentBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.Installment;

public class InstallmentService {

    /**
     * Creates the Installment
     * @param entity
     * @param configName
     * @return {@link Installment}
     * @throws ApiException
     */
    public static Installment create(Installment entity, String configName) throws ApiException {
        return new InstallmentBuilder(entity) {
            @Override
            public void setupValidations() {

            }
        }.execute(configName);
    }

    /**
     * Gets installment details by ID
     *
     * @param installmentId the ID of the installment
     * @param configName the configuration name
     * @return {@link Installment}
     * @throws ApiException
     */
    public static Installment get(String installmentId, String configName) throws ApiException {
        return new InstallmentBuilder(installmentId) {
            @Override
            public void setupValidations() {

            }
        }.execute(configName);
    }
}
