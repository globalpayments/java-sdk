package com.global.api.entities.payroll;

import com.global.api.entities.enums.PayGroupFrequency;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;

public class PayGroup extends PayrollCollectionItem {
    private PayGroupFrequency frequency;

    public PayGroupFrequency getFrequency() {
        return frequency;
    }

    public PayGroup() {
        super("PayGroupId", "PayGroupName");
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) {
        super.fromJson(doc, encoder);
        frequency = EnumUtils.parse(PayGroupFrequency.class, doc.getInt("PayFrequency"));
    }
}
