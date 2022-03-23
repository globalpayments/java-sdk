package com.global.api.network.entities.nts;
import com.global.api.entities.enums.MailIndicatorType;
import com.global.api.entities.enums.MailMessageCodeType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class NtsMailData {
    @Getter
    @Setter
    private Integer mailCount;
    @Getter
    @Setter
    private MailIndicatorType mailIndicator;
    @Getter
    @Setter
    private Integer mailMessageType;
    @Getter
    @Setter
    private MailMessageCodeType mailMessageCode;
    @Getter
    @Setter
    private Integer mailTextLength;
    @Getter
    @Setter
    private String mailText;
}
