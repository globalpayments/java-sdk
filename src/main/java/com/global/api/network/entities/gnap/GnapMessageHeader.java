package com.global.api.network.entities.gnap;

import com.global.api.network.enums.gnap.MessageSubType;
import com.global.api.network.enums.gnap.MessageType;
import com.global.api.network.enums.gnap.TransactionCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GnapMessageHeader {
    @Builder.Default
    private String deviceType="9.";
    private String transmissionNumber;
    private String terminalId;
    @Builder.Default
    private String currentDate= DateTime.now().toString("yyMMdd");
    @Builder.Default
    private String currentTime=DateTime.now().toString("hhmmss");
    private MessageType messageType;
    private MessageSubType messageSubType;
    private TransactionCode transactionCode;
    @Builder.Default
    private Integer processingFlag1=0;
    private Integer processingFlag2;
    @Builder.Default
    private Integer processingFlag3=0;
    @Builder.Default
    private String responseCode="000";
}
