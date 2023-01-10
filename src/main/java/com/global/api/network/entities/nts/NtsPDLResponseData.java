package com.global.api.network.entities.nts;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NtsPDLResponseData implements INtsResponseMessage {

    private String nextParameterType;
    private String nextSequenceNumber;
    private String numberOfEntries;
    private String parameterType;
    private String parameterVersion;
    private String parameterSequenceNumber;

    //START OF NETWORK DATA
    private String networkParameterCode;
    private Integer networkDataLength;
    private String accessCode;
    private String primaryNumber;
    private String secondaryNumber;
    private String downloadNumber;
    private String pollCode;

    //START OF UNIT DATA
    private String unitParameterCode;
    private String unitLength;
    private String unitName;
    private String unitAddress;
    private String unitCity;
    private String unitState;

    //START OF USER DATA
    private String userParameterCode;
    private String userDataLength;
    private String userData;


    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsPDLResponseData pdlResponse = new NtsPDLResponseData();
        StringParser stringParser = new StringParser(buffer);

        // Common fields
        pdlResponse.setNextParameterType(stringParser.readString(2));
        pdlResponse.setNextSequenceNumber(stringParser.readString(2));
        pdlResponse.setNumberOfEntries(stringParser.readString(1));
        pdlResponse.setParameterType(stringParser.readString(2));
        pdlResponse.setParameterVersion(stringParser.readString(3));
        pdlResponse.setParameterSequenceNumber(stringParser.readString(2));
        // Start of Network Data
        if (pdlResponse.getParameterType().equals("10")) {
            pdlResponse.setNetworkParameterCode(stringParser.readString(2));
            pdlResponse.setNetworkDataLength(stringParser.readInt(2));
            if(pdlResponse.getNetworkDataLength() <= 37) {
                pdlResponse.setAccessCode(stringParser.readString(2));
                pdlResponse.setPrimaryNumber(stringParser.readString(11));
                pdlResponse.setSecondaryNumber(stringParser.readString(11));
                pdlResponse.setDownloadNumber(stringParser.readString(11));
                pdlResponse.setPollCode(stringParser.readString(2));
            } else {
                pdlResponse.setAccessCode(stringParser.readString(2));
                pdlResponse.setPrimaryNumber(stringParser.readString(28));
                pdlResponse.setSecondaryNumber(stringParser.readString(28));
                pdlResponse.setDownloadNumber(stringParser.readString(28));
                pdlResponse.setPollCode(stringParser.readString(2));
            }
        }
        //Start of Unit Data
        if (pdlResponse.getParameterType().equals("20") || pdlResponse.getParameterType().equals("10")) {
            pdlResponse.setUnitParameterCode(stringParser.readString(2));
            pdlResponse.setUnitLength(stringParser.readString(2));
            pdlResponse.setUnitName(stringParser.readString(20));
            pdlResponse.setUnitAddress(stringParser.readString(18));
            pdlResponse.setUnitCity(stringParser.readString(16));
            pdlResponse.setUnitState(stringParser.readString(2));
        }
        //START OF USER DATA
        if (pdlResponse.getParameterType().equals("30")) {
            pdlResponse.setUserParameterCode(stringParser.readString(2));
            pdlResponse.setUserDataLength(stringParser.readString(3));
            pdlResponse.setUserData(stringParser.readString(74));
        }
        return pdlResponse;
    }
}
