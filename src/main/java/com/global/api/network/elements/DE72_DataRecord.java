package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.entities.nts.POSSiteConfigurationData;
import com.global.api.utils.StringUtils;

public class DE72_DataRecord implements IDataElement<DE72_DataRecord> {
    String RECORD_FORMAT = "SCFG";
    private POSSiteConfigurationData posData;

    public DE72_DataRecord(POSSiteConfigurationData posData){
        this.posData = posData;
    }

    public DE72_DataRecord fromByteArray(byte[] buffer) {
        return this;
    }

    public byte[] toByteArray() {
        String rvalue = "";

        if (posData != null){
            rvalue = RECORD_FORMAT;
            String posConfigData = StringUtils.padRight(posData.getMessageVersion(),3,'0')
                    .concat(StringUtils.padRight(posData.getTransactionDate(),8,'0'))
                    .concat(StringUtils.padRight(posData.getTransactionTime(),6,'0'))
                    .concat(StringUtils.padLeft(posData.getCompanyName(),15,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandCompanyId(),5,' '))
                    .concat(StringUtils.padLeft(posData.getMerchantFranchiseName(),20,' '))
                    .concat(StringUtils.padLeft(posData.getMerchantIdUnitPlusTid(),15,' '))
                    .concat(StringUtils.padLeft(posData.getMerchantAddressStreet(),30,' '))
                    .concat(StringUtils.padLeft(posData.getMerchantAddressCity(),20,' '))
                    .concat(StringUtils.padLeft(posData.getMerchantAddressState(),2,' '))
                    .concat(StringUtils.padRight(posData.getMerchantAddressZip(),5,'0'))
                    .concat(StringUtils.padLeft(posData.getMerchantPhoneNumber(),12,' '))
                    .concat(StringUtils.padLeft(posData.getSiteBrand(),15,' '))
                    .concat(StringUtils.padRight(posData.getMerchantType(),4,'0'))
                    .concat(StringUtils.padLeft(posData.getPosSystemType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getMethodOfOperation(),1,' '))
                    .concat(StringUtils.padLeft(posData.getPosVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPosProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandPosTerminalType(),3,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandPosSoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandTerminalSpec(),1,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandTerminalSpecVersion(),4,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandPaymentEngine(),1,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandPaymentVertical(),1,' '))
                    .concat(StringUtils.padLeft(posData.getPosHardwareVersion(),4,' '))
                    .concat(StringUtils.padLeft(posData.getPosSoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getPosOperatingSystem(),8,' '))
                    .concat(StringUtils.padLeft(posData.getMiddlewareVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getMiddlewareProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getMiddlewareType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getMiddlewareSoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getReceiptPrinterType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getReceiptPrinterModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getJournalPrinterType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getJournalPrinterModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getInsidePedMultiLaneDeviceType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getInsidePedMultiLaneDeviceVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getInsidePedMultiLaneDeviceProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getKeyManagementSchemeInside(),1,' '))
                    .concat(StringUtils.padLeft(posData.getPinEncryptionInside(),1,' '))
                    .concat(StringUtils.padLeft(posData.getOutsidePedType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getOutsidePedVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getOutsidePedProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getKeyManagementSchemeOutside(),1,' '))
                    .concat(StringUtils.padLeft(posData.getPinEncryptionOutside(),1,' '))
                    .concat(StringUtils.padLeft(posData.getCheckReaderVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getCheckReaderProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getInsideContactlessReaderType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getInsideContactlessReaderVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getInsideContactlessReaderProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getOutsideContactlessReaderType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getOutsideContactlessReaderVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getOutsideContactlessReaderProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getCommunicationMedia(),1,' '))
                    .concat(StringUtils.padLeft(posData.getCommunicationProtocol(),1,' '))
                    .concat(StringUtils.padLeft(posData.getInternetBroadbandUse(),1,' '))
                    .concat(StringUtils.padLeft(posData.getDatawireAccess(),1,' '))
                    .concat(StringUtils.padLeft(posData.getMicronodeModelNumber(),8,' '))
                    .concat(StringUtils.padLeft(posData.getMicronodeSoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getModemRouterType(),1,' '))
                    .concat(StringUtils.padLeft(posData.getModemRouterVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getModemRouterProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getModemPhoneNumber(),12,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandPrimaryDialNumberOrIpPort(),21,' '))
                    .concat(StringUtils.padLeft(posData.getHeartlandSecondaryDialNumberOrIpPort(),21,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserInterfaceVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserInterfaceProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserInterfaceSoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserVendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getDispenserSoftwareVersion(),8,' '))
                    .concat(StringUtils.padRight(posData.getDispenserQuantity(),2,'0'))
                    .concat(StringUtils.padRight(posData.getNumberOfScannersPeripherals(),2,'0'))
                    .concat(StringUtils.padLeft(posData.getScanner1Vendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getScanner1ProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getScanner1SoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral2Vendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral2ProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral2SoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral3Vendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral3ProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral3SoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral4Vendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral4ProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral4SoftwareVersion(),8,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral5Vendor(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral5ProductNameOrModel(),15,' '))
                    .concat(StringUtils.padLeft(posData.getPeripheral5SoftwareVersion(),8,' '));

            rvalue = rvalue.concat(posConfigData);
        }
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
