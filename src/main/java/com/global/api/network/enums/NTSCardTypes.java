package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.utils.StringUtils;

public enum NTSCardTypes implements IStringConstant {

    Proprietary("01", 15),
    Mastercard("02", 40),
    Visa("03",31),
    PinDebit("04",45),
    AmericanExpress("05", 40),
    Discover("07",30),
    PayPal("09", 30),
    WexFleet("11", 20),
    WexProprietaryFleet("12",20),
    SuperAmericaProprietaryFleet("17",30),
    CITGOFleet("18",37),
    LoyaltyCard("21",30),
    VisaFleet("24",31),
    VoyagerFleet("28",31),
    SuperAmericaCoBranded("29",40),
    StoredValueOrGlobalPaymentsGiftCard("31",30),
    ValueLink("32",30),
    FuelmanFleet("33",20),
    FleetWide("34",20),
    MastercardFleet("35",40),
    MastercardPurchasing("36",40),
    EBTFoodStamps("39",31),
    EBTCashBenefits("40",31),
    Seven11InternetPrepaid("42",40),
    InToPlane("50",30),
    AVCard("51",30),
    FleetOne("54",20),
    Centego("55",40),
    DropTank("56",30);

    private final String value;
    private final Integer timeOut;
    NTSCardTypes(String value,Integer timeOut ) {
        this.value = value;
        this.timeOut = timeOut;
    }
    public String getValue() {
        return value;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public byte[] getBytes() {
        return getValue().getBytes();
    }
}
