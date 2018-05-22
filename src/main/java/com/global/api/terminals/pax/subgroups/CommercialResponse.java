package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class CommercialResponse implements IResponseSubGroup {
    private String poNumber;
    private String customerCode;
    private boolean taxExempt;
    private String taxExemptId;

    public String getPoNumber() {
        return poNumber;
    }
    public String getCustomerCode() {
        return customerCode;
    }
    public boolean isTaxExempt() {
        return taxExempt;
    }
    public String getTaxExemptId() {
        return taxExemptId;
    }

    public CommercialResponse(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            this.poNumber = data[0];
            this.customerCode = data[1];
            this.taxExempt = data[2].equals("1");
            this.taxExemptId = data[3];
        }
        catch (IndexOutOfBoundsException e) {
            // nom nom
        }
    }
}