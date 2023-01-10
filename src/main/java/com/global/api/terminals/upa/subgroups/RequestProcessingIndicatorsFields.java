package com.global.api.terminals.upa.subgroups;

import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.upa.Entities.Enums.UpaAcquisitionType;
import com.global.api.utils.JsonDoc;

import java.util.ArrayList;

public class RequestProcessingIndicatorsFields implements IRequestSubGroup {
    ArrayList<UpaAcquisitionType> acquisitionTypes = new ArrayList<UpaAcquisitionType>();
    private boolean quickChip;
    private boolean checkLuhn;

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
//        boolean hasContents = false;

        // will use the most common settings for these parameters for the time being
        // will later add builder methods to set these values

        params.set("quickChip", "Y");
        params.set("checkLuhn", "N");

        return params;
        
//        return hasContents ? params : null;
    }

    @Override
    public String getElementString() {
        return null;
    }
}
