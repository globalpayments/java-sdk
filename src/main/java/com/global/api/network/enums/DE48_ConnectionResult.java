package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_ConnectionResult implements IStringConstant {
    NormalCompletion("00"),
    NoCarrierDetected("01"),
    BusySignal("02"),
    RingNoAnswer("03"),
    No_ENQ_FromHost("04"),
    GenericReceiveError("05"),
    GenericTransmitError("06"),
    NoResponseFromHostAfterTransmit("07"),
    DialingAlternate_01("08"),
    DialingAlternate_02("09"),
    RedialingPrimary_OrAlternate("10"),
    TransactionSentDirectToAlternate_NoError("11"),
    GenericLeasedLineError("12"),
    TooMany_NAKs_Sending("13"),
    TooMany_NAKs_Receiving("14"),
    Received_EOT_AwaitingResponse("15"),
    Parity_Framing_Overrun_ErrorOnReceive("16"),
    ResponseBufferOverflow("17"),
    LostCarrierAwaitingResponse("18"),
    Redialing_InvalidCustomerData("20"),
    Redialing_FailedErrorCheckResponse("21"),
    NoPollOnLeasedLine_AttemptingDialUp("50"),
    TimeoutOnLeadedLine_AttemptingDialUp("51"),
    TransmitNotReadyOnLeadedLine_AttemptingDialUp("52"),
    TimeoutOnLeasedLineAwaitingResponse_AttemptingDialUp("53"),
    ConnectionError_ISDN("60"),
    No_ENQ_FromHost_ISDN("61"),
    Received_EOT_AwaitingResponse_ISDN("62"),
    Hardware_OS_Error("70"),
    SPS_LinkCommunicationTest("71"),
    LostCarrier_Before_ENQ("90"),
    LostCarrier_After_1_ENQ("91"),
    LostCarrier_After_2_ENQ("92"),
    LostCarrier_After_3_ENQ("93"),
    LostCarrier_After_4_ENQ("94"),
    LostCarrier_After_5_ENQ("95"),
    LostCarrier_After_6_ENQ("96"),
    CommunicationError("99");

    private final String value;
    DE48_ConnectionResult(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
