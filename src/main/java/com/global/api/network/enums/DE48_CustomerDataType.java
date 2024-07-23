package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_CustomerDataType implements IStringConstant {
    UnencryptedIdNumber("0"),
    Vehicle_Number("1"),
    Vehicle_Number_Code3("3"),
    Id_Number_Code3("3"),
    VehicleTag("2"),
    DriverId_EmployeeNumber("3"),
    Odometer_Reading("4"),
    DriverLicense_Number("5"),
    DriverLicense_State_Province("6"),
    DriverLicense_Name("7"),
    WORKORDER_PONUMBER("8"),
    InvoiceNumber("9"),
    TripNumber("A"),
    UnitNumber("B"),
    TrailerHours_ReferHours("C"),
    DateofBirth("D"),
    PostalCode("E"),
    EnteredData_Numeric("F"),
    EnteredData_AlphaNumeric("G"),
    SocialSecurityNumber("Q"),
    CardPresentSecurityCode("R"),
    ServicePrompt("S"),
    PassportNumber("T"),
    JobNumber("U"),
    Department("V"),
    LoyaltyInformation("W"),
    MerchantOrderNumber("Z"),
    MaintenanceNumber("a"),
    TrailerNumber("b"),
    HubometerNumber("c"),
    ADDITIONALPROMPTDATA1("d"),
    ADDITIONALPROMPTDATA2("e"),
    EMPLOYEENUMBER("f");

    private final String value;
    DE48_CustomerDataType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
