package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BeneficialOwnerData {

    /** Owners Count*/
    private int ownersCount;

    /** Owners List */
    private List<OwnersData> ownersList;

    public BeneficialOwnerData() {
        ownersList = new ArrayList<>();
    }
}
