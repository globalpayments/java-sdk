package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class PayrollData extends PayrollEntity {
    private ArrayList<PayrollRecord> records;

    public PayrollData(PayrollRecord... records) {
        this.records = new ArrayList<PayrollRecord>();
        this.records.addAll(Arrays.asList(records));
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException {
        throw new ApiException("Method not implemented");
    }

    public IPayrollRequestBuilder postPayrollRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String[] payrollRecords = new String[records.size()];
                for(int i = 0; i < records.size(); i++) {
                    payrollRecords[i] = records.get(i).toJson(encoder);
                }
                String requestBody = String.format("[%s]", StringUtils.join(",", payrollRecords));

                return new PayrollRequest("/api/pos/timeclock/PostPayData", requestBody);
            }
        };
    }
}
