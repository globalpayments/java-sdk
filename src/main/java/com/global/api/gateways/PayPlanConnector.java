package com.global.api.gateways;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.*;
import com.global.api.utils.DateUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.global.api.utils.ValueConverter;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PayPlanConnector extends RestGateway implements IRecurringGateway {
    private String secretApiKey;
    private ValueConverter<Date> dateConverter = new ValueConverter<Date>(){
        public Date call(String value) {
            if(value == null)
                return null;

            SimpleDateFormat parser = new SimpleDateFormat("MMddyyyy");
            try {
                return parser.parse(value);
            }
            catch(ParseException e) { return null; }
        }
    };
    private ValueConverter<BigDecimal> amountConverter = new ValueConverter<BigDecimal>() {
        public BigDecimal call(String value) {
            if(value != null)
                return StringUtils.toAmount(value);
            return null;
        }
    };
    private ValueConverter<Boolean> yesNoConverter = new ValueConverter<Boolean>() {
        public Boolean call(String value) {
            return (value != null) && !value.equals("No");
        }
    };

    public String getSecretApiKey() {
        return secretApiKey;
    }
    public void setSecretApiKey(String secretApiKey) {
        this.secretApiKey = secretApiKey;
        if(secretApiKey != null) {
            byte[] encoded = Base64.encodeBase64(secretApiKey.getBytes());
            String auth = String.format("Basic %s", new String(encoded));
            headers.put("Authorization", auth);
        }
    }
    public boolean supportsRetrieval() { return true; }
    public boolean supportsUpdatePaymentDetails() { return false; }

    public <T> T processRecurring(RecurringBuilder<T> builder, Class<T> clazz) throws ApiException {
        JsonDoc request = new JsonDoc();

        if(builder.getTransactionType() == TransactionType.Create || builder.getTransactionType() == TransactionType.Edit) {
            if(builder.getEntity() instanceof Customer) {
                buildCustomer(request, (Customer)builder.getEntity());
            }
            else if(builder.getEntity() instanceof RecurringPaymentMethod) {
                buildPaymentMethod(request, (RecurringPaymentMethod)builder.getEntity(), builder.getTransactionType());
            }
            else if(builder.getEntity() instanceof Schedule) {
                buildSchedule(request, (Schedule)builder.getEntity(), builder.getTransactionType());
            }
        }
        else if(builder.getTransactionType() == TransactionType.Search) {
            for(String key: builder.getSearchCriteria().keySet()) {
                request.set(key, builder.getSearchCriteria().get(key));
            }
        }
        else if(builder.getTransactionType() == TransactionType.Delete) {
            request.set("forceDelete", builder.isForceDelete());
        }

        String response = doTransaction(mapMethod(builder.getTransactionType()), mapUrl(builder, clazz), request.toString());
        return mapResponse(response, clazz);
    }

    private <T> T mapResponse(String rawResponse, Class<T> clazz) throws ApiException {
        // DELETE returns nothing and that's OK
        if(StringUtils.isNullOrEmpty(rawResponse))
            return null;

        // Normal response elements
        JsonDoc response = JsonDoc.parse(rawResponse);
        try {
            T type = clazz.newInstance();
            if(type instanceof Customer)
                return hydrateCustomer(response, clazz);
            else if(type instanceof CustomerCollection) {
                for(JsonDoc customer: response.getEnumerator("results")) {
                    ((CustomerCollection)type).add(hydrateCustomer(customer, Customer.class));
                }
                return type;
            }
            else if(type instanceof RecurringPaymentMethod)
                return hydratePaymentMethod(response, clazz);
            else if(type instanceof RecurringPaymentMethodCollection) {
                for(JsonDoc payment: response.getEnumerator("results")) {
                    ((RecurringPaymentMethodCollection)type).add(hydratePaymentMethod(payment, RecurringPaymentMethod.class));
                }
                return type;
            }
            else if(type instanceof Schedule)
                return hydrateSchedule(response, clazz);
            else if(type instanceof ScheduleCollection) {
                for(JsonDoc schedule: response.getEnumerator("results")) {
                    ((ScheduleCollection)type).add(hydrateSchedule(schedule, Schedule.class));
                }
                return type;
            }
            return type;
        }
        catch(Exception e) {
            throw new ApiException(e.getMessage(), e);    
        }
    }

    private String mapMethod(TransactionType type) {
        switch(type) {
            case Create:
            case Search:
                return "POST";
            case Edit:
                return "PUT";
            case Delete:
                return "DELETE";
            default:
                return "GET";
        }
    }

    private String mapUrl(RecurringBuilder<?> builder, Class<?> clazz) throws ApiException {
        if(clazz == null)
            throw new ApiException("Cannot determine endpoint.");

        String suffix = "";
        if(builder.getTransactionType() == TransactionType.Fetch
                || builder.getTransactionType() == TransactionType.Delete
                || builder.getTransactionType() == TransactionType.Edit)
            suffix = "/" + builder.getKey();

        try {
            Object type = clazz.newInstance();
            if(type instanceof Customer || type instanceof CustomerCollection)
                return String.format("%s%s", (builder.getTransactionType() == TransactionType.Search) ? "searchCustomers" : "customers", suffix);
            else if(type instanceof RecurringPaymentMethod || type instanceof RecurringPaymentMethodCollection) {
                String paymentMethod = "";
                if(builder.getTransactionType() == TransactionType.Create)
                    paymentMethod = (((RecurringPaymentMethod)builder.getEntity()).getPaymentMethod() instanceof Credit) ? "CreditCard" : "ACH";
                else if(builder.getTransactionType() == TransactionType.Edit)
                    paymentMethod = ((RecurringPaymentMethod)builder.getEntity()).getPaymentType().replace(" ", "");
                return String.format("%s%s%s", (builder.getTransactionType() == TransactionType.Search) ? "searchPaymentMethods" : "paymentMethods", paymentMethod, suffix);
            }
            else if(type instanceof Schedule || type instanceof ScheduleCollection)
                return String.format("%s%s", (builder.getTransactionType() == TransactionType.Search) ? "searchSchedules" : "schedules", suffix);
            throw new UnsupportedTransactionException();
        }
        catch(Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    private JsonDoc buildCustomer(JsonDoc request, Customer customer) {
        if (customer != null) {
            request.set("customerIdentifier", customer.getId());
            request.set("firstName", customer.getFirstName());
            request.set("lastName", customer.getLastName());
            request.set("company", customer.getCompany());
            request.set("customerStatus", customer.getStatus());
            request.set("primaryEmail", customer.getEmail());
            request.set("phoneDay", customer.getHomePhone());
            request.set("phoneEvening", customer.getWorkPhone());
            request.set("phoneMobile", customer.getMobilePhone());
            request.set("fax", customer.getFax());
            request.set("title", customer.getTitle());
            request.set("department", customer.getDepartment());
            buildAddress(request, customer.getAddress());
        }
        return request;
    }

    private JsonDoc buildPaymentMethod(JsonDoc request, RecurringPaymentMethod payment, TransactionType type) {
        if (payment != null) {
            request.set("preferredPayment", payment.isPreferredPayment());
            request.set("paymentMethodIdentifier", payment.getId());
            request.set("customerKey", payment.getCustomerKey());
            request.set("nameOnAccount", payment.getNameOnAccount());
            buildAddress(request, payment.getAddress());

            if (type == TransactionType.Create) {
                String tokenValue = getToken(payment.getPaymentMethod());
                boolean hasToken = tokenValue != null;
                JsonDoc paymentInfo = null;
                if (payment.getPaymentMethod() instanceof ICardData) {
                    ICardData method = (ICardData) payment.getPaymentMethod();
                    paymentInfo = request.subElement(hasToken ? "alternateIdentity" : "card")
                            .set("type", hasToken ? "SINGLEUSETOKEN" : null)
                            .set(hasToken ? "token" : "number", hasToken ? tokenValue : method.getNumber())
                            .set("expMon", method.getExpMonth())
                            .set("expYear", method.getExpYear());
                    request.set("cardVerificationValue", method.getCvn());
                }
                else if (payment.getPaymentMethod() instanceof ITrackData) {
                    ITrackData method = (ITrackData)payment.getPaymentMethod();
                    paymentInfo = request.subElement("track")
                            .set("data", method.getValue())
                            .set("dataEntryMode", method.getEntryMethod().getValue().toUpperCase());
                }
                else if (payment.getPaymentMethod() instanceof eCheck) {
                    eCheck check = (eCheck)payment.getPaymentMethod();
                    request.set("achType", StringUtils.toInitialCase(check.getAccountType().getValue()))
                            .set("accountType", StringUtils.toInitialCase(check.getCheckType()))
                            .set("telephoneIndicator", (check.getSecCode().equals(SecCode.Ccd) || check.getSecCode().equals(SecCode.Ppd)) ? false : true)
                            .set("routingNumber", check.getRoutingNumber())
                            .set("accountNumber", check.getAccountNumber())
                            .set("accountHolderYob", check.getBirthYear())
                            .set("driversLicenseState", check.getDriversLicenseState())
                            .set("driversLicenseNumber", check.getDriversLicenseNumber())
                            .set("socialSecurityNumberLast4", check.getSsnLast4());
                    request.remove("country");
                }

                if (payment.getPaymentMethod() instanceof IEncryptable) {
                    EncryptionData enc = ((IEncryptable)payment.getPaymentMethod()).getEncryptionData();
                    if (enc != null && paymentInfo != null) {
                            paymentInfo.set("trackNumber", enc.getTrackNumber());
                            paymentInfo.set("key", enc.getKtb());
                            paymentInfo.set("encryptionType", "E3");
                    }
                }
            }
            else { // EDIT FIELDS
                request.remove("customerKey");
                request.set("paymentStatus", payment.getStatus());
                request.set("cpcTaxType", payment.getTaxType());
                request.set("expirationDate", payment.getExpirationDate());
            }
        }
        return request;
    }

    private JsonDoc buildSchedule(JsonDoc request, Schedule schedule, TransactionType type) {
        if (schedule != null) {
            request.set("scheduleIdentifier", schedule.getId());
            request.set("scheduleName", schedule.getName());
            request.set("scheduleStatus", schedule.getStatus());
            request.set("paymentMethodKey", schedule.getPaymentKey());

            buildAmount(request, "subtotalAmount", schedule.getAmount(), schedule.getCurrency(), type);
            buildAmount(request, "taxAmount", schedule.getTaxAmount(), schedule.getCurrency(), type);

            request.set("deviceId", schedule.getDeviceId());
            request.set("processingDateInfo", mapProcessingDate(schedule));
            buildDate(request, "endDate", schedule.getEndDate(), (type == TransactionType.Edit));
            request.set("reprocessingCount", schedule.getReprocessingCount() == null ? 3 : schedule.getReprocessingCount());
            request.set("emailReceipt", schedule.getEmailReceipt());
            request.set("emailAdvanceNotice", schedule.isEmailNotification() ? "Yes" : "No");
            // debt repay ind
            request.set("invoiceNbr", schedule.getInvoiceNumber());
            request.set("poNumber", schedule.getPoNumber());
            request.set("description", schedule.getDescription());
            request.set("numberOfPayments", schedule.getNumberOfPayments());

            if (type == TransactionType.Create) {
                request.set("customerKey", schedule.getCustomerKey());
                buildDate(request, "startDate", schedule.getStartDate());
                request.set("frequency", schedule.getFrequency());
                request.set("duration", mapDuration(schedule));
            }
            else { // Edit Fields
                if (!schedule.isHasStarted()) {
                    buildDate(request, "startDate", schedule.getStartDate());
                    request.set("frequency", schedule.getFrequency());
                    request.set("duration", mapDuration(schedule));
                }
                else {
                    buildDate(request, "cancellationDate", schedule.getCancellationDate());
                    buildDate(request, "nextProcressingDate", schedule.getNextProcessingDate());
                }
            }
        }
        return request;
    }

    private String mapDuration(Schedule schedule) {
        if (schedule.getNumberOfPayments() != null)
            return "Limited Number";
        else if (schedule.getEndDate() != null)
            return "End Date";
        else return "Ongoing";
    }
    private String mapProcessingDate(Schedule schedule) {
        List<ScheduleFrequency> frequencies = new ArrayList<ScheduleFrequency>();
        frequencies.add(ScheduleFrequency.Monthly);
        frequencies.add(ScheduleFrequency.BiMonthly);
        frequencies.add(ScheduleFrequency.Quarterly);
        frequencies.add(ScheduleFrequency.SemiAnnually);

        if (frequencies.contains(schedule.getFrequency())) {
            switch (schedule.getPaymentSchedule()) {
                case FirstOfTheMonth:
                    return "First";
                case LastOfTheMonth:
                    return "Last";
                default: {
                    int day = schedule.getStartDate().getDay();
                    if (day > 28)
                        return "Last";
                    return day + "";
                }
            }
        }
        else if (schedule.getFrequency().equals(ScheduleFrequency.SemiMonthly)) {
            if (schedule.getPaymentSchedule().equals(PaymentSchedule.LastOfTheMonth))
                return "Last";
            return "First";
        }
        return null;
    }

    private JsonDoc buildDate(JsonDoc request, String name, Date date) {
        return buildDate(request, name, date, false);
    }
    private JsonDoc buildDate(JsonDoc request, String name, Date date, boolean force) {
        if (date != null || force) {
            String value = (date != null) ? DateUtils.toString(date, "MMddyyyy") : null;
            request.set(name, value, force);
        }
        return request;
    }

    private JsonDoc buildAmount(JsonDoc request, String name, BigDecimal amount, String currency, TransactionType type) {
        if (amount != null) {
            JsonDoc node = request.subElement(name);
            node.set("value", StringUtils.toNumeric(amount));
            if (type == TransactionType.Create)
                node.set("currency", currency);
        }
        return request;
    }

    private JsonDoc buildAddress(JsonDoc request, Address address) {
        if (address != null) {
            request.set("addressLine1", address.getStreetAddress1());
            request.set("addressLine2", address.getStreetAddress2());
            request.set("city", address.getCity());
            request.set("country", address.getCountry());
            request.set("stateProvince", address.getState());
            request.set("zipPostalCode", address.getPostalCode());
        }
        return request;
    }

    private <T> T hydrateCustomer(JsonDoc response, Class<T> clazz) {
        try {
            T customer = clazz.newInstance();
            if(customer instanceof Customer) {
                Address address = new Address();
                address.setStreetAddress1(response.getString("addressLine1"));
                address.setStreetAddress2(response.getString("addressLine2"));
                address.setCity(response.getString("city"));
                address.setProvince(response.getString("stateProvince"));
                address.setPostalCode(response.getString("zipPostalCode"));
                address.setCountry(response.getString("country"));
                
                ((Customer)customer).setKey(response.getString("customerKey"));
                ((Customer)customer).setId(response.getString("customerIdentifier"));
                ((Customer)customer).setFirstName(response.getString("firstName"));
                ((Customer)customer).setLastName(response.getString("lastName"));
                ((Customer)customer).setCompany(response.getString("company"));
                ((Customer)customer).setStatus(response.getString("customerStatus"));
                ((Customer)customer).setTitle(response.getString("title"));
                ((Customer)customer).setDepartment(response.getString("department"));
                ((Customer)customer).setEmail(response.getString("primaryEmail"));
                ((Customer)customer).setHomePhone(response.getString("phoneDay"));
                ((Customer)customer).setWorkPhone(response.getString("phoneEvening"));
                ((Customer)customer).setMobilePhone(response.getString("phoneMobile"));
                ((Customer)customer).setFax(response.getString("fax"));
                ((Customer)customer).setAddress(address);

                if(response.has("paymentMethods")) {
                    List<RecurringPaymentMethod> paymentMethods = new ArrayList<RecurringPaymentMethod>();
                    for(JsonDoc paymentResponse : response.getEnumerator("paymentMethods")) {
                        RecurringPaymentMethod paymentMethod = hydratePaymentMethod(paymentResponse, RecurringPaymentMethod.class);
                        if(paymentMethod != null)
                            paymentMethods.add(paymentMethod);
                    }
                    ((Customer)customer).setPaymentMethods(paymentMethods);
                }
                return customer;
            }
            return null;
        }
        catch(Exception e) {
            return null;    
        }
    }

    private <T> T hydratePaymentMethod(JsonDoc response, Class<T> clazz){
        try{
            T payment = clazz.newInstance();
            if(payment instanceof RecurringPaymentMethod) {
                Address address = new Address();
                address.setStreetAddress1(response.getString("addressLine1"));
                address.setStreetAddress2(response.getString("addressLine2"));
                address.setCity(response.getString("city"));
                address.setProvince(response.getString("stateProvince"));
                address.setPostalCode(response.getString("zipPostalCode"));
                address.setCountry(response.getString("country"));

                ((RecurringPaymentMethod)payment).setKey(response.getString("paymentMethodKey"));
                ((RecurringPaymentMethod)payment).setPaymentType(response.getString("paymentMethodType"));
                ((RecurringPaymentMethod)payment).setPreferredPayment(response.getBool("preferredPayment"));
                ((RecurringPaymentMethod)payment).setStatus(response.getString("paymentStatus"));
                ((RecurringPaymentMethod)payment).setId(response.getString("paymentMethodIdentifier"));
                ((RecurringPaymentMethod)payment).setCustomerKey(response.getString("customerKey"));
                ((RecurringPaymentMethod)payment).setNameOnAccount(response.getString("nameOnAccount"));
                ((RecurringPaymentMethod)payment).setCommercialIndicator(response.getString("cpcInd"));
                ((RecurringPaymentMethod)payment).setTaxType(response.getString("cpcTaxType"));
                ((RecurringPaymentMethod)payment).setExpirationDate(response.getString("expirationDate"));
                ((RecurringPaymentMethod)payment).setAccountNumberLast4(response.getString("accountNumberLast4"));
                ((RecurringPaymentMethod)payment).setAddress(address);
                return payment;
            }
            return null;
        }
        catch(Exception e) {
            return null;
        }
    }

    private <T> T hydrateSchedule(JsonDoc response, Class<T> clazz) {
        try{
            T schedule = clazz.newInstance();
            if(schedule instanceof Schedule) {
                ((Schedule)schedule).setKey(response.getString("scheduleKey"));
                ((Schedule)schedule).setId(response.getString("scheduleIdentifier"));
                ((Schedule)schedule).setCustomerKey(response.getString("customerKey"));
                ((Schedule)schedule).setName(response.getString("scheduleName"));
                ((Schedule)schedule).setStatus(response.getString("scheduleStatus"));
                ((Schedule)schedule).setPaymentKey(response.getString("paymentMethodKey"));
                if (response.has("subtotalAmount")) {
                    JsonDoc subtotal = response.get("subtotalAmount");
                    ((Schedule)schedule).setAmount(subtotal.getValue("value", amountConverter));
                    ((Schedule)schedule).setCurrency(subtotal.getString("currency"));
                }
                if (response.has("taxAmount")) {
                    JsonDoc taxAmount = response.get("taxAmount");
                    ((Schedule)schedule).setTaxAmount(taxAmount.getValue("value", amountConverter));
                }
                ((Schedule)schedule).setDeviceId(response.getInt("DeviceId"));
                ((Schedule)schedule).setStartDate(response.getValue("startDate", dateConverter));
                ((Schedule)schedule).setPaymentSchedule(response.getValue("processingDateInfo", new ValueConverter<PaymentSchedule>() {
                    public PaymentSchedule call(String value) {
                        if (value == null) return PaymentSchedule.Dynamic;
                        if (value.equals("Last"))
                            return PaymentSchedule.LastOfTheMonth;
                        else if (value.equals("First"))
                            return PaymentSchedule.FirstOfTheMonth;
                        return PaymentSchedule.Dynamic;
                    }
                }));
                ((Schedule)schedule).setFrequency(response.getValue("frequency", new ValueConverter<ScheduleFrequency>() {
                    public ScheduleFrequency call(String value) {
                        if(value != null)
                            return ScheduleFrequency.valueOf(value.replace("-", ""));
                        return ScheduleFrequency.Weekly;
                    }
                }));
                ((Schedule)schedule).setEndDate(response.getValue("endDate", dateConverter));
                ((Schedule)schedule).setReprocessingCount(response.getInt("reprocessingCount"));
                ((Schedule)schedule).setEmailReceipt(response.getValue("emailReceipt", new ValueConverter<EmailReceipt>() {
                    public EmailReceipt call(String value) {
                        if(value != null)
                            return EmailReceipt.valueOf(value);
                        return EmailReceipt.Never;
                    }
                }));
                ((Schedule)schedule).setEmailNotification(response.getValue("emailAdvanceNotice", yesNoConverter));
                // dept repay indicator
                ((Schedule)schedule).setInvoiceNumber(response.getString("invoiceNbr"));
                ((Schedule)schedule).setPoNumber(response.getString("poNumber"));
                ((Schedule)schedule).setDescription(response.getString("description"));
                // statusSetDate
                ((Schedule)schedule).setNextProcessingDate(response.getValue("nextProcessingDate", dateConverter));
                // previousProcessingDate
                // approvedTransactionCount
                // failureCount
                // totalApprovedAmountToDate
                // numberOfPaymentsRemaining
                ((Schedule)schedule).setCancellationDate(response.getValue("cancellationDate", dateConverter));
                // creationDate
                // lastChangeDate
                ((Schedule)schedule).setHasStarted(response.getBool("scheduleStarted"));
                return schedule;
            }
            return null;
        }
        catch(Exception e) {
            return null;
        }
    }

    private String getToken(IPaymentMethod paymentMethod) {
        if(paymentMethod instanceof ITokenizable) {
            String tokenValue = ((ITokenizable)paymentMethod).getToken();
            if(tokenValue != null && !tokenValue.equals(""))
                return tokenValue;
            return null;
        }
        return null;
    }
}
