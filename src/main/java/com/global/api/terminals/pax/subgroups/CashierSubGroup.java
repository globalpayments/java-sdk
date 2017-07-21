package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class CashierSubGroup implements IRequestSubGroup, IResponseSubGroup {
    private String clerkId;
    private String shiftId;

    public String getClerkId() {
        return clerkId;
    }
    public void setClerkId(String clerkId) {
        this.clerkId = clerkId;
    }
    public String getShiftId() {
        return shiftId;
    }
    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public CashierSubGroup() { }
    public CashierSubGroup(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            clerkId = data[0];
            shiftId = data[1];
        }
        catch (IndexOutOfBoundsException e) {
            // nom nom
        }
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(clerkId);
        sb.append((char) ControlCodes.US.getByte());
        sb.append(shiftId);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}