package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import lombok.Getter;
import lombok.Setter;

public class NtsCreditResponse {
    @Getter
    @Setter
    private NTSCardTypes cardType;
    @Getter
    @Setter
    private	String accountNumber;
    @Getter
    @Setter
    private	String approvalCode;
    @Getter
    @Setter
    private AuthorizerCode authorizer;
    @Getter
    @Setter
    private	String expandedUserData;
    @Getter
    @Setter
    private	int hostResponseLength;
    @Getter
    @Setter
    private	String hostResponseArea;
}
