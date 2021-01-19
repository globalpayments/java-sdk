package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DisputeSortProperty implements IStringConstant {
    Id("id"),
    ARN("arn"),
    Brand("brand"),
    Status("status"),
    Stage("stage"),
    FromStageTimeCreated("from_stage_time_created"),
    ToStageTimeCreated("to_stage_time_created"),
    AdjustmentFunding("adjustment_funding"),
    FromAdjustmentTimeCreated("from_adjustment_time_created"),
    ToAdjustmentTimeCreated("to_adjustment_time_created");

    String value;
    DisputeSortProperty(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
