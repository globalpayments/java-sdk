package com.global.api.network.entities.nts;


import com.global.api.entities.enums.Target;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class POSSiteConfigurationData {
    private Target target = Target.NTS;
    private String messageVersion;
    private String transactionDate = DateTime.now().toString("YYYYMMdd");
    private String transactionTime = DateTime.now().toString("hhmmss");
    private String companyName;
    private String globalPaymentsCompanyId;
    private String merchantFranchiseName;
    private String merchantIdUnitPlusTid;
    private String merchantAddressStreet;
    private String merchantAddressCity;
    private String merchantAddressState;
    private String merchantAddressZip;
    private String merchantPhoneNumber;
    private String siteBrand;
    private String merchantType = "5541";
    private String posSystemType;
    private String methodOfOperation;
    private String posVendor;
    private String posProductNameOrModel;
    private String globalPaymentsPosTerminalType;
    private String globalPaymentsPosSoftwareVersion;
    private String globalPaymentsTerminalSpec = "N";
    private String globalPaymentsTerminalSpecVersion;
    private String globalPaymentsPaymentEngine = "V";
    private String globalPaymentsPaymentVertical = "C";
    private String posHardwareVersion;
    private String posSoftwareVersion;
    private String posOperatingSystem;
    private String middlewareVendor;
    private String middlewareProductNameOrModel;
    private String middlewareType;
    private String middlewareSoftwareVersion;
    private String receiptPrinterType;
    private String receiptPrinterModel;
    private String journalPrinterType;
    private String journalPrinterModel;
    private String insidePedMultiLaneDeviceType;
    private String insidePedMultiLaneDeviceVendor;
    private String insidePedMultiLaneDeviceProductNameOrModel;
    private String keyManagementSchemeInside = "D";
    private String pinEncryptionInside;
    private String outsidePedType;
    private String outsidePedVendor;
    private String outsidePedProductNameOrModel;
    private String keyManagementSchemeOutside = "D";
    private String pinEncryptionOutside;
    private String checkReaderVendor;
    private String checkReaderProductNameOrModel;
    private String insideContactlessReaderType;
    private String insideContactlessReaderVendor;
    private String insideContactlessReaderProductNameOrModel;
    private String outsideContactlessReaderType;
    private String outsideContactlessReaderVendor;
    private String outsideContactlessReaderProductNameOrModel;
    private String communicationMedia;
    private String communicationProtocol;
    private String internetBroadbandUse;
    private String datawireAccess;
    private String micronodeModelNumber;
    private String micronodeSoftwareVersion;
    private String modemRouterType;
    private String modemRouterVendor;
    private String modemRouterProductNameOrModel;
    private String modemPhoneNumber;
    private String globalPaymentsPrimaryDialNumberOrIpPort;
    private String globalPaymentsSecondaryDialNumberOrIpPort;
    private String dispenserInterfaceVendor;
    private String dispenserInterfaceProductNameOrModel;
    private String dispenserInterfaceSoftwareVersion;
    private String dispenserVendor;
    private String dispenserProductNameOrModel;
    private String dispenserSoftwareVersion;
    private String dispenserQuantity;
    private String numberOfScannersPeripherals;
    private String scanner1Vendor;
    private String scanner1ProductNameOrModel;
    private String scanner1SoftwareVersion;
    private String peripheral2Vendor;
    private String peripheral2ProductNameOrModel;
    private String peripheral2SoftwareVersion;
    private String peripheral3Vendor;
    private String peripheral3ProductNameOrModel;
    private String peripheral3SoftwareVersion;
    private String peripheral4Vendor;
    private String peripheral4ProductNameOrModel;
    private String peripheral4SoftwareVersion;
    private String peripheral5Vendor;
    private String peripheral5ProductNameOrModel;
    private String peripheral5SoftwareVersion;

    public POSSiteConfigurationData( Target target){
        this.target = target;
        if (this.target == Target.VAPS ){
            merchantType = "5542";
            globalPaymentsTerminalSpec = "P";
            globalPaymentsPaymentEngine = "V";
        }else if (this.target == Target.NWS ){
            merchantType = "5542";
            globalPaymentsTerminalSpec = "P";
            globalPaymentsPaymentEngine = "N";
        }
    }
}
