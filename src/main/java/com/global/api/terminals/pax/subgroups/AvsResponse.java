package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class AvsResponse implements IResponseSubGroup {
    private String avsResponseCode;
    private String avsResponseMessage;

    public String getAvsResponseCode() {
        return avsResponseCode;
    }
    public String getAvsResponseMessage() {
        return avsResponseMessage;
    }

    public AvsResponse(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            avsResponseCode = data[0];
            avsResponseMessage = data[1];
        }
        catch (IndexOutOfBoundsException e) {
            // Nom nom
        }
    }
}