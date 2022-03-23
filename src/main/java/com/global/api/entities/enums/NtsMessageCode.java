package com.global.api.entities.enums;

public enum NtsMessageCode implements IStringConstant{
	AuthorizationOrBalanceInquiry("01"),
	DataCollectOrSale("02"),
	CreditAdjustment("03"),
	CombinedAuthorizationOrDataCollect("04"),
	RequestToBalacnce("06"),
	Mail("07"),
	PinDebit("08"),
	ReversalOrVoid("11"),
	RetransmitDataCollect("12"),
	RetransmitCreditAdjustment("13"),
	RetransmitRequestToBalance("16"),
	ParameterDataLoad("20"),
	EmvParameterDataLoad("21"),
	Ebt("27"),
	DoubleDataCollect("39"),
	PendingMessage("45"),
	ForceReversalOrForceVoid("C1"),
	ForceCollectOrForceSale("C2"),
	ForceCreditAdjustment("C3"),
	ForceRequestToBalance("C6"),
	RetransmitForceCollect("D2"),
	RetransmitForceCreditAdjustment("D3"),
	RetransmitForceRequestToBalance("D6"),
	PosSiteConfiguration("SC"),
	UtilityMessage("UM");

	String value;
	NtsMessageCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
