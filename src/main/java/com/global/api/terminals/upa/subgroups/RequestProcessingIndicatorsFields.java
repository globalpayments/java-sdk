package com.global.api.terminals.upa.subgroups;

import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.upa.Entities.Enums.UpaAcquisitionType;
import com.global.api.terminals.upa.Entities.Enums.UpaCardTypeFilter;
import com.global.api.utils.JsonDoc;

import java.util.ArrayList;

public class RequestProcessingIndicatorsFields implements IRequestSubGroup {
    ArrayList<UpaAcquisitionType> acquisitionTypes = new ArrayList<UpaAcquisitionType>();
    ArrayList<UpaCardTypeFilter> cardTypeFilter = new ArrayList<>();
    private String cardTypesString = "";
    private boolean quickChip;
    private boolean checkLuhn;

    public void setParams(TerminalAuthBuilder builder) {
        if(builder.getCardTypeFilter() != null) {
            this.cardTypeFilter.addAll(builder.getCardTypeFilter());
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
//        boolean hasContents = false;

        // will use the most common settings for these parameters for the time being
        // will later add builder methods to set these values

        params.set("quickChip", "Y");
        params.set("checkLuhn", "N");

        if (cardTypeFilter != null && !cardTypeFilter.isEmpty()) {
            cardTypesString = "";

            cardTypeFilter.forEach(
                    x -> {
                        cardTypesString += x.name();
                        cardTypesString += "|";
                    }
            );

            cardTypesString = cardTypesString.substring(0, cardTypesString.length() - 1);

            params.set("cardTypeFilter", cardTypesString);
        }
        return params;
        
//        return hasContents ? params : null;
    }

    @Override
    public String getElementString() {
        return null;
    }
}
