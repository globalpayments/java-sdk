package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

public abstract class PayrollEntity {
    abstract void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException;
}
