package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum MasterFleetProductCode implements IStringConstant {
	Not_Used("00"),
	Unleaded_Regular("01"),
	Unleaded_Mid_Grade("02"),
	Unleaded_Premium("03"),
	Unleaded_Super("04"),
	Methanol_Unleaded_Regular("05"),
	Methanol_Unleaded_Mid_Grade("06"),
	Methanol_Unleaded_Premium("07"),
	Methanol_Unleaded_Super("08"),
	Methanol_Regular_Leaded("09"),
	Regular_Leaded_Gasoline("11"),
	Diesel("12"),
	Diesel_Premium("13"),
	Kerosene("14"),
	LPG("15"),
	Compressed_Natural_Gas("16"),
	M85("17"),
	E85("18"),
	Ethanol_Unleaded_Regular("19"),
	Ethanol_Unleaded_Mid_Grade("20"),
	Ethanol_Unleaded_Premium("21"),
	Ethanol_Unleaded_Super("22"),
	Ethanol_Regular_Leaded("23"),
	Unleaded_Reformulated("24"),
	Unleaded_Mid_Grade_Reformulated("25"),
	Dyed_Diesel("26"),
	Gasohol("27"),
	Biodiesel("28"),
	Ultralow_Sulfer_Diesel("29"),
	Motor_Oil("30"),
	Oil_Change("31"),
	Engine_Service("32"),
	Transmission_Service("33"),
	Brake_Service("34"),
	Solvent("35"),
	Brake_Fluid("36"),
	Miscellaneous_Parts("37"),
	Miscellaneous_Labor("38"),
	Miscellaneous_Repairs("39"),
	TBA("40"),
	Tires("41"),
	Batteries("42"),
	Automotive_Accessories("43"),
	Automotive_Glass("44"),
	Car_Wash("45"),
	Towing("46"),
	Cigarettes_Tobacco("70"),
	Health_Beauty_Aid("78"),
	Miscellaneous_Food_Grocery("79"),
	Soda("80"),
	Beer_Wine("81"),
	Milk_Juice("82"),
	Restaurant("83"),
	Miscellaneous_Beverage("89"),
	Miscellaneous_Other("99");

	private final String value;
	
	MasterFleetProductCode(String value) {
        this.value = value;
    }
    public String getValue() {
    	return this.value;
    }
    public byte[] getBytes() {
    	return this.value.getBytes();
    }

}
