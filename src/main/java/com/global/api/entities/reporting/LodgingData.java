package com.global.api.entities.reporting;

public class LodgingData {
	private String advancedDepositType;
	private String lodgingDataEdit;
	private boolean noShow;
	private boolean preferredCustomer;
	private String prestigiousPropertyLimit;
	
	public String getAdvancedDepositType() {
		return advancedDepositType;
	}
	public void setAdvancedDepositType(String advancedDepositType) {
		this.advancedDepositType = advancedDepositType;
	}
	public String getLodgingDataEdit() {
		return lodgingDataEdit;
	}
	public void setLodgingDataEdit(String lodgingDataEdit) {
		this.lodgingDataEdit = lodgingDataEdit;
	}
	public boolean isNoShow() {
		return noShow;
	}
	public void setNoShow(boolean noShow) {
		this.noShow = noShow;
	}
	public boolean isPreferredCustomer() {
		return preferredCustomer;
	}
	public void setPreferredCustomer(boolean preferredCustomer) {
		this.preferredCustomer = preferredCustomer;
	}
	public String getPrestigiousPropertyLimit() {
		return prestigiousPropertyLimit;
	}
	public void setPrestigiousPropertyLimit(String prestigiousPropertyLimit) {
		this.prestigiousPropertyLimit = prestigiousPropertyLimit;
	}
}