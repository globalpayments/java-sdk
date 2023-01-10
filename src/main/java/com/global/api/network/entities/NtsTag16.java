package com.global.api.network.entities;

import com.global.api.network.enums.SecurityData;
import com.global.api.network.enums.ServiceCode;
import lombok.Getter;
import lombok.Setter;


public class NtsTag16 {
    @Getter
    @Setter
    private int pumpNumber;
    @Getter
    @Setter
    private int workstationId;
    @Getter
    @Setter
    private ServiceCode serviceCode;
    @Getter
    @Setter
    private SecurityData securityData;
}
