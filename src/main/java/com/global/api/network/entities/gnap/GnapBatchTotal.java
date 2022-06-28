package com.global.api.network.entities.gnap;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class GnapBatchTotal {
    private int totalSalesTransaction;
    private BatchTotalSign signTotalSale=BatchTotalSign.Default;
    private BigDecimal totalSaleAmount;
    private int totalRefundTransaction;
    private BatchTotalSign signTotalRefund=BatchTotalSign.Default;
    private BigDecimal totalRefundAmount;
    private int totalAdjustmentTransaction;
    private BatchTotalSign signTotalAdjustment=BatchTotalSign.Default;
    private BigDecimal totalAdjustmentAmount;

    public String getValue(SequenceNumber sequenceNumber)
    {
        StringBuilder sb=new StringBuilder();
        sb.append(StringUtils.padLeft(sequenceNumber.getShiftCounter(),3,'0'));
        sb.append(StringUtils.padLeft(sequenceNumber.getBatchCounter(),3,'0'));
        sb.append(StringUtils.padLeft(totalSalesTransaction,4,'0'));
        sb.append(signTotalSale.getValue());
        sb.append(StringUtils.padLeft(StringUtils.toNumeric(totalSaleAmount),9,'0'));

        sb.append(StringUtils.padLeft(totalRefundTransaction,4,'0'));
        sb.append(signTotalRefund.getValue());
        sb.append(StringUtils.padLeft(StringUtils.toNumeric(totalRefundAmount),9,'0'));

        sb.append(StringUtils.padLeft(totalAdjustmentTransaction,4,'0'));
        sb.append(signTotalAdjustment.getValue());
        sb.append(StringUtils.padLeft(StringUtils.toNumeric(totalAdjustmentAmount),9,'0'));
        return sb.toString();
    }
}
