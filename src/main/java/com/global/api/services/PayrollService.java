package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.payroll.*;
import com.global.api.gateways.PayrollConnector;
import com.global.api.serviceConfigs.PayrollConfig;
import com.global.api.terminals.abstractions.IDisposable;
import org.joda.time.DateTime;

import java.util.List;

public class PayrollService implements IDisposable {
    private String configName;
    private PayrollConnector connector;

    public PayrollService(PayrollConfig config) throws ApiException {
        this(config, "default");
    }
    public PayrollService(PayrollConfig config, String configName) throws ApiException {
        this.configName = configName;
        ServicesContainer.configureService(config, configName);

        // set the connector and sign in
        this.connector = ServicesContainer.getInstance().getPayroll(configName);
        this.connector.signIn();
    }

    public List<ClientInfo> getClientInfo(int federalEin) throws ApiException {
        ClientInfo request = new ClientInfo();
        request.setFederalEin(federalEin);

        PayrollResponse<ClientInfo> response = connector.sendEncryptedRequest(request.getClientInfoRequest(), ClientInfo.class);
        if(response != null)
            return response.getResults();
        return null;
    }

    // GetEmployees
    public EmployeeFinder getEmployees(String clientCode) throws ApiException {
        return new EmployeeFinder(connector).withClientCode(clientCode);
    }

    // AddEmployee
    public Employee addEmployee(Employee employee) throws ApiException {
        PayrollResponse<Employee> response = connector.sendEncryptedRequest(employee.addEmployeeRequest(), Employee.class);
        if(response != null)
            return response.getResults().get(0);
        return null;
    }

    // UpdateEmployee
    public Employee updateEmployee(Employee employee) throws ApiException {
        PayrollResponse<Employee> response = connector.sendEncryptedRequest(employee.updateEmployeeRequest(), Employee.class);
        if(response != null)
            return response.getResults().get(0);
        return null;
    }

    // TerminateEmployee
    public Employee terminateEmployee(Employee employee, DateTime terminationDate, TerminationReason terminationReason) throws ApiException {
        return terminateEmployee(employee, terminationDate, terminationReason, false);
    }
    public Employee terminateEmployee(Employee employee, DateTime terminationDate, TerminationReason terminationReason, boolean deactivateAccounts) throws ApiException {
        employee.setTerminationDate(terminationDate);
        employee.setTerminationReasonId(terminationReason.getId());
        employee.setDeactivateAccounts(deactivateAccounts);

        connector.sendEncryptedRequest(employee.terminateEmployeeRequest(), Employee.class);
        List<Employee> results = getEmployees(employee.getClientCode())
                .withEmployeeId(employee.getEmployeeId())
                .find();

        if(results != null && results.size() > 0)
            return results.get(0);
        return null;
    }

    // GetTerminationReasons
    public List<TerminationReason> getTerminationReasons(String clientCode) throws ApiException {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setClientCode(clientCode);

        return getTerminationReasons(clientInfo);
    }
    public List<TerminationReason> getTerminationReasons(ClientInfo client) throws ApiException {
        return getPayrollCollectionItem(client, TerminationReason.class);
    }

    // GetWorkLocations
    public List<WorkLocation> getWorkLocations(String clientCode) throws ApiException {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setClientCode(clientCode);

        return getWorkLocations(clientInfo);
    }
    public List<WorkLocation> getWorkLocations(ClientInfo client) throws ApiException {
        return getPayrollCollectionItem(client, WorkLocation.class);
    }

    // GetLaborFields
    public List<LaborField> getLaborFields(String clientCode) throws ApiException {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setClientCode(clientCode);

        return getLaborFields(clientInfo);
    }
    public List<LaborField> getLaborFields(ClientInfo client) throws ApiException {
        return getPayrollCollectionItem(client, LaborField.class);
    }

    // GetPayGroups
    public List<PayGroup> getPayGroups(String clientCode) throws ApiException {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setClientCode(clientCode);

        return getPayGroups(clientInfo);
    }
    public List<PayGroup> getPayGroups(ClientInfo client) throws ApiException {
        return getPayrollCollectionItem(client, PayGroup.class);
    }

    // GetPayItems
    public List<PayItem> getPayItems(String clientCode) throws ApiException {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setClientCode(clientCode);

        return getPayItems(clientInfo);
    }
    public List<PayItem> getPayItems(ClientInfo client) throws ApiException {
        return getPayrollCollectionItem(client, PayItem.class);
    }

    // PostPayData
    public boolean postPayrollData(PayrollRecord... payrollRecords) throws ApiException {
        return postPayrollData(new PayrollData(payrollRecords));
    }
    public boolean postPayrollData(PayrollData payrollData) throws ApiException {
        PayrollResponse<PayrollData> response = connector.sendEncryptedRequest(payrollData.postPayrollRequest(), PayrollData.class);
        return response != null;
    }

    private <T extends PayrollCollectionItem> List<T> getPayrollCollectionItem(ClientInfo client, Class<T> clazz) throws ApiException {
        PayrollResponse<T> response = connector.sendEncryptedRequest(client.getCollectionRequestByType(), clazz);
        if(response != null)
            return response.getResults();
        return null;
    }

    public void dispose() {
        try {
            connector.signOut();
        }
        catch(Exception exc) { /* NOM NOM */ }
    }
}
