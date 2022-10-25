package com.example.restservice.entities;

public class MobileData {

	private String encodedData;
    private String applicationReference;
    private String sdkInterface;
    private String[] sdkUiTypes;
    private String ephemeralPublicKeyX;
    private String ephemeralPublicKeyY;
    private Integer maximumTimeout;
    private String referenceNumber;
    private String sdkTransReference;

//    public MobileData setSdkUiTypes(SdkUiType... sdkUiTypes) {
//        this.sdkUiTypes = sdkUiTypes;
//        return this;
//    }

//    public enum SdkUiType {
//    	TEXT,
//    	SINGLE_SELECT,
//    	MULTI_SELECT,
//    	OUT_OF_BAND,
//    	HTML_OTHER;
//    }    
//    
//    public enum SdkInterface {
//        NATIVE,
//        BROWSER,
//        BOTH;
//    }

	public String getEncodedData() {
		return encodedData;
	}

	public void setEncodedData(String encodedData) {
		this.encodedData = encodedData;
	}

	public String getApplicationReference() {
		return applicationReference;
	}

	public void setApplicationReference(String applicationReference) {
		this.applicationReference = applicationReference;
	}

	public String getSdkInterface() {
		return sdkInterface;
	}

	public void setSdkInterface(String sdkInterface) {
		this.sdkInterface = sdkInterface;
	}

	public String getEphemeralPublicKeyX() {
		return ephemeralPublicKeyX;
	}

	public String getEphemeralPublicKeyY() {
		return ephemeralPublicKeyY;
	}

	public Integer getMaximumTimeout() {
		return maximumTimeout;
	}

	public void setMaximumTimeout(Integer maximumTimeout) {
		this.maximumTimeout = maximumTimeout;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getSdkTransReference() {
		return sdkTransReference;
	}

	public void setSdkTransReference(String sdkTransReference) {
		this.sdkTransReference = sdkTransReference;
	}

	public String[] getSdkUiTypes() {
		return sdkUiTypes;
	}

}
