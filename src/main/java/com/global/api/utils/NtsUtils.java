package com.global.api.utils;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.network.entities.nts.*;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.paymentMethods.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NtsUtils {

    // Logger
    private static boolean isEnableLogging = false;

    /**
     * This function checks that whether entry method provided
     * is supports user data expansion or not.
     *
     * @param entryMethod
     * @return
     */
    public static Boolean isUserDataExpansionEntryMethod(NTSEntryMethod entryMethod) {
        return entryMethod == NTSEntryMethod.ECommerceNoTrackDataAttended
                || entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattendedAfd
                || entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattendedCat
                || entryMethod == NTSEntryMethod.ECommerceNoTrackDataUnattended
                || entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataAttended
                || entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedAfd
                || entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedCat
                || entryMethod == NTSEntryMethod.SecureEcommerceNoTrackDataUnattended
                || entryMethod == NTSEntryMethod.ContactEmvNoTrackDataAttended
                || entryMethod == NTSEntryMethod.ContactlessEmvNoTrackDataAttended
                || entryMethod == NTSEntryMethod.ContactEmvNoTrackDataUnattendedCat
                || entryMethod == NTSEntryMethod.ContactEmvNoTrackDataUnattended
                || entryMethod == NTSEntryMethod.ContactlessEmvNoTrackDataUnattended;
    }


    /**
     * This function checks that whether the given entry method
     * is not supports track 1 or track 2.
     *
     * @param entryMethod
     * @return
     */
    public static boolean isNoTrackEntryMethods(NTSEntryMethod entryMethod) {
        return isUserDataExpansionEntryMethod(entryMethod)
                || entryMethod == NTSEntryMethod.MagneticStripeWithoutTrackDataUnattended
                || entryMethod == NTSEntryMethod.MagneticStripeWithoutTrackDataAttended
                || entryMethod == NTSEntryMethod.ManualAttended;
    }


    /**
     * Logging the NTS request
     *
     * @param fieldName
     * @param value
     */
    public static void log(String fieldName, String value) {
        if (isEnableLogging) {
            System.out.println( StringUtils.padRight(fieldName.toUpperCase(Locale.ENGLISH), 20, ' ')  + " : " + value);
        }
    }

    public static <E extends Enum<E>> void log(String fieldName, E value) {
        if (isEnableLogging && value != null) {
            System.out.println( StringUtils.padRight(fieldName.toUpperCase(Locale.ENGLISH), 20, ' ')  + " : [ " + value.name() + " - " + ((IStringConstant) value).getValue() + " ]");
        }
    }

    public static void log(String value) {
        if (isEnableLogging) {
            System.out.println(value);
        }
    }

    public static void log(String fieldName, Integer value) {
        log(fieldName, String.valueOf(value));
    }

    /**
     * Enable the logging.
     */
    public static void enableLogging() {
        isEnableLogging = true;
    }


    /**
     * This needed for the void and reversal transaction.
     *
     * @return
     */
    public static TransactionReference prepareTransactionReference(NtsResponse ntsResponse) {
        NtsAuthCreditResponseMapper ntsAuthCreditResponseMapper = null;
        NtsSaleCreditResponseMapper ntsSaleCreditResponseMapper = null;
        NtsVoidReversalResponse ntsVoidReversalResponse=null;
        String approvalCode = null;
        String hostResponseArea = null;
        Integer sequenceNumber= null;
        Integer batchNumber= null;
        AuthorizerCode authorizer = null;
        String debitAuthorizer = ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue();

        TransactionReference reference = new TransactionReference();
        if (ntsResponse.getNtsResponseMessage() instanceof NtsAuthCreditResponseMapper) {
            ntsAuthCreditResponseMapper = (NtsAuthCreditResponseMapper) ntsResponse.getNtsResponseMessage();
            approvalCode = getOrDefault(ntsAuthCreditResponseMapper.getCreditMapper().getApprovalCode(), "");
            hostResponseArea = getOrDefault(ntsAuthCreditResponseMapper.getCreditMapper().getHostResponseArea(), "");
            authorizer = ntsAuthCreditResponseMapper.getCreditMapper().getAuthorizer();
        } else if (ntsResponse.getNtsResponseMessage() instanceof NtsSaleCreditResponseMapper) {
            ntsSaleCreditResponseMapper = (NtsSaleCreditResponseMapper) ntsResponse.getNtsResponseMessage();
            approvalCode = getOrDefault(ntsSaleCreditResponseMapper.getCreditMapper().getApprovalCode(), "");
            hostResponseArea = getOrDefault(ntsSaleCreditResponseMapper.getCreditMapper().getHostResponseArea(), "");
            sequenceNumber = ntsSaleCreditResponseMapper.getSequenceNumber();
            batchNumber = ntsSaleCreditResponseMapper.getBatchNumber();
            authorizer =  ntsSaleCreditResponseMapper.getCreditMapper().getAuthorizer();
        } else if (ntsResponse.getNtsResponseMessage() instanceof NtsDebitResponse) {
            authorizer = AuthorizerCode.Interchange_Authorized;
            approvalCode = ((NtsDebitResponse) ntsResponse.getNtsResponseMessage()).getCode();
            debitAuthorizer = ((NtsDebitResponse) ntsResponse.getNtsResponseMessage()).getAuthorizerCode().getValue();
        } else if (ntsResponse.getNtsResponseMessage() instanceof NtsEbtResponse) {
            authorizer = AuthorizerCode.Interchange_Authorized;
            approvalCode = ((NtsEbtResponse) ntsResponse.getNtsResponseMessage()).getApprovalCode();
            debitAuthorizer = ((NtsEbtResponse) ntsResponse.getNtsResponseMessage()).getAuthorizerCode().getValue();
        } else if (ntsResponse.getNtsResponseMessage() instanceof NtsDataCollectResponse) {
            approvalCode = ((NtsDataCollectResponse) ntsResponse.getNtsResponseMessage()).getApprovalCode();
        }else if (ntsResponse.getNtsResponseMessage() instanceof NtsVoidReversalResponse) {
            ntsVoidReversalResponse = (NtsVoidReversalResponse) ntsResponse.getNtsResponseMessage();
            approvalCode = getOrDefault(ntsVoidReversalResponse.getCreditMapper().getApprovalCode(), "");
            hostResponseArea = getOrDefault(ntsVoidReversalResponse.getCreditMapper().getHostResponseArea(), "");
            authorizer = ntsVoidReversalResponse.getCreditMapper().getAuthorizer();
        }

        reference.setApprovalCode(approvalCode);
        reference.setOriginalMessageCode(getOrDefault(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getNtsMessageCode().getValue(), "")); // 01 for auth transaction.
        reference.setAuthCode(getOrDefault(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue(), ""));
        reference.setOriginalTransactionDate(getOrDefault(ntsResponse.getNtsResponseMessageHeader().getTransactionDate(), ""));
        reference.setOriginalTransactionTime(getOrDefault(ntsResponse.getNtsResponseMessageHeader().getTransactionTime(), ""));
        reference.setAuthorizer(authorizer);
        reference.setDebitAuthorizer(debitAuthorizer);
        reference.setAuthCode(debitAuthorizer);
        reference.setOriginalTransactionTime(getOrDefault(ntsResponse.getNtsResponseMessageHeader().getTransactionTime(), ""));
        if (!StringUtils.isNullOrEmpty(hostResponseArea)) {
            Map<UserDataTag, String> userData = mapUserData(hostResponseArea);
            reference.setBankcardData(userData);
            reference.setSystemTraceAuditNumber(userData.getOrDefault(UserDataTag.Stan, ""));
            reference.setMastercardBanknetRefNo(userData.getOrDefault(UserDataTag.MasterCardBanknetRefId, ""));
            reference.setMastercardBanknetSettlementDate(userData.getOrDefault(UserDataTag.MasterCardSettlementDate, ""));
            reference.setVisaTransactionId(userData.getOrDefault(UserDataTag.VisaTransactionId, ""));
            reference.setDiscoverNetworkRefId(userData.getOrDefault(UserDataTag.DiscoverNetworkRefId, ""));
        }
        if (sequenceNumber!= null){
            reference.setSequenceNumber(sequenceNumber);
        }
        if (batchNumber!= null){
            reference.setBatchNumber(batchNumber);
        }
        return reference;
    }

    /**
     * Map the Host response user data.
     *
     * @param userData
     * @return
     */
    private static Map<UserDataTag, String> mapUserData(String userData) {
        Map<UserDataTag, String> dataMap = new HashMap<>();
        String[] res = userData.split("\\\\");
        for (int i = 1; i < res.length; i = i + 2) {
            dataMap.put(ReverseStringEnumMap.parse(res[i], UserDataTag.class), res[i + 1]);
        }
        return dataMap;
    }

    /**
     * Setting the default values to field to avoid the null pointer exception.
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static String getOrDefault(String value, String defaultValue) {
        return StringUtils.isNullOrEmpty(value) ? defaultValue : value;
    }


    /**
     * Mapping the card types.
     *
     * @param paymentMethod
     * @return
     */
    public static NTSCardTypes mapCardType(IPaymentMethod paymentMethod) {
        if (paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference) paymentMethod;
            if (transactionReference.getOriginalPaymentMethod() != null) {
                paymentMethod = transactionReference.getOriginalPaymentMethod();
            }
        }
        if (paymentMethod instanceof Debit) {
            Debit card = (Debit) paymentMethod;
            if (card.getCardType().equals("PinDebit")) {
                return NTSCardTypes.PinDebit;
            }
            if (card.getCardType().equals("Amex")) {
                return NTSCardTypes.AmericanExpress;
            } else if (card.getCardType().equals("MC")) {
                return NTSCardTypes.Mastercard;
            } else if (card.getCardType().equals("MCFleet")) {
                return NTSCardTypes.MastercardFleet;
            } else if (card.getCardType().equals("WexFleet")) {
                return NTSCardTypes.WexFleet;
            } else if (card.getCardType().equals("Visa")) {
                return NTSCardTypes.Visa;
            } else if (card.getCardType().equals("VisaFleet")) {
                return NTSCardTypes.VisaFleet;
            } else if (card.getCardType().equals("Discover")) {
                return NTSCardTypes.Discover;
            } else if (card.getCardType().equals("VoyagerFleet")) {
                return NTSCardTypes.VoyagerFleet;
            } else if (card.getCardType().equals("FuelmanFleet")) {
                return NTSCardTypes.FuelmanFleet;
            } else if (card.getCardType().equals("FleetWide")) {
                return NTSCardTypes.FleetWide;
            }

        } else if (paymentMethod instanceof Credit) {
            Credit card = (Credit) paymentMethod;
            if (card.getCardType().equals("Amex")) {
                return NTSCardTypes.AmericanExpress;
            } else if (card.getCardType().equals("MC")) {
                return NTSCardTypes.Mastercard;
            } else if (card.getCardType().equals("MCFleet")) {
                return NTSCardTypes.MastercardFleet;
            } else if (card.getCardType().equals("WexFleet")) {
                return NTSCardTypes.WexFleet;
            } else if (card.getCardType().equals("Visa")) {
                return NTSCardTypes.Visa;
            } else if (card.getCardType().equals("VisaFleet")) {
                return NTSCardTypes.VisaFleet;
            } else if (card.getCardType().equals("Discover")) {
                return NTSCardTypes.Discover;
            } else if (card.getCardType().equals("VoyagerFleet")) {
                return NTSCardTypes.VoyagerFleet;
            } else if (card.getCardType().equals("FuelmanFleet")) {
                return NTSCardTypes.FuelmanFleet;
            } else if (card.getCardType().equals("FleetWide")) {
                return NTSCardTypes.FleetWide;
            }else if (card.getCardType().equals("PayPal")){
                return NTSCardTypes.PayPal;
            } else if (card.getCardType().equals("VisaReadyLink")){
                return NTSCardTypes.PinDebit;
            } else if (card.getCardType().equals("MastercardPurchasing")) {
                return NTSCardTypes.MastercardPurchasing;
            }
        } else if (paymentMethod instanceof GiftCard) {
            GiftCard card = (GiftCard) paymentMethod;

            if (card.getCardType().equals("ValueLink")) {
                return NTSCardTypes.ValueLink;
            } else if (card.getCardType().equals("HeartlandGift") || card.getCardType().equals("StoredValue")) {
                return NTSCardTypes.StoredValueOrHeartlandGiftCard;
            }
        } else if (paymentMethod instanceof EBT) {
            EBT card = (EBT) paymentMethod;
            if (card.getEbtCardType().equals(EbtCardType.CashBenefit)) {
                return NTSCardTypes.EBTCashBenefits;
            } else if (card.getEbtCardType().equals(EbtCardType.FoodStamp)) {
                return NTSCardTypes.EBTFoodStamps;
            }
        }
        return null;
    }

    public static boolean isSVSGiftCard(TransactionType transactionType, PaymentMethodType paymentMethodType) {
        return (transactionType.equals(TransactionType.Activate)
                || transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.AddValue)
                || transactionType.equals(TransactionType.Issue)
                || transactionType.equals(TransactionType.Refund)
                || transactionType.equals(TransactionType.Void)
                || transactionType.equals(TransactionType.Balance)
                || transactionType.equals(TransactionType.PreAuthCompletion)
                || transactionType.equals(TransactionType.Reversal))
                && Objects.equals(paymentMethodType, PaymentMethodType.Gift);
    }

    public static boolean isEBTCard(TransactionType transactionType, PaymentMethodType paymentMethod) {
        return (transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Balance)
                || transactionType.equals(TransactionType.BenefitWithdrawal)
                || transactionType.equals(TransactionType.Void)
                || transactionType.equals(TransactionType.PreAuthCompletion)
                || transactionType.equals(TransactionType.Refund)
                || transactionType.equals(TransactionType.Reversal))
                && Objects.equals(paymentMethod, PaymentMethodType.EBT);
    }

    public static String prepareExpDateWithoutTrack(String expiryDate) {
        return expiryDate.substring(2) + expiryDate.substring(0, 2);
    }

    public static <T extends TransactionBuilder<Transaction>> TransactionCode getTransactionCodeForTransaction(T builder, BigDecimal cashBackAmount, BigDecimal settlementAmount) {
        TransactionType transactionType = builder.getTransactionType();
        TransactionCode originalCode = null;
        if(builder.getPaymentMethod() instanceof TransactionReference){
            originalCode = ((TransactionReference) builder.getPaymentMethod()).getOriginalTransactionCode();
        }
        switch (transactionType) {
            case Balance:
                return TransactionCode.BalanceInquiry;
            case Sale:
                if (cashBackAmount != null) {
                    return TransactionCode.PurchaseCashBack;
                } else if (builder.getTransactionModifier() == TransactionModifier.Voucher) {
                    return TransactionCode.VoucherSale;
                }
                return TransactionCode.Purchase;
            case Refund:
                if (builder.getTransactionModifier() == TransactionModifier.Voucher) {
                    return TransactionCode.VoucherReturn;
                }
                return TransactionCode.PurchaseReturn;
            case Auth:
                return TransactionCode.PreAuthorizationFunds;
            case PreAuthCompletion:
                return TransactionCode.PreAuthCompletion;
            case Void:
                return TransactionCode.PreAuthCancelation;
            case Reversal:
                if (settlementAmount != null) {
                    return TransactionCode.PurchaseCashBackReversal;
                } else if (originalCode == TransactionCode.Load){
                    return  TransactionCode.LoadReversal;
                }
                return TransactionCode.PurchaseReversal;
            case BenefitWithdrawal:
                return TransactionCode.Withdrawal;
            case AddValue:
                return TransactionCode.Load;
            case LoadReversal:
                return TransactionCode.LoadReversal;
            default:
                return null;
        }
    }

    public static <T extends TransactionBuilder<Transaction>> TransactionTypeIndicator getTransactionTypeIndicatorForTransaction(T builder) {
        TransactionType transactionType = builder.getTransactionType();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionTypeIndicator originalTransactionTypeIndicator = null;
        if (paymentMethod instanceof TransactionReference) {
            originalTransactionTypeIndicator = ((TransactionReference) paymentMethod).getOriginalTransactionTypeIndicator();
        }

        switch (transactionType) {
            case Balance:
                return TransactionTypeIndicator.BalanceInquiry;
            case Activate:
                return TransactionTypeIndicator.CardActivation;
            case Deactivate:
                return TransactionTypeIndicator.ActivateCancellation;
            case Sale:
                return TransactionTypeIndicator.Purchase;
            case Refund:
                return TransactionTypeIndicator.MerchandiseReturn;
            case Auth:
                return TransactionTypeIndicator.PreAuthorization;
            case PreAuthCompletion:
                return TransactionTypeIndicator.PreAuthorizationCompletion;
            case AddValue:
                return TransactionTypeIndicator.RechargeCardBalance;
            case Issue:
                return TransactionTypeIndicator.CardIssue;
            case Reversal:
                if (originalTransactionTypeIndicator != null) {
                    if (originalTransactionTypeIndicator == TransactionTypeIndicator.CardActivation) {
                        return TransactionTypeIndicator.ActivateReversal;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.CardIssue) {
                        return TransactionTypeIndicator.IssueReversal;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.Purchase) {
                        return TransactionTypeIndicator.PurchaseReversal;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.MerchandiseReturn) {
                        return TransactionTypeIndicator.MerchandiseReturnReversal;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.PreAuthorization) {
                        return TransactionTypeIndicator.PreAuthorizationReversal;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.RechargeCardBalance) {
                        return TransactionTypeIndicator.RechargeReversal;
                    } else {
                        return null;
                    }
                }
                return null;
            case Void:
                if (originalTransactionTypeIndicator != null) {
                    if (originalTransactionTypeIndicator == TransactionTypeIndicator.CardActivation) {
                        return TransactionTypeIndicator.ActivateCancellation;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.CardIssue) {
                        return TransactionTypeIndicator.IssueCancellation;
                    } else if (originalTransactionTypeIndicator == TransactionTypeIndicator.Purchase) {
                        return TransactionTypeIndicator.PurchaseCancellation;
                    } else {
                        return null;
                    }
                }
                return null;
            default:
                return null;
        }
    }

    public static <T extends TransactionBuilder<Transaction>> EntryMethod isEcommerceEntryMethod(T builder){
        if(builder instanceof AuthorizationBuilder){
            AuthorizationBuilder authorizationBuilder = (AuthorizationBuilder) builder;
            if(authorizationBuilder.getEcommerceInfo() != null && authorizationBuilder.getEcommerceInfo().getChannel() == EcommerceChannel.Ecom){
                if(builder.getPaymentMethod() instanceof CreditCardData){
                    CreditCardData card = (CreditCardData) builder.getPaymentMethod();
                    if(card.getThreeDSecure() != null){
                        return EntryMethod.SecureEcommerce;
                    } else if(authorizationBuilder.getStoredCredential() != null){
                        return EntryMethod.CardOnFileEcommerce;
                    }
                    return EntryMethod.ECommerce;
                }

            }
        } else if(builder instanceof ManagementBuilder){
            ManagementBuilder managementBuilder = (ManagementBuilder) builder;
            TransactionReference transactionReference = null;
            if (builder.getPaymentMethod() instanceof TransactionReference) {
                transactionReference = (TransactionReference) builder.getPaymentMethod();
            }
            if(managementBuilder.getEcommerceInfo() != null && managementBuilder.getEcommerceInfo().getChannel() == EcommerceChannel.Ecom){
                if(transactionReference!=null) {
                    if (transactionReference.getOriginalPaymentMethod() instanceof CreditCardData) {
                        CreditCardData card = (CreditCardData) transactionReference.getOriginalPaymentMethod();
                        if (card.getThreeDSecure() != null) {
                            return EntryMethod.SecureEcommerce;
                        } else if (managementBuilder.getStoredCredential() != null) {
                            return EntryMethod.CardOnFileEcommerce;
                        }
                        return EntryMethod.ECommerce;
                    }
                }

            }
        }
        return null;
    }

    public static NTSEntryMethod isAttendedOrUnattendedEntryMethod(EntryMethod entryMethod, TrackNumber trackNumber, OperatingEnvironment operatingEnvironment) {
        switch (entryMethod) {
            case Swipe:
                if (trackNumber == TrackNumber.TrackOne) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.MagneticStripeTrack1DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.MagneticStripeTrack1DataUnattendedAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.MagneticStripeTrack1DataUnattendedCat;
                    }

                } else if (trackNumber == TrackNumber.TrackTwo) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.MagneticStripeTrack2DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.MagneticStripeTrack2DataUnattendedAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.MagneticStripeTrack2DataUnattendedCat;
                    }


                } else {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.MagneticStripeWithoutTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedOffPremise) {
                        return NTSEntryMethod.MagneticStripeWithoutTrackDataUnattended
                                ;
                    }
                }

            case BarCode:
                return NTSEntryMethod.BarCode;

            case ContactEMV:
                if (trackNumber == TrackNumber.TrackTwo) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactEmvTrack2DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactEmvTrack2DataUnattendedCat;
                    }else if(operatingEnvironment==OperatingEnvironment.UnattendedAfd){
                        return NTSEntryMethod.ContactEmvTrack2DataUnattendedAfd;
                    }
                } else if(trackNumber==TrackNumber.Unknown){
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactEmvNoTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactEmvNoTrackDataUnattendedCat;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedOffPremise) {
                        return NTSEntryMethod.ContactEmvNoTrackDataUnattended;
                    }
                }
            case ContactlessEMV:
                if (trackNumber == TrackNumber.TrackTwo) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactlessEmvTrack2DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactlessEmvTrack2DataUnattendedCat;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.ContactlessEmvTrack2DataUnattendedAfd;

                    }
                } else if(trackNumber==TrackNumber.Unknown){
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactlessEmvNoTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactlessEmvNoTrackDataUnattendedCat;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedOffPremise) {
                        return NTSEntryMethod.ContactEmvNoTrackDataUnattended;
                    }
                }
            case ContactlessRFID:
                if (trackNumber == TrackNumber.TrackOne) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactlessRfidTrack1DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactlessRfidTrack1DataUnattendedCat;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedOffPremise) {
                        return NTSEntryMethod.ContactlessRfidTrack1DataUnattened;
                    }
                } else if (trackNumber == TrackNumber.TrackTwo) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ContactlessRfidTrack2DataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ContactlessRfidTrack2DataUnattendedCat;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedOffPremise) {
                        return NTSEntryMethod.ContactlessRfidTrack2DataAttended;
                    }
                }
            case QrCode:
                if (trackNumber == TrackNumber.TrackOne) {
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.QrCodeTrack2Data;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.QrCodeTrack2DataAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.QrCodeTrack2DataCat;
                    }
                }
            case ContactlessRfidRingTechnology:
                if (trackNumber == TrackNumber.TrackOne) {
                    if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.ContactlessRfidRingTechnologyTrack1Data;
                    }
                }
                else if(trackNumber==TrackNumber.TrackTwo){
                    if(operatingEnvironment==OperatingEnvironment.UnattendedAfd){
                        return NTSEntryMethod.ContactlessRfidRingTechnologyTrack2Data;
                    }
                }
            case ECommerce:
                if(trackNumber==TrackNumber.Unknown){
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.ECommerceNoTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.ECommerceNoTrackDataUnattendedAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.ECommerceNoTrackDataUnattendedCat;
                    }
                }
            case SecureEcommerce:
                if(trackNumber==TrackNumber.Unknown){
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.SecureEcommerceNoTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.SecureEcommerceNoTrackDataUnattendedCat;
                    } else if(operatingEnvironment==OperatingEnvironment.UnattendedOffPremise){
                        return NTSEntryMethod.SecureEcommerceNoTrackDataUnattended;
                    }
                }
            case CardOnFileEcommerce:
                if(trackNumber==TrackNumber.Unknown){
                    if (operatingEnvironment == OperatingEnvironment.Attended) {
                        return NTSEntryMethod.CardOnFileEcommerceNoTrackDataAttended;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedAfd) {
                        return NTSEntryMethod.CardOnFileEcommerceNoTrackDataUnattendedAfd;
                    } else if (operatingEnvironment == OperatingEnvironment.UnattendedCat) {
                        return NTSEntryMethod.CardOnFileEcommerceNoTrackDataUnattendedCat;
                    } else if(operatingEnvironment==OperatingEnvironment.UnattendedOffPremise){
                        return NTSEntryMethod.CardOnFileECommerceNoTrackDataUnattended;
                    }
                }
        }
        return null;
    }
    public static Date getDateObject(String dateString){
        Date date=null;
        SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");

        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: ", e);
        }
        return date;
    }


    // 99v999 & 999v999 format conversion
    public static String toNumeric(BigDecimal amount, int length) {
        String rvalue = StringUtils.toNumeric(amount);
        char paddingCharacter = '0';
        if(rvalue.length() >= 3 && length >= 5){
            rvalue = rvalue + paddingCharacter;
            return StringUtils.padLeft(rvalue, length, paddingCharacter);
        }
        else {
            return StringUtils.padRight(rvalue, length, paddingCharacter);
        }
    }

}