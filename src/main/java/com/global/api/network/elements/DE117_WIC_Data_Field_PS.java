package com.global.api.network.elements;

import lombok.Getter;
import lombok.Setter;

public class DE117_WIC_Data_Field_PS {
    @Getter @Setter private String upcData ;
    @Getter @Setter private String categoryCode ;
    @Getter @Setter private String subCategoryCode ;
    @Getter @Setter private String units ;
    @Getter @Setter private String itemPrice ;
    @Getter @Setter private String purchaseQuantity ;
    @Getter @Setter private String itemActionCode ;
    @Getter @Setter private String originalItemPrice ;
    @Getter @Setter private String originalPurchaseQuantity ;
    @Getter @Setter private String upcDataLength ;

}
