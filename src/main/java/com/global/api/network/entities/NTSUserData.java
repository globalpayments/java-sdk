package com.global.api.network.entities;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import com.global.api.network.elements.DE63_ProductDataEntry;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.EmvData;
import com.global.api.utils.EmvUtils;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class NTSUserData {
    private static final String WEX_FALLBACK="FALLBACK";
    private NTSUserData() {
        throw new IllegalStateException("NTSUserData.class");
    }

    public static String getBankCardUserData(TransactionBuilder<Transaction> builder, IPaymentMethod paymentMethod,
                                             NTSCardTypes cardType, NtsMessageCode messageCode, AcceptorConfig acceptorConfig) {

        TransactionModifier modifier = builder.getTransactionModifier();
        TransactionType transactionType = builder.getTransactionType();

        String cvn = null;
        if (paymentMethod instanceof ICardData) {
            cvn = ((ICardData) paymentMethod).getCvn();
            cvn = StringUtils.padRight(cvn, 4, ' ');
        }

        boolean isAVSUsed = StringUtils.isNullOrEmpty(cvn);

        String uniqueDeviceId = StringUtils.padRight(builder.getUniqueDeviceId(), 4, ' ');

        StringBuilder sb = new StringBuilder();

        String amount = StringUtils.toNumeric(builder.getAmount());
        if ((cardType.equals(NTSCardTypes.MastercardFleet) || cardType.equals(NTSCardTypes.VisaFleet))
                && (transactionType.equals(TransactionType.DataCollect) || transactionType.equals(TransactionType.Capture))) {
                sb.append(getFleetDataTag08(builder.getFleetData(), cardType));
            if (builder.getNtsProductData() != null) {
                sb.append(getProductDataTag09(builder, cardType));
            }
            return sb.append("?").toString();
        }

        int totalNoOfTags = 0; // Tag counter.

        // 01 Function Code
        if (!StringUtils.isNullOrEmpty(builder.getTagData()) || transactionType.equals(TransactionType.Void) || transactionType.equals(TransactionType.Balance)) {
            String functionCode = null;
            if ((messageCode == NtsMessageCode.DataCollectOrSale && builder.getTransactionModifier() == TransactionModifier.Offline) ||
                    messageCode == NtsMessageCode.ForceCollectOrForceSale)
                functionCode = FunctionCode.OfflineApprovedSaleAdvice.getValue();
            else if (messageCode == NtsMessageCode.AuthorizationOrBalanceInquiry && modifier != TransactionModifier.ChipDecline) {
                BigDecimal tranAmount = StringUtils.toFractionalAmount(amount);
                if (tranAmount.equals(new BigDecimal(0))) {
                    functionCode = FunctionCode.BalanceInquiry.getValue();
                }
            } else if (modifier == TransactionModifier.ChipDecline && messageCode == NtsMessageCode.AuthorizationOrBalanceInquiry) {
                functionCode = FunctionCode.OfflineDeclineAdvice.getValue();
            } else if (messageCode == NtsMessageCode.ReversalOrVoid ||
                    messageCode == NtsMessageCode.ForceReversalOrForceVoid) {
                functionCode = FunctionCode.Void.getValue();
            }
            if (!StringUtils.isNullOrEmpty(functionCode)) {
                totalNoOfTags++; // Increment the counter if tag is used.
                sb.append(UserDataTag.FunctionCode.getValue()).append("\\");
                sb.append(functionCode).append("\\");
            }
        }


        // 02 TerminalCapability
        if (acceptorConfig.hasPosConfiguration_BankcardData()) {
            sb.append(UserDataTag.TerminalCapability.getValue()).append("\\");
            sb.append(acceptorConfig.getTerminalCapabilityForBankcard()).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }
        // 03 Stan
        if ((messageCode == NtsMessageCode.ReversalOrVoid || messageCode == NtsMessageCode.ForceReversalOrForceVoid)
                && paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            sb.append(UserDataTag.Stan.getValue()).append("\\");
            sb.append(reference.getSystemTraceAuditNumber()).append("\\"); // Get from host response area
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // 04 and 05 is expected in host response are not in request.
        // 06

        // 07 ZipCode
        if (!(cardType.equals(NTSCardTypes.MastercardFleet) || cardType.equals(NTSCardTypes.VisaFleet))
                && isAVSUsed
                && ((transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale))
                && builder.getZipCode() != null)) {
            sb.append(UserDataTag.ZipCode.getValue()).append("\\");
            sb.append(StringUtils.padRight(builder.getZipCode(), 9, ' ')).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }


        // 08 FleetAuthData
        if (cardType.equals(NTSCardTypes.MastercardFleet) || cardType.equals(NTSCardTypes.VisaFleet)) {
            FleetData fleetData = builder.getFleetData();
            if ((transactionType.equals(TransactionType.Auth) ||
                    transactionType.equals(TransactionType.Sale))
                    && fleetData != null) {
                sb.append(UserDataTag.FleetAuthData.getValue()).append("\\");
                sb.append(getFleetDataTag08(fleetData, cardType));
                sb.append("\\");
                totalNoOfTags++;
            }
        }

        //09
        if ((cardType.equals(NTSCardTypes.MastercardFleet) || cardType.equals(NTSCardTypes.VisaFleet))
                && transactionType.equals(TransactionType.Sale)
                && builder.getNtsProductData() != null) {
            sb.append(UserDataTag.ProductDataTag.getValue()).append("\\");
            sb.append(getProductDataTag09(builder, cardType));
            sb.append("?");
            sb.append("\\"); // Added separator
            totalNoOfTags++;

        } else if ((cardType.equals(NTSCardTypes.Mastercard)
                || cardType.equals(NTSCardTypes.Visa)
                || cardType.equals(NTSCardTypes.AmericanExpress)
                || cardType.equals(NTSCardTypes.Discover))
                && (transactionType.equals(TransactionType.Sale)
                && builder.getNtsProductData() != null)) {
            sb.append(UserDataTag.ProductDataTag.getValue()).append("\\");
            sb.append(getProductDataTag09(builder, cardType));
            sb.append("\\"); // Added separator
            totalNoOfTags++;

        }

        // 10 Reserved

        //11 MasterCardBanknetRefId & 12 Settlement Date
        if ((cardType.equals(NTSCardTypes.Mastercard) || cardType.equals(NTSCardTypes.MastercardFleet)|| cardType.equals(NTSCardTypes.MastercardPurchasing))
                && (transactionType.equals(TransactionType.Void) ||
                transactionType.equals(TransactionType.Balance))
                && (paymentMethod instanceof TransactionReference)) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            sb.append(UserDataTag.MasterCardBanknetRefId.getValue()).append("\\");
            sb.append(reference.getMastercardBanknetRefNo()).append("\\"); // Get from host response area
            totalNoOfTags++; // Increment the counter if tag is used.
            sb.append(UserDataTag.MasterCardSettlementDate.getValue()).append("\\"); // 12 Settlement Date
            sb.append(reference.getMastercardBanknetSettlementDate()).append("\\"); // Get from host response area
            totalNoOfTags++; // Increment the counter if tag is used.
        }


        // 13 Cvn
        if ((cardType.equals(NTSCardTypes.Mastercard)
                || cardType.equals(NTSCardTypes.Visa)
                || cardType.equals(NTSCardTypes.Discover)
                || cardType.equals(NTSCardTypes.AmericanExpress))
                && !isAVSUsed
                && (transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Sale)
                || messageCode.equals(NtsMessageCode.AuthorizationOrBalanceInquiry))) {
            sb.append(UserDataTag.Cvn.getValue()).append("\\");
            sb.append(cvn).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.

        }

        // 14 Discover Network Ref Id
        if (((cardType.equals(NTSCardTypes.Discover) || cardType.equals(NTSCardTypes.PayPal)) && (messageCode == NtsMessageCode.ReversalOrVoid ||
                messageCode == NtsMessageCode.ForceReversalOrForceVoid)) && (paymentMethod instanceof TransactionReference)) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            sb.append(UserDataTag.DiscoverNetworkRefId.getValue()).append("\\");
            sb.append(reference.getDiscoverNetworkRefId() + "\\"); // Get from host response area
            totalNoOfTags++; // Increment the counter if tag is used.

        }

        // 15 Reserved

        // 16
        if (builder.getNtsTag16() != null) {
            sb.append(UserDataTag.Tag16.getValue()).append("\\");
            sb.append(getTagData16(builder.getNtsTag16())).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        //17 Card Sequence No // Only for EMV
        if (!StringUtils.isNullOrEmpty(builder.getTagData()) && builder.getCardSequenceNumber() != null) {
            sb.append(UserDataTag.CardSequenceNumber.getValue()).append("\\");
            sb.append(builder.getCardSequenceNumber()).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // 18 Visa Transaction Id
        if ((cardType.equals(NTSCardTypes.Visa) || cardType.equals(NTSCardTypes.VisaFleet)) && transactionType.equals(TransactionType.Void)
                && (paymentMethod instanceof TransactionReference)) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            sb.append(UserDataTag.VisaTransactionId.getValue()).append("\\");
            sb.append(reference.getVisaTransactionId()).append("\\"); // Get from host response area (left justify)
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // 19

        // 20 Cash Over Amount
        if ((cardType.equals(NTSCardTypes.Discover))
                && (transactionType.equals(TransactionType.Sale)
                && builder.getCashBackAmount() != null)) {
            sb.append(UserDataTag.CashOverAmount.getValue()).append("\\");
            sb.append(StringUtils.toNumeric(builder.getCashBackAmount(), 6)).append("\\"); // Check desc
            totalNoOfTags++; // Increment the counter if tag is used.

        }

        // 21 Unique Device Id // Only for EMV
        if (!transactionType.equals(TransactionType.Void) && !StringUtils.isNullOrEmpty(uniqueDeviceId)) {
            sb.append(UserDataTag.UniqueDeviceId.getValue()).append("\\");
            sb.append(uniqueDeviceId).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // 22 Emv Pin Block // Only for EMV, 23 Emv Ksn // Only for EMV,  24 Emv Max Pin Entry // Only for EMV & 25 Emv Chip Auth Code
        if (!transactionType.equals(TransactionType.Void) && !StringUtils.isNullOrEmpty(builder.getTagData())) {
            if (messageCode.equals(NtsMessageCode.AuthorizationOrBalanceInquiry) || messageCode.equals(NtsMessageCode.DataCollectOrSale)) {
                if (paymentMethod instanceof IPinProtected) {
                    String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();
                    if (!StringUtils.isNullOrEmpty(pinBlock)) {
                        sb.append(UserDataTag.EmvPinBlock.getValue()).append("\\"); // 22 Emv Pin Block
                        sb.append(pinBlock.substring(0,16)).append("\\");
                        totalNoOfTags++; // Increment the counter if tag is used.
                    }
                }
                if (paymentMethod instanceof IPinProtected) {
                    String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();
                    if (pinBlock != null) {
                        sb.append(UserDataTag.EmvKsn.getValue()).append("\\"); // 23 Emv Ksn // Only for EMV
                        sb.append(StringUtils.padLeft(pinBlock.substring(16), 20, ' ')).append("\\");
                        totalNoOfTags++; // Increment the counter if tag is used.
                    }
                }
                if (builder.getEmvMaxPinEntry() != null) {
                    sb.append(UserDataTag.EmvMaxPinEntry.getValue()).append("\\"); // 24 Emv Max Pin Entry
                    sb.append(builder.getEmvMaxPinEntry()).append("\\");
                    totalNoOfTags++; // Increment the counter if tag is used.
                }
            }

            if (modifier == TransactionModifier.Offline
                    || modifier == TransactionModifier.ChipDecline) {
                sb.append(UserDataTag.EmvChipAuthCode.getValue()).append("\\"); // 25 Emv Chip Auth Code
                if (messageCode == NtsMessageCode.DataCollectOrSale ||
                        messageCode == NtsMessageCode.ForceCollectOrForceSale)
                    sb.append(EmvAuthCode.OfflineApproved.getValue()).append("\\");
                else if (messageCode == NtsMessageCode.AuthorizationOrBalanceInquiry)
                    sb.append(EmvAuthCode.OfflineDeclined.getValue()).append("\\");
                else if (messageCode == NtsMessageCode.ReversalOrVoid ||
                        messageCode == NtsMessageCode.ForceReversalOrForceVoid)
                    sb.append(EmvAuthCode.UnableToGoOnlineOfflineApproved.getValue()).append("\\");
                else
                    sb.append(EmvAuthCode.UnableToGoOnlineOfflineDeclined.getValue()).append("\\");
                totalNoOfTags++; // Increment the counter if tag is used.
            }
        }

        // 26 Goods Sold
        if (cardType.equals(NTSCardTypes.AmericanExpress) && (transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale))) {
            sb.append(UserDataTag.GoodsSold.getValue()).append("\\");
            sb.append(((AuthorizationBuilder) builder).getGoodsSold()).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }


        // 27 Reserved

        // 28 Ecommerce Data1
        if ((cardType.equals(NTSCardTypes.Visa) || cardType.equals(NTSCardTypes.VisaFleet) ||
                cardType.equals(NTSCardTypes.AmericanExpress) || cardType.equals(NTSCardTypes.Discover) || cardType.equals(NTSCardTypes.PayPal)) &&
                ((transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale))
                        && builder.getEcommerceData1() != null)) {
            sb.append(UserDataTag.EcommerceData1.getValue()).append("\\");
            sb.append(builder.getEcommerceData1()).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.

        }

        // 29 Ecommerce Data2
        if ((cardType.equals(NTSCardTypes.Visa) || cardType.equals(NTSCardTypes.VisaFleet) ||
                cardType.equals(NTSCardTypes.AmericanExpress) || cardType.equals(NTSCardTypes.Discover) || cardType.equals(NTSCardTypes.PayPal))
                && ((transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale)) && builder.getEcommerceData2() != null)) {
            sb.append(UserDataTag.EcommerceData2.getValue()).append("\\");
            sb.append(builder.getEcommerceData2()).append("\\");
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // 30 MCUCAF // For E-com entry methods, 31 MCWalletId // For all E-com entry methods & 32 MCSLI // For all E-com entry methods
        if (((cardType.equals(NTSCardTypes.Mastercard) || cardType.equals(NTSCardTypes.MastercardFleet) ||
                cardType.equals(NTSCardTypes.MastercardPurchasing)) && (transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale)))
                && (paymentMethod instanceof ITrackData)) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), acceptorConfig.getOperatingEnvironment());
            if (entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataAttended ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedAfd ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedCat ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattended) {
                sb.append(UserDataTag.MCUCAF.getValue()).append("\\");
                sb.append("" + "\\");
                totalNoOfTags++; // Increment the counter if tag is used.
                sb.append(UserDataTag.MCWalletId.getValue()).append("\\"); // 31 MCWalletId // For all E-com entry methods
                sb.append("" + "\\");
                totalNoOfTags++; // Increment the counter if tag is used.
                sb.append(UserDataTag.MCSLI.getValue()).append("\\"); // 32 MCSLI // For all E-com entry methods
                sb.append("" + "\\");
                totalNoOfTags++; // Increment the counter if tag is used.
            }
        }


        // 33 Ecommerce Auth Indicator & 34 Ecommerce Merchant Order No
        if ((transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale)) && ((cardType.equals(NTSCardTypes.VisaFleet)) || (cardType.equals(NTSCardTypes.MastercardFleet))) && (paymentMethod instanceof ITrackData)) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), acceptorConfig.getOperatingEnvironment());
            if (entryMethod == NTSEntryMethod.ECommerceNoTrackDataAttended ||
                    entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattendedAfd ||
                    entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattended ||
                    entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattendedCat ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataAttended ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedAfd ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedCat ||
                    entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattended) {
                sb.append(UserDataTag.EcommerceAuthIndicator.getValue() + "\\"); // 33 Ecommerce Auth Indicator // For all E-com entry methods
                sb.append(builder.getEcommerceAuthIndicator() + "\\");
                totalNoOfTags++; // Increment the counter if tag is used.

                if (builder.getInvoiceNumber() != null) {
                    sb.append(UserDataTag.EcommerceMerchantOrderNumber.getValue() + "\\"); // 34 Ecommerce Merchant Order No
                    sb.append(builder.getInvoiceNumber() + "\\");
                    totalNoOfTags++; // Increment the counter if tag is used.
                }
            }
        }


        // 99 Integrated Circuit Card
        if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
            sb.append(UserDataTag.IntegratedCircuitCard.getValue()).append("\\");
            if (builder.getTagData()!=null && builder.getTagData().contains("\\99\\FALLBACK")){
                sb.append(builder.getTagData().substring(4,builder.getTagData().length()).toUpperCase());
            }else {
                EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), true);
                sb.append(tagData.getAcceptedTagData()); // Check EMV fallback
            }
            totalNoOfTags++; // Increment the counter if tag is used.
        }

        // Removing the unwanted '\' char.
        if (sb.charAt(sb.length() - 1) == '\\') {
            sb.delete(sb.length() - 1, sb.length());
        }

        // Adding the number of tags.
        return StringUtils.padLeft(totalNoOfTags, 2, '0') + "\\" + sb.toString();
    }

    private static int mapServiceByCardType(ServiceLevel serviceLevel, NTSCardTypes ntsCardTypes) {
        switch (ntsCardTypes) {
            case VoyagerFleet:
                return mapServiceVoyager(serviceLevel);
            case WexFleet:
                return mapServiceWexFleet(serviceLevel);
            case FleetWide:
            case FuelmanFleet:
            case MastercardFleet:
                return mapService(serviceLevel);
            default:
                return 0;
        }
    }

    public static int mapService(ServiceLevel serviceLevel) {
        switch (serviceLevel) {
            case SelfServe:
                return 1;
            case FullServe:
                return 2;
            case Other_NonFuel:
                return 3;
            case NoFuelPurchased:
            default:
                return 0;
        }
    }

    public static int mapServiceWexFleet(ServiceLevel serviceLevel) {
        switch (serviceLevel) {
            case FullServe:
                return 01;
            case SelfServe:
                return 02;
            case NoFuelPurchased:
            default:
                return 0;

        }
    }


    public static int mapServiceVoyager(ServiceLevel serviceLevel) {
        switch (serviceLevel) {
            case FullServe:
                return 1;
            case Other:
                return 2;
            case SelfServe:
                return 0;
            case Unknown:
            default:
                return 9;
        }
    }

    public static int mapUnitMeasure(UnitOfMeasure unitOfMeasure) {
        switch (unitOfMeasure) {
            case CaseOrCarton:
                return 1;
            case Gallons:
                return 2;
            case Kilograms:
                return 3;
            case Liters:
                return 4;
            case Pounds:
                return 5;
            case Quarts:
                return 6;
            case Units:
                return 7;
            case Ounces:
                return 8;
            case OtherOrUnknown:
            default:
                return 0;
        }
    }

    public static int mapUnitMeasureFleet(UnitOfMeasure unitOfMeasure) {
        switch (unitOfMeasure) {
            case Gallons:
                return 1;
            case Liters:
                return 2;
            case Pounds:
                return 3;
            case Kilograms:
                return 4;
            case ImperialGallons:
                return 5;
            case NoFuelPurchased:
            default:
                return 0;
        }
    }


    public static String getTagData16(NtsTag16 ntsTag16) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.padLeft(String.valueOf(ntsTag16.getPumpNumber()), 2, '0')); // Pump Number
        sb.append(StringUtils.padLeft(String.valueOf(ntsTag16.getWorkstationId()), 2, '0')); // Workstation Id
        if(ntsTag16.getTimeStamp()!=null) {
            sb.append(new SimpleDateFormat("MMddyy").format(ntsTag16.getTimeStamp()));
            sb.append(new SimpleDateFormat("HHmmss").format(ntsTag16.getTimeStamp()));
        }else{
            sb.append(DateTime.now(DateTimeZone.UTC).toString("MMddyy"));
            sb.append(DateTime.now(DateTimeZone.UTC).toString("HHmmss"));
        }
        sb.append(ntsTag16.getServiceCode().getValue()); // Service Code
        sb.append(ntsTag16.getSecurityData().getValue()); // Security Data

        return sb.toString();
    }

    public static StringBuilder getFleetDataTag08(FleetData fleetData, NTSCardTypes ntsCardTypes) {
        StringBuilder sb = new StringBuilder();
        switch (ntsCardTypes) {
            case VisaFleet:
                if(fleetData!=null) {
                    if (fleetData.getOdometerReading() != null) {
                        sb.append(StringUtils.padLeft(fleetData.getOdometerReading() != null ? fleetData.getOdometerReading() : "", 7, '0'));
                    } else{
                        sb.append(String.format("%07d",0));
                    }
                        if(fleetData.getDriverId() != null || fleetData.getVehicleNumber() != null || fleetData.getVehicleNumber() != null){
                            if (fleetData.getDriverId() != null) {
                                sb.append(StringUtils.padRight(fleetData.getDriverId(), 17, ' '));
                            } else if (fleetData.getVehicleNumber() != null) {
                                sb.append(StringUtils.padRight(fleetData.getVehicleNumber(), 17, ' '));
                            } else if (fleetData.getGenericIdentificationNo() != null) {
                                sb.append(StringUtils.padRight(fleetData.getGenericIdentificationNo(), 17, ' '));
                            }
                        }else{
                            sb.append(StringUtils.padRight("", 17, ' '));
                        }
                }else {
                    sb.append(String.format("%07d",0));
                    sb.append(StringUtils.padRight("", 17, ' '));
                }
                return sb;

            case MastercardFleet:
                if(fleetData!=null) {
                    if (fleetData.getOdometerReading() != null) {
                        sb.append(StringUtils.padLeft(fleetData.getOdometerReading(), 7, '0')); // check in mastercard fleet & visa fleet chapter user data
                    } else {
                        sb.append(String.format("%7s", " "));
                    }
                    if (fleetData.getDriverId() != null) {
                        sb.append(StringUtils.padLeft(fleetData.getDriverId(), 6, '0'));
                    } else {
                        sb.append(String.format("%6s", " "));
                    }
                    if (fleetData.getVehicleNumber() != null) {
                        sb.append(StringUtils.padLeft(fleetData.getVehicleNumber(), 6, '0'));
                    } else {
                        sb.append(String.format("%6s", " "));
                    }
                }else {
                    sb.append(StringUtils.padRight("", 7, ' '));
                    sb.append(StringUtils.padRight("", 6, ' '));
                    sb.append(StringUtils.padRight("", 6, ' '));
                }

                return sb;
            default:
                return null;
        }
    }

    public static StringBuilder getProductDataTag09(TransactionBuilder builder, NTSCardTypes ntsCardTypes) {
        StringBuilder sb = new StringBuilder();
        NtsProductData productData = builder.getNtsProductData();
        List<DE63_ProductDataEntry> fuel = productData.getFuelDataEntries();
        List<DE63_ProductDataEntry> nonFuel = productData.getNonFuelDataEntries();
        PurchaseType purchaseType = productData.getPurchaseType();
        ServiceLevel serviceLevelVisaFleet = productData.getServiceLevel();
        boolean fuelFlag = fuel != null;
        int serviceLevel = mapServiceByCardType(productData.getServiceLevel(), ntsCardTypes);
        switch (ntsCardTypes) {
            case VisaFleet:
                sb.append(purchaseType.getValue());
                addProductDataForSimilarFuelProducts(fuel);
                for (int i = 0; i < 1; i++) {
                    if (fuelFlag && i < fuel.size()) {
                        sb.append(StringUtils.padLeft(fuel.get(i).getCode(), 2, ' '));
                        sb.append(StringUtils.padLeft(purchaseType.equals(PurchaseType.Fuel) ? fuel.get(i).getUnitOfMeasure().getValue() : " ", 1, ' '));
                        sb.append(StringUtils.toNumeric(fuel.get(i).getQuantity(), 6));
                        sb.append(StringUtils.toFormatDigit(fuel.get(i).getPrice(), 5, 3));
                        sb.append(StringUtils.toNumeric(fuel.get(i).getAmount(), 9));
                    } else {
                        sb.append(String.format("%2s", " "));
                        sb.append(String.format("%1s", " "));
                        sb.append(String.format("%06d", 0));
                        sb.append(String.format("%05d", 0));
                        sb.append(String.format("%09d", 0));
                    }
                }
                sb.append(serviceLevelVisaFleet.getValue());
                sb.append(getRollUpData(builder, ntsCardTypes, productData, 3));
                if (productData.getSalesTax() != null)
                    sb.append(StringUtils.toNumeric(productData.getSalesTax(), 5));
                else
                    sb.append(String.format("%05d",0));
                return sb;

            case MastercardFleet:
                sb.append(productData.getProductCodeType().getValue());

                addProductDataForSimilarFuelProducts(fuel);

                for (int i = 0; i < 1; i++) {
                    if (fuelFlag && i < fuel.size()) {

                        sb.append(StringUtils.padLeft(fuel.get(i).getCode(), 2, '0'));
                        if (i == 0)
                            sb.append(serviceLevel);
                        if(serviceLevel!=3) {
                            sb.append(StringUtils.padLeft(mapUnitMeasureFleet(fuel.get(i).getUnitOfMeasure()), 1, '0'));
                            sb.append(StringUtils.toDecimal(fuel.get(i).getPrice(), 5));
                            sb.append(StringUtils.toNumeric(fuel.get(i).getQuantity(), 6));
                            sb.append(StringUtils.toNumeric(fuel.get(i).getAmount(), 9));
                        }else{
                            sb.append(String.format("%1s", " "));
                            sb.append(String.format("%5s", " "));
                            sb.append(String.format("%6s", " "));
                            sb.append(String.format("%9s", " "));
                        }

                    } else {
                        sb.append(String.format("%2d", 0));
                        sb.append("0");
                        sb.append(String.format("%1d", 0));
                        sb.append(String.format("%5d", 0));
                        sb.append(String.format("%6d", 0));
                        sb.append(String.format("%9d", 0));
                    }
                }
                sb.append(getRollUpData(builder, ntsCardTypes, productData, 3));
                if (productData.getSalesTax() != null)
                    sb.append(StringUtils.toNumeric(productData.getSalesTax(), 5));
                else
                    sb.append(String.format("%05d",0));
                return sb;
            case Mastercard:
            case Visa:
            case AmericanExpress:
            case Discover:
            case StoredValueOrHeartlandGiftCard:
            case PinDebit:
                // Preparing product data fuel
                if (fuelFlag && !fuel.isEmpty()) {
                    String code = "";
                    String pcode = null;
                    BigDecimal price = new BigDecimal(0),
                            quantity = new BigDecimal(0),
                            amount = new BigDecimal(0);
                    List<DE63_ProductDataEntry> uniqueList=new ArrayList<>();

                    for (int i = 0; i < fuel.size(); i++) {
                        for (int j = i+1; j < fuel.size(); j++) {
                            if (fuel.get(i).getCode().equals(fuel.get(j).getCode())) {
                                pcode = StringUtils.padLeft(fuel.get(i).getCode(), 3, '0');
                                uniqueList.add(fuel.get(j));
                            }
                        }
                    }

                    for (int i = 0; i < fuel.size(); i++) {
                        if (fuel.size() > 1) {
                            if (!uniqueList.isEmpty()) {
                                code = StringUtils.padLeft(pcode, 3, '0');
                            } else {
                                code = StringUtils.padLeft("99", 3, '0');
                            }
                            price = new BigDecimal(0);
                            quantity = quantity.add(fuel.get(i).getQuantity());
                            amount = amount.add(fuel.get(i).getAmount());
                        } else {
                            sb.append(StringUtils.padLeft(fuel.get(i).getCode(), 3, '0'));
                            sb.append(StringUtils.toDecimal(fuel.get(i).getPrice(), 5));
                            sb.append(StringUtils.toDecimal(fuel.get(i).getQuantity(), 7));
                            sb.append(StringUtils.toNumeric(fuel.get(i).getAmount(), 8));
                        }
                    }
                    if (fuel.size() > 1) {
                        sb.append(StringUtils.padLeft(code, 3, '0'));
                        sb.append(StringUtils.toDecimal(price, 5));
                        sb.append(StringUtils.toDecimal(quantity, 7));
                        sb.append(StringUtils.toNumeric(amount, 8));
                    }
                }

                // Preparing product data non-fuel
                int nonFuelProductLimit = fuel.size() >= 1 ? 4 : 5;
                String code = "";
                BigDecimal price = new BigDecimal(0),
                        quantity = new BigDecimal(0),
                        amount = new BigDecimal(0);
                List<DE63_ProductDataEntry> duplicateListNonFuel=new ArrayList<>();

                for (int i = 0; i < nonFuel.size(); i++) {
                    for (int j = i+1; j < nonFuel.size(); j++) {
                        if (nonFuel.get(i).getCode().equals(nonFuel.get(j).getCode())) {
                            duplicateListNonFuel.add(nonFuel.get(j));
                            duplicateListNonFuel.add(nonFuel.get(i));
                        }
                    }
                }
                if(!duplicateListNonFuel.isEmpty()){
                    HashSet uniqueSet = new HashSet(duplicateListNonFuel);
                    duplicateListNonFuel.clear();
                    duplicateListNonFuel.addAll(uniqueSet);
                    nonFuel.removeAll(duplicateListNonFuel);

                    for(int i=0; i< duplicateListNonFuel.size();i++){
                        amount= duplicateListNonFuel.get(i).getAmount();
                        quantity = duplicateListNonFuel.get(i).getQuantity();
                        int cnt = 0;

                        for(int j=i+1; j< duplicateListNonFuel.size(); j++){
                            if(duplicateListNonFuel.get(i).getCode().equals(duplicateListNonFuel.get(j).getCode()) && !(duplicateListNonFuel.get(j).getCode().equals("-1")) ) {
                                cnt ++;
                                code = duplicateListNonFuel.get(i).getCode();
                                price = new BigDecimal(0);
                                quantity = quantity.add(duplicateListNonFuel.get(j).getQuantity());
                                amount = amount.add(duplicateListNonFuel.get(j).getAmount());
                                duplicateListNonFuel.get(j).setCode("-1");
                            }
                        }
                        if(cnt>= 1) {
                            DE63_ProductDataEntry de63ProductDataEntry = new DE63_ProductDataEntry();
                            de63ProductDataEntry.setCode(code);
                            de63ProductDataEntry.setPrice(price);
                            de63ProductDataEntry.setQuantity(quantity);
                            de63ProductDataEntry.setAmount(amount);
                            nonFuel.add(de63ProductDataEntry);
                        }
                    }

                }

                for (int i = 0; i < Math.max(nonFuel.size(), nonFuelProductLimit); i++) {
                    if (nonFuel.size() > nonFuelProductLimit && i >= nonFuelProductLimit - 1) {
                        code = StringUtils.padLeft("400", 3, '0');
                        price = new BigDecimal(0);
                        quantity = quantity.add(nonFuel.get(i).getQuantity());
                        amount = amount.add(nonFuel.get(i).getAmount());
                    } else if (!nonFuel.isEmpty() && i <= nonFuel.size() - 1) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 3, '0'));
                        sb.append(StringUtils.toDecimal(nonFuel.get(i).getPrice(), 5));
                        sb.append(StringUtils.toDecimal(nonFuel.get(i).getQuantity(), 7));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 8));
                    } else {
                        sb.append(String.format("%03d", 0));
                        sb.append(String.format("%05d", 0));
                        sb.append(String.format("%07d", 0));
                        sb.append(String.format("%08d", 0));
                    }
                }
                if (nonFuel.size() > nonFuelProductLimit) {
                    sb.append(StringUtils.padLeft(code, 3, '0'));
                    sb.append(StringUtils.toDecimal(price, 5));
                    sb.append(StringUtils.toDecimal(quantity, 7));
                    sb.append(StringUtils.toNumeric(amount, 8));
                }


                // tax
                if (productData.getSalesTax() != null)
                    sb.append(StringUtils.toNumeric(productData.getSalesTax(), 7));
                else
                    sb.append(String.format("%07d", 0));

                // PDL FUEL DISCOUNT
                if (productData.getDiscount() != null)
                    sb.append(StringUtils.toNumeric(productData.getDiscount(), 5));
                else
                    sb.append(String.format("%05d", 0));

                // Filler
                sb.append(StringUtils.padLeft("", 12, '0'));
                return sb;
            default:
                return null;
        }

    }

    public static String getRequestToBalanceUserData(TransactionBuilder<Transaction> builder) {
        StringBuilder sb = new StringBuilder();
        NtsRequestToBalanceData data = ((ManagementBuilder) builder).getNtsRequestsToBalanceData();
        sb.append(StringUtils.padLeft(data.getDaySequenceNumber(), 3, '0'));
        sb.append(StringUtils.padLeft(StringUtils.toNumeric(data.getPdlBatchDiscount(), 7), 7, '0'));
        sb.append(StringUtils.padRight(data.getVendorSoftwareNumber(), 30, ' '));
        return sb.toString();
    }

    public static String getNonBankCardUserData(TransactionBuilder<Transaction> builder, NTSCardTypes cardType, NtsMessageCode messageCode, AcceptorConfig acceptorConfig) throws ApiException {
        StringBuilder sb = new StringBuilder();
        NtsProductData productData = builder.getNtsProductData();
        BigDecimal salesTax = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(0);
        int serviceLevel = 0;
        String referenceMessageCode = null;
        FleetData fleetData = builder.getFleetData();
        if (productData != null) {
            serviceLevel = mapServiceByCardType(productData.getServiceLevel(), cardType);
            if (productData.getSalesTax() != null)
                salesTax = productData.getSalesTax();
            if (productData.getDiscount() != null)
                discount = productData.getDiscount();
        }
        TransactionType transactionType = builder.getTransactionType();
        if (builder instanceof ManagementBuilder && builder.getPaymentMethod() instanceof TransactionReference) {
            referenceMessageCode = ((TransactionReference) builder.getPaymentMethod()).getOriginalMessageCode();
        }
        switch (cardType) {
            case MastercardPurchasing:
                if (builder.getTransactionType().equals(TransactionType.DataCollect) ||
                        builder.getTransactionType().equals(TransactionType.Capture)) {
                    if (messageCode.equals(NtsMessageCode.DataCollectOrSale) || messageCode.equals(NtsMessageCode.CreditAdjustment)) {
                        if (builder.getCustomerCode() != null)
                                sb.append(StringUtils.padRight(builder.getCustomerCode(), 17, ' '));
                             else
                                sb.append(String.format("%17s", " "));
                        sb.append(getMastercardPurchasingFuelList(builder));
                        sb.append(getMastercardPurchasingNonFuelList(builder));
                        if (productData.getSalesTax() != null)
                            sb.append(StringUtils.toNumeric(productData.getSalesTax(), 5));
                        else
                            sb.append(String.format("%05d", 0));
                        sb.append(StringUtils.padLeft("", 12, ' '));
                        sb.append("?");
                    }
                }
                break;
            case Mastercard:
            case Visa:
            case AmericanExpress:
            case Discover:
            case StoredValueOrHeartlandGiftCard:
            case PinDebit:
                IPaymentMethod paymentMethod =  builder.getPaymentMethod();
                if(paymentMethod instanceof TransactionReference){
                    paymentMethod = ((TransactionReference)paymentMethod).getOriginalPaymentMethod();
                }
                if ((builder.getTransactionType() == TransactionType.DataCollect
                        || builder.getTransactionType() == TransactionType.Capture)
                        && builder.getNtsProductData() != null
                        && builder.getNtsTag16() != null) {
                    // Data Collect user data for non-fleet bankcards.
                    sb.append(StringUtils.padLeft(String.valueOf(builder.getNtsTag16().getPumpNumber()), 2, '0')); // Pump Number
                    sb.append(StringUtils.padLeft(String.valueOf(builder.getNtsTag16().getWorkstationId()), 2, '0')); // Workstation Id
                    TransactionReference transactionReference = (TransactionReference) builder.getPaymentMethod();
                    sb.append(transactionReference.getOriginalTransactionDate());
                    sb.append(DateTime.now(DateTimeZone.UTC).toString("yy"));
                    sb.append(transactionReference.getOriginalTransactionTime());
                    sb.append(builder.getNtsTag16().getServiceCode().getValue()); // Service Code
                    sb.append(builder.getNtsTag16().getSecurityData().getValue()); // Security Data
                    if (builder.getZipCode() != null) {
                        sb.append(StringUtils.padRight(builder.getZipCode(), 9, '0'));
                    } else {
                        sb.append(StringUtils.padRight("", 9, '0'));
                    }
                    if (builder.getCardSequenceNumber() != null) {
                        sb.append(StringUtils.padLeft(builder.getCardSequenceNumber(), 4, '0'));
                    } else {
                        sb.append(StringUtils.padLeft("", 4, '0'));
                    }
                    sb.append(getProductDataTag09(builder, cardType).toString());
                }else {
                    if (!builder.getTransactionType().equals(TransactionType.DataCollect)
                            && !builder.getTransactionType().equals(TransactionType.Capture)) {
                        TransactionTypeIndicator indicator = NtsUtils.getTransactionTypeIndicatorForTransaction(builder);
                        sb.append(StringUtils.padRight(indicator.getValue(), 8, ' '));
                        sb.append(StringUtils.padLeft(builder.getSystemTraceAuditNumber(), 6, '0'));
                    }
                }
                break;
            case FuelmanFleet:

            case FleetWide:
                if ((builder.getTransactionType().equals(TransactionType.DataCollect)
                        || builder.getTransactionType().equals(TransactionType.Capture)
                        || builder.getTransactionType().equals(TransactionType.Sale)) && (!messageCode.equals(NtsMessageCode.CreditAdjustment))) {
                    Optional<FleetData> fleetObj =  Optional.ofNullable(fleetData);
                    if(fleetObj.isPresent()){
                        sb.append(String.valueOf(StringUtils.padLeft(fleetObj.get().getDriverId() != null ? fleetObj.get().getDriverId() : 0,5,'0')));
                        sb.append(String.valueOf(StringUtils.padLeft(fleetObj.get().getOdometerReading() != null ? fleetObj.get().getOdometerReading() : 0,6,'0')));
                    } else{
                        sb.append(String.format("%05d",0));
                        sb.append(String.format("%06d",0));
                    }
                    sb.append(getFleetCorList(builder, cardType));
                    sb.append(getRollUpData(builder, cardType, productData, 4));

                    sb.append(StringUtils.toNumeric(salesTax, 5));
                } else if (builder.getTransactionType().equals(TransactionType.Auth)) {
                    if(fleetData!=null) {
                        sb.append(StringUtils.padLeft(fleetData.getDriverId(), 5, '0'));
                        sb.append(StringUtils.padLeft(fleetData.getOdometerReading(), 6, '0'));
                    }
                } else if (messageCode.equals(NtsMessageCode.CreditAdjustment)) {
                    sb.append(getFleetCorCreditAdjustment(builder));
                }
                break;
            case WexFleet:
                if (builder.getTransactionType().equals(TransactionType.Auth)) {
                    sb.append(getWexFleetPromptList(builder));
                    sb.append(StringUtils.padLeft(serviceLevel, 2, '0'));
                    sb.append("074");
                    sb.append(StringUtils.toNumeric(builder.getAmount(), 7));
                    sb.append(fleetData!=null ?
                            StringUtils.padRight(fleetData.getPurchaseDeviceSequenceNumber(), 5, '0'):
                            StringUtils.padRight("", 5, '0'));
                    if (builder.getTagData() != null) {
                        sb.append(builder.getCardSequenceNumber() != null ? builder.getCardSequenceNumber() : "000");
                        sb.append(mapEmvTransactionType(builder.getTransactionModifier()));
                        sb.append(acceptorConfig.getAvailableProductCapability().getValue());
                        sb.append(StringUtils.padLeft(builder.getTagData().length(), 4, '0'));
                        sb.append(builder.getTagData());
                    } else if(builder.getTransactionModifier()!= null && builder.getTransactionModifier().equals(TransactionModifier.Fallback)){
                        sb.append(builder.getCardSequenceNumber() != null ? builder.getCardSequenceNumber() : "000");
                        sb.append(mapEmvTransactionType(builder.getTransactionModifier()));
                        sb.append(acceptorConfig.getAvailableProductCapability().getValue());
                        sb.append(StringUtils.padLeft(WEX_FALLBACK.length(), 4, '0'));
                        sb.append(WEX_FALLBACK);
                    }
                } else if ((builder.getTransactionType().equals(TransactionType.DataCollect) ||
                        builder.getTransactionType().equals(TransactionType.Capture)
                        || builder.getTransactionType().equals(TransactionType.Sale))
                        && !messageCode.equals(NtsMessageCode.CreditAdjustment)) {
                    List<DE63_ProductDataEntry> fuelList = productData.getFuelDataEntries();
                    sb.append(getWexFleetPromptList(builder));

                    addProductDataForSimilarFuelProducts(fuelList);

                    if (fuelList != null && !fuelList.isEmpty()) {
                        for (int i = 0; i < fuelList.size(); i++) {
                            if (i == 0) {
                                sb.append(StringUtils.padLeft(mapUnitMeasure(fuelList.get(i).getUnitOfMeasure()), 1, '0'));
                                sb.append(StringUtils.padLeft(serviceLevel, 2, '0'));
                                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, '0'));
                                sb.append(StringUtils.toFormatDigit(fuelList.get(i).getQuantity(), 7, 3));
                                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 7));
                            } else if (i == 2) {
                                break;
                            } else {
                                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, '0'));
                                sb.append(StringUtils.padLeft(mapUnitMeasure(fuelList.get(i).getUnitOfMeasure()), 1, '0'));
                                sb.append(StringUtils.toFormatDigit(fuelList.get(i).getQuantity(), 6, 3));
                                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 6));
                            }
                        }
                    } else {
                        sb.append(String.format("%01d", 0));
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%03d", 0));
                        sb.append(String.format("%07d", 0));
                        sb.append(String.format("%07d", 0));
                    }
                    int rollUp = fuelList != null ? fuelList.size() >= 2 ? 6 : 7 : 7;
                    sb.append(getRollUpData(builder, cardType, productData, rollUp));
                    sb.append(fleetData != null ?
                            StringUtils.padLeft(fleetData.getPurchaseDeviceSequenceNumber(), 5, '0'):
                            StringUtils.padLeft("", 5, '0'));
                    sb.append(StringUtils.toNumeric(salesTax, 5));
                    sb.append(StringUtils.toNumeric(discount, 5));
                    if (builder.getTagData() != null) {
                        sb.append(builder.getCardSequenceNumber() != null ? builder.getCardSequenceNumber() : "000");
                        sb.append(mapEmvTransactionType(builder.getTransactionModifier()));
                        sb.append(StringUtils.padLeft(builder.getTagData().length(), 4, '0'));
                        sb.append(builder.getTagData());
                    }
                    else if(builder.getTransactionModifier()!= null && builder.getTransactionModifier().equals(TransactionModifier.Fallback)){
                        sb.append(builder.getCardSequenceNumber() != null ? builder.getCardSequenceNumber() : "000");
                        sb.append(mapEmvTransactionType(builder.getTransactionModifier()));
                        sb.append(acceptorConfig.getAvailableProductCapability().getValue());
                        sb.append(StringUtils.padLeft(WEX_FALLBACK.length(), 4, '0'));
                        sb.append(WEX_FALLBACK);
                    }
                } else if (messageCode.equals(NtsMessageCode.CreditAdjustment)) {
                    sb.append(fleetData != null ?
                            StringUtils.padLeft(fleetData.getPurchaseDeviceSequenceNumber(), 5, '0'):
                            StringUtils.padLeft("", 5, '0'));
                    if (fleetData != null)
                        sb.append(StringUtils.padRight(fleetData.getDriverId(), 6, ' '));
                    else
                        sb.append(StringUtils.padRight("", 6, '0'));
                    sb.append(StringUtils.padLeft(builder.getBatchNumber(), 2, '0'));
                    sb.append(StringUtils.padLeft(builder.getSequenceNumber(), 3, '0'));
                    TransactionReference transactionReference = (TransactionReference) builder.getPaymentMethod();
                    sb.append(transactionReference.getOriginalTransactionDate());
                } else if (referenceMessageCode != null && referenceMessageCode.equals("01")
                        && builder.getTransactionType().equals(TransactionType.Reversal)) {

                    sb.append(StringUtils.padLeft(serviceLevel, 2, '0'));
                    for (DE63_ProductDataEntry entry : productData.getFuelDataEntries()) {
                        sb.append("074");
                        sb.append(StringUtils.toNumeric(entry.getAmount(), 7));
                    }
                    sb.append(fleetData!= null ?
                            StringUtils.padRight(fleetData.getPurchaseDeviceSequenceNumber(), 5, '0'):
                            StringUtils.padRight("", 5, '0'));
                } else if (referenceMessageCode != null && referenceMessageCode.equals("02")
                        && transactionType.equals(TransactionType.Reversal)) {
                    List<DE63_ProductDataEntry> fuelList = productData.getFuelDataEntries();
                    if (fuelList != null && !fuelList.isEmpty()) {
                        for (int i = 0; i < fuelList.size(); i++) {
                            if (i == 0) {
                                sb.append(StringUtils.padLeft(mapUnitMeasure(fuelList.get(i).getUnitOfMeasure()), 1, '0'));
                                sb.append(StringUtils.padLeft(serviceLevel, 2, '0'));
                                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, '0'));
                                sb.append(StringUtils.toFormatDigit(fuelList.get(i).getQuantity(), 7, 3));
                                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 7));
                            } else {
                                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, '0'));
                                sb.append(StringUtils.padLeft(mapUnitMeasure(fuelList.get(i).getUnitOfMeasure()), 1, '0'));
                                sb.append(StringUtils.toFormatDigit(fuelList.get(i).getQuantity(), 6, 3));
                                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 6));
                            }
                        }
                    } else {
                        sb.append(String.format("%01d", 0));
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%03d", 0));
                        sb.append(String.format("%07d", 0));
                        sb.append(String.format("%07d", 0));
                    }
                    sb.append(getRollUpData(builder, cardType, productData, 7));
                    if(fleetData!=null) {
                        sb.append(fleetData.getPurchaseDeviceSequenceNumber());
                    }
                    sb.append(StringUtils.toNumeric(salesTax, 5));
                    sb.append(StringUtils.toNumeric(discount, 5));
                }
                break;
            case VoyagerFleet:
                if (builder.getTransactionType().equals(TransactionType.Auth)) {
                    sb.append(fleetData != null ?
                            StringUtils.padLeft(fleetData.getOdometerReading(), 7, '0'):
                            StringUtils.padLeft(0, 7, '0'));
                    sb.append(fleetData != null ?
                            StringUtils.padLeft(fleetData.getDriverId(), 6, '0'):
                            StringUtils.padLeft(0, 6, '0'));
                } else if (builder.getTransactionType().equals(TransactionType.DataCollect)
                        || builder.getTransactionType().equals(TransactionType.Sale) ||
                        builder.getTransactionType().equals(TransactionType.Capture)) {
                    if (messageCode.equals(NtsMessageCode.DataCollectOrSale)) {
                        Optional<FleetData> fleetObj =  Optional.ofNullable(fleetData);
                        if(fleetObj.isPresent()){
                            sb.append(String.valueOf(StringUtils.padLeft(fleetObj.get().getOdometerReading() != null ? fleetObj.get().getOdometerReading() : 0,7,'0')));
                            sb.append(String.valueOf(StringUtils.padLeft(fleetObj.get().getDriverId() != null ? fleetObj.get().getDriverId() : 0,6,'0')));
                        } else {
                            sb.append(String.format("%07d",0));
                            sb.append(String.format("%06d",0));
                        }
                        sb.append(serviceLevel);
                        sb.append(getVoyagerFleetFuelList(builder));
                        sb.append(getRollUpData(builder, cardType, productData, 4));
                        sb.append(StringUtils.toNumeric(salesTax, 6));
                    } else if (messageCode.equals(NtsMessageCode.CreditAdjustment)) {

                        sb.append(builder.getInvoiceNumber());
                        sb.append(serviceLevel);
                        sb.append(getVoyagerFleetFuelList(builder));
                        sb.append(getRollUpData(builder, cardType, productData, 4));
                        sb.append(StringUtils.toNumeric(salesTax, 6));
                    }
                }
                break;
            default:
                break;
        }
        return sb.toString();
    }


    private static StringBuilder getRollUpData(TransactionBuilder builder, NTSCardTypes cardType, NtsProductData productData, int rollUpAt) {
        StringBuilder sb = new StringBuilder();
        TransactionType transactionType = builder.getTransactionType();
        List<DE63_ProductDataEntry> nonFuel = productData.getNonFuelDataEntries();
        int nonFuelSize = nonFuel.size();
        float sumAmount = 0.0f;
        if (cardType.equals(NTSCardTypes.VisaFleet)) {

            if(transactionType.equals(TransactionType.DataCollect) || transactionType.equals(TransactionType.Sale)){
                combineProductDataForSimilarNonFuelProducts(nonFuel);
                nonFuelSize = nonFuel.size();
            }

            if (nonFuelSize >= rollUpAt) {
                for (int i = 0; i < nonFuelSize; i++) {
                    if (i < rollUpAt - 1) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                    } else {
                        sumAmount += nonFuel.get(i).getAmount().floatValue();
                    }
                }
                sb.append(StringUtils.padLeft(90, 2, ' '));
                sb.append(StringUtils.padLeft(nonFuelSize - rollUpAt + 1, 2, '0'));
                sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 6));
            } else {
                for (int i = 0; i < rollUpAt; i++) {
                    if (i < nonFuelSize) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                    } else {
                        sb.append(String.format("%2s", " "));
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%06d", 0));
                    }
                }
            }
        }else if (cardType.equals(NTSCardTypes.MastercardFleet)) {

            if(transactionType.equals(TransactionType.DataCollect) || transactionType.equals(TransactionType.Sale)){
                combineProductDataForSimilarNonFuelProducts(nonFuel);
                nonFuelSize = nonFuel.size();
            }

            if (nonFuelSize >= rollUpAt) {
                for (int i = 0; i < nonFuelSize; i++) {
                    if (i < rollUpAt - 1) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, '0'));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                    } else {
                        sumAmount += nonFuel.get(i).getAmount().floatValue();
                    }
                }
                sb.append(StringUtils.padLeft(99, 2, '0'));
                sb.append(StringUtils.padLeft(nonFuelSize - rollUpAt + 1, 2, '0'));
                sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 6));
            } else {
                for (int i = 0; i < rollUpAt; i++) {
                    if (i < nonFuelSize) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, '0'));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                    } else {
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%06d", 0));
                    }
                }
            }
        } else if (cardType.equals(NTSCardTypes.WexFleet)) {
            if (transactionType.equals(TransactionType.Reversal)) {
                int x = productData.getFuelDataEntries().size() == 2 ? 1 : 0;
                rollUpAt = rollUpAt - x;
                if (nonFuelSize > rollUpAt) {
                    for (int i = 0; i < nonFuelSize; i++) {
                        if (i < rollUpAt - 1) {
                            sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 3, '0'));
                            sb.append(StringUtils.padLeft(mapUnitMeasure(nonFuel.get(i).getUnitOfMeasure()), 1, '0'));
                            if (i == 0 - x) {
                                sb.append(StringUtils.toFormatDigit(nonFuel.get(i).getQuantity(), 6, 3));
                            } else {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 3, '0'));
                            }
                            sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                        } else {
                            sumAmount += nonFuel.get(i).getAmount().floatValue();
                        }
                    }
                    sb.append(StringUtils.padLeft(400, 3, '0'));
                    sb.append(String.format("%01d", 0));
                    sb.append(StringUtils.padLeft(1, 1, '0'));
                    sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 6));
                } else {
                    for (int i = 0; i < rollUpAt; i++) {
                        if (i < nonFuelSize) {
                            sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 3, '0'));
                            sb.append(StringUtils.padLeft(mapUnitMeasure(nonFuel.get(i).getUnitOfMeasure()), 1, '0'));
                            if (i == 0 - x) {
                                sb.append(StringUtils.toFormatDigit(nonFuel.get(i).getQuantity(), 6, 3));
                            } else {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 3, '0'));
                            }
                            sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                        } else {
                            sb.append(String.format("%03d", 0));
                            sb.append(String.format("%01d", 0));
                            if (i == 0 - x) {
                                sb.append(String.format("%06d", 0));
                            } else {
                                sb.append(String.format("%03d", 0));
                            }
                            sb.append(String.format("%06d", 0));
                        }
                    }
                }
            } else {
                combineProductDataForSimilarNonFuelProducts(nonFuel);
                nonFuelSize = nonFuel.size();

                int x = productData.getFuelDataEntries().size() >= 2 ? 1 : 0;
                if (nonFuelSize > rollUpAt) {
                    for (int i = 0; i < nonFuelSize; i++) {
                        if (i < rollUpAt - 1) {
                            sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 3, '0'));
                            sb.append(StringUtils.padLeft(mapUnitMeasure(nonFuel.get(i).getUnitOfMeasure()), 1, '0'));
                            if (i == 0 - x) {
                                sb.append(StringUtils.toFormatDigit(nonFuel.get(i).getQuantity(), 6, 3));
                            } else if (i == 1 - x || i == 2 - x || i == 3 - x) {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 3, '0'));
                            } else if (i == 4 - x) {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                            } else {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 1, '0'));
                            }
                            sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                        } else {
                            sumAmount += nonFuel.get(i).getAmount().floatValue();
                        }
                    }
                    sb.append(StringUtils.padLeft(400, 3, '0'));
                    sb.append(String.format("%01d", 0));
                    sb.append(StringUtils.padLeft(1, 1, '0'));
                    sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 6));
                } else {
                    for (int i = 0; i < rollUpAt; i++) {
                        if (i < nonFuelSize) {
                            sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 3, '0'));
                            sb.append(StringUtils.padLeft(mapUnitMeasure(nonFuel.get(i).getUnitOfMeasure()), 1, '0'));
                            if (i == 0 - x) {
                                sb.append(StringUtils.toFormatDigit(nonFuel.get(i).getQuantity(), 6, 3));
                            } else if (i == 1 - x || i == 2 - x || i == 3 - x) {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 3, '0'));
                            } else if (i == 4 - x) {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                            } else {
                                sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 1, '0'));
                            }
                            sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 6));
                        } else {
                            sb.append(String.format("%03d", 0));
                            sb.append(String.format("%01d", 0));
                            if (i == 0 - x) {
                                sb.append(String.format("%06d", 0));
                            } else if (i == 1 - x || i == 2 - x || i == 3 - x) {
                                sb.append(String.format("%03d", 0));
                            } else if (i == 4 - x) {
                                sb.append(String.format("%02d", 0));
                            } else {
                                sb.append(String.format("%01d", 0));
                            }
                            sb.append(String.format("%06d", 0));
                        }
                    }
                }
            }

        } else if (cardType.equals(NTSCardTypes.FuelmanFleet) || cardType.equals(NTSCardTypes.FleetWide)) {
            if (nonFuelSize > 4) {
                for (int index = 0; index < nonFuelSize; index++) {
                    if (index < rollUpAt - 1) {
                        sb.append(StringUtils.padLeft(nonFuel.get(index).getCode(), 3, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(index).getQuantity().intValue(), 4, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(index).getAmount(), 5));
                    } else {
                        sumAmount += nonFuel.get(index).getAmount().floatValue();
                    }
                }
                sb.append(StringUtils.padLeft(400, 3, ' '));
                sb.append(StringUtils.padLeft(0001, 4, '0'));
                sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 5));
            } else {
                nonFuel = nonFuel.stream().sorted(Comparator.comparing(DE63_ProductDataEntry::getAmount).reversed()).collect(Collectors.toList());
                for (int index = 0; index < rollUpAt; index++) {
                    if (index < nonFuelSize) {
                        sb.append(StringUtils.padLeft(nonFuel.get(index).getCode(), 3, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(index).getQuantity().intValue(), 4, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(index).getAmount(), 5));
                    } else {
                        sb.append(String.format("%3s", " "));
                        sb.append(String.format("%04d", 0));
                        sb.append(String.format("%05d", 0));
                    }
                }
            }
        } else if (cardType.equals(NTSCardTypes.VoyagerFleet)) {
            sumAmount = 0.0f;

            combineProductDataForSimilarNonFuelProducts(nonFuel);
            nonFuelSize = nonFuel.size();

            if (nonFuelSize > rollUpAt) {
                for (int i = 0; i < nonFuelSize; i++) {
                    if (i < rollUpAt - 1) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 5));
                    } else {
                        sumAmount += nonFuel.get(i).getAmount().floatValue();
                    }
                }
                sb.append(StringUtils.padLeft(33, 2, ' '));
                sb.append("01");
                sb.append(StringUtils.toNumeric(BigDecimal.valueOf(sumAmount), 5));
            } else {
                nonFuel = nonFuel.stream().sorted(Comparator.comparing(DE63_ProductDataEntry::getAmount).reversed()).collect(Collectors.toList());
                for (int i = 0; i < rollUpAt; i++) {
                    if (i < nonFuelSize) {
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getCode(), 2, ' '));
                        sb.append(StringUtils.padLeft(nonFuel.get(i).getQuantity().intValue(), 2, '0'));
                        sb.append(StringUtils.toNumeric(nonFuel.get(i).getAmount(), 5));
                    } else {
                        sb.append(String.format("%2s", " "));
                        sb.append(String.format("%02d", 0));
                        sb.append(String.format("%05d", 0));
                    }
                }
            }
        }
        return sb;
    }

    private static String mapEmvTransactionType(TransactionModifier transTypeIndicator) {
        switch (transTypeIndicator) {
            case Fallback:
                return "F";
            case Offline:
                return "A";
            case ChipDecline:
                return "D";
            default:
                return " ";
        }
    }

    private static StringBuffer getFleetCorCreditAdjustment(TransactionBuilder<Transaction> builder) {
        StringBuffer sb = new StringBuffer();
        TransactionReference reference = null;
        if (builder.getPaymentMethod() instanceof TransactionReference) {
            reference = (TransactionReference) builder.getPaymentMethod();
            sb.append(StringUtils.padLeft(reference.getApprovalCode(), 6, '0'));
            sb.append(StringUtils.padLeft(reference.getBatchNumber(), 2, '0'));
            sb.append(StringUtils.padLeft(reference.getSequenceNumber(), 3, '0'));
        }
        sb.append(DateTime.now().toString("yy"));
        sb.append(reference.getOriginalTransactionDate());
        sb.append(reference.getOriginalTransactionTime());
        return sb;
    }
    private static StringBuffer getMastercardPurchasingFuelList(TransactionBuilder<Transaction> builder) {
        StringBuffer sb = new StringBuffer();
        NtsProductData productData = builder.getNtsProductData();
        List<DE63_ProductDataEntry> fuelList = productData.getFuelDataEntries();
        if(!fuelList.isEmpty()){addProductDataForSimilarFuelProducts(fuelList);}
        for (int i = 0; i < 1; i++) {
            if (fuelList != null && i < fuelList.size()) {

                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, ' '));
                sb.append(StringUtils.toNumeric(fuelList.get(i).getQuantity(), 5));
                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 6));
                sb.append(StringUtils.padLeft(mapUnitMeasureFleet(fuelList.get(i).getUnitOfMeasure()), 1, ' '));


            } else {
                sb.append(String.format("%3s", " "));
                sb.append(String.format("%5s", " "));
                sb.append(String.format("%6s", " "));
                sb.append(String.format("%1s", " "));
            }

        }
        return sb;
    }
    private static StringBuffer getMastercardPurchasingNonFuelList(TransactionBuilder<Transaction> builder) {
        StringBuffer sb = new StringBuffer();
        NtsProductData productData = builder.getNtsProductData();
        List<DE63_ProductDataEntry> nonFuelList = productData.getNonFuelDataEntries();
        if(!nonFuelList.isEmpty()){combineProductDataForSimilarNonFuelProducts(nonFuelList);}
        for (int i = 0; i < 2; i++) {
            if (nonFuelList != null && i < nonFuelList.size()) {

                sb.append(StringUtils.padLeft(nonFuelList.get(i).getCode(), 3, ' '));
                sb.append(StringUtils.padLeft(nonFuelList.get(i).getQuantity().intValue(), 5, '0'));
                sb.append(StringUtils.toNumeric(nonFuelList.get(i).getAmount(), 6));
                sb.append(StringUtils.padLeft(mapUnitMeasureFleet(nonFuelList.get(i).getUnitOfMeasure()), 1, ' '));

            } else {
                sb.append(String.format("%3s", " "));
                sb.append(String.format("%5s", " "));
                sb.append(String.format("%6s", " "));
                sb.append(String.format("%1s", " "));
            }

        }
        return sb;
    }

    private static StringBuffer getWexFleetPromptList(TransactionBuilder<Transaction> builder) throws ApiException {
        StringBuffer sb = new StringBuffer();
        FleetData fleetData = builder.getFleetData();
        int noOfPrompt= fleetData != null ? getWexPromptCount(fleetData) : 0;

        int promptSize = builder.getTagData() != null ?
                Math.min(noOfPrompt, 6) :
                Math.min(noOfPrompt, 3);
        sb.append(promptSize);
        if (fleetData != null) {
            sb.append(getWEXPromptData(fleetData, promptSize));
        }

        // Added remaining 0 for padding purpose.
        int remainingPrompt = builder.getTagData() != null ?
                Math.max(promptSize, 6) :
                Math.max(promptSize, 3);
        for (int i = promptSize; i < remainingPrompt; i++) {
            sb.append(String.format("%01d", 0));
            sb.append(String.format("%02d", 0));
            sb.append(String.format("%012d", 0));
        }
        return sb;
    }

    private static StringBuffer getVoyagerFleetFuelList(TransactionBuilder<Transaction> builder) {
        StringBuffer sb = new StringBuffer();
        NtsProductData productData = builder.getNtsProductData();
        List<DE63_ProductDataEntry> fuelList = productData.getFuelDataEntries();
        addProductDataForSimilarFuelProducts(fuelList);
        for (int i = 0; i < 2; i++) {
            if (fuelList != null && i < fuelList.size()) {

                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 2, '0'));
                sb.append(StringUtils.toNumeric(fuelList.get(i).getQuantity(), 5));
                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 5));
            } else {
                sb.append(String.format("%02d", 0));
                sb.append(String.format("%05d", 0));
                sb.append(String.format("%05d", 0));
            }
        }
        return sb;
    }

    private static StringBuffer getFleetCorList(TransactionBuilder<Transaction> builder, NTSCardTypes ntsCardTypes) {
        StringBuffer sb = new StringBuffer();
        NtsProductData productData = builder.getNtsProductData();
        List<DE63_ProductDataEntry> fuelList = productData.getFuelDataEntries();
        int serviceLevel = mapServiceByCardType(productData.getServiceLevel(), ntsCardTypes);
        for (int i = 0; i < 1; i++) {
            if (fuelList != null && i < fuelList.size()) {
                sb.append(mapUnitMeasureFleet(fuelList.get(i).getUnitOfMeasure()));
                sb.append(serviceLevel);
                sb.append(StringUtils.padLeft(fuelList.get(i).getCode(), 3, ' '));
                sb.append(StringUtils.toDecimal(fuelList.get(i).getPrice(), 5));
                sb.append(NtsUtils.toNumeric(fuelList.get(i).getQuantity(), 6));
                sb.append(StringUtils.toNumeric(fuelList.get(i).getAmount(), 5));
            } else {
                sb.append(String.format("%1s", "0"));
                sb.append(serviceLevel);
                sb.append(String.format("%3s", " "));
                sb.append(String.format("%05d", 0));
                sb.append(String.format("%06d", 0));
                sb.append(String.format("%05d", 0));
            }
        }
        return sb;
    }
    private  static  StringBuffer getWEXPromptData(FleetData data, Integer promptSize){
        StringBuffer sb = new StringBuffer();
        int sizeFlag = 0;
        if(data.getVehicleNumber() != null){
            sb.append(getWexPrompt(data.getVehicleNumber(), "1"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getUserId() != null){
            sb.append(getWexPrompt(data.getUserId(), "2"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getDriverId() != null){
            sb.append(getWexPrompt(data.getDriverId(), "3"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getOdometerReading() != null){
            sb.append(getWexPrompt(data.getOdometerReading(), "4"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getDriversLicenseNumber() != null){
            sb.append(getWexPrompt(data.getDriversLicenseNumber(), "5"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getEnteredData() != null){
            sb.append(getWexPrompt(data.getEnteredData(), "6"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getJobNumber() != null){
            sb.append(getWexPrompt(data.getJobNumber(), "7"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getDepartment() != null){
            sb.append(getWexPrompt(data.getDepartment(), "8"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        if(data.getOtherPromptCode() != null){
            sb.append(getWexPrompt(data.getOtherPromptCode(), "9"));
            sizeFlag ++;
            if(sizeFlag >= promptSize){
                return sb;
            }
        }
        return sb;
    }

    private static StringBuffer getWexPrompt(String data, String promptCode){
        return new StringBuffer()
                .append(promptCode) // PromptCode
                .append(StringUtils.padLeft(data.length(), 2, '0')) // Data Length
                .append(StringUtils.padLeft(data, 12, '0')); // Actual Data

    }
    private static int getWexPromptCount(FleetData fleetData){
        int noOfPrompt =0;

        List<String> promptCode = new ArrayList<>();
        promptCode.add(fleetData.getVehicleNumber());
        promptCode.add(fleetData.getDriverId());
        promptCode.add(fleetData.getDepartment());
        promptCode.add(fleetData.getUserId());
        promptCode.add(fleetData.getDriversLicenseNumber());
        promptCode.add(fleetData.getJobNumber());
        promptCode.add(fleetData.getOtherPromptCode());
        promptCode.add(fleetData.getOdometerReading());
        promptCode.add(fleetData.getMaintenanceNumber());
        promptCode.add(fleetData.getHubometerNumber());
        promptCode.add(fleetData.getTrailerNumber());
        promptCode.add(fleetData.getTrailerReferHours());
        promptCode.add(fleetData.getTripNumber());
        promptCode.add(fleetData.getEnteredData());

        noOfPrompt = Math.toIntExact(promptCode.stream().filter(code -> code != null).count());
        promptCode.clear();

        return noOfPrompt;
    }

    public static void addProductDataForSimilarFuelProducts(List<DE63_ProductDataEntry> fuel){
        // Preparing product data fuel
        String code = "";
        BigDecimal price = new BigDecimal(0),
                quantity = new BigDecimal(0),
                amount = new BigDecimal(0);
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.NoFuelPurchased;
        List<DE63_ProductDataEntry> duplicateListFuel=new ArrayList<>();

        for (int i = 0; i < fuel.size(); i++) {
            for (int j = i+1; j < fuel.size(); j++) {
                if (fuel.get(i).getCode().equals(fuel.get(j).getCode())) {
                    duplicateListFuel.add(fuel.get(j));
                    duplicateListFuel.add(fuel.get(i));
                }
            }
        }
        if(!duplicateListFuel.isEmpty()){
            HashSet uniqueSet = new HashSet(duplicateListFuel);
            duplicateListFuel.clear();
            duplicateListFuel.addAll(uniqueSet);

            for (int i = 0; i < duplicateListFuel.size(); i++) {
                if (duplicateListFuel.size() > 1) {
                    code = duplicateListFuel.get(i).getCode();
                    price = new BigDecimal(0);
                    quantity = quantity.add(duplicateListFuel.get(i).getQuantity());
                    amount = amount.add(duplicateListFuel.get(i).getAmount());
                    unitOfMeasure = duplicateListFuel.get(i).getUnitOfMeasure();
                }
            }
            DE63_ProductDataEntry de63ProductDataEntry = new DE63_ProductDataEntry();
            de63ProductDataEntry.setCode(code);
            de63ProductDataEntry.setPrice(price);
            de63ProductDataEntry.setQuantity(quantity);
            de63ProductDataEntry.setAmount(amount);
            de63ProductDataEntry.setUnitOfMeasure(unitOfMeasure);

            fuel.removeAll(duplicateListFuel);
            fuel.add(de63ProductDataEntry);
        }
    }
    private static void combineProductDataForSimilarNonFuelProducts(List<DE63_ProductDataEntry> nonFuel){

        // Preparing product data non-fuel
        String code = "";
        BigDecimal price = new BigDecimal(0),
                quantity = new BigDecimal(0),
                amount = new BigDecimal(0);
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.NoFuelPurchased;
        List<DE63_ProductDataEntry> duplicateListNonFuel = new ArrayList<>();
        HashSet< DE63_ProductDataEntry> duplicateMap = new HashSet<>();

        int cnt = 0;
        String previousCode="";
        for (int i = 0; i < nonFuel.size(); i++) {
            if(!duplicateMap.contains(nonFuel.get(i).getCode())) {
                cnt = 0;
            }
            for (int j = i + 1; j < nonFuel.size(); j++) {
                cnt++;
                if (nonFuel.get(i).getCode().equals(nonFuel.get(j).getCode())) {
                    duplicateMap.add(nonFuel.get(j));
                    if(cnt==1) {
                        duplicateMap.add(nonFuel.get(i));
                    }
                }
            }
        }
        duplicateListNonFuel.addAll(duplicateMap);
        nonFuel.removeAll(duplicateListNonFuel);

        if(duplicateListNonFuel.size()>0){

            for(int i=0; i< duplicateListNonFuel.size();i++){
                amount= duplicateListNonFuel.get(i).getAmount();
                quantity = duplicateListNonFuel.get(i).getQuantity();
                cnt = 0;

                for(int j=i+1; j< duplicateListNonFuel.size(); j++){
                    if(duplicateListNonFuel.get(i).getCode().equals(duplicateListNonFuel.get(j).getCode()) && !(duplicateListNonFuel.get(j).getCode().equals("-1")) ) {
                        cnt ++;
                        code = duplicateListNonFuel.get(i).getCode();
                        price = new BigDecimal(0);
                        quantity = quantity.add(duplicateListNonFuel.get(j).getQuantity());
                        amount = amount.add(duplicateListNonFuel.get(j).getAmount());
                        unitOfMeasure = duplicateListNonFuel.get(j).getUnitOfMeasure();
                        duplicateListNonFuel.get(j).setCode("-1");
                    }
                }
                if(cnt>= 1) {
                    DE63_ProductDataEntry de63ProductDataEntry = new DE63_ProductDataEntry();
                    de63ProductDataEntry.setCode(code);
                    de63ProductDataEntry.setPrice(price);
                    de63ProductDataEntry.setQuantity(quantity);
                    de63ProductDataEntry.setAmount(amount);
                    de63ProductDataEntry.setUnitOfMeasure(unitOfMeasure);
                    nonFuel.add(de63ProductDataEntry);
                }
            }
        }
    }
}