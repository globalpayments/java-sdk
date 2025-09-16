package com.global.api.utils;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.AdditionalTaxDetails;
import com.global.api.entities.CommercialLineItem;
import com.global.api.entities.CreditDebitIndicator;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransitRequestBuilder {
    private final ElementTree et;
    private final String root;
    private final Map<String, String> values;
    private String batchDeviceId;
    private String batchUserId;
    private Integer paymentCount;
    private Integer sequenceNumber;
    private List<CommercialLineItem> productDetails;
    private AdditionalTaxDetails additionalTaxDetails;
    private Boolean allowDuplicates;

    public TransitRequestBuilder(String root) {
        this.root = root;
        this.values = new HashMap<>();
        this.et = new ElementTree();
    }

    private String get(String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        }
        return null;
    }

    private boolean hasBatchParams() {
        return (!StringUtils.isNullOrEmpty(batchDeviceId) || !StringUtils.isNullOrEmpty(batchUserId));
    }

    private boolean hasPartialShipmentData() {
        return (sequenceNumber != null || paymentCount != null);
    }

    private boolean hasProductDetails() {
        return productDetails != null && !productDetails.isEmpty();
    }

    public TransitRequestBuilder set(String key, String value) {
        if (!StringUtils.isNullOrEmpty(value)) {
            values.put(key, value);
        }
        return this;
    }

    public TransitRequestBuilder set(String key, Integer value) {
        return set(key, value, 1);
    }

    public TransitRequestBuilder set(String key, Integer value, int length) {
        if (value != null) {
            String strValue = StringUtils.padLeft(value.toString(), length, '0');
            values.put(key, strValue);
        }
        return this;
    }

    public TransitRequestBuilder set(String key, Enum<?> value) {
        if (value != null) {
            values.put(key, value.toString());
        }
        return this;
    }

    public TransitRequestBuilder setAdditionalTaxDetails(AdditionalTaxDetails details) {
        this.additionalTaxDetails = details;
        return this;
    }

    public TransitRequestBuilder setBatchParams(String deviceId, String userId) {
        this.batchDeviceId = deviceId;
        this.batchUserId = userId;
        return this;
    }

    public TransitRequestBuilder setProductDetails(List<CommercialLineItem> productData) {
        this.productDetails = productData;
        return this;
    }

    public TransitRequestBuilder setPartialShipmentData(Integer sequenceNumber, Integer paymentCount) {
        this.sequenceNumber = sequenceNumber;
        this.paymentCount = paymentCount;
        return this;
    }

    public TransitRequestBuilder setAllowDuplicates(Boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        return this;
    }

    public <T extends TransactionBuilder<Transaction>> String buildRequest(T builder) {
        Element transaction = et.element(root);


        for (String element : buildRequestMap(builder)) {
            if ("productDetails".equals(element) && hasProductDetails()) {
                for (CommercialLineItem item : productDetails) {
                    Element productElement = et.subElement(transaction, "productDetails");

                    et.subElement(productElement, "productCode", item.getProductCode());
                    et.subElement(productElement, "productName", item.getName());
                    et.subElement(productElement, "price", StringUtils.toNumeric(item.getUnitCost()));
                    et.subElement(productElement, "quantity", item.getQuantity());
                    et.subElement(productElement, "measurementUnit", item.getUnitOfMeasure());

                    // discount details
                    if (item.getDiscountDetails() != null) {
                        Element discountDetails = et.subElement(productElement, "productDiscountDetails");
                        et.subElement(discountDetails, "productDiscountName", item.getDiscountDetails().getDiscountName());
                        et.subElement(discountDetails, "productDiscountAmount", StringUtils.toNumeric(item.getDiscountDetails().getDiscountAmount()));
                        et.subElement(discountDetails, "productDiscountPercentage", StringUtils.toNumeric(item.getDiscountDetails().getDiscountPercentage()));
                        et.subElement(discountDetails, "productDiscountType", item.getDiscountDetails().getDiscountType());
                        et.subElement(discountDetails, "priority", item.getDiscountDetails().getDiscountPriority());
                        et.subElement(discountDetails, "stackable", item.getDiscountDetails().getDiscountIsStackable() ? "YES" : "NO");
                    }

                    // tax details
                    if (item.getTaxAmount() != null) {
                        Element taxDetails = et.subElement(productElement, "productTaxDetails");
                        et.subElement(taxDetails, "productTaxName", item.getTaxName());
                        et.subElement(taxDetails, "productTaxAmount", StringUtils.toNumeric(item.getTaxAmount()));
                        et.subElement(taxDetails, "productTaxPercentage", StringUtils.toNumeric(item.getTaxPercentage()));
                    }

                    et.subElement(productElement, "productNotes", item.getDescription());
                    et.subElement(productElement, "productCommodityCode", item.getCommodityCode());
                    et.subElement(productElement, "alternateTaxID", item.getAlternateTaxId());
                    if (CreditDebitIndicator.Credit.equals(item.getCreditDebitIndicator())) {
                        et.subElement(productElement, "creditIndicator", "YES");
                    }
                }
            } else if ("additionalTaxDetails".equals(element) && additionalTaxDetails != null) {
                Element taxDetails = et.subElement(transaction, "additionalTaxDetails");
                et.subElement(taxDetails, "taxType", additionalTaxDetails.getTaxType());
                et.subElement(taxDetails, "taxAmount", StringUtils.toNumeric(additionalTaxDetails.getTaxAmount()));
                et.subElement(taxDetails, "taxRate", StringUtils.toNumeric(additionalTaxDetails.getTaxRate()));
                et.subElement(taxDetails, "taxCategory", EnumUtils.getMapping(Target.Transit, additionalTaxDetails.getTaxCategory()));
            } else if ("partialShipmentData".equals(element) && hasPartialShipmentData()) {
                Element paymentDetails = et.subElement(transaction, "partialShipmentData");
                et.subElement(paymentDetails, "currentPaymentSequenceNumber", sequenceNumber);
                et.subElement(paymentDetails, "totalPaymentCount", paymentCount);
            } else if ("batchCloseParameter".equals(element) && hasBatchParams()) {
                Element batchParams = et.subElement(transaction, "batchCloseParameter");
                et.subElement(batchParams, "currentPaymentSequenceNumber", batchDeviceId);
                et.subElement(batchParams, "totalPaymentCount", batchUserId);
            } else {
                et.subElement(transaction, element, get(element));
            }
        }

        if (Boolean.TRUE.equals(allowDuplicates)) {
            et.subElement(transaction, "skipDuplicateCheck", "Y");
        }
        return et.toString(transaction);
    }

    private <T extends TransactionBuilder<Transaction>> LinkedList<String> buildRequestMap(T builder) {
        switch (builder.getTransactionType()) {
            case Auth:
            case Sale:
                if (builder.getPaymentMethod() instanceof com.global.api.paymentMethods.Debit) {
                    return buildList("deviceID|transactionKey|manifest|cardDataSource|transactionAmount|tip|salesTax|additionalTaxDetails|currencyCode|track2Data|track3Data|emulatedTrackData|emvTags|emvFallbackCondition|lastChipRead|paymentAppVersion|emcContactlessToContactChip|pin|pinKsn|secureCode|digitalPaymentCryptogram|programProtocol|directoryServerTransactionID|paymentAccountReference|panReferenceIdentifier|nfcTags|ksn|transactionMID|externalReferenceID|operatorID|orderNumber|cardOnFile|merchantReportID|encryptionType|tokenRequired|healthCareAccountType|prescriptionAmount|visionAmount|dentalAmount|clinicAmount|isQualifiedIIAS|rxNumber|couponID|providerID|providerToken|locationID|notifyEmailID|customerCode|firstName|lastName|transTotalDiscountAmount|transDiscountName|transDiscountAmount|transDiscountPercentage|priority|stackable|productDetails|productDiscountName|productDiscountAmount|productDiscountPercentage|productDiscountType|priority|stackable|productTaxName|productTaxAmount|productTaxPercentage|productTaxType|productVariation|modifierName|modifierValue|modifierPrice|productNotes|softDescriptor|developerID|registeredUserIndicator|lastRegisteredChangeDate|laneID|authorizationIndicator|terminalCapability|terminalOperatingEnvironment|cardholderAuthenticationMethod|terminalAuthenticationCapability|terminalOutputCapability|maxPinLength|terminalCardCaptureCapability|cardholderPresentDetail|cardPresentDetail|cardDataInputMode|cardholderAuthenticationEntity|cardDataOutputCapability|splitTenderPayment|splitTenderID|splitTenderConsolidatedReceipt|noIndividualTransactionReceipt");
                }
                return buildList("deviceID|transactionKey|manifest|cardDataSource|transactionAmount|tip|salesTax|additionalTaxDetails|shippingCharges|dutyCharges|surcharge|additionalAmountType|additionalAmount|additionalAmountSign|currencyCode|cardNumber|expirationDate|cvv2|track1Data|track2Data|track3Data|emulatedTrackData|cardHolderName|secureCode|securityProtocol|ucafCollectionIndicator|digitalPaymentCryptogram|programProtocol|directoryServerTransactionID|paymentAccountReference|panReferenceIdentifier|eciIndicator|cardOnFileTransactionIdentifier|emvTags|pin|pinKsn|emvFallbackCondition|lastChipRead|paymentAppVersion|emvContactlessToContactChip|nfcTags|walletSource|checkOutID|addressLine1|zip|transactionMID|externalReferenceID|operatorID|orderNumber|cardOnFile|merchantReportID|encryptionType|ksn|tokenRequired|healthCareAccountType|prescriptionAmount|visionAmount|dentalAmount|clinicAmount|isQualifiedIIAS|rxNumber|couponID|providerID|providerToken|locationID|notifyEmailID|orderID|customerCode|firstName|lastName|customerPhone|transTotalDiscountAmount|transDiscountName|transDiscountAmount|transDiscountPercentage|priority|stackable|productDetails|productDiscountName|productDiscountAmount|productDicsountPercentage|productDiscountType|priority|stackable|productTaxName|productTaxAmount|productTaxPercentage|productTaxType|productVariation|modifierName|modifierValue|modifierPrice|productNotes|productDiscountIndicator|orderNotes|orderServiceTimestamp|commercialCardLevel|purchaseOrder|chargeDescriptor|customerVATNumber|customerRefID|orderDate|summaryCommodityCode|vatInvoice|chargeDescriptor2|chargeDescriptor3|chargeDescriptor4|supplierReferenceNumber|shipFromZip|shipToZip|destinationCountryCode|orderID|tokenRequesterID|softDescriptor|terminalCapability|terminalOperatingEnvironment|cardholderAuthenticationMethod|terminalAuthenticationCapability|terminalOutputCapability|maxPinLength|terminalCardCaptureCapability|cardholderPresentDetail|cardPresentDetail|cardDataInputMode|cardholderAuthenticationEntity|cardDataOutputCapability|mPosAcceptanceDeviceType|developerID|paymentFacilitatorIdentifier|paymentFacilitatorName|subMerchantIdentifier|subMerchantName|subMerchantCountryCode|subMerchantStateCode|subMerchantCity|subMerchantPostalCode|subMerchantEmailId|subMerchantPhone|isoIdentifier|isRecurring|billingType|paymentCount|currentPaymentCount|isoIdentifier|registeredUserIndicator|lastRegisteredChangeDate|laneID|authorizationIndicator|splitTenderPayment|splitTenderID|splitTenderConsolidatedReceipt|noIndividualTransactionReceipt");

            case Balance:
                return buildList("deviceID|transactionKey|manifest|cardDataSource|currencyCode|track1Data|track2Data|track3Data|emulatedTrackData|cardNumber|expirationDate|cvv2|cardHolderName|secureCode|securityProtocol|ucafCollectionIndicator|digitalPaymentCryptogram|programProtocol|directoryServerTransactionID|paymentAccountReference|panReferenceIdentifier|eciIndicator|cardOnFileTransactionIdentifier|nfcTags|walletSource|checkOutID|dtvv|addressLine1|zip|transactionMID|externalReferenceID|operatorID|orderNumber|cardOnFile|encryptionType|ksn|tokenRequired|customerCode|firstName|lastName|customerPhone|tokenRequesterID|softDescriptor|developerID|laneID|terminalCapability|terminalOperatingEnvironment|cardholderAuthenticationMethod|terminalAuthenticationCapability|terminalOutputCapability|maxPinLength|terminalCardCaptureCapability|cardholderPresentDetail|cardPresentDetail|cardDataInputMode|cardholderAuthenticationEntity|cardDataOutputCapability|mPosAcceptanceDeviceType");

            case BatchClose:
                return buildList("deviceID|transactionKey|manifest|operatingUserID|batchCloseParameter");

            case Capture:
                return buildList("deviceID|transactionKey|manifest|transactionAmount|tip|salesTax|additionalTaxDetails|shippingCharges|dutyCharges|surcharge|additionalAmountType|additionalAmount|additionalAmountSign|transactionID|externalReferenceID|operatorID|isPartialShipment|partialShipmentData|softDescriptor|merchantReportID|customerCode|firstName|lastName|transTotalDiscountAmount|transDiscountName|transDiscountAmount|transDiscountPercentage|priority|stackable|productDetails|productDiscountName|productDiscountAmount|productDicsountPercentage|productDiscountType|priority|stackable|productTaxName|productTaxAmount|productTaxPercentage|productTaxType|productVariation|modifierName|modifierValue|modiferPrice|productNotes|productDiscountIndicator|orderNotes|orderServiceTimestamp|commercialCardLevel|purchaseOrder|chargeDescriptor|customerVATNumber|customerRefID|orderDate|summaryCommodityCode|vatInvoice|chargeDescriptor2|chargeDescriptor3|chargeDescriptor4|supplierReferenceNumber|shipFromZip|shipToZip|destinationCountryCode|developerID|paymentFacilitatorIdentifier|paymentFacilitatorName|subMerchantIdentifier|subMerchantName|subMerchantCountryCode|subMerchantStateCode|subMerchantCity|subMerchantPostalCode|subMerchantEmailId|subMerchantPhone");
            case Edit:
                return buildList("deviceID|transactionKey|tip|transactionID|operatorID|softDescriptor|merchantReportID|developerID");
            case Refund:
                return buildList("deviceID|transactionKey|manifest|cardDataSource|transactionAmount|tip|salesTax|additionalTaxDetails|shippingCharges|dutyCharges|surcharge|additionalAmountType|additionalAmount|additionalAmountSign|currencyCode|cardNumber|expirationDate|cvv2|track1Data|track2Data|track3Data|emulatedTrackData|cardHolderName|secureCode|securityProtocol|ucafCollectionIndicator|digitalPaymentCryptogram|programProtocol|directoryServerTransactionID|paymentAccountReference|panReferenceIdentifier|eciIndicator|cardOnFileTransactionIdentifier|emvTags|emvFallbackCondition|lastChipRead|paymentAppVersion|emvContactlessToContactChip|pin|pinKsn|terminalCapability|terminalOperatingEnvironment|cardholderAuthenticationMethod|terminalAuthenticationCapability|terminalOutputCapability|maxPinLength|terminalCardCaptureCapability|cardholderPresentDetail|cardPresentDetail|cardDataInputMode|cardholderAuthenticationEntity|cardDataOutputCapability|mPosAcceptanceDeviceType|nfcTags|operatorID|ksn|encryptionType|tokenRequired|customerCode|firstName|lastName|customerPhone|productDetails|productTaxName|productTaxAmount|productTaxPercentage|productVariation|modifierName|modifierValue|modifierPrice|productNotes|purchaseOrder|softDescriptor|merchantReportID|orderNumber|transactionID|externalReferenceID|orderID|tokenRequesterID|developerID");
            case Reversal:
            case Void:
                return buildList("deviceID|transactionKey|manifest|transactionAmount|tip|salesTax|additionalTaxDetails|shippingCharges|surcharge|currencyCode|transactionID|externalReferenceID|operatorID|tokenRequired|productDetails|productTaxName|productTaxAmount|productTaxPercentage|productVariation|modifierName|modifierValue|modifierPrice|productNotes|developerID|voidReason|laneID|achCancelNote");

            case Verify:
                return buildList("deviceID|transactionKey|manifest|cardDataSource|currencyCode|track1Data|track2Data|track3Data|emulatedTrackData|cardNumber|expirationDate|cvv2|cardHolderName|secureCode|securityProtocol|ucafCollectionIndicatordigitalPaymentCryptogram|programProtocol|directoryServerTransactionID||paymentAccountReference|panReferenceIdentifier|eciIndicator|nfcTags|walletSource|checkOutID|addressLine1|zip|transactionMID|externalReferenceID|operatorID|orderNumber|cardOnFile|merchantReportID|encryptionType|ksn|tokenRequired|customerCode|firstName|lastName|tokenRequesterID|softDescriptor|developerID|laneID|terminalCapability|terminalOperatingEnvironment|cardholderAuthenticationMethod|terminalAuthenticationCapability|terminalOutputCapability|maxPinLength|terminalCardCaptureCapability|cardholderPresentDetail|cardPresentDetail|cardDataInputMode|cardholderAuthenticationEntity|cardDataOutputCapability|mPosAcceptanceDeviceType");

            case Tokenize:
                return buildList("deviceID|transactionKey|cardDataSource|cardNumber|expirationDate|cardHolderName|cardVerification|developerID");

            default:
                throw new UnsupportedOperationException("Unsupported transaction type: " + builder.getTransactionType());
        }
    }

    private LinkedList<String> buildList(String str) {
        LinkedList<String> list = new LinkedList<>();

        String[] values = str.split("\\|");
        for (String value : values) {
            if (!StringUtils.isNullOrEmpty(value)) {
                list.add(value);
            }
        }

        return list;
    }
}