package com.global.api.network.elements;

import com.global.api.network.enums.DE62_IME_Subfield1;
import lombok.Getter;
import lombok.Setter;

public class DE62_IME_EcommerceData {

    @Getter @Setter
    private DE62_IME_Subfield1 de62ImeSubfield1;
    @Getter @Setter
    private String de62ImeSubfield2;
    @Getter @Setter
    private String de62ImeSubfield3;

}