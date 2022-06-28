package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum GnapFIDS implements IStringConstant {
    //FIDS
    FID_A("Cardholder Billing Address","A"),
    FID_B("Transaction Amount","B"),
    FID_C("Original Transaction Amount","C"),
    FID_D("Account Type","D"),
    FID_F("Approval Code","F"),
    FID_L("Balance Info","L"),
    FID_M("PIN Encryption Key","M"),
    FID_N("Customer ID","N"),
    FID_O("Customer ID Type","O"),
    FID_P("Draft Capture Flag","P"),
    FID_Q("Echo Data","Q"),
    FID_R("Card Type","R"),
    FID_S("Invoice Number","S"),
    FID_T("Original Invoice Number","T"),
    FID_U("Language Code","U"),
    FID_X("ISO Response Code","X"),
    FID_Y("Cardholder Billing Postal/ZIP Code","Y"),
    FID_Z("Address Verification (AVS) Status Code","Z"),
    FID_a("Optional Data","a"),
    FID_b("PIN Block","b"),
    FID_d("Retailer ID","d"),
    FID_e("POS Condition Code","e"),
    FID_g("Response Message","g"),
    FID_h("Sequence Number","h"),
    FID_i("Original POS Sequence Number for Interac IN-APP Refund","i"),
    FID_j("Mastercard Banknet Reference Number, Visa Transaction Identifier or Discover Network Reference ID","j"),
    FID_m("Day Totals","m"),
    FID_p("Paypass Device Type Indicator","p"),
    FID_q("Track 2 Data","q"),
    FID_t("Global Payments Pinpad serial Number","t"),
    FTD_w("Driver Id","w"),
    FID_y("AMEX Automated Address Verification (AAV) Enhanced Format","y"),
    FID_z("Union Pay Indicator","z"),
    FID_4("Message Reason Codes for Merchant Initiated Transactions","4"),
    FID_5("Transaction Info","5"),
    FID_6("Product Sub-FIDs","6");

    String fidDesc;
    String value;
    GnapFIDS(String fidDesc,String value) {
        this.fidDesc=fidDesc;
        this.value = value;
    }

    public String getValue() {return this.value;}
    public String getFidDesc(){return this.fidDesc;}
    public byte[] getBytes() { return this.value.getBytes(); }
}


