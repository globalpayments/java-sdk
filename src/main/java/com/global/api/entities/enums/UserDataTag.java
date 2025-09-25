package com.global.api.entities.enums;

public enum UserDataTag implements IStringConstant{
	FunctionCode("01"),
	TerminalCapability("02"),
	Stan("03"),
	PartiallyApproved("04"),
	ApprovedAmount("05"),
	RemainingBalance("06"),
	ZipCode("07"),
	FleetAuthData("08"),
	ProductDataTag("09"),
	//Reserved("10"),
	MasterCardBanknetRefId("11"),
	MasterCardSettlementDate("12"),
	Cvn("13"),
	DiscoverNetworkRefId("14"),
	//Reserved("15"),
	Tag16("16"),
	CardSequenceNumber("17"),
	VisaTransactionId("18"),
	VoidTag("19"),
	CashOverAmount("20"),
	UniqueDeviceId("21"),
	EmvPinBlock("22"),
	EmvKsn("23"),
	EmvMaxPinEntry("24"),
	EmvChipAuthCode("25"),
	GoodsSold("26"),
	//Reserved("27"),
	EcommerceData1("28"),
	EcommerceData2("29"),
	MCUCAF("30"),
	MCWalletId("31"),
	MCSLI("32"),
	EcommerceAuthIndicator("33"),
	EcommerceMerchantOrderNumber("34"),
	MITCITFlag("40"),
	IntegratedCircuitCard("99"),
	AvailableProducts("AvailableProducts"),
	EmvDataLength("EmvDataLength"),
	EvmData("EvmData"),
	ReceiptText("ReceiptText"),
	PurchaseRestrictionFlag("41"),
	HostBasedRestrictionResponse("42"),
	ExtendedFleetPromptData("43");
	
	String value;
	UserDataTag(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
