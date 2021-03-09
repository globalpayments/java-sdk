package com.global.api.entities.billing;

import java.math.BigDecimal;
import java.util.Date;

import com.global.api.entities.Customer;
import com.global.api.entities.enums.BillPresentment;

/**
 * Represents a bill to be paid in a transaction. Consists of a type and one to
 * four identifiers.
 */
public class Bill {
    /**
     * The name of the bill type
     */
    protected String billType;

    /**
     * The first bill identifier
     */
    protected String identifier1;

    /**
     * The second identifier
     */
    protected String identifier2;
    
    /**
     * The third identifier
     */
    protected String identifier3;
    
    /**
     * The fourth identifier
     */
    protected String identifier4;
    
    /**
     * The amount to apply to the bill
     */
    protected BigDecimal amount;
    
    /**
     * The Customer information for the bill
     */
    protected Customer customer;
    
    /**
     * The Presentment Status of the bill
     */
    protected BillPresentment billPresentment;
    
    /**
     * The date the bill is due
     */
    protected Date dueDate;

    public String getBillType() {
        return billType;
    }

    public String getIdentifier1() {
        return identifier1;
    }
    
    public String getIdentifier2() {
        return identifier2;
    }
    
    public String getIdentifier3() {
        return identifier3;
    }
    
    public String getIdentifier4() {
        return identifier4;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public BillPresentment getBillPresentment() {
        return billPresentment;
    }
    
    public Date getDueDate() {
        return dueDate;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public void setIdentifier1(String identifier1) {
        this.identifier1 = identifier1;
    }

    public void setIdentifier2(String identifier2) {
        this.identifier2 = identifier2;
    }

    public void setIdentifier3(String identifier3) {
        this.identifier3 = identifier3;
    }

    public void setIdentifier4(String identifier4) {
        this.identifier4 = identifier4;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setBillPresentment(BillPresentment billPresentment) {
        this.billPresentment = billPresentment;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
