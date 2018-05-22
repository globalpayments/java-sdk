package com.global.api.entities.payroll;

import com.global.api.utils.JsonDoc;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PayrollRecord {
    private Integer recordId;
    private String clientCode;
    private Integer employeeId;
    private ArrayList<LaborField> payItemLaborFields;
    private String payItemTitle;
    public BigDecimal hours;
    public BigDecimal dollars;
    public BigDecimal payRate;

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public ArrayList<LaborField> getPayItemLaborFields() {
        return payItemLaborFields;
    }

    public void setPayItemLaborFields(ArrayList<LaborField> payItemLaborFields) {
        this.payItemLaborFields = payItemLaborFields;
    }

    public String getPayItemTitle() {
        return payItemTitle;
    }

    public void setPayItemTitle(String payItemTitle) {
        this.payItemTitle = payItemTitle;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public BigDecimal getDollars() {
        return dollars;
    }

    public void setDollars(BigDecimal dollars) {
        this.dollars = dollars;
    }

    public BigDecimal getPayRate() {
        return payRate;
    }

    public void setPayRate(BigDecimal payRate) {
        this.payRate = payRate;
    }

    String toJson(PayrollEncoder encoder) {
        return new JsonDoc()
                .set("RecordId", recordId)
                .set("ClientCode", encoder.encode(clientCode))
                .set("EmployeeId", employeeId)
                //.set("PayItemLaborFields", payItemLaborFields)
                .set("PayItemTitle", payItemTitle)
                .set("Hours", hours != null ? hours.toString() : null)
                .set("Dollars", dollars != null ? dollars.toString() : null)
                .set("PayRate", payRate != null ? payRate.toString() : null)
                .toString();
    }
}
