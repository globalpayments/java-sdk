package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum MasterPurchaseProductCode implements IStringConstant {
	
	Not_Used("000"),
	Unleaded_Regular("001"),
	Unleaded_Mid_Grade("002"),
	Unleaded_Premium("003"),
	Unleaded_Super("004"),
	Methanol_Unleaded_Regular("005"),
	Methanol_Unleaded_Mid_Grade("006"),
	Methanol_Unleaded_Premium("007"),
	Methanol_Unleaded_Super("008"),
	Methanol_Regular_Leaded("009"),
	Regular_Leaded_Gasoline("011"),
	Diesel("012"),
	Diesel_Premium("013"),
	Kerosene("014"),
	LPG("015"),
	Compressed_Natural_Gas("016"),
	M85("017"),
	E85("018"),
	Ethanol_Unleaded_Regular("019"),
	Ethanol_Unleaded_Mid_Grade("020"),
	Ethanol_Unleaded_Premium("021"),
	Ethanol_Unleaded_Super("022"),
	Ethanol_Regular_Leaded("023"),
	Unleaded_Reformulated("024"),
	Unleaded_Mid_Grade_Reformulated("025"),
	Dyed_Diesel("026"),
	Gasohol("027"),
	Biodiesel("028"),
	Ultralow_Sulfer_Diesel("029"),
	Aviation_100_Octane("100"),
	Jet_Fuel("101"),
	Aviation_Fuel("102"),
	Marine_Fuel("150"),
	Miscellaneous_Fuel("200"),
	Liquid_Natural_Gas("201"),
	White_Gas("202"),
	Racing_Fuel("203"),
	Motor_Oil("030"),
	Oil_Change("031"),
	Engine_Service("032"),
	Transmission_Service("033"),
	Brake_Service("034"),
	Solvent("035"),
	Brake_Fluid("036"),
	Miscellaneous_Parts("037"),
	Miscellaneous_Labor("038"),
	Miscellaneous_Repairs("039"),
	TBA("040"),
	Tires("041"),
	Batteries("042"),
	Automotive_Accessories("043"),
	Automotive_Glass("044"),
	Car_Wash("045"),
	Towing("046"),
	Cigarettes_Tobacco("070"),
	Health_Beauty_Aid("078"),
	Miscellaneous_Food_Grocery("079"),
	Soda("080"),
	Beer_Wine("081"),
	Milk_Juice("082"),
	Restaurant("083"),
	Miscellaneous_Beverage("089"),
	Miscellaneous_Other("099"),
	Aviation_Maintenance("300"),
	De_icing("301"),
	APU_or_Aircraft_Jump_Start("302"),
	Aviation_Catering("303"),
	Tie_down_or_Hangar("304"),
	Landing_Fee("305"),
	Ramp_Fee("306"),
	Call_Out_Fee("307"),
	Plane_Rental("308"),
	Instruction_Fee("309"),
	Miscellaneous_Aviation("310"),
	Flight_Planning_Weather_Fees("311"),
	Charter_Fees("312"),
	Ground_Handling("313"),
	Communications_Fees("314"),
	Aircraft_Cleaning("315"),
	Cargo_Handling("316"),
	Aviation_Accessories("317"),
	Boat_Service("350");
	
	private final String value;
		
	MasterPurchaseProductCode(String value) {
	        this.value = value;
	    }
	    public String getValue() {
	    	return this.value;
	    }
	    public byte[] getBytes() {
	    	return this.value.getBytes();
	    }

}
