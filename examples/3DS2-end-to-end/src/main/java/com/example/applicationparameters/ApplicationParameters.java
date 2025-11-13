/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.applicationparameters;

public class ApplicationParameters {

    private static final String APP_ID = "UJqPrAhrDkGzzNoFInpzKqoI8vfZtGRV";

    // TODO Return here the base URL of your application
    public static String getBaseUrl() {
        return "https://example.com:8443/3DS2-end-to-end";
    }

    public static String getAppId() {
        return APP_ID;
    }

    public static String getAppKey() {
        Properties properties = new Properties();
        String appKey = prop.getProperty("3ds2.appKey");
        return appKey;
    }

}
