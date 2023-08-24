package com.global.api.network.elements;

import lombok.Getter;
import lombok.Setter;

public class DE117_WIC_Data_Field_EA {
    @Getter @Setter private String upcData ;
    @Getter @Setter private String itemDescription;
    @Getter @Setter private String categoryCode ;
    @Getter @Setter private String categoryDescription ;
    @Getter @Setter private String subCategoryCode ;
    @Getter @Setter private String subCategoryDescription ;
    @Getter @Setter private String unitOfMeasure ;
    @Getter @Setter private String packageSize ;
    @Getter @Setter private String benefitQuantity ;
    @Getter @Setter private String benefitUnitDescription ;
    @Getter @Setter private String upcDataLength ;

}
