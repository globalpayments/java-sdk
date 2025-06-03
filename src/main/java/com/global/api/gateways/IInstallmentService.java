package com.global.api.gateways;

import com.global.api.builders.InstallmentBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.Installment;

public interface IInstallmentService {

    Installment processInstallment(InstallmentBuilder builder) throws ApiException;
}
