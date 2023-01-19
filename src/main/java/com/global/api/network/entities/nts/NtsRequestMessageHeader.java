package com.global.api.network.entities.nts;

import com.global.api.entities.enums.NtsMessageCode;
import com.global.api.entities.enums.PinIndicator;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
    @Setter
    private String transactionDate = DateTime.now(DateTimeZone.UTC).toString("MMdd");
    @Getter
    @Setter
    private String transactionTime = DateTime.now(DateTimeZone.UTC).toString("HHmmss");
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
