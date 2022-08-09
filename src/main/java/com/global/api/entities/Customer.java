package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.services.RecurringService;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
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
        return RecurringService.create(this, Customer.class, configName);
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
        saveChanges("default");
    }

    public void saveChanges(String configName) throws ApiException {
        try{
            RecurringService.edit(this, Customer.class, configName);
        } catch (ApiException e) {
            throw new ApiException("Update failed, see inner exception for more details", e);
        }
    }

    public RecurringPaymentMethod addPaymentMethod(String paymentId, IPaymentMethod paymentMethod) {
        return addPaymentMethod(paymentId, paymentMethod, null);
    }

    public RecurringPaymentMethod addPaymentMethod(String paymentId, IPaymentMethod paymentMethod, StoredCredential storedCredential) {
        String nameOnAccount = String.format("%s %s", firstName, lastName);
        if(StringUtils.isNullOrEmpty(nameOnAccount))
            nameOnAccount = company;

        RecurringPaymentMethod method = new RecurringPaymentMethod(paymentMethod);
        method.setAddress(address);
        method.setCustomerKey(key);
        method.setId(paymentId);
        method.setNameOnAccount(nameOnAccount);
        method.setStoredCredential(storedCredential);
        return method;
    }
}