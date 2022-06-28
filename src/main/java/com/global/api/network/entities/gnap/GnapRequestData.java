package com.global.api.network.entities.gnap;


import com.global.api.network.enums.gnap.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import  com.global.api.entities.enums.AccountType;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class GnapRequestData extends GnapData {

    /** FID D - Account Type */
    private AccountType accountType;
    /** FID R - Card Type */
    private CardType cardType;
    /** FID S - Invoice Number */
    private String  invoiceNumber;
    /** FID T - Original Invoice Number */
    private String originalInvoiceNumber;
    /** FID U - Language Code */
    private LanguageCode languageCode;
    /** FID a - Optional Data */
    private OptionalData optionalData;
    /** FID d - Retailer ID */
    private String retailerID;
    /** FID e - POS Condition Code */
    private POSConditionCode posConditionCode;
    /** FID 4 - Message Reason Codes for Merchant Initiated Transactions */
    private MerchantReasonCode merchantReasonCodes;
}
