package com.global.api.entities.payroll;

public interface IPayrollRequestBuilder {
    PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz);
}
