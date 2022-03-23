package com.global.api.network.entities.nts;

import lombok.Getter;
import lombok.Setter;

public class NtsResponse {
    @Getter
    @Setter
    private NtsResponseMessageHeader ntsResponseMessageHeader;
    @Getter
    @Setter
    private INtsResponseMessage ntsResponseMessage;

}
