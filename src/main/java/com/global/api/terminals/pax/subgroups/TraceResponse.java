package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class TraceResponse implements IResponseSubGroup {
    private String transactionNumber;
    private String referenceNumber;
    private String timeStamp;

    public String getTransactionNumber() {
        return transactionNumber;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public String getTimeStamp() {
        return timeStamp;
    }

    public TraceResponse(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            this.transactionNumber = data[0];
            this.referenceNumber = data[1];
            this.timeStamp = data[2];
        }
        catch (IndexOutOfBoundsException e) {
            // nom nom
        }
    }
}