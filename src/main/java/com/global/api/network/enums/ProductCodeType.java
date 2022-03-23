package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ProductCodeType implements IStringConstant{
	
	NoPrompt("0"),
	IdnumberAndOdometerOrVehicleId("1"),
	VehicleNumberAndOdometerOrUserId("2"),
	DriverNumberAndOdometerOrDriverId("3"),
	Odometer("4"),
	DriverNumber("5"),
	Data("6"),
	JobNumber("7"),
	Department("8"),
	Other("9"),
	MaintenanceID("A"),
	HubometerOrHubReading("B"),
	TrailerHoursOrReferHours("C"),
	TrailerNumber("D"),
	TripNumber("E"),
	UnitNumber("F"),
	WorkOrderOrPoNumber("G");

	String value;
	ProductCodeType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
