package com.global.api.entities.payroll;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.global.api.utils.ValueConverter;
import org.joda.time.DateTime;

import java.math.BigDecimal;

public class Employee extends PayrollEntity {
    private String clientCode;
    private int employeeId;
    private EmploymentStatus employmentStatus;
    private DateTime hireDate;
    private DateTime terminationDate;
    private String terminationReasonId;
    private String employeeNumber;
    private EmploymentCategory employmentCategory;
    private Integer timeClockId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String ssn;
    private String address1;
    private String address2;
    private String city;
    private String stateCode;
    private String zipCode;
    private MaritalStatus maritalStatus;
    private DateTime birthDay;
    private Gender gender;
    private int payGroupId;
    private PayTypeCode payTypeCode;
    private BigDecimal hourlyRate;
    private BigDecimal perPaySalary;
    private int workLocationId;
    private boolean deactivateAccounts;

    public String getClientCode() {
        return clientCode;
    }
    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }
    public int getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }
    public DateTime getHireDate() {
        return hireDate;
    }
    public void setHireDate(DateTime hireDate) {
        this.hireDate = hireDate;
    }
    public DateTime getTerminationDate() {
        return terminationDate;
    }
    public void setTerminationDate(DateTime terminationDate) {
        this.terminationDate = terminationDate;
    }
    public String getTerminationReasonId() {
        return terminationReasonId;
    }
    public void setTerminationReasonId(String terminationReasonId) {
        this.terminationReasonId = terminationReasonId;
    }
    public String getEmployeeNumber() {
        return employeeNumber;
    }
    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    public EmploymentCategory getEmploymentCategory() {
        return employmentCategory;
    }
    public void setEmploymentCategory(EmploymentCategory employmentCategory) {
        this.employmentCategory = employmentCategory;
    }
    public Integer getTimeClockId() {
        return timeClockId;
    }
    public void setTimeClockId(Integer timeClockId) {
        this.timeClockId = timeClockId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        this.address1 = address1;
    }
    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getStateCode() {
        return stateCode;
    }
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }
    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    public DateTime getBirthDay() {
        return birthDay;
    }
    public void setBirthDay(DateTime birthDay) {
        this.birthDay = birthDay;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public int getPayGroupId() {
        return payGroupId;
    }
    public void setPayGroupId(int payGroupId) {
        this.payGroupId = payGroupId;
    }
    public PayTypeCode getPayTypeCode() {
        return payTypeCode;
    }
    public void setPayTypeCode(PayTypeCode payTypeCode) {
        this.payTypeCode = payTypeCode;
    }
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    public BigDecimal getPerPaySalary() {
        return perPaySalary;
    }
    public void setPerPaySalary(BigDecimal perPaySalary) {
        this.perPaySalary = perPaySalary;
    }
    public int getWorkLocationId() {
        return workLocationId;
    }
    public void setWorkLocationId(int workLocationId) {
        this.workLocationId = workLocationId;
    }
    public boolean isDeactivateAccounts() {
        return deactivateAccounts;
    }
    public void setDeactivateAccounts(boolean deactivateAccounts) {
        this.deactivateAccounts = deactivateAccounts;
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException {
        ValueConverter<DateTime> dateConverter = new ValueConverter<DateTime>() {
            @Override
            public DateTime call(String value) throws Exception {
                if(value != null)
                    return DateTime.parse(value);
                return null;
            }
        };

        try {
            clientCode = encoder.decode(doc.getString("ClientCode"));
            employeeId = doc.getInt("EmployeeId");
            employmentStatus = EnumUtils.parse(EmploymentStatus.class, doc.getString("EmploymentStatus"));
            hireDate = doc.getValue("HireDate", dateConverter);
            terminationDate = doc.getValue("TerminationDate", dateConverter);
            terminationReasonId = doc.getString("TerminationReasonId");
            employeeNumber = doc.getString("EmployeeNumber");
            employmentCategory = EnumUtils.parse(EmploymentCategory.class, doc.getString("EmploymentCategory"));
            timeClockId = doc.getInt("TimeClockId");
            firstName = doc.getString("FirstName");
            lastName = encoder.decode(doc.getString("LastName"));
            middleName = doc.getString("MiddleName");
            ssn = encoder.decode(doc.getString("Ssn"));
            address1 = encoder.decode(doc.getString("Address1"));
            address2 = doc.getString("Address2");
            city = doc.getString("City");
            stateCode = doc.getString("StateCode");
            zipCode = encoder.decode(doc.getString("ZipCode"));
            maritalStatus = EnumUtils.parse(MaritalStatus.class, doc.getString("MaritalStatus"));

            // Birthday
            String birthday = encoder.decode(doc.getString("BirthDay"));
            if (!StringUtils.isNullOrEmpty(birthday))
                birthDay = DateTime.parse(birthday);

            gender = EnumUtils.parse(Gender.class, doc.getString("Gender"));
            payGroupId = doc.getInt("PayGroupId");
            payTypeCode = EnumUtils.parse(PayTypeCode.class, doc.getString("PayTypeCode"));
            hourlyRate = new BigDecimal(encoder.decode(doc.getString("HourlyRate")));
            perPaySalary = new BigDecimal(encoder.decode(doc.getString("PerPaySalary")));
            workLocationId = doc.getInt("WorkLocationId");
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }

    public IPayrollRequestBuilder addEmployeeRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String requestBody = new JsonDoc()
                        .set("ClientCode", encoder.encode(clientCode))
                        .set("EmployeeId", employeeId)
                        .set("EmploymentStatus", employmentStatus)
                        .set("HireDate", hireDate.toString())
                        //.set("TerminationDate", TerminationDate)
                        //.set("TerminationReasonId", TerminationReasonId)
                        //.set("EmployeeNumber", EmployeeNumber)
                        .set("EmploymentCategory", employmentCategory)
                        .set("TimeClockId", timeClockId)
                        .set("FirstName", firstName)
                        .set("LastName", encoder.encode(lastName))
                        .set("MiddleName", middleName)
                        .set("SSN", encoder.encode(ssn))
                        .set("Address1", encoder.encode(address1))
                        .set("Address2", address2)
                        .set("City", city)
                        .set("StateCode", stateCode)
                        .set("ZipCode", encoder.encode(zipCode))
                        .set("MaritalStatus", maritalStatus)
                        .set("BirthDate", encoder.encode(birthDay))
                        .set("Gender", gender)
                        .set("PayGroupId", payGroupId)
                        .set("PayTypeCode", payTypeCode)
                        .set("HourlyRate", encoder.encode(hourlyRate))
                        .set("PerPaySalary", encoder.encode(perPaySalary))
                        .set("WorkLocationId", workLocationId).toString();

                return new PayrollRequest("/api/pos/employee/AddEmployee", requestBody);
            }
        };
    }

    public IPayrollRequestBuilder updateEmployeeRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String requestBody = new JsonDoc()
                    .set("ClientCode", encoder.encode(clientCode))
                    .set("EmployeeId", employeeId)
                    .set("EmploymentStatus", employmentStatus)
                    .set("HireDate", hireDate.toString())
                    //.set("TerminationDate", TerminationDate)
                    //.set("TerminationReasonId", TerminationReasonId)
                    .set("EmployeeNumber", employeeNumber)
                    .set("EmploymentCategory", employmentCategory)
                    .set("TimeClockId", timeClockId)
                    .set("FirstName", firstName)
                    .set("LastName", encoder.encode(lastName))
                    .set("MiddleName", middleName)
                    .set("SSN", encoder.encode(ssn))
                    .set("Address1", encoder.encode(address1))
                    .set("Address2", address2)
                    .set("City", city)
                    .set("StateCode", stateCode)
                    .set("ZipCode", encoder.encode(zipCode))
                    .set("MaritalStatus", maritalStatus)
                    .set("BirthDate", encoder.encode(birthDay))
                    .set("Gender", gender)
                    .set("PayGroupId", payGroupId)
                    .set("PayTypeCode", payTypeCode)
                    .set("HourlyRate", encoder.encode(hourlyRate))
                    .set("PerPaySalary", encoder.encode(perPaySalary))
                    .set("WorkLocationId", workLocationId).toString();

                return new PayrollRequest("/api/pos/employee/UpdateEmployee", requestBody);
            }
        };
    }

    public IPayrollRequestBuilder terminateEmployeeRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String requestBody = new JsonDoc()
                        .set("ClientCode", encoder.encode(clientCode))
                        .set("EmployeeId", employeeId)
                        .set("TerminationDate", terminationDate.toString("MM/dd/yyyy"))
                        .set("TerminationReasonId", terminationReasonId)
                        .set("InactivateDirectDepositAccounts", deactivateAccounts ? 1 : 0)
                        .toString();

                return new PayrollRequest("/api/pos/employee/TerminateEmployee", requestBody);
            }
        };
    }
}
