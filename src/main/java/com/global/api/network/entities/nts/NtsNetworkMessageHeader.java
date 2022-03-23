package com.global.api.network.entities.nts;

import com.global.api.entities.enums.*;
import com.global.api.network.enums.CardDataInputCapability;
import lombok.Getter;
import lombok.Setter;

public class NtsNetworkMessageHeader {
    @Getter
    @Setter
    private int messageType;
    @Getter
    @Setter
    private int companyNumber;
    @Getter
    @Setter
    private String binTerminalId;
    @Getter
    @Setter
    private String binTerminalType;
    @Getter
    @Setter
    private NtsHostResponseCode responseCode;
    @Getter
    @Setter
    private int timeoutValue;
    @Getter
    @Setter
    private CardDataInputCapability inputCapabilityCode;
    @Getter
    @Setter
    private String terminalDestinationTag;
    @Getter
    @Setter
    private String softwareVersion;
    @Getter
    @Setter
    private PinIndicator pinIndicator;
    @Getter
    @Setter
    private LogicProcessFlag logicProcessFlag;
    @Getter
    @Setter
    private NtsMessageCode ntsMessageCode;
    @Getter
    @Setter
    private TerminalType terminalType;
    @Getter
    @Setter
    private String unitNumber;
    @Getter
    @Setter
    private int terminalId;
}
