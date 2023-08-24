package com.global.api.network.elements;

public class EWICData {
    private DE117_WIC_Data_Fields ewicData;

    public EWICData(){
        ewicData = new DE117_WIC_Data_Fields();
    }
    public void add(DE117_WIC_Data_Field_EA eaData) {
        ewicData.setEAData(eaData);
    }

    public void add(DE117_WIC_Data_Field_PS psData) {
        ewicData.setPSData(psData);
    }

    public DE117_WIC_Data_Fields toDataElement() {
        return ewicData;
    }
}
