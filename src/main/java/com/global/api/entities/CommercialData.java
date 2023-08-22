package com.global.api.entities;

import com.global.api.entities.enums.TaxType;
import com.global.api.entities.enums.TransactionModifier;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommercialData {
    private AdditionalTaxDetails additionalTaxDetails ;
    private TransactionModifier commercialIndicator ;
    private String customerVAT_Number ;
    private String CustomerReferenceId ;
    private String description ;
    private BigDecimal discountAmount ;
    private BigDecimal dutyAmount ;
    private String destinationPostalCode ;
    private String destinationCountryCode ;
    private BigDecimal freightAmount ;
    private List<CommercialLineItem> lineItems  ;
    private DateTime orderDate ;
    private String originPostalCode ;
    private String poNumber ;
    private String supplierReferenceNumber ;
    private BigDecimal taxAmount;
    private TaxType taxType ;
    private String summaryCommodityCode ;
    private String vat_InvoiceNumber ;

    public CommercialData(TaxType taxType) {
        new CommercialData(taxType, TransactionModifier.LevelII);
    }

    public CommercialData(TaxType taxType, TransactionModifier level) {
        this.taxType = taxType;
        this.commercialIndicator = level;
        lineItems = new ArrayList<CommercialLineItem>();
    }

    public CommercialData AddLineItems(CommercialLineItem items) {
        lineItems.add(items);
        return this;
    }
}
