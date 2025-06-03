package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.Installment;

public interface IInstallmentEntity {
    Installment create(String configName) throws ApiException;
}
