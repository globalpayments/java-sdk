package com.global.api.tests.payroll;

import com.global.api.entities.enums.EmploymentStatus;
import com.global.api.entities.enums.FilterPayTypeCode;
import com.global.api.entities.enums.MaritalStatus;
import com.global.api.entities.enums.PayTypeCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.payroll.*;
import com.global.api.serviceConfigs.PayrollConfig;
import com.global.api.services.PayrollService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PayrollTests {
    private PayrollService _service;

    public PayrollTests() throws ApiException {
        PayrollConfig config = new PayrollConfig();
        config.setUsername("testapiuser.russell");
        config.setPassword("payroll2!");
        config.setApiKey("iGF9UtaLc526poWWNgUpiCoO3BckcZUKNF3nhyKul8A=");
        config.setServiceUrl("https://taapi.heartlandpayrollonlinetest.com/PosWebUI/Test/Test");
        config.setTimeout(0);

        _service = new PayrollService(config);
    }

    @Test
    public void getClientInfoTest() throws ApiException {
        List<ClientInfo> clients = _service.getClientInfo(578901244);
        assertNotNull(clients);
        if (clients.size() > 0) {
            assertNotNull(clients.get(0).getClientCode());
            assertNotNull(clients.get(0).getClientName());
            assertEquals(578901244, (long)clients.get(0).getFederalEin());
        }
    }

    @Test
    public void getActiveEmployeesTest() throws ApiException {
        List<Employee> employees = _service.getEmployees("0140SY42")
                .activeOnly(true)
                .find();
        assertNotNull(employees);

        for(Employee employee: employees)
            assertEquals(EmploymentStatus.Active, employee.getEmploymentStatus());
    }

    @Test
    public void getInactiveEmployeesTest() throws ApiException {
        List<Employee> employees = _service.getEmployees("0140SY42")
                .activeOnly(false)
                .find();
        assertNotNull(employees);
    }

    @Test
    public void get20EmployeesTest() throws ApiException {
        List<Employee>  employees = _service.getEmployees("0140SY42")
                .activeOnly(false)
                .withEmployeeCount(20)
                .find();
        assertNotNull(employees);
        assertTrue(employees.size() == 20);
    }

    @Test
    public void getEmployeesInDateRangeTest() throws ApiException {
        List<Employee>  employees = _service.getEmployees("0140SY42")
                .withFromDate(DateTime.parse("01/01/2014", DateTimeFormat.forPattern("MM/dd/yyyy")))
                .withToDate(DateTime.parse("01/01/2015", DateTimeFormat.forPattern("MM/dd/yyyy")))
                .find();
        assertNotNull(employees);
        assertFalse(false);
    }

    @Test
    public void getHourlyEmployeesTest() throws ApiException {
        List<Employee>  employees = _service.getEmployees("0140SY42")
                .withPayType(FilterPayTypeCode.Hourly)
                .find();
        assertNotNull(employees);

        for(Employee employee: employees)
            assertTrue(PayTypeCode.Hourly.equals(employee.getPayTypeCode()));
    }

    @Test
    public void get1099EmployeesTest() throws ApiException {
        List<Employee>  employees = _service.getEmployees("0140SY42")
                .withPayType(FilterPayTypeCode.T1099)
                .find();
        assertNotNull(employees);

        for(Employee employee: employees)
            assertFalse(PayTypeCode.Hourly.equals(employee.getPayTypeCode()));
    }

    @Test
    public void getSingleEmployeeTest() throws ApiException {
        List<Employee>  employees = _service.getEmployees("0140SY42")
                .withEmployeeId(284045)
                .find();
        assertNotNull(employees);
        assertEquals(1, employees.size());
    }

    @Test
    public void updateEmployeeTest() throws ApiException {
        Employee  employee = _service.getEmployees("0140SY42")
                .activeOnly(true)
                .find().get(0);
        assertNotNull(employee);

        MaritalStatus status = MaritalStatus.Married;
        if (employee.getMaritalStatus().equals(MaritalStatus.Married))
            status = MaritalStatus.Single;
        employee.setMaritalStatus(status);

        Employee response = _service.updateEmployee(employee);
        assertNotNull(response);
        assertEquals(status, employee.getMaritalStatus());
    }

    @Test
    public void getTerminationReasonsTest() throws ApiException {
        List<TerminationReason> terminationReasons = _service.getTerminationReasons("0140SY42");
        assertNotNull(terminationReasons);
        assertTrue(terminationReasons.size() > 0);
    }

    @Test
    public void terminateEmployeeTest() throws ApiException {
        Employee employee = _service.getEmployees("0140SY42")
                .withEmployeeId(284045)
                .find().get(0);
        assertNotNull(employee);

        TerminationReason terminationReason = _service.getTerminationReasons("0140SY42").get(0);
        assertNotNull(terminationReason);

        DateTime terminationDate = DateTime.now();
        Employee response = _service.terminateEmployee(employee, terminationDate, terminationReason, false);
        assertNotNull(response);
        assertEquals(terminationDate, employee.getTerminationDate());
        assertEquals(response.getEmploymentStatus(), EmploymentStatus.Terminated);
    }

    @Test
    public void getWorLocationsTest() throws ApiException {
        List<WorkLocation> collection = _service.getWorkLocations("0140SY42");
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
    }

    @Test
    public void getLaborFieldsTest() throws ApiException {
        List<LaborField> collection = _service.getLaborFields("0140SY42");
        assertNotNull(collection);
        assertTrue(collection.size() == 0);
    }

    @Test
    public void getPayGroupsTest() throws ApiException {
        List<PayGroup> collection = _service.getPayGroups("0140SY42");
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
    }

    @Test
    public void getPayItemsTest() throws ApiException {
        List<PayItem> collection = _service.getPayItems("0140SY42");
        assertNotNull(collection);
        assertTrue(collection.size() > 0);
    }

    @Test
    public void postPayDataTest() throws ApiException {
        PayItem payItem = _service.getPayItems("0140SY42").get(0);

        PayrollRecord record = new PayrollRecord();
        record.setRecordId(1);
        record.setClientCode("0140SY42");
        record.setEmployeeId(284045);
        record.setHours(new BigDecimal("80"));
        record.setPayItemTitle(payItem.getDescription());

        boolean response = _service.postPayrollData(record);
        assertNotNull(response);
        assertTrue(response);
    }
}
