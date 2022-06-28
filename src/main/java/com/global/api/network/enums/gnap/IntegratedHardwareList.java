package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum IntegratedHardwareList implements IStringConstant {
    ICT250("ICT"),
    IUP100("IUP"),
    IUP250("IU2"),
    IUP300("IU3"),
    Spot3("M3"),
    Spot7("M7"),
    Lane3000("LN3"),
    Lane5000("LN5"),
    Lane7000("LN7"),
    Lane8000("LN8"),
    Link2500("LN2"),
    P400("P40"),
    V400("V40"),
    M400("M40"),
    BBP("BBP"),
    ICT25("IC25"),
    IWL250B_G("IW25"),
    Omni_3300("3300"),
    Omni_3750_DialOnly("375D"),
    Omni_3750_Dual("375P"),
    Vx510("V510"),
    Vx570("V570"),
    Vx610("V610"),
    Vx670("V670"),
    Vx810("V810"),
    Desk_5000("DK50"),
    Move_5000("MV50"),
    Lane_3000("LN30"),
    Link_2500("LK25"),
    Unknown("9999");

    String value;
    IntegratedHardwareList(String value){this.value=value;}

    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
