package com.global.api.network.entities.nts;

import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.nts.PendingRequestIndicator;
import com.global.api.network.enums.nts.ServicingHostName;
import lombok.Getter;
import lombok.Setter;

public class NtsResponseMessageHeader {
    @Getter
    @Setter
    private String transactionDate;
    @Getter
    @Setter
    private String transactionTime;
    @Getter
    @Setter
    private PriorMessageInformation priorMessageInformation;
    @Getter
    @Setter
    private PendingRequestIndicator pendingRequestIndicator;
    @Getter
    @Setter
    private ServicingHostName servicingHostName;
    @Getter
    @Setter
    private String dataCollectResponseCode;
    @Getter
    @Setter
    private NtsNetworkMessageHeader ntsNetworkMessageHeader;

}
