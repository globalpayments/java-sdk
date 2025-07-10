package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum CardIssuerEntryTag implements IStringConstant {
    StoredValueCards("1xx"),
    LoyaltyCards("2xx"),
    PrivateLabelCards("3xx"),
    SearsProprietaryDeferDate("3SF"),
    SearsProprietaryDelayDate("3SL"),
    Bank_CreditCards("Bxx"),
    Checks("Cxx"),
    PIN_DebitCards("Dxx"),
    PIN_DebitAuthorizer("D00"),
    ElectronicBenefitsTransfer("Exx"),
    FleetCards("Fxx"),
    Wex_SpecVersionSupport("F00"),
    Wex_PurchaseDeviceSequenceNumber("F01"),
    VISAFLEET2DOT0CARDPRESENTBYCARDHOLDER("F03"),
    PrepaidServiceSystem("Gxx"),
    AmountSentToIssuerOnBehalfOfPos("IAM"),
    AccountFromCardIssuer("IAN"),
    CardIssuerAuthenticationResponseCode("IAR"),
    AccountTypeFromCardIssuer("IAT"),
    MastercardUCAFData("IAU"),
    Mastercard3DSCryptogram("IAU"),
    AvsResponseCode("IAV"),
    ChipConditionCode("ICC"),
    CreditPlan("ICP"),
    CvnResponseCode("ICV"),
    DiagnosticMessage("IDG"),
    ExtendedExpirationDate("IED"),
    GiftCardPurchase("IGS"),
    UniqueDeviceId("IID"),
    MastercardDSRPCryptogram("IMD"),
    MastercardECommerceIndicators("IME"),
    MastercardRemoteCommerceAcceptorIdentifier("IMU"),
    MASTERCARD_CIT_MIT_INDICATOR("IMC"),
    MastercardWalletID("IMW"),
    PiggyBackActionCode("IPA"),
    ReceiptText("IPR"),
    OriginalResponse_ActionCode("IRA"),
    CenterCallNumber("IRC"),
    IssuerReferenceNumber("IRN"),
    RetrievalReferenceNumber("IRR"),
    IssuerSpecificTransactionMatchData("ITM"),
    DisplayText("ITX"),
    HOSTBASEDPURCHASERESTRICTION("IVR"),
    Alternate_DE41("I41"),
    Alternate_DE42("I42"),
    DialError("NDE"),
    DiscoverNetworkReferenceId("ND2"),
    NTS_MastercardBankNet_ReferenceNumber("NM1"),
    NTS_MastercardBankNet_SettlementDate("NM2"),
    NTS_POS_Capability("NPC"),
    PetroleumSwitch("NPS"),
    SwipeIndicator("NSI"),
    TerminalError("NTE"),
    NTS_System("NTS"),
    VisaTransactionId("NV1"),
    HeartlandTimeRequest("HTR"),
    MastercardWalletId("IMW"),
    CheckInformation("C00"),
    CheckExpandedOrRawMICRData("C02"),
    EWICMerchantId("EIS"),
    EwicIssuingEntity("EWS"),
    MerchantActionCode("IAC"),
    ActualStan("IST"),
    DeclineCategoryCode("ICA"),
    WexAvailableProductRestrictions("WAP"),
    CardIssuerAuthenticationData("IAD"),
    StoreAndForwardFlag("GSF"),
    SVSVersion("G00");

    private final String value;
    CardIssuerEntryTag(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }

    public static CardIssuerEntryTag findPartial(String value) {
        switch(value.toCharArray()[0]) {
            case '1':
                return StoredValueCards;
            case '2':
                return LoyaltyCards;
            case '3':
                return PrivateLabelCards;
            case 'B':
                return Bank_CreditCards;
            case 'C':
                return Checks;
            case 'D':
                return PIN_DebitCards;
            case 'E':
                return ElectronicBenefitsTransfer;
            case 'F':
                return FleetCards;
            case 'G':
                return PrepaidServiceSystem;
            case 'N':
                return NTS_System;
            default:
                return null;
        }
    }
}
