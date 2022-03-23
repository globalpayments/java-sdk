package com.global.api.network.entities.nts;

import com.global.api.entities.enums.NtsMessageCode;
import com.global.api.entities.enums.PinIndicator;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

public class NtsRequestMessageHeader {
    @Getter
    @Setter
    private String terminalDestinationTag;
    @Getter
    @Setter
    private PinIndicator pinIndicator;
    @Getter
    @Setter
    private NtsMessageCode ntsMessageCode;
    @Getter
    private String transactionDate=DateTime.now().toString("MMdd");
    @Getter
    private String transactionTime=DateTime.now().toString("hhmmss");
    @Getter
    @Setter
    private int priorMessageResponseTime;
    @Getter
    @Setter
    private int priorMessageConnectTime;
    @Getter
    @Setter
    private String priorMessageCode;
}
