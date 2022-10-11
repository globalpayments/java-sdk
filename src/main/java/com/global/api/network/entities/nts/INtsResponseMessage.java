package com.global.api.network.entities.nts;

import com.global.api.entities.enums.*;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.nts.PendingRequestIndicator;
import com.global.api.network.enums.nts.ServicingHostName;
import com.global.api.utils.StringParser;

public interface INtsResponseMessage {

    static NtsResponseMessageHeader getHeader(byte [] buffer) {
        NtsNetworkMessageHeader ntsMessageHeader = new NtsNetworkMessageHeader();
        NtsResponseMessageHeader ntsResponseMessageHeader = new NtsResponseMessageHeader();

        StringParser sp = new StringParser(buffer);
        ntsMessageHeader.setMessageType(sp.readInt(1));
        ntsMessageHeader.setCompanyNumber(sp.readInt(3));
        ntsMessageHeader.setBinTerminalId(sp.readString(1));
        ntsMessageHeader.setBinTerminalType(sp.readString(1));
        ntsMessageHeader.setResponseCode(sp.readStringConstant(2, NtsHostResponseCode.class));
        ntsMessageHeader.setTimeoutValue(sp.readInt(3));
        sp.readString(1); // Filter
        ntsMessageHeader.setInputCapabilityCode(sp.readStringConstant(1, CardDataInputCapability.class));
        sp.readString(1); // Filter
        ntsMessageHeader.setTerminalDestinationTag(sp.readString(3));
        ntsMessageHeader.setSoftwareVersion(sp.readString(2));
        ntsMessageHeader.setPinIndicator(sp.readStringConstant(1, PinIndicator.class));
        ntsMessageHeader.setLogicProcessFlag(sp.readStringConstant(1, LogicProcessFlag.class));
        ntsMessageHeader.setNtsMessageCode(sp.readStringConstant(2, NtsMessageCode.class));
        ntsMessageHeader.setTerminalType(sp.readStringConstant(2, TerminalType.class));
        ntsMessageHeader.setUnitNumber(sp.readString(11));
        ntsMessageHeader.setTerminalId(sp.readInt(2));

        ntsResponseMessageHeader.setNtsNetworkMessageHeader(ntsMessageHeader);
        ntsResponseMessageHeader.setPendingRequestIndicator(sp.readStringConstant(1, PendingRequestIndicator.class));
        ntsResponseMessageHeader.setTransactionDate(sp.readString(4));
        ntsResponseMessageHeader.setTransactionTime(sp.readString(6));
        ntsResponseMessageHeader.setServicingHostName(sp.readStringConstant(1, ServicingHostName.class));
        ntsResponseMessageHeader.setDataCollectResponseCode(sp.readString(2));

        return ntsResponseMessageHeader;
    }
    INtsResponseMessage setNtsResponseMessage(byte [] buffer, boolean emvFlag);
   }
