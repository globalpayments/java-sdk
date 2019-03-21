package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;

public class BoardingConfig extends Configuration {
    private String portal;

    public String getPortal() {
        return portal;
    }
    public void setPortal(String portal) {
        this.portal = portal;
    }

    public void configureContainer(ConfiguredServices services) {
        // TODO: put this when completing the online boarding work
    }
}
