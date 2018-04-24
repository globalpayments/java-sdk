package com.global.api.entities.payroll;

import com.global.api.entities.enums.FilterPayTypeCode;
import com.global.api.entities.enums.PayTypeCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;

public class EmployeeFilter {
    private String clientCode;
    private Integer employeeId;
    private boolean active;
    private Integer employeeOffset;
    private Integer employeeCount;
    private DateTime fromDate;
    private DateTime toDate;
    private FilterPayTypeCode payTypeCode;

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
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Integer getEmployeeOffset() {
        return employeeOffset;
    }
    public void setEmployeeOffset(Integer employeeOffset) {
        this.employeeOffset = employeeOffset;
    }
    public Integer getEmployeeCount() {
        return employeeCount;
    }
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    public DateTime getFromDate() {
        return fromDate;
    }
    public void setFromDate(DateTime fromDate) {
        this.fromDate = fromDate;
    }
    public DateTime getToDate() {
        return toDate;
    }
    public void setToDate(DateTime toDate) {
        this.toDate = toDate;
    }
    public FilterPayTypeCode getPayTypeCode() {
        return payTypeCode;
    }
    public void setPayTypeCode(FilterPayTypeCode payTypeCode) {
        this.payTypeCode = payTypeCode;
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException {
        throw new ApiException("Method not implemented.");
    }

    public IPayrollRequestBuilder getEmployeeRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String requestBody = new JsonDoc()
                        .set("ClientCode", encoder.encode(clientCode))
                        .set("EmployeeId", employeeId)
                        .set("ActiveEmployeeOnly", active)
                        .set("EmployeeOffset", employeeOffset)
                        .set("EmployeeCount", employeeCount)
                        .set("FromDate", fromDate != null ? fromDate.toString("MM/dd/yyyy HH:mm:ss") : null)
                        .set("ToDate", toDate != null ? toDate.toString("MM/dd/yyyy HH:mm:ss") : null)
                        .set("PayTypeCode", payTypeCode)
                        .toString();

                return new PayrollRequest((employeeId != null) ? "/api/pos/employee/GetEmployee" : "/api/pos/employee/GetEmployees", requestBody);
            }
        };
    }
}
