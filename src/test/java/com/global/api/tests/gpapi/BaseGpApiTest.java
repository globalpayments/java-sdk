package com.global.api.tests.gpapi;

import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;
import com.global.api.utils.EnumUtils;

public class BaseGpApiTest {

    static final String APP_ID = "OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj";
    static final String APP_KEY = "qM31FmlFiyXRHGYh";
    // Nacho's Credentials
    // static final String APP_ID = "Uyq6PzRbkorv2D4RQGlldEtunEeGNZll";
    // static final String APP_KEY = "QDsW1ETQKHX6Y4TA";
    static final String GP_API_CONFIG_NAME = "GpApiConfig";

    static final String SUCCESS = "SUCCESS";
    static final String VERIFIED = "VERIFIED";
    static final String CLOSED = "CLOSED";

    // TODO: Remove if they are not useful for future tests
    protected static String getMapping(IMappedConstant value) {
        return EnumUtils.getMapping(value, Target.GP_API);
    }

    protected static String getMapping(IMappedConstant value, Target target) {
        return EnumUtils.getMapping(value, target);
    }

}
