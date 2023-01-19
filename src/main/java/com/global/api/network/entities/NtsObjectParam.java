package com.global.api.network.entities;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.LogicProcessFlag;
import com.global.api.entities.enums.TerminalType;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.network.enums.NetworkProcessingFlag;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.MessageWriter;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NtsObjectParam {
    private TransactionBuilder ntsBuilder;
    private MessageWriter ntsRequest;
    private AcceptorConfig ntsAcceptorConfig;
    private String ntsUserData;
    private boolean ntsEnableLogging;
    private IBatchProvider ntsBatchProvider;
    private NTSCardTypes ntsCardType;
    private String terminalId;
    private String binTerminalId;
    private String binTerminalType;
    private CardDataInputCapability inputCapabilityCode;
    private String softwareVersion;
    private LogicProcessFlag logicProcessFlag;
    private TerminalType terminalType;
    private String unitNumber;
    private String companyId;
    private int timeout;
}
