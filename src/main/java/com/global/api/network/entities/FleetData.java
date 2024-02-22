package com.global.api.network.entities;

import lombok.Getter;
import lombok.Setter;

public class FleetData {
    private String department;
    private String driverId;
    private String driversLicenseNumber;
    private String enteredData;
    private String jobNumber;
    private String odometerReading;
    private String purchaseDeviceSequenceNumber;
    private String servicePrompt;
    private String userId;
    private String vehicleNumber;
    private String vehicleTag;
    private String unitNumber;
    private String tripNumber;
    private String trailerReferHours;
    private String referenceNumber;
    @Setter @Getter
    private String genericIdentificationNo;
    @Setter @Getter
    private String maintenanceNumber;
    @Setter @Getter
    private String trailerNumber;
    @Setter @Getter
    private String hubometerNumber;
    @Setter @Getter
    private String otherPromptCode;
    @Setter @Getter
    private String workOrderPoNumber;
    @Setter @Getter
    private String additionalPromptData1;
    @Setter @Getter
    private String additionalPromptData2;
    @Setter @Getter
    private String employeeNumber;
    public String getTrailerReferHours() {
        return trailerReferHours;
    }
    public void setTrailerReferHours(String trailerReferHours) {
        this.trailerReferHours = trailerReferHours;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public String getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(String tripNumber) {
        this.tripNumber = tripNumber;
    }



    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getDriverId() {
        return driverId;
    }
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }
    public void setDriversLicenseNumber(String driversLicenseNumber) {
        this.driversLicenseNumber = driversLicenseNumber;
    }
    public String getEnteredData() {
        return enteredData;
    }
    public void setEnteredData(String enteredData) {
        this.enteredData = enteredData;
    }
    public String getJobNumber() {
        return jobNumber;
    }
    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }
    public String getOdometerReading() {
        return odometerReading;
    }
    public void setOdometerReading(String odometerReading) {
        this.odometerReading = odometerReading;
    }
    public String getPurchaseDeviceSequenceNumber() {
        return purchaseDeviceSequenceNumber;
    }
    public void setPurchaseDeviceSequenceNumber(String purchaseDeviceSequenceNumber) {
        this.purchaseDeviceSequenceNumber = purchaseDeviceSequenceNumber;
    }
    public String getServicePrompt() {
        return servicePrompt;
    }
    public void setServicePrompt(String servicePrompt) {
        this.servicePrompt = servicePrompt;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    public String getVehicleTag() {
        return vehicleTag;
    }
    public void setVehicleTag(String vehicleTag) {
        this.vehicleTag = vehicleTag;
	}
	public String getReferenceNumber() {
		return referenceNumber;
	}
	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

}
