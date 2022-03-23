package com.global.api.network.entities.nts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@With
@AllArgsConstructor
public class NtsRequestToBalanceData {
    private Integer daySequenceNumber;
    private BigDecimal pdlBatchDiscount;
    private String vendorSoftwareNumber;
}
