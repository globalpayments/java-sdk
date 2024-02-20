package com.global.api.entities.enums;

public enum NtsHostResponseCode implements IStringConstant{
	Space(" "),
	Success("00"),
	DenialRequestToBalance("01"),
	InvalidPin("02"),
	ReservedForProprietaryUseDenial("03"),
	ReservedForProprietaryUseApproved01("04"),
	ReservedForProprietaryUseApproved02("05"),
	Denial("06"),
	PinTriesExceeded("07"),
	CreditAdjustment("08"),
	DenialMasterCard("10"),
	InsufficientFunds("11"),
	OfflineDeclineAdvice("12"),
	InvalidFuel("13"),
	DenialHeartlandGiftCard("14"),
	PartiallyApproved("15"),
	VelocityReferral("16"),
	TerminalDisabled("17"),
	AccountBalanceExceededOrMaximumNumberOfRechargesExceeded("18"),
	HeartlandGiftCardAlreadyActivated("19"),
	PickupCard("20"),
	HeartlandGiftCardNotAvailableForActivation("21"),
	InvalidPinCentegoOrInvalidDriverFleetCor("28"),
	AvsReferralForFullyOrPartially("30"),
	HeartlandGiftCardDenial("35"),
	AsynchronousTerminalsOnlyTimeout("37"),
	TerminalSuccessfullySentMessageToNode("38"),
	ZipCodeNotMatch("39"),
	HostSystemFailure("40"),
	PrimaryHostIsNotAvailable("41"),
	AllowStandInProcessing("48"),
	InvalidCard("50"),
	EncryptionError("52"),
	TooManyQueuedOrNoConnection("53"),
	EmvPdlError("54"),
	InvalidPinEncryption("55"),
	NoTelephoneLinePresent("57"),
	TerminalReceivedNeitherPollsNorSelects("57"),
	ExpiredCard("60"),
	DeclinedEntry("65"),
	NeverReceivedAnEnq("67"),
	TerminalCouldNotCommunicateWithNode("68"),
	FormatError("70"),
	ReceivedDisconnect("77"),
	ReceivedPollsNotReceiveAcknowledge("78"),
	Code70TwiceInRow("79"),
	TerminalTimeout("80"),
	PinDebitNetworkError("81"),
	InquiryBalanceNotAvailable("83"),
	NetworkUnavailable("84"),
	DuplicateAuthorization("85"),
	UdpTerminalsTimeout("87"),
	TerminalHasApprovedTheTransactionInStandIn("88"),
	TerminalTimeoutLostConnection("90"),
	StoreAndForwardModeAndSaleWithoutAuthorized("92"),
	TerminalHasApprovedTheTransactionInStandInForCode90("98");

	final String value;
	NtsHostResponseCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

	public static String getValueByString(String value){
		for(NtsHostResponseCode hrc: NtsHostResponseCode.values()){
			if(hrc.value.equals(value)){
				return hrc.name();
			}
		}
		return null;
	}
}
