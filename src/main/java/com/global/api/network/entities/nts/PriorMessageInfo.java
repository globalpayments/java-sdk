package com.global.api.network.entities.nts;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PriorMessageInfo {
    private int priorMessageResponseTime=999;
    private int priorMessageConnectTime;
    private String priorMessageCode;
}