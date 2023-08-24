package com.global.api.network.elements;

import com.global.api.network.NetworkMessage;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DataElementId;
import com.global.api.network.enums.Iso8583MessageType;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class DE117_WIC_Data_Fields implements IDataElement<DE117_WIC_Data_Fields> {
    @Getter
    @Setter
    private String dataSetIdentifier;
    @Getter
    @Setter
    private int dataLength;
    private DE117_WIC_Data_Field_EA EAData;
    public void setEAData(DE117_WIC_Data_Field_EA EAData) {
        this.EAData = EAData;
    }
    public DE117_WIC_Data_Field_EA getEAData() {
        return EAData;
    }
    private DE117_WIC_Data_Field_PS PSData;
    public void setPSData(DE117_WIC_Data_Field_PS de117_wic_data_field_ps){
        this.PSData=de117_wic_data_field_ps;
    }
    public DE117_WIC_Data_Field_PS getPSData(){
        return PSData;
    }

    public DE117_WIC_Data_Fields fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);
        dataSetIdentifier = sp.readString(2);
        if (dataSetIdentifier.equals("EA")) {
            NetworkMessage nm = NetworkMessage.parse(buffer, Iso8583MessageType.SubElement_DE_0117_EA);
            EAData.setUpcData(nm.getString(DataElementId.DE_002));
            EAData.setItemDescription(nm.getString(DataElementId.DE_003));
            EAData.setCategoryCode(nm.getString(DataElementId.DE_004));
            EAData.setCategoryDescription(nm.getString(DataElementId.DE_005));
            EAData.setSubCategoryCode(nm.getString(DataElementId.DE_006));
            EAData.setSubCategoryDescription(nm.getString(DataElementId.DE_007));
            EAData.setUnitOfMeasure(nm.getString(DataElementId.DE_008));
            EAData.setPackageSize(nm.getString(DataElementId.DE_009));
            EAData.setBenefitQuantity(nm.getString(DataElementId.DE_011));
            EAData.setBenefitUnitDescription(nm.getString(DataElementId.DE_012));
            EAData.setUpcDataLength(nm.getString(DataElementId.DE_013));

        } else if (dataSetIdentifier.equals("PS")) {
            NetworkMessage nm = NetworkMessage.parse(buffer, Iso8583MessageType.SubElement_DE_0117_PS);
            PSData.setUpcData(nm.getString(DataElementId.DE_002));
            PSData.setCategoryCode(nm.getString(DataElementId.DE_003));
            PSData.setSubCategoryCode(nm.getString(DataElementId.DE_004));
            PSData.setUnits(nm.getString(DataElementId.DE_005));
            PSData.setItemPrice(nm.getString(DataElementId.DE_006));
            PSData.setPurchaseQuantity(nm.getString(DataElementId.DE_007));
            PSData.setItemActionCode(nm.getString(DataElementId.DE_008));
            PSData.setOriginalItemPrice(nm.getString(DataElementId.DE_009));
            PSData.setOriginalPurchaseQuantity(nm.getString(DataElementId.DE_010));
            PSData.setUpcDataLength(nm.getString(DataElementId.DE_011));

        }
        return this;
    }

    public byte[] toByteArray() {
        String rvalue = "";

        if (EAData != null) {
        String eaData = EAData.getUpcData()
                .concat(EAData.getItemDescription()
                .concat(StringUtils.padLeft(EAData.getCategoryCode() + "", 2, '0'))
                .concat(StringUtils.checkForNull(EAData.getCategoryDescription()))
                .concat(StringUtils.padLeft(EAData.getSubCategoryCode() + "", 3, '0'))
                .concat(StringUtils.checkForNull(EAData.getSubCategoryDescription()))
                .concat(StringUtils.checkForNull(EAData.getUnitOfMeasure()))
                .concat(StringUtils.checkForNull(EAData.getPackageSize()))
                .concat(StringUtils.checkForNull(EAData.getPackageSize()))
                .concat(StringUtils.padRight(EAData.getBenefitQuantity() + "", 5, '0'))
                .concat(StringUtils.checkForNull(EAData.getBenefitUnitDescription()))
                .concat(StringUtils.checkForNull(EAData.getUpcDataLength())));

        NetworkMessage message = new NetworkMessage(Iso8583MessageType.SubElement_DE_0117_EA)
                .set(DataElementId.DE_002, EAData.getUpcData())
                .set(DataElementId.DE_003, EAData.getItemDescription())
                .set(DataElementId.DE_004, StringUtils.padLeft(EAData.getCategoryCode() + "", 2, '0'))
                .set(DataElementId.DE_005, EAData.getCategoryDescription() != null ?(EAData.getCategoryDescription()) : "")
                .set(DataElementId.DE_006, StringUtils.padLeft(EAData.getSubCategoryCode() + "", 3, '0'))
                .set(DataElementId.DE_007, EAData.getSubCategoryDescription()!= null ? (EAData.getSubCategoryDescription()) : "")
                .set(DataElementId.DE_008, EAData.getUnitOfMeasure() != null ? (EAData.getUnitOfMeasure()) : "")
                .set(DataElementId.DE_009, EAData.getPackageSize() != null ? (EAData.getPackageSize()) : "")
                .set(DataElementId.DE_011, StringUtils.padLeft(EAData.getBenefitQuantity() + "", 5, '0'))
                .set(DataElementId.DE_012, EAData.getBenefitUnitDescription() != null ? (EAData.getBenefitUnitDescription()) : "")
                .set(DataElementId.DE_013, EAData.getUpcDataLength() != null ? (EAData.getUpcDataLength()).toString() : "");

        byte[] messageArray = message.buildMessage();
        rvalue = "EA".concat(message.getBitmap().toBinaryString().concat(eaData));

        }
        if (PSData != null) {
            String psData = StringUtils.padLeft(PSData.getUpcData() + "", 17, '0')
                .concat(StringUtils.checkForNull(PSData.getCategoryCode()))
                .concat(StringUtils.checkForNull(PSData.getSubCategoryCode()))
                .concat(StringUtils.checkForNull(PSData.getUnits()))
                .concat(StringUtils.padLeft(PSData.getItemPrice() + "", 6, '0'))
                .concat(StringUtils.padLeft(PSData.getPurchaseQuantity() + "", 5, '0'))
                .concat(StringUtils.padLeft(PSData.getItemActionCode() + "", 2 ,'0'))
                .concat(StringUtils.padLeft(PSData.getUpcDataLength(), 2, '0'));

            NetworkMessage message = new NetworkMessage(Iso8583MessageType.SubElement_DE_0117_PS)
                .set(DataElementId.DE_002, StringUtils.padLeft(PSData.getUpcData() + "", 17, '0'))
                .set(DataElementId.DE_003, PSData.getCategoryCode())
                .set(DataElementId.DE_004, PSData.getSubCategoryCode())
                .set(DataElementId.DE_005, PSData.getUnits())
                .set(DataElementId.DE_006, StringUtils.padLeft(PSData.getItemPrice() + "", 6, '0'))
                .set(DataElementId.DE_007, StringUtils.padLeft(PSData.getPurchaseQuantity() + "", 5, '0'))
                .set(DataElementId.DE_008, StringUtils.padLeft(PSData.getItemActionCode() + "", 2, '0'))
                .set(DataElementId.DE_009, PSData.getOriginalItemPrice())
                .set(DataElementId.DE_010, PSData.getOriginalPurchaseQuantity())
                .set(DataElementId.DE_011, StringUtils.padLeft(PSData.getUpcDataLength() + "", 2, '0'));

            byte[] messageArray = message.buildMessage();
            String de="PS";
            rvalue = de.concat(psData);
        }
        return rvalue.getBytes();
    }
}