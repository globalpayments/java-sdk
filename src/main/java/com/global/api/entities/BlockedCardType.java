package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BlockedCardType {

    public Boolean consumerDebit;

    public Boolean consumerCredit;

    public Boolean commercialCredit;

    public Boolean commercialDebit;


    public boolean areAllPropertiesSetToNull() {
        return consumerDebit == null
                && consumerCredit == null
                && commercialCredit == null
                && commercialDebit == null;
    }

}
