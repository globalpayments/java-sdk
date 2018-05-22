package com.global.api.entities.payroll;

import com.global.api.entities.enums.FilterPayTypeCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.PayrollConnector;
import org.joda.time.DateTime;

import java.util.List;

public class EmployeeFinder {
    private PayrollConnector connector;
    private EmployeeFilter _filter;

    public EmployeeFinder(PayrollConnector connector) {
        _filter = new EmployeeFilter();
        this.connector = connector;
    }

    public EmployeeFinder withClientCode(String value) {
        _filter.setClientCode(value);
        return this;
    }
    public EmployeeFinder withEmployeeId(int value) {
        _filter.setEmployeeId(value);
        return this;
    }
    public EmployeeFinder activeOnly(boolean value) {
        _filter.setActive(value);
        return this;
    }
    public EmployeeFinder withEmployeeOffset(int value) {
        _filter.setEmployeeOffset(value);
        return this;
    }
    public EmployeeFinder withEmployeeCount(int value) {
        _filter.setEmployeeCount(value);
        return this;
    }
    public EmployeeFinder withFromDate(DateTime value) {
        _filter.setFromDate(value);
        return this;
    }
    public EmployeeFinder withToDate(DateTime value) {
        _filter.setToDate(value);
        return this;
    }
    public EmployeeFinder withPayType(FilterPayTypeCode value) {
        _filter.setPayTypeCode(value);
        return this;
    }

    public List<Employee> find() throws ApiException {
        PayrollResponse<Employee> response = connector.sendEncryptedRequest(_filter.getEmployeeRequest(), Employee.class);
        return response.getResults();
    }
}
