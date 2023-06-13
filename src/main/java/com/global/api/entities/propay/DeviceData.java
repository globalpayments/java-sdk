package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class DeviceData {

    /** List of information about the device like name,quantity,etc */
    private List<DeviceInfo> devices;

    public DeviceData() {
        devices=new ArrayList<>();
    }
}
