package com.global.api.entities.enums;

public enum NtsHostResponseCode implements IStringConstant{
	Space(" ", "Space"),
	Success("00", "Success"),
	DenialRequestToBalance("01", "Denial"),
	InvalidPin("02", "InvalidPin"),
	ReservedForProprietaryUseDenial("03", "ReservedForProprietaryUseDenial"),
	ReservedForProprietaryUseApproved01("04", "ReservedForProprietaryUseApproved01"),
	ReservedForProprietaryUseApproved02("05", "ReservedForProprietaryUseApproved02"),
	Denial("06", "Denial"),
	PinTriesExceeded("07", "PinTriesExceeded"),
	CreditAdjustment("08", "CreditAdjustment"),
	DenialMasterCard("10", "Denial"),
	InsufficientFunds("11", "InsufficientFunds"),
	OfflineDeclineAdvice("12", "OfflineDeclineAdvice"),
	InvalidFuel("13", "InvalidFuel"),
	DenialHeartlandGiftCard("14", "Denial"),
	PartiallyApproved("15", "PartiallyApproved"),
	VelocityReferral("16", "VelocityReferral"),
	TerminalDisabled("17", "TerminalDisabled"),
	AccountBalanceExceededOrMaximumNumberOfRechargesExceeded("18", "AccountBalanceExceededOrMaximumNumberOfRechargesExceeded"),
	HeartlandGiftCardAlreadyActivated("19", "HeartlandGiftCardAlreadyActivated"),
	PickupCard("20", "PickupCard"),
	HeartlandGiftCardNotAvailableForActivation("21", "HeartlandGiftCardNotAvailableForActivation"),
	InvalidPinCentegoOrInvalidDriverFleetCor("28", "InvalidPinCentegoOrInvalidDriverFleetCor"),
	AvsReferralForFullyOrPartially("30", "AvsReferralForFullyOrPartially"),
	HeartlandGiftCardDenial("35", "HeartlandGiftCardDenial"),
	AsynchronousTerminalsOnlyTimeout("37", "AsynchronousTerminalsOnlyTimeout"),
	TerminalSuccessfullySentMessageToNode("38", "TerminalSuccessfullySentMessageToNode"),
	ZipCodeNotMatch("39", "ZipCodeNotMatch"),
	HostSystemFailure("40", "HostSystemFailure"),
	PrimaryHostIsNotAvailable("41", "PrimaryHostIsNotAvailable"),
	AllowStandInProcessing("48", "AllowStandInProcessing"),
	InvalidCard("50", "InvalidCard"),
	EncryptionError("52", "EncryptionError"),
	TooManyQueuedOrNoConnection("53", "TooManyQueuedOrNoConnection"),
	EmvPdlError("54", "EmvPdlError"),
	InvalidPinEncryption("55", "InvalidPinEncryption"),
	NoTelephoneLinePresent("57", "NoTelephoneLinePresent"),
	TerminalReceivedNeitherPollsNorSelects("57", "TerminalReceivedNeitherPollsNorSelects"),
	ExpiredCard("60", "ExpiredCard"),
	DeclinedEntry("65", "DeclinedEntry"),
	NeverReceivedAnEnq("67", "NeverReceivedAnEnq"),
	TerminalCouldNotCommunicateWithNode("68", "TerminalCouldNotCommunicateWithNode"),
	FormatError("70", "FormatError"),
	ReceivedDisconnect("77", "ReceivedDisconnect"),
	ReceivedPollsNotReceiveAcknowledge("78", "ReceivedPollsNotReceiveAcknowledge"),
	Code70TwiceInRow("79", "Code70TwiceInRow"),
	TerminalTimeout("80", "TerminalTimeout"),
	PinDebitNetworkError("81", "PinDebitNetworkError"),
	InquiryBalanceNotAvailable("83", "InquiryBalanceNotAvailable"),
	NetworkUnavailable("84", "NetworkUnavailable"),
	DuplicateAuthorization("85", "DuplicateAuthorization"),
	UdpTerminalsTimeout("87", "UdpTerminalsTimeout"),
	TerminalHasApprovedTheTransactionInStandIn("88", "TerminalHasApprovedTheTransactionInStandIn"),
	TerminalTimeoutLostConnection("90", "TerminalTimeoutLostConnection"),
	StoreAndForwardModeAndSaleWithoutAuthorized("92", "StoreAndForwardModeAndSaleWithoutAuthorized"),
	TerminalHasApprovedTheTransactionInStandInForCode90("98", "TerminalHasApprovedTheTransactionInStandInForCode90");

	final String value;
	final String name;
	NtsHostResponseCode(String value, String name) {
        this.value = value;
		this.name = name;
    }
    public String getValue() { return this.value; }

	public String getName() {
		return this.name;
	}

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
