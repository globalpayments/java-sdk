package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

import java.util.ArrayList;

public class PayrollResponse <TResult extends PayrollEntity> extends BasePayrollResponse {
    private ArrayList<TResult> results;

    public ArrayList<TResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<TResult> results) {
        this.results = results;
    }

    public PayrollResponse(String rawResponse, PayrollEncoder encoder, Class<TResult> clazz) throws ApiException {
        super(rawResponse);

        results = new ArrayList<TResult>();
        if (rawResults != null) {
            for(JsonDoc result: rawResults) {
                try {
                    TResult item = clazz.newInstance();
                    item.fromJson(result, encoder);
                    results.add(item);
                }
                catch(Exception exc) {
                    throw new ApiException(exc.getMessage(), exc);
                }
            }
        }
    }
}
