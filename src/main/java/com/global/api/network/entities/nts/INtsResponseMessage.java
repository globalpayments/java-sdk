package com.global.api.network.entities.nts;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.nts.PendingRequestIndicator;
import com.global.api.network.enums.nts.ServicingHostName;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;

public interface INtsResponseMessage {

    static NtsResponseMessageHeader getHeader(byte [] buffer) throws GatewayException {
        NtsNetworkMessageHeader ntsMessageHeader = new NtsNetworkMessageHeader();
        NtsResponseMessageHeader ntsResponseMessageHeader = new NtsResponseMessageHeader();

        NtsUtils.log("--------------------- RESPONSE HEADER ---------------------");
        StringParser sp = new StringParser(buffer);
        ntsMessageHeader.setMessageType(sp.readInt(1));
        NtsUtils.log("message type", ntsMessageHeader.getMessageType());

        ntsMessageHeader.setCompanyNumber(sp.readInt(3));
        NtsUtils.log("company number", ntsMessageHeader.getCompanyNumber());

        ntsMessageHeader.setBinTerminalId(sp.readString(1));
        NtsUtils.log("binary terminal id", ntsMessageHeader.getBinTerminalId());

        ntsMessageHeader.setBinTerminalType(sp.readString(1));
        NtsUtils.log("binary terminal type", ntsMessageHeader.getBinTerminalType());

        ntsMessageHeader.setResponseCode(sp.readStringConstant(2, NtsHostResponseCode.class));
        NtsUtils.log("Host Response Code", ntsMessageHeader.getResponseCode());

        ntsMessageHeader.setTimeoutValue(sp.readInt(3));
        sp.readString(1); // Filter
        ntsMessageHeader.setInputCapabilityCode(sp.readStringConstant(1, CardDataInputCapability.class));
        NtsUtils.log("Input Capability Code", ntsMessageHeader.getInputCapabilityCode());

        sp.readString(1); // Filter
        ntsMessageHeader.setTerminalDestinationTag(sp.readString(3));
        NtsUtils.log("Terminal Destination Tag", ntsMessageHeader.getTerminalDestinationTag());

        ntsMessageHeader.setSoftwareVersion(sp.readString(2));
        NtsUtils.log("Software Version", ntsMessageHeader.getSoftwareVersion());

        ntsMessageHeader.setPinIndicator(sp.readStringConstant(1, PinIndicator.class));
        NtsUtils.log("Pin Indicator", ntsMessageHeader.getPinIndicator());

        ntsMessageHeader.setLogicProcessFlag(sp.readStringConstant(1, LogicProcessFlag.class));
        NtsUtils.log("Logic Process Flag or Store_And_Forward_Indicator", ntsMessageHeader.getLogicProcessFlag());

        ntsMessageHeader.setNtsMessageCode(sp.readStringConstant(2, NtsMessageCode.class));
        NtsUtils.log("Message Code", ntsMessageHeader.getNtsMessageCode());

        ntsMessageHeader.setTerminalType(sp.readStringConstant(2, TerminalType.class));
        NtsUtils.log("Terminal Type", ntsMessageHeader.getTerminalType());

        ntsMessageHeader.setUnitNumber(sp.readString(11));
        NtsUtils.log("Unit Number", String.valueOf(ntsMessageHeader.getUnitNumber()));

        ntsMessageHeader.setTerminalId(sp.readInt(2));
        NtsUtils.log("Terminal Id", String.valueOf(ntsMessageHeader.getTerminalId()));


        ntsResponseMessageHeader.setNtsNetworkMessageHeader(ntsMessageHeader);
        ntsResponseMessageHeader.setPendingRequestIndicator(sp.readStringConstant(1, PendingRequestIndicator.class));
        NtsUtils.log("Pending Request Indicator",ntsResponseMessageHeader.getPendingRequestIndicator());

        ntsResponseMessageHeader.setTransactionDate(sp.readString(4));
        NtsUtils.log("Transaction Date", String.valueOf(ntsResponseMessageHeader.getTransactionDate()));

        ntsResponseMessageHeader.setTransactionTime(sp.readString(6));
        NtsUtils.log("Transaction Time", String.valueOf(ntsResponseMessageHeader.getTransactionTime()));

        ntsResponseMessageHeader.setServicingHostName(sp.readStringConstant(1, ServicingHostName.class));
        NtsUtils.log("Servicing Host Name", ntsResponseMessageHeader.getServicingHostName());

        ntsResponseMessageHeader.setDataCollectResponseCode(sp.readString(2));
        NtsUtils.log("DataCollect Response Code", ntsResponseMessageHeader.getDataCollectResponseCode());

        return ntsResponseMessageHeader;
    }
    INtsResponseMessage setNtsResponseMessage(byte [] buffer, boolean emvFlag);
   }
