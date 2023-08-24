package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum VisaFleetProductCode implements IStringConstant {
	
	Other("00"),
	Unleaded_Regular_86("01"),
	Unleaded_Regular_87("02"),
	Unleaded_Mid_Grade_88("03"),
	Unleaded_Mid_Grade_89("04"),
	Unleaded_Premium_90("05"),
	Unleaded_Premium_91("06"),
	Unleaded_Super_92("07"),
	Unleaded_Super_93("08"),
	Unleaded_Super_94("09"),
	Regular_Leaded("11"),
	Diesel("12"),
	Diesel_Premium("13"),
	Kerosene("14"),
	LPG("15"),
	Gasohol("16"),
	CNG("17"),
	Methanol_85("18"),
	Methanol_10("19"),
	Methanol_7("20"),
	Methanol_5("21"),
	Ethanol_85("22"),
	Ethanol_10("23"),
	Ethanol_7("24"),
	Ethanol_5("25"),
	Jet_Fuel("26"),
	Aviation_Fuel("27"),
	OffRoad_diesel("28"),
	Marine("29"),
	Motor_Oil("30"),
	Oil_Change("31"),
	Engine_Service("32"),
	Transmission_Service("33"),
	Brake_Service("34"),
	Unassigned_Repair_Values("35"),
	Miscellaneous_Repairs("39"),
	Tires_Batteries_and_Accessories("40"),
	Tires("41"),
	Batteries("42"),
	Automotive_Accessories("43"),
	Automotive_Glass("44"),
	Car_Wash("45"),
	Unassigned_Automotive_Products_and_Services("46"),
	Cigarettes_and_Tobacco("70"),
	Unassigned_Food_and_Grocery_Items("71"),
	Health_and_Beauty_Aids("78"),
	Miscellaneous_Grocery("79"),
	Soda("80"),
	Beer_and_Wine("81"),
	Milk_and_Juice("82"),
	Unassigned_Beverage_Items("83"),
	Miscellaneous("90"),
	Unassigned("91");

	private final String value;
	VisaFleetProductCode(String value) {
        this.value = value;
    }

    public String getValue() {
    	return this.value;
    }
    public byte[] getBytes() {
    	return this.value.getBytes();
    }
}
