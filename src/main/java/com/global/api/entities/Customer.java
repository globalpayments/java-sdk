package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.services.RecurringService;
import com.global.api.utils.StringUtils;

import java.util.List;

public class Customer extends RecurringEntity<Customer> {
    private String title;
    private String firstName;
    private String lastName;
    private String company;
    private String customerPassword;
    private String dateOfBirth;
    private String domainName;
    private String deviceFingerPrint;
    private Address address;
    private String homePhone;
    private String workPhone;
    private String fax;
    private String mobilePhone;
    private String email;
    private String comments;
    private String department;
    private String status;
    private List<RecurringPaymentMethod> paymentMethods;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
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
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getCustomerPassword() {
        return customerPassword;
    }
    public void setCustomerPassword(String customerPassword) {
        this.customerPassword = customerPassword;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    public String getDeviceFingerPrint() {
        return deviceFingerPrint;
    }
    public void setDeviceFingerPrint(String deviceFingerPrint) {
        this.deviceFingerPrint = deviceFingerPrint;
    }
    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }
    public String getHomePhone() {
        return homePhone;
    }
    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }
    public String getWorkPhone() {
        return workPhone;
    }
    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }
    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        this.fax = fax;
    }
    public String getMobilePhone() {
        return mobilePhone;
    }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public List<RecurringPaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }
    public void setPaymentMethods(List<RecurringPaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public Customer() {
        //super(Customer.class);
    }
    public Customer(String id) {
        this.id = id;
    }

    public Customer create() throws ApiException {
        return create("default");
    }
    public Customer create(String configName) throws ApiException {
        return RecurringService.create(this, Customer.class);
    }

    public void delete() throws ApiException {
        delete(false);
    }
    public void delete(boolean force) throws ApiException {
        try{
            RecurringService.delete(this, Customer.class, force);
        }
        catch(ApiException e) {
            throw new ApiException("Failed to delete payment method, see inner exception for more details.", e);
        }
    }

    public static Customer find(String id) throws ApiException {
        return find(id, "default");
    }
    public static Customer find(String id, String configName) throws ApiException {
        checkSupportsRetrieval(configName);

        List<Customer> response = RecurringService.search(CustomerCollection.class)
                .addSearchCriteria("customerIdentifier", id)
                .execute();
        if(response.size() > 0) {
            Customer entity = response.get(0);
            if (entity != null)
                return RecurringService.get(entity.getKey(), Customer.class);
        }
        return null;
    }

    public static List<Customer> findAll() throws ApiException {
        return findAll("default");
    }
    public static List<Customer> findAll(String configName) throws ApiException {
        checkSupportsRetrieval(configName);
        return RecurringService.search(CustomerCollection.class).execute();
    }

    public static Customer get(String key) throws ApiException {
        return get(key, "default");
    }
    public static Customer get(String key, String configName) throws ApiException {
        checkSupportsRetrieval(configName);
        return RecurringService.get(key, Customer.class);
    }

    public void saveChanges() throws ApiException {
        try{
            RecurringService.edit(this, Customer.class);
        }
        catch (ApiException e) {
            throw new ApiException("Update failed, see inner exception for more details", e);
        }
    }

    public RecurringPaymentMethod addPaymentMethod(String paymentId, IPaymentMethod paymentMethod) {
        String nameOnAccount = String.format("%s %s", firstName, lastName);
        if(StringUtils.isNullOrEmpty(nameOnAccount))
            nameOnAccount = company;

        RecurringPaymentMethod method = new RecurringPaymentMethod(paymentMethod);
        method.setAddress(address);
        method.setCustomerKey(key);
        method.setId(paymentId);
        method.setNameOnAccount(nameOnAccount);
        return method;
    }
}