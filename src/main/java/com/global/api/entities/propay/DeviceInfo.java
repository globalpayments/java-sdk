package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class DeviceInfo {

    /** Unique name of the device being ordered */
    private String name;

    /** Number of devices ordered. Defaults to 0 */
    private int quantity;

    /** A list of attributes for the specific device. This will be null if no attributes are set */
    private List<DeviceAttributeInfo> attributes;
}
