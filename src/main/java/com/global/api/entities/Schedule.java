package com.global.api.entities;

import com.global.api.entities.enums.EmailReceipt;
import com.global.api.entities.enums.PaymentSchedule;
import com.global.api.entities.enums.ScheduleFrequency;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.RecurringService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Schedule extends RecurringEntity<Schedule> {
    private BigDecimal amount;
    private Date cancellationDate;
    private String currency;
    private String customerKey;
    private String description;
    private Integer deviceId;
    private boolean emailNotification;
    private EmailReceipt emailReceipt = EmailReceipt.Never;
    private Date endDate;
    private ScheduleFrequency frequency;
    private boolean hasStarted;
    private String invoiceNumber;
    private String name;
    private Date nextProcessingDate;
    private Integer numberOfPayments;
    private String poNumber;
    private String paymentKey;
    private PaymentSchedule paymentSchedule = PaymentSchedule.Dynamic;
    private Integer reprocessingCount;
    private Date startDate;
    private String status;
    private BigDecimal taxAmount;

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public Date getCancellationDate() {
        return cancellationDate;
    }
    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getCustomerKey() {
        return customerKey;
    }
    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    public boolean isEmailNotification() {
        return emailNotification;
    }
    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }
    public EmailReceipt getEmailReceipt() {
        return emailReceipt;
    }
    public void setEmailReceipt(EmailReceipt emailReceipt) {
        this.emailReceipt = emailReceipt;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public ScheduleFrequency getFrequency() {
        return frequency;
    }
    public void setFrequency(ScheduleFrequency frequency) {
        this.frequency = frequency;
    }
    public boolean isHasStarted() {
        return hasStarted;
    }
    public void setHasStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getNextProcessingDate() {
        return nextProcessingDate;
    }
    public void setNextProcessingDate(Date nextProcessingDate) {
        this.nextProcessingDate = nextProcessingDate;
    }
    public Integer getNumberOfPayments() {
        return numberOfPayments;
    }
    public void setNumberOfPayments(Integer numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }
    public String getPoNumber() {
        return poNumber;
    }
    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }
    public String getPaymentKey() {
        return paymentKey;
    }
    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
    public PaymentSchedule getPaymentSchedule() {
        return paymentSchedule;
    }
    public void setPaymentSchedule(PaymentSchedule paymentSchedule) {
        this.paymentSchedule = paymentSchedule;
    }
    public Integer getReprocessingCount() {
        return reprocessingCount;
    }
    public void setReprocessingCount(Integer reprocessingCount) {
        this.reprocessingCount = reprocessingCount;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    public BigDecimal getTotalAmount() {
        if(amount != null) {
            if(taxAmount != null) {
                return amount.add(taxAmount);
            }
            return amount;
        }
        return BigDecimal.ZERO;
    }

    public Schedule withAmount(BigDecimal value) {
        amount = value;
        return this;
    }
    public Schedule withCurrency(String value) {
        currency = value;
        return this;
    }
    public Schedule withCustomerKey(String value) {
        customerKey = value;
        return this;
    }
    public Schedule withDescription(String value) {
        description = value;
        return this;
    }
    public Schedule withDeviceId(int value) {
        deviceId = value;
        return this;
    }
    public Schedule withEmailNotification(boolean value) {
        emailNotification = value;
        return this;
    }
    public Schedule withEmailReceipt(EmailReceipt value) {
        emailReceipt = value;
        return this;
    }
    public Schedule withEndDate(Date value) {
        endDate = value;
        return this;
    }
    public Schedule withFrequency(ScheduleFrequency value) {
        frequency = value;
        return this;
    }
    public Schedule withInvoiceNumber(String value) {
        invoiceNumber = value;
        return this;
    }
    public Schedule withName(String value) {
        name = value;
        return this;
    }
    public Schedule withNumberOfPayments(Integer value) {
        numberOfPayments = value;
        return this;
    }
    public Schedule withPoNumber(String value) {
        poNumber = value;
        return this;
    }
    public Schedule withPaymentKey(String value) {
        paymentKey = value;
        return this;
    }
    public Schedule withPaymentSchedule(PaymentSchedule value) {
        paymentSchedule = value;
        return this;
    }
    public Schedule withReprocessingCount(Integer value) {
        reprocessingCount = value;
        return this;
    }
    public Schedule withStartDate(Date value) {
        startDate = value;
        return this;
    }
    public Schedule withStatus(String value) {
        status = value;
        return this;
    }
    public Schedule withTaxAmount(BigDecimal value) {
        taxAmount = value;
        return this;
    }

    public Schedule() {}
    public Schedule(String customerKey, String paymentKey) {
        this.customerKey = customerKey;
        this.paymentKey = paymentKey;
    }

    public Schedule create(String configName) throws ApiException {
        return RecurringService.create(this, Schedule.class);
    }

    public void delete() throws ApiException {
        delete(false);
    }
    public void delete(boolean force) throws ApiException {
        try{
            RecurringService.delete(this, Schedule.class, force);
        }
        catch(ApiException e) {
            throw new ApiException("Failed to delete payment method, see inner exception for more details.", e);
        }
    }

    public static Schedule find(String id) throws ApiException {
        return find(id, "default");
    }
    public static Schedule find(String id, String configName) throws ApiException {
        checkSupportsRetrieval(configName);

        List<Schedule> response = RecurringService.search(ScheduleCollection.class)
                .addSearchCriteria("scheduleIdentifier", id)
                .execute();
        if(response.size() > 0) {
            Schedule entity = response.get(0);
            if (entity != null)
                return RecurringService.get(entity.getKey(), Schedule.class);
        }
        return null;
    }

    public static List<Schedule> findAll() throws ApiException {
        return findAll("default");
    }
    public static List<Schedule> findAll(String configName) throws ApiException {
        checkSupportsRetrieval(configName);
        return RecurringService.search(ScheduleCollection.class).execute();
    }

    public static Schedule get(String key) throws ApiException {
        return get(key, "default");
    }
    public static Schedule get(String key, String configName) throws ApiException {
        checkSupportsRetrieval(configName);
        return RecurringService.get(key, Schedule.class);
    }

    public void saveChanges() throws ApiException {
        try{
            RecurringService.edit(this, Schedule.class);
        }
        catch (ApiException e) {
            throw new ApiException("Update failed, see inner exception for more details", e);
        }
    }
}

