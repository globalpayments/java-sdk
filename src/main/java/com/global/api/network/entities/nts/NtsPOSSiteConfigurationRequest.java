package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;

public class NtsPOSSiteConfigurationRequest implements INtsRequestMessage {

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {
        AuthorizationBuilder builder = (AuthorizationBuilder) ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();

        // POS Data
        POSSiteConfigurationData requestData = builder.getPosSiteConfigurationData();

        NtsUtils.log("MESSAGE VERSION ", requestData.getMessageVersion());
        request.addRange(StringUtils.padRight(requestData.getMessageVersion(),3,'0'), 3);

        NtsUtils.log("TRANSACTION DATE ", requestData.getTransactionDate());
        request.addRange(requestData.getTransactionDate(), 8);

        NtsUtils.log("TRANSACTION TIME ", requestData.getTransactionTime());
        request.addRange(requestData.getTransactionTime(), 6);

        NtsUtils.log("COMPANY NAME", requestData.getCompanyName());
        request.addRange(StringUtils.padRight(requestData.getCompanyName(), 15, ' '), 15);

        NtsUtils.log("HEARTLAND COMPANY ID", requestData.getHeartlandCompanyId());
        request.addRange(StringUtils.padLeft(requestData.getHeartlandCompanyId(), 5, ' '), 5);

        NtsUtils.log("MERCHANT / FRANCHISE NAME", requestData.getMerchantFranchiseName());
        request.addRange(StringUtils.padRight(requestData.getMerchantFranchiseName(), 20, ' '), 20);

        NtsUtils.log("MERCHANT ID/UNIT # Plus TID", requestData.getMerchantIdUnitPlusTid());
        request.addRange(StringUtils.padRight(requestData.getMerchantIdUnitPlusTid(), 15, ' '), 15);

        NtsUtils.log("MERCHANT ADDRESS STREET", requestData.getMerchantAddressStreet());
        request.addRange(StringUtils.padRight(requestData.getMerchantAddressStreet(), 30, ' '), 30);

        NtsUtils.log("MERCHANT ADDRESS CITY", requestData.getMerchantAddressCity());
        request.addRange(StringUtils.padRight(requestData.getMerchantAddressCity(), 20, ' '), 20);

        NtsUtils.log("MERCHANT ADDRESS STATE", requestData.getMerchantAddressState());
        request.addRange(StringUtils.padRight(requestData.getMerchantAddressState(), 2, ' '), 2);

        NtsUtils.log("MERCHANT ADDRESS ZIP", requestData.getMerchantAddressZip());
        request.addRange(StringUtils.padLeft(requestData.getMerchantAddressZip(), 5, '0'), 5);

        NtsUtils.log("MERCHANT PHONE NUMBER", requestData.getMerchantPhoneNumber());
        request.addRange(StringUtils.padRight(requestData.getMerchantPhoneNumber(), 12, ' '), 12);

        NtsUtils.log("SITE BRAND", requestData.getSiteBrand());
        request.addRange(StringUtils.padRight(requestData.getSiteBrand(), 15, ' '), 15);

        NtsUtils.log("MERCHANT TYPE", requestData.getMerchantType());
        request.addRange(StringUtils.padLeft(requestData.getMerchantType(), 4, '0'), 4);

        NtsUtils.log("POS SYSTEM TYPE", requestData.getPosSystemType());
        request.addRange(StringUtils.padRight(requestData.getPosSystemType(), 1, ' '), 1);

        NtsUtils.log("METHOD OF OPERATION ", requestData.getMethodOfOperation());
        request.addRange(StringUtils.padRight(requestData.getMethodOfOperation(), 1, ' '), 1);

        NtsUtils.log("POS VENDOR ", requestData.getPosVendor());
        request.addRange(StringUtils.padRight(requestData.getPosVendor(), 15, ' '), 15);

        NtsUtils.log("POS PRODUCT NAME or MODEL", requestData.getPosProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getPosProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("HEARTLAND POS TERMINAL TYPE", requestData.getHeartlandPosTerminalType());
        request.addRange(StringUtils.padLeft(requestData.getHeartlandPosTerminalType(), 3, '0'), 3);

        NtsUtils.log("HEARTLAND POS SOFTWARE VERSION", requestData.getHeartlandPosSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getHeartlandPosSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("HEARTLAND TERMINAL SPEC", requestData.getHeartlandTerminalSpec());
        request.addRange(StringUtils.padRight(requestData.getHeartlandTerminalSpec(),1,' '), 1);

        NtsUtils.log("HEARTLAND NTS TERMINAL SPEC VERSION", requestData.getHeartlandTerminalSpecVersion());
        request.addRange(StringUtils.padLeft(requestData.getHeartlandTerminalSpecVersion(), 4, '0'), 4);

        NtsUtils.log("HEARTLAND PAYMENT ENGINE", requestData.getHeartlandPaymentEngine());
        request.addRange(StringUtils.padRight(requestData.getHeartlandPaymentEngine(),1,' '), 1);

        NtsUtils.log("HEARTLAND PAYMENT VERTICAL", requestData.getHeartlandPaymentVertical());
        request.addRange(StringUtils.padRight(requestData.getHeartlandPaymentVertical(),1,' '), 1);

        NtsUtils.log("POS HARDWARE VERSION", requestData.getPosHardwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPosHardwareVersion(), 4, ' '), 4);

        NtsUtils.log("POS SOFTWARE VERSION", requestData.getPosSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPosSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("POS OPERATING SYSTEM ", requestData.getPosOperatingSystem());
        request.addRange(StringUtils.padRight(requestData.getPosOperatingSystem(), 8, ' '), 8);

        NtsUtils.log("MIDDLEWARE VENDOR ", requestData.getMiddlewareVendor());
        request.addRange(StringUtils.padRight(requestData.getMiddlewareVendor(), 15, ' '), 15);

        NtsUtils.log("MIDDLEWARE PRODUCT NAME or MODEL", requestData.getMiddlewareProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getMiddlewareProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("MIDDLEWARE TYPE", requestData.getMiddlewareType());
        request.addRange(StringUtils.padRight(requestData.getMiddlewareType(), 1, ' '), 1);

        NtsUtils.log("MIDDLEWARE SOFTWARE VERSION", requestData.getMiddlewareSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getMiddlewareSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("RECEIPT PRINTER TYPE", requestData.getReceiptPrinterType());
        request.addRange(StringUtils.padRight(requestData.getReceiptPrinterType(), 1, ' '), 1);

        NtsUtils.log("RECEIPT PRINTER MODEL", requestData.getReceiptPrinterModel());
        request.addRange(StringUtils.padRight(requestData.getReceiptPrinterModel(), 15, ' '), 15);

        NtsUtils.log("JOURNAL PRINTER TYPE", requestData.getJournalPrinterType());
        request.addRange(StringUtils.padRight(requestData.getJournalPrinterType(), 1, ' '), 1);

        NtsUtils.log("JOURNAL PRINTER MODEL", requestData.getJournalPrinterModel());
        request.addRange(StringUtils.padRight(requestData.getJournalPrinterModel(), 15, ' '), 15);

        NtsUtils.log("INSIDE PED / MULTI-LANE DEVICE TYPE ", requestData.getInsidePedMultiLaneDeviceType());
        request.addRange(StringUtils.padRight(requestData.getInsidePedMultiLaneDeviceType(), 1, ' '), 1);

        NtsUtils.log("INSIDE PED / MULTI-LANE DEVICE VENDOR", requestData.getInsidePedMultiLaneDeviceVendor());
        request.addRange(StringUtils.padRight(requestData.getInsidePedMultiLaneDeviceVendor(), 15, ' '), 15);

        NtsUtils.log("INSIDE PED / MULTI-LANE DEVICE PRODUCT NAME OR MODEL", requestData.getInsidePedMultiLaneDeviceProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getInsidePedMultiLaneDeviceProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("KEY MANAGEMENT SCHEME (INSIDE)  ", requestData.getKeyManagementSchemeInside());
        request.addRange(StringUtils.padRight(requestData.getKeyManagementSchemeInside(), 1, ' '), 1);

        NtsUtils.log("PIN ENCRYPTION (INSIDE) ", requestData.getPinEncryptionInside());
        request.addRange(StringUtils.padRight(requestData.getPinEncryptionInside(), 1, ' '), 1);

        NtsUtils.log("OUTSIDE PED TYPE", requestData.getOutsidePedType());
        request.addRange(StringUtils.padRight(requestData.getOutsidePedType(), 1, ' '), 1);

        NtsUtils.log("OUTSIDE PED VENDOR", requestData.getOutsidePedVendor());
        request.addRange(StringUtils.padRight(requestData.getOutsidePedVendor(), 15, ' '), 15);

        NtsUtils.log("OUTSIDE PED PRODUCT NAME or MODEL ", requestData.getOutsidePedProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getOutsidePedProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("KEY MANAGEMENT SCHEME (OUTSIDE)", requestData.getKeyManagementSchemeOutside());
        request.addRange(StringUtils.padRight(requestData.getKeyManagementSchemeOutside(), 1, ' '), 1);

        NtsUtils.log("PIN ENCRYPTION (OUTSIDE) ", requestData.getPinEncryptionOutside());
        request.addRange(StringUtils.padRight(requestData.getPinEncryptionOutside(), 1, ' '), 1);

        NtsUtils.log("CHECK READER VENDOR", requestData.getCheckReaderVendor());
        request.addRange(StringUtils.padRight(requestData.getCheckReaderVendor(), 15, ' '), 15);

        NtsUtils.log("CHECK READER PRODUCT NAME or MODEL", requestData.getCheckReaderProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getCheckReaderProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("INSIDE CONTACTLESS READER TYPE", requestData.getInsideContactlessReaderType());
        request.addRange(StringUtils.padRight(requestData.getInsideContactlessReaderType(), 1, ' '), 1);

        NtsUtils.log("INSIDE CONTACTLESS READER VENDOR", requestData.getInsideContactlessReaderVendor());
        request.addRange(StringUtils.padRight(requestData.getInsideContactlessReaderVendor(), 15, ' '), 15);

        NtsUtils.log("INSIDE CONTACTLESS READER PRODUCT NAME or MODEL ", requestData.getInsideContactlessReaderProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getInsideContactlessReaderProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("OUTSIDE CONTACTLESS READER TYPE ", requestData.getOutsideContactlessReaderType());
        request.addRange(StringUtils.padRight(requestData.getOutsideContactlessReaderType(), 1, ' '), 1);

        NtsUtils.log("OUTSIDE CONTACTLESS READER VENDOR", requestData.getOutsideContactlessReaderVendor());
        request.addRange(StringUtils.padRight(requestData.getOutsideContactlessReaderVendor(), 15, ' '), 15);

        NtsUtils.log("OUTSIDE CONTACTLESS READER PRODUCT NAME or MODEL ", requestData.getOutsideContactlessReaderProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getOutsideContactlessReaderProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("COMMUNICATION MEDIA ", requestData.getCommunicationMedia());
        request.addRange(StringUtils.padRight(requestData.getCommunicationMedia(), 1, ' '), 1);

        NtsUtils.log("COMMUNICATION PROTOCOL ", requestData.getCommunicationProtocol());
        request.addRange(StringUtils.padRight(requestData.getCommunicationProtocol(), 1, ' '), 1);

        NtsUtils.log("INTERNET / BROADBAND USE", requestData.getInternetBroadbandUse());
        request.addRange(StringUtils.padRight(requestData.getInternetBroadbandUse(), 1, ' '), 1);

        NtsUtils.log("DATAWIRE ACCESS", requestData.getDatawireAccess());
        request.addRange(StringUtils.padRight(requestData.getDatawireAccess(), 1, ' '), 1);

        NtsUtils.log("MICRONODE MODEL NUMBER", requestData.getMicronodeModelNumber());
        request.addRange(StringUtils.padRight(requestData.getMicronodeModelNumber(), 8, ' '), 8);

        NtsUtils.log("MICRONODE SOFTWARE VERSION ", requestData.getMicronodeSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getMicronodeSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("MODEM/ROUTER TYPE", requestData.getModemRouterType());
        request.addRange(StringUtils.padRight(requestData.getModemRouterType(), 1, ' '), 1);

        NtsUtils.log("MODEM/ROUTER VENDOR", requestData.getModemRouterVendor());
        request.addRange(StringUtils.padRight(requestData.getModemRouterVendor(), 15, ' '), 15);

        NtsUtils.log("MODEM/ROUTER PRODUCT NAME or MODEL", requestData.getModemRouterProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getModemRouterProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("MODEM PHONE NUMBER", requestData.getModemPhoneNumber());
        request.addRange(StringUtils.padRight(requestData.getModemPhoneNumber(), 12, ' '), 12);

        NtsUtils.log("HEARTLAND PRIMARY DIAL NUMBER or IP/PORT", requestData.getHeartlandPrimaryDialNumberOrIpPort());
        request.addRange(StringUtils.padRight(requestData.getHeartlandPrimaryDialNumberOrIpPort(), 21, ' '), 21);

        NtsUtils.log("HEARTLAND SECONDARY DIAL NUMBER or IP/PORT", requestData.getHeartlandSecondaryDialNumberOrIpPort());
        request.addRange(StringUtils.padRight(requestData.getHeartlandSecondaryDialNumberOrIpPort(), 21, ' '), 21);

        NtsUtils.log("DISPENSER INTERFACE VENDOR ", requestData.getDispenserVendor());
        request.addRange(StringUtils.padRight(requestData.getDispenserVendor(), 15, ' '), 15);

        NtsUtils.log("DISPENSER INTERFACE PRODUCT NAME or MODEL ", requestData.getDispenserInterfaceProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getDispenserInterfaceProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("DISPENSER INTERFACE SOFTWARE VERSION", requestData.getDispenserInterfaceSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getDispenserInterfaceSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("DISPENSER VENDOR", requestData.getDispenserVendor());
        request.addRange(StringUtils.padRight(requestData.getDispenserVendor(), 15, ' '), 15);

        NtsUtils.log("DISPENSER PRODUCT NAME or MODEL ", requestData.getDispenserProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getDispenserProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("DISPENSER SOFTWARE VERSION", requestData.getDispenserSoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getDispenserSoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("DISPENSER QUANTITY", requestData.getDispenserQuantity());
        request.addRange(StringUtils.padRight(requestData.getDispenserQuantity(), 2, '0'), 2);

        NtsUtils.log("NUMBER OF SCANNERS / PERIPHERALS", requestData.getNumberOfScannersPeripherals());
        request.addRange(StringUtils.padRight(requestData.getNumberOfScannersPeripherals(), 2, '0'), 2);

        NtsUtils.log("SCANNER 1 VENDOR", requestData.getScanner1Vendor());
        request.addRange(StringUtils.padRight(requestData.getScanner1Vendor(), 15, ' '), 15);

        NtsUtils.log("SCANNER 1 PRODUCT NAME or MODEL", requestData.getScanner1ProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getScanner1ProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("SCANNER 1 SOFTWARE VERSION ", requestData.getScanner1SoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getScanner1SoftwareVersion(), 8, ' '), 8);


        NtsUtils.log("PERIPHERAL 2 VENDOR", requestData.getPeripheral2Vendor());
        request.addRange(StringUtils.padRight(requestData.getPeripheral2Vendor(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 2 PRODUCT NAME or MODEL", requestData.getPeripheral2ProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getPeripheral2ProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 2 SOFTWARE VERSION ", requestData.getPeripheral2SoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPeripheral2SoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("PERIPHERAL 3 VENDOR", requestData.getPeripheral3Vendor());
        request.addRange(StringUtils.padRight(requestData.getPeripheral3Vendor(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 3 PRODUCT NAME or MODEL", requestData.getPeripheral3ProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getPeripheral3ProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 3 SOFTWARE VERSION ", requestData.getPeripheral3SoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPeripheral3SoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("PERIPHERAL 4 VENDOR", requestData.getPeripheral4Vendor());
        request.addRange(StringUtils.padRight(requestData.getPeripheral4Vendor(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 4 PRODUCT NAME or MODEL", requestData.getPeripheral4ProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getPeripheral4ProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 4 SOFTWARE VERSION ", requestData.getPeripheral4SoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPeripheral4SoftwareVersion(), 8, ' '), 8);

        NtsUtils.log("PERIPHERAL 5 VENDOR", requestData.getPeripheral5Vendor());
        request.addRange(StringUtils.padRight(requestData.getPeripheral5Vendor(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 5 PRODUCT NAME or MODEL", requestData.getPeripheral5ProductNameOrModel());
        request.addRange(StringUtils.padRight(requestData.getPeripheral5ProductNameOrModel(), 15, ' '), 15);

        NtsUtils.log("PERIPHERAL 5 SOFTWARE VERSION ", requestData.getPeripheral5SoftwareVersion());
        request.addRange(StringUtils.padRight(requestData.getPeripheral5SoftwareVersion(), 8, ' '), 8);

        return request;
    }
}
