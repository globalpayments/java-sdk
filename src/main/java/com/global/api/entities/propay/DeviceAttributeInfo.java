package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceAttributeInfo {

    /**
     * Name of the attribute item. For example "Heartland.AMD.OfficeKey" which is specific to Portico devices for AMD. The avlue of this item is passed to Heartland for equipment boarding
     * AttributeName and AttributeValue are optional as a pair. But if one is specified, both must be specified.
    */
    private String name;

    /**
     * Value of the attribute item. In the above example, the value for the attribute named "Heartland.AMD.OfficeKey"
     * AttributeName and AttributeValue are optional as a pair. But if one is specified, both must be specified.
     */
    private String value;

}
