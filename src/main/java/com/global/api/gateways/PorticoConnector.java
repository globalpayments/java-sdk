package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.reporting.AltPaymentData;
import com.global.api.entities.reporting.CheckData;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.*;
import com.global.api.utils.*;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@Accessors(chain = true)
@Setter
public class PorticoConnector extends XmlGateway implements IPaymentGateway, IReportingService {
    private int siteId;
    private int licenseId;
    private int deviceId;
    private String username;
    private String password;
    private String developerId;
    private String versionNumber;
    private String secretApiKey;
    private String sdkNameVersion;
    private String cardType;

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    @Override
    public boolean supportsOpenBanking() {
        return false;
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        TransactionType type = builder.getTransactionType();
        TransactionModifier modifier = builder.getTransactionModifier();
        PaymentMethodType paymentType = builder.getPaymentMethod().getPaymentMethodType();

        // build request
        Element transaction = et.element(mapTransactionType(builder));
        Element block1 = et.subElement(transaction, "Block1");
        if (type.equals(TransactionType.Sale) || type.equals(TransactionType.Auth)) {
            if (paymentType != PaymentMethodType.Gift && paymentType != PaymentMethodType.ACH) {
                et.subElement(block1, "AllowDup", builder.isAllowDuplicates() ? "Y" : "N");
                if (modifier.equals(TransactionModifier.None) && paymentType != PaymentMethodType.EBT && paymentType != PaymentMethodType.Recurring)
                    et.subElement(block1, "AllowPartialAuth", builder.isAllowPartialAuth() ? "Y" : "N");
            }
        }
        et.subElement(block1, "Amt", builder.getAmount());
        et.subElement(block1, "GratuityAmtInfo", builder.getGratuity());
        et.subElement(block1, "ConvenienceAmtInfo", builder.getConvenienceAmount());
        et.subElement(block1, "ShippingAmtInfo", builder.getShippingAmount());

        // surcharge
        if (builder.getSurchargeAmount() != null) {
            et.subElement(block1, "SurchargeAmtInfo", builder.getSurchargeAmount().toString());
        }

        // because plano...
        et.subElement(block1, paymentType == PaymentMethodType.Debit ? "CashbackAmtInfo" : "CashBackAmount", builder.getCashBackAmount());

        // offline auth code
        et.subElement(block1, "OfflineAuthCode", builder.getOfflineAuthCode());

        // alias action
        if (type.equals(TransactionType.Alias)) {
            et.subElement(block1, "Action").text(builder.getAliasAction());
            et.subElement(block1, "Alias").text(builder.getAlias());
        }

        boolean isCheck = (paymentType.equals(PaymentMethodType.ACH));
        if (isCheck || builder.getBillingAddress() != null || !StringUtils.isNullOrEmpty(builder.getCardHolderLanguage())) {
            Element holder = et.subElement(block1, isCheck ? "ConsumerInfo" : "CardHolderData");

            Address address = builder.getBillingAddress();
            if (address != null) {
                et.subElement(holder, isCheck ? "Address1" : "CardHolderAddr", address.getStreetAddress1());
                et.subElement(holder, isCheck ? "City" : "CardHolderCity", address.getCity());
                et.subElement(holder, isCheck ? "State" : "CardHolderState", address.getProvince());
                et.subElement(holder, isCheck ? "Zip" : "CardHolderZip", address.getPostalCode());
            }

            if (isCheck) {
                eCheck check = (eCheck) builder.getPaymentMethod();
                if (!StringUtils.isNullOrEmpty(check.getCheckHolderName())) {
                    String[] names = check.getCheckHolderName().split(" ", 2);
                    et.subElement(holder, "FirstName", names[0]);
                    et.subElement(holder, "LastName", names[1]);
                }
                et.subElement(holder, "CheckName", check.getCheckName());
                et.subElement(holder, "PhoneNumber", check.getPhoneNumber());
                et.subElement(holder, "DLNumber", check.getDriversLicenseNumber());
                et.subElement(holder, "DLState", check.getDriversLicenseState());

                if (!StringUtils.isNullOrEmpty(check.getSsnLast4()) || check.getBirthYear() != 0) {
                    Element identity = et.subElement(holder, "IdentityInfo");
                    et.subElement(identity, "SSNL4", check.getSsnLast4());
                    et.subElement(identity, "DOBYear", check.getBirthYear());
                }
            } else if (builder.getPaymentMethod() instanceof CreditCardData) {
                CreditCardData card = (CreditCardData) builder.getPaymentMethod();
                if (!StringUtils.isNullOrEmpty(card.getCardHolderName())) {
                    String[] names = card.getCardHolderName().split(" ", 2);
                    et.subElement(holder, "CardHolderFirstName", names[0]);
                    if (names.length > 1) {
                        et.subElement(holder, "CardHolderLastName", names[1]);
                    }
                }
            }

            // card holder language
            if (!isCheck && !StringUtils.isNullOrEmpty(builder.getCardHolderLanguage())) {
                et.subElement(holder, "CardHolderLanguage", builder.getCardHolderLanguage());
            }
        }

        // card data
        String tokenValue = getToken(builder.getPaymentMethod());
        boolean hasToken = !StringUtils.isNullOrEmpty(tokenValue);

        // because debit is weird (Ach too)
        Element cardData = null;
        if (paymentType.equals(PaymentMethodType.Debit) || paymentType.equals(PaymentMethodType.ACH))
            cardData = block1;
        else cardData = et.element("CardData");

        if (builder.getPaymentMethod() instanceof ICardData) {
            ICardData card = (ICardData) builder.getPaymentMethod();

            // card on File
            if (builder.getTransactionInitiator() != null || !StringUtils.isNullOrEmpty(builder.getCardBrandTransactionId())) {
                Element cardOnFileData = et.subElement(block1, "CardOnFileData");
                if (builder.getTransactionInitiator() == StoredCredentialInitiator.CardHolder) {
                    et.subElement(cardOnFileData, "CardOnFile", EnumUtils.getMapping(Target.Portico, StoredCredentialInitiator.CardHolder));
                } else {
                    et.subElement(cardOnFileData, "CardOnFile", EnumUtils.getMapping(Target.Portico, StoredCredentialInitiator.Merchant));
                }
                et.subElement(cardOnFileData, "CardBrandTxnId", builder.getCardBrandTransactionId());
            }

            Element manualEntry = et.subElement(cardData, hasToken ? "TokenData" : "ManualEntry");
            et.subElement(manualEntry, hasToken ? "TokenValue" : "CardNbr").text(tokenValue != null ? tokenValue : card.getNumber());
            et.subElement(manualEntry, "ExpMonth", card.getExpMonth() != null ? card.getExpMonth().toString() : null);
            et.subElement(manualEntry, "ExpYear", card.getExpYear() != null ? card.getExpYear().toString() : null);
            et.subElement(manualEntry, "CVV2", card.getCvn());
            et.subElement(manualEntry, "ReaderPresent", card.isReaderPresent() ? "Y" : "N");
            et.subElement(manualEntry, "CardPresent", card.isCardPresent() ? "Y" : "N");
            block1.append(cardData);

            // secure 3d
            if (card instanceof CreditCardData) {
                ThreeDSecure secureEcom = ((CreditCardData) card).getThreeDSecure();
                CreditCardData creditCardData = (CreditCardData) card;
                if (secureEcom != null) {
                    // 3d Secure Element
                    if (!StringUtils.isNullOrEmpty(secureEcom.getEci()) && !isAppleOrGooglePay(secureEcom.getPaymentDataSource())) {
                        Element secure3D = et.subElement(block1, "Secure3D");
                        et.subElement(secure3D, "Version", getSecure3DVersion(secureEcom.getVersion()));
                        et.subElement(secure3D, "AuthenticationValue", secureEcom.getCavv());
                        et.subElement(secure3D, "ECI", secureEcom.getEci());
                        et.subElement(secure3D, "DirectoryServerTxnId", secureEcom.getXid());
                    }

                    // WalletData Element
                    if (isAppleOrGooglePay(secureEcom.getPaymentDataSource())) {
                        Element walletData = et.subElement(block1, "WalletData");
                        et.subElement(walletData, "PaymentSource", secureEcom.getPaymentDataSource());
                        et.subElement(walletData, "Cryptogram", secureEcom.getCavv());
                        et.subElement(walletData, "ECI", secureEcom.getEci());
                    }
                }

                //WalletData Element
                if (
                        (
                                creditCardData.getMobileType() == MobilePaymentMethodType.APPLEPAY
                                        || creditCardData.getMobileType() == MobilePaymentMethodType.GOOGLEPAY
                        )
                                && creditCardData.getPaymentDataSourceType() != null
                                && isAppleOrGooglePay(creditCardData.getPaymentDataSourceType())
                ) {
                    Element walletData = et.subElement(block1, "WalletData");
                    et.subElement(walletData, "PaymentSource", creditCardData.getPaymentDataSourceType());
                    et.subElement(walletData, "Cryptogram", secureEcom != null ? secureEcom.getCavv() : null);
                    et.subElement(walletData, "ECI", creditCardData.getEci());
                    if (creditCardData.getMobileType() != null) {

                        et.subElementCdata(walletData, "DigitalPaymentToken", creditCardData.getToken());
                        if (block1.has("CardData")) {
                            block1.remove("CardData");
                        }
                        if (block1.has("CardHolderData")) {
                            block1.remove("CardHolderData");
                        }
                    }
                }

            }

            // recurring data
            if (builder.getTransactionModifier().equals(TransactionModifier.Recurring)) {
                Element recurring = et.subElement(block1, "RecurringData");
                et.subElement(recurring, "ScheduleID", builder.getScheduleId());
                et.subElement(recurring, "OneTime").text(builder.isOneTimePayment() ? "Y" : "N");
            }
        } else if (builder.getPaymentMethod() instanceof ITrackData) {
            ITrackData track = (ITrackData) builder.getPaymentMethod();

            Element trackData = et.subElement(cardData, hasToken ? "TokenData" : "TrackData");
            if (!hasToken) {
                trackData.text(track.getValue());
                trackData.set("method", track.getEntryMethod());
                if (paymentType == PaymentMethodType.Credit) {
                    // tag data
                    if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
                        Element tagData = et.subElement(block1, "TagData");
                        Element tagValues = et.subElement(tagData, "TagValues", builder.getTagData());
                        tagValues.set("source", "chip");
                    }
                    Element emvData = et.subElement(block1, "EMVData");
                    if (builder.getEmvChipCondition() != null) {
                        String chipCondition = builder.getEmvChipCondition() == EmvChipCondition.ChipFailPreviousSuccess ? "CHIP_FAILED_PREV_SUCCESS" : "CHIP_FAILED_PREV_FAILED";
                        et.subElement(emvData, "EMVChipCondition", chipCondition);
                    }
                    if (builder.getPaymentMethod() instanceof IPinProtected && (((IPinProtected) builder.getPaymentMethod()).getPinBlock()) != null) {
                        et.subElement(emvData, "PINBlock", ((IPinProtected) builder.getPaymentMethod()).getPinBlock());
                    }
                    if (!block1.has("EMVChipCondition") && !block1.has("PINBlock")) {
                        block1.remove("EMVData");
                    }
                }
                if (paymentType == PaymentMethodType.Debit) {
                    String chipCondition = null;
                    if (builder.getEmvChipCondition() != null)
                        chipCondition = builder.getEmvChipCondition() == EmvChipCondition.ChipFailPreviousSuccess ? "CHIP_FAILED_PREV_SUCCESS" : "CHIP_FAILED_PREV_FAILED";

                    et.subElement(block1, "AccountType",
                            EnumUtils.getMapping(Target.Portico, builder.getAccountType()));
                    et.subElement(block1, "EMVChipCondition", chipCondition);
                    et.subElement(block1, "MessageAuthenticationCode", builder.getMessageAuthenticationCode());
                    et.subElement(block1, "PosSequenceNbr", builder.getPosSequenceNumber());
                    et.subElement(block1, "ReversalReasonCode", builder.getReversalReasonCode());
                    if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
                        Element tagData = et.subElement(block1, "TagData");
                        et.subElement(tagData, "TagValues", builder.getTagData()).set("source", "chip");
                    }
                } else block1.append(cardData);
            } else et.subElement(trackData, "TokenValue").text(tokenValue);
        } else if (builder.getPaymentMethod() instanceof GiftCard) {
            GiftCard card = (GiftCard) builder.getPaymentMethod();

            // currency
            et.subElement(block1, "Currency", builder.getCurrency());

            // if it's replace put the new card and change the card data name to be old card data
            if (type.equals(TransactionType.Replace)) {
                GiftCard replacement = builder.getReplacementCard();
                Element newCardData = et.subElement(block1, "NewCardData");
                et.subElement(newCardData, replacement.getValueType(), replacement.getValue());
                et.subElement(newCardData, "PIN", replacement.getPin());

                cardData = et.element("OldCardData");
            }
            et.subElement(cardData, card.getValueType(), card.getValue());
            et.subElement(cardData, "PIN", card.getPin());

            if (builder.getAliasAction() != AliasAction.Create)
                block1.append(cardData);
        } else if (builder.getPaymentMethod() instanceof eCheck) {
            eCheck check = (eCheck) builder.getPaymentMethod();

            // check action
            et.subElement(block1, "CheckAction").text("SALE");

            // account info
            if (StringUtils.isNullOrEmpty(check.getToken())) {
                Element accountInfo = et.subElement(block1, "AccountInfo");
                et.subElement(accountInfo, "RoutingNumber", check.getRoutingNumber());
                et.subElement(accountInfo, "AccountNumber", check.getAccountNumber());
                et.subElement(accountInfo, "CheckNumber", check.getCheckNumber());
                et.subElement(accountInfo, "MICRData", check.getMicrNumber());
                et.subElement(accountInfo, "AccountType", EnumUtils.getMapping(Target.Portico,
                        check.getAccountType()));
            } else et.subElement(block1, "TokenValue").text(tokenValue);

            et.subElement(block1, "DataEntryMode", check.getEntryMode().getValue().toUpperCase(Locale.ENGLISH));
            et.subElement(block1, "CheckType", check.getCheckType());
            et.subElement(block1, "SECCode", check.getSecCode());

            // verify info
            Element verify = et.subElement(block1, "VerifyInfo");
            et.subElement(verify, "CheckVerify").text(check.isCheckVerify() ? "Y" : "N");
            et.subElement(verify, "ACHVerify").text(check.isAchVerify() ? "Y" : "N");
        } else if (builder.getPaymentMethod() instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) builder.getPaymentMethod();
            et.subElement(block1, "GatewayTxnId", reference.getTransactionId());
            et.subElement(block1, "ClientTxnId", reference.getClientTransactionId());
        } else if (builder.getPaymentMethod() instanceof RecurringPaymentMethod) {
            RecurringPaymentMethod method = (RecurringPaymentMethod) builder.getPaymentMethod();

            // card on File
            if (builder.getTransactionInitiator() != null || !StringUtils.isNullOrEmpty(builder.getCardBrandTransactionId())) {
                Element cardOnFileData = et.subElement(block1, "CardOnFileData");
                if (builder.getTransactionInitiator() == StoredCredentialInitiator.CardHolder) {
                    et.subElement(cardOnFileData, "CardOnFile", EnumUtils.getMapping(Target.Portico, StoredCredentialInitiator.CardHolder));
                } else {
                    et.subElement(cardOnFileData, "CardOnFile", EnumUtils.getMapping(Target.Portico, StoredCredentialInitiator.Merchant));
                }
                et.subElement(cardOnFileData, "CardBrandTxnId", builder.getCardBrandTransactionId());
            }
            // check action
            if (method.getPaymentType().equals("ACH")) {
                block1.remove("AllowDup");
                et.subElement(block1, "CheckAction").text("SALE");
            }

            // payment method stuff
            et.subElement(block1, "PaymentMethodKey").text(method.getKey());
            if (method.getPaymentMethod() != null && method.getPaymentMethod() instanceof CreditCardData) {
                CreditCardData card = (CreditCardData) method.getPaymentMethod();
                Element data = et.subElement(block1, "PaymentMethodKeyData");
                et.subElement(data, "ExpMonth", card.getExpMonth());
                et.subElement(data, "ExpYear", card.getExpYear());
                et.subElement(data, "CVV2", card.getCvn());
            }

            // recurring data
            Element recurring = et.subElement(block1, "RecurringData");
            et.subElement(recurring, "ScheduleID", builder.getScheduleId());
            et.subElement(recurring, "OneTime").text(builder.isOneTimePayment() ? "Y" : "N");
        }

        // pin block
        if (builder.getPaymentMethod() instanceof IPinProtected) {
            if (!type.equals(TransactionType.Reversal))
                et.subElement(block1, "PinBlock", ((IPinProtected) builder.getPaymentMethod()).getPinBlock());
        }

        // encryption
        if (builder.getPaymentMethod() instanceof IEncryptable) {
            EncryptionData encryptionData = ((IEncryptable) builder.getPaymentMethod()).getEncryptionData();

            if (encryptionData != null) {
                Element enc = et.subElement(cardData, "EncryptionData");
                et.subElement(enc, "Version").text(encryptionData.getVersion());
                et.subElement(enc, "EncryptedTrackNumber", encryptionData.getTrackNumber());
                et.subElement(enc, "KTB", encryptionData.getKtb());
                et.subElement(enc, "KSN", encryptionData.getKsn());
            }
        }

        // set token flag
        if (builder.getPaymentMethod() instanceof ITokenizable && builder.getPaymentMethod().getPaymentMethodType() != PaymentMethodType.ACH) {
            et.subElement(cardData, "TokenRequest").text(builder.isRequestMultiUseToken() ? "Y" : "N");
        }

        // balance inquiry type
        if (builder.getPaymentMethod() instanceof IBalanceable)
            et.subElement(block1, "BalanceInquiryType", builder.getBalanceInquiryType());

        // cpc request
        if (builder.isLevel2Request())
            et.subElement(block1, "CPCReq", "Y");

        // details
        if (!StringUtils.isNullOrEmpty(builder.getCustomerId()) || !StringUtils.isNullOrEmpty(builder.getDescription()) || !StringUtils.isNullOrEmpty(builder.getInvoiceNumber())) {
            Element addons = et.subElement(block1, "AdditionalTxnFields");
            et.subElement(addons, "CustomerID", builder.getCustomerId());
            et.subElement(addons, "Description", builder.getDescription());
            et.subElement(addons, "InvoiceNbr", builder.getInvoiceNumber());
        }

        // ecommerce info
        if (builder.getEcommerceInfo() != null) {
            EcommerceInfo ecom = builder.getEcommerceInfo();
            et.subElement(block1, "Ecommerce", ecom.getChannel());
            if (!StringUtils.isNullOrEmpty(builder.getInvoiceNumber()) || ecom.getShipMonth() != null) {
                Element direct = et.subElement(block1, "DirectMktData");
                et.subElement(direct, "DirectMktInvoiceNbr").text(builder.getInvoiceNumber());
                et.subElement(direct, "DirectMktShipDay").text(ecom.getShipDay().toString());
                et.subElement(direct, "DirectMktShipMonth").text(ecom.getShipMonth().toString());
            }
        }

        // dynamic descriptor
        et.subElement(block1, "TxnDescriptor", builder.getDynamicDescriptor());

        // auto substantiation
        if (builder.getAutoSubstantiation() != null) {
            Element autoSub = et.subElement(block1, "AutoSubstantiation");

            int index = 0;
            String[] fieldNames = new String[]{"First", "Second", "Third", "Fourth"};
            for (Map.Entry<String, BigDecimal> amount : builder.getAutoSubstantiation().getAmounts().entrySet()) {
                if (amount.getValue() != null && !amount.getValue().equals(new BigDecimal("0"))) {
                    if (index > 3) {
                        throw new BuilderException("You may only specify three different subtotals in a single transaction.");
                    }

                    Element amountNode = et.subElement(autoSub, fieldNames[index++] + "AdditionalAmtInfo");
                    et.subElement(amountNode, "AmtType", amount.getKey());
                    et.subElement(amountNode, "Amt", StringUtils.toNumeric(amount.getValue()));
                }
            }

            et.subElement(autoSub, "MerchantVerificationValue", builder.getAutoSubstantiation().getMerchantVerificationValue());
            et.subElement(autoSub, "RealTimeSubstantiation", builder.getAutoSubstantiation().isRealTimeSubstantiation() ? "Y" : "N");
        }

        // lodging data
//        if(builder.getLodgingData() != null) {
//            LodgingData lodging = builder.getLodgingData();
//
//            Element lodgingElement = et.subElement(block1, "LodgingData");
//            et.subElement(lodgingElement, "PrestigiousPropertyLimit", lodging.getPrestigiousPropertyLimit());
//            et.subElement(lodgingElement, "NoShow", lodging.isNoShow() ? "Y" : "N");
//            et.subElement(lodgingElement, "AdvancedDepositType", lodging.getAdvancedDepositType());
//            et.subElement(lodgingElement, "PreferredCustomer", lodging.isPreferredCustomer() ? "Y" : "N");
//            if(lodging.getFolioNumber() != null || lodging.getStayDuration() != null || lodging.getCheckInDate() != null || lodging.getCheckOutDate() != null || lodging.getRate() != null || lodging.getExtraCharges() != null) {
//                Element lodgingDataEdit = et.subElement(lodgingElement, "LodgingDataEdit");
//                et.subElement(lodgingDataEdit, "FolioNumber", lodging.getFolioNumber());
//                et.subElement(lodgingDataEdit, "Duration", lodging.getStayDuration());
//                if(lodging.getCheckInDate() != null) {
//                    et.subElement(lodgingDataEdit, "CheckInDate", lodging.getCheckInDate().toString("mm/DD/YYYY"));
//                }
//                if(lodging.getCheckOutDate() != null) {
//                    et.subElement(lodgingDataEdit, "CheckOutDate", lodging.getCheckOutDate().toString("mm/DD/YYYY"));
//                }
//                et.subElement(lodgingDataEdit, "Rate", lodging.getRate());
//                if(lodging.getExtraCharges() != null) {
//                    Element extraChargesElement = et.subElement(lodgingDataEdit, "ExtraCharges");
//                    for(ExtraChargeType chargeType: lodging.getExtraCharges().keySet()) {
//                        et.subElement(extraChargesElement, chargeType.toString(), "Y");
//                    }
//                    et.subElement(lodgingDataEdit, "ExtraChargeAmtInfo", lodging.getExtraChargeAmount());
//                }
//            }
//        }

        String response = doTransaction(buildEnvelope(et, transaction, builder.getClientTransactionId(), builder));
        return mapResponse(response, builder.getPaymentMethod());
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException("Portico does not support hosted payments.");
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        TransactionType type = builder.getTransactionType();
        TransactionModifier modifier = builder.getTransactionModifier();
        // payment method
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        PaymentMethodType paymentMethodType = paymentMethod.getPaymentMethodType();
        if (paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            paymentMethod = reference.getOriginalPaymentMethod();
        }

        // build request
        Element transaction = et.element(mapTransactionType(builder));

        if (!type.equals(TransactionType.BatchClose)) {
            PaymentMethodType paymentType = builder.getPaymentMethod().getPaymentMethodType();

            Element root;
            if (type.equals(TransactionType.Reversal)
                    || type.equals(TransactionType.Refund)
                    || paymentType.equals(PaymentMethodType.Gift)
                    || paymentType.equals(PaymentMethodType.ACH)
                    || type.equals(TransactionType.Increment))
                root = et.subElement(transaction, "Block1");
            else root = transaction;

            // amount
            if (builder.getAmount() != null)
                et.subElement(root, "Amt").text(builder.getAmount().toString());

            // auth amount
            if (builder.getAuthAmount() != null)
                et.subElement(root, "AuthAmt").text(builder.getAuthAmount().toString());

            // gratuity
            if (builder.getGratuity() != null)
                et.subElement(root, "GratuityAmtInfo").text(builder.getGratuity().toString());

            // surcharge
            if (builder.getSurchargeAmount() != null) {
                et.subElement(root, "SurchargeAmtInfo", builder.getSurchargeAmount().toString());
            }

            // Transaction ID
            et.subElement(root, "GatewayTxnId", builder.getTransactionId());

            // reversal
            if (type.equals(TransactionType.Reversal) || ((paymentMethodType != null) && paymentType.equals(PaymentMethodType.ACH))) {
                // client transaction id
                et.subElement(root, "ClientTxnId", builder.getClientTransactionId());

                // reversal reason code & PosSequenceNumber
                if (paymentType.equals(PaymentMethodType.Debit)) {
                    if (builder.getEmvChipCondition() != null) {
                        String chipCondition = builder.getEmvChipCondition() == EmvChipCondition.ChipFailPreviousSuccess ? "CHIP_FAILED_PREV_SUCCESS" : "CHIP_FAILED_PREV_FAILED";
                        et.subElement(root, "EMVChipCondition", chipCondition);
                    }
                    et.subElement(root, "ReversalReasonCode", builder.getReversalReasonCode());
                    et.subElement(root, "PosSequenceNbr", builder.getPosSequenceNumber());
                    et.subElement(root, "AccountType",
                            EnumUtils.getMapping(Target.Portico, builder.getAccountType()));

                    // track data
                    if (paymentMethod != null) {
                        DebitTrackData track = (DebitTrackData) paymentMethod;
                        if (type.equals(TransactionType.Reversal))
                            et.subElement(root, "TrackData", track.getValue());
                        et.subElement(root, "PinBlock", track.getPinBlock());

                        EncryptionData encryptionData = track.getEncryptionData();

                        if (encryptionData != null) {
                            Element enc = et.subElement(root, "EncryptionData");
                            et.subElement(enc, "Version").text(encryptionData.getVersion());
                            et.subElement(enc, "EncryptedTrackNumber", encryptionData.getTrackNumber());
                            et.subElement(enc, "KTB", encryptionData.getKtb());
                            et.subElement(enc, "KSN", encryptionData.getKsn());
                        }
                    }
                }

                // tag data
                if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
                    Element tagData = et.subElement(root, "TagData");
                    et.subElement(tagData, "TagValues", builder.getTagData()).set("source", "chip");
                }
            }

            // Level II Data and Level_III Data
            if (builder.getCommercialData() != null) {
                CommercialData cd = builder.getCommercialData();
                if (modifier.equals(TransactionModifier.LevelII) || modifier.equals(TransactionModifier.Level_III)) {
                    Element cpc = et.subElement(root, "CPCData");
                    et.subElement(cpc, "CardHolderPONbr", cd.getPoNumber());
                    et.subElement(cpc, "TaxType", cd.getTaxType());
                    et.subElement(cpc, "TaxAmt", cd.getTaxAmount());
                }

                if (modifier.equals(TransactionModifier.Level_III) && (paymentMethodType != null) && (paymentMethodType.equals(PaymentMethodType.Credit))) {
                    Element cdc = et.subElement(root, "CorporateData");
                    boolean isVisa = cardType.equals("Visa");
                    Element data = et.subElement(cdc, isVisa ? "Visa" : "MC");

                    buildLineItems(et, data, isVisa, cd.getLineItems());

                    if (isVisa) {
                        et.subElement(data, "SummaryCommodityCode", cd.getSummaryCommodityCode());
                        et.subElement(data, "DiscountAmt", cd.getDiscountAmount());
                        et.subElement(data, "FreightAmt", cd.getFreightAmount());
                        et.subElement(data, "DutyAmt", cd.getDutyAmount());
                        et.subElement(data, "DestinationPostalZipCode", cd.getDestinationPostalCode());
                        et.subElement(data, "ShipFromPostalZipCode", cd.getOriginPostalCode());
                        et.subElement(data, "DestinationCountryCode", cd.getDestinationCountryCode());
                        et.subElement(data, "InvoiceRefNbr", cd.getCustomerReferenceId());
                        et.subElement(data, "VATTaxAmtFreight", cd.getAdditionalTaxDetails() != null && cd.getAdditionalTaxDetails()
                                .getTaxAmount() != null ? cd.getAdditionalTaxDetails().getTaxAmount() : cd.getTaxAmount());
                        if (cd.getOrderDate() != null) {
                            et.subElement(data, "OrderDate", cd.getOrderDate().toString("yyyy-MM-dd'T'HH:mm:ss"));
                        }
                        if (cd.getAdditionalTaxDetails() != null) {
                            et.subElement(data, "VATTaxRateFreight", cd.getAdditionalTaxDetails().getTaxRate());
                        }
                    }
                }
            }

            // lodging data
            if (builder.getLodgingData() != null) {
                LodgingData lodging = builder.getLodgingData();

                //Element lodgingElement = et.subElement(root, "LodgingData");
                if (lodging.getExtraCharges() != null) {
                    Element lodgingDataEdit = et.subElement(root, "LodgingDataEdit");

                    Element extraChargesElement = et.subElement(lodgingDataEdit, "ExtraCharges");
                    for (ExtraChargeType chargeType : lodging.getExtraCharges().keySet()) {
                        et.subElement(extraChargesElement, chargeType.toString(), "Y");
                    }
                    et.subElement(lodgingDataEdit, "ExtraChargeAmtInfo", lodging.getExtraChargeAmount());
                }
            }

            // Token Management
            if (builder.getTransactionType() == TransactionType.TokenUpdate
                    || builder.getTransactionType() == TransactionType.TokenDelete
                    || builder.getTransactionType() == TransactionType.Capture) {
                if (builder.getPaymentMethod() instanceof ITokenizable) {
                    ITokenizable token = (ITokenizable) builder.getPaymentMethod();

                    // Set the token value
                    et.subElement(root, "TokenValue", token.getToken());

                    Element tokenActions = et.subElement(root, "TokenActions");
                    if (builder.getTransactionType() == TransactionType.TokenUpdate) {
                        CreditCardData card = (CreditCardData) builder.getPaymentMethod();

                        Element setElement = et.subElement(tokenActions, "Set");

                        Element expMonth = et.subElement(setElement, "Attribute");
                        et.subElement(expMonth, "Name", "expmonth");
                        et.subElement(expMonth, "Value", card.getExpMonth());

                        Element expYear = et.subElement(setElement, "Attribute");
                        et.subElement(expYear, "Name", "expyear");
                        et.subElement(expYear, "Value", card.getExpYear());
                    } else {
                        et.subElement(tokenActions, "Delete");
                    }
                }
            }

            // details
            if (!StringUtils.isNullOrEmpty(builder.getCustomerId()) || !StringUtils.isNullOrEmpty(builder.getDescription()) || !StringUtils.isNullOrEmpty(builder.getInvoiceNumber())) {
                Element addons = et.subElement(root, "AdditionalTxnFields");
                et.subElement(addons, "CustomerID", builder.getCustomerId());
                et.subElement(addons, "Description", builder.getDescription());
                et.subElement(addons, "InvoiceNbr", builder.getInvoiceNumber());
            }
        }

        String response = doTransaction(buildEnvelope(et, transaction, builder.getClientTransactionId(), builder));
        return mapResponse(response, builder.getPaymentMethod());
    }

    public <TResult> TResult processReport(ReportBuilder<TResult> builder, Class<TResult> clazz) throws ApiException {
        ElementTree et = new ElementTree();

        Element transaction = et.element(mapReportType(builder.getReportType()));
        et.subElement(transaction, "TzConversion", builder.getTimeZoneConversion());
        if (builder instanceof TransactionReportBuilder) {
            TransactionReportBuilder<TResult> trb = (TransactionReportBuilder<TResult>) builder;
            if (trb.getTransactionId() != null) {
                et.subElement(transaction, "TxnId", trb.getTransactionId());
            } else {
                Element criteria = et.subElement(transaction, "Criteria");
                et.subElement(criteria, "StartUtcDT", trb.getStartDate() == null ? null : formatDate(trb.getStartDate()));
                et.subElement(criteria, "EndUtcDT", trb.getEndDate() == null ? null : formatDate(trb.getEndDate()));
                et.subElement(criteria, "AuthCode", trb.getSearchBuilder().getAuthCode());
                et.subElement(criteria, "CardHolderLastName", trb.getSearchBuilder().getCardHolderLastName());
                et.subElement(criteria, "CardHolderFirstName", trb.getSearchBuilder().getCardHolderFirstName());
                et.subElement(criteria, "CardNbrFirstSix", trb.getSearchBuilder().getCardNumberFirstSix());
                et.subElement(criteria, "CardNbrLastFour", trb.getSearchBuilder().getCardNumberLastFour());
                et.subElement(criteria, "InvoiceNbr", trb.getSearchBuilder().getInvoiceNumber());
                et.subElement(criteria, "CardHolderPONbr", trb.getSearchBuilder().getCardHolderPoNumber());
                et.subElement(criteria, "CustomerID", trb.getSearchBuilder().getCustomerId());
                et.subElement(criteria, "IssuerResult", trb.getSearchBuilder().getIssuerTransactionId());
                et.subElement(criteria, "SettlementAmt", trb.getSearchBuilder().getSettlementAmount());
                et.subElement(criteria, "IssTxnId", trb.getSearchBuilder().getIssuerTransactionId());
                et.subElement(criteria, "RefNbr", trb.getSearchBuilder().getReferenceNumber());
                et.subElement(criteria, "UserName", trb.getSearchBuilder().getUsername());
                et.subElement(criteria, "ClerkID", trb.getSearchBuilder().getClerkId());
                et.subElement(criteria, "BatchSeqNbr", trb.getSearchBuilder().getBatchSequenceNumber());
                et.subElement(criteria, "BatchId", trb.getSearchBuilder().getBatchId());
                et.subElement(criteria, "SiteTrace", trb.getSearchBuilder().getSiteTrace());
                et.subElement(criteria, "DisplayName", trb.getSearchBuilder().getDisplayName());
                et.subElement(criteria, "ClientTxnId", trb.getSearchBuilder().getClientTransactionId());
                et.subElement(criteria, "UniqueDeviceId", trb.getSearchBuilder().getUniqueDeviceId());
                et.subElement(criteria, "AcctNbrLastFour", trb.getSearchBuilder().getAccountNumberLastFour());
                et.subElement(criteria, "BankRoutingNbr", trb.getSearchBuilder().getBankRoutingNumber());
                et.subElement(criteria, "CheckNbr", trb.getSearchBuilder().getCheckNumber());
                et.subElement(criteria, "CheckFirstName", trb.getSearchBuilder().getCheckFirstName());
                et.subElement(criteria, "CheckLastName", trb.getSearchBuilder().getCheckLastName());
                et.subElement(criteria, "CheckName", trb.getSearchBuilder().getCheckName());
                et.subElement(criteria, "GiftCurrency", trb.getSearchBuilder().getGiftCurrency());
                et.subElement(criteria, "GiftMaskedAlias", trb.getSearchBuilder().getGiftMaskedAlias());
                et.subElement(criteria, "PaymentMethodKey", trb.getSearchBuilder().getPaymentMethodKey());
                et.subElement(criteria, "ScheduleID", trb.getSearchBuilder().getScheduleId());
                et.subElement(criteria, "BuyerEmailAddress", trb.getSearchBuilder().getBuyerEmailAddress());
                et.subElement(criteria, "AltPaymentStatus", trb.getSearchBuilder().getAltPaymentStatus());
                et.subElement(criteria, "SAFIndicator", trb.getSearchBuilder().getSafIndicator());
            }
        }

        String response = doTransaction(buildEnvelope(et, transaction));
        return mapReportResponse(response, builder.getReportType(), clazz);
    }

    private String buildEnvelope(ElementTree et, Element transaction) {
        return buildEnvelope(et, transaction, null, null);
    }
  
    private String buildEnvelope(ElementTree et, Element transaction, String clientTransactionId, TransactionBuilder builder) {
        et.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        et.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        et.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

        Element envelope = et.element("soap:Envelope");

        Element body = et.subElement(envelope, "soap:Body");
        Element request = et.subElement(body, "PosRequest").set("xmlns", "http://Hps.Exchange.PosGateway");
        Element version1 = et.subElement(request, "Ver1.0");

        // header
        Element header = et.subElement(version1, "Header");
        et.subElement(header, "SecretAPIKey", secretApiKey);
        et.subElement(header, "SiteId", siteId);
        et.subElement(header, "LicenseId", licenseId);
        et.subElement(header, "DeviceId", deviceId);
        et.subElement(header, "UserName", username);
        et.subElement(header, "Password", password);
        et.subElement(header, "DeveloperID", developerId);
        et.subElement(header, "VersionNbr", versionNumber);
        et.subElement(header, "ClientTxnId", clientTransactionId);
        et.subElement(header, "PosReqDT", this.getPosReqDT());
        et.subElement(header, "SDKNameVersion", sdkNameVersion != null ? sdkNameVersion : "java;version=" + getReleaseVersion());

        if(builder != null && builder.getIsSAFIndicator() != null) {
            Element safData = et.subElement(header, "SAFData");
            et.subElement(safData, "SAFIndicator", builder.getIsSAFIndicator() ? "Y" : "N");

            if(builder.getSafOrignDT() != null) {
                et.subElement(safData, "SAFOrigDT", builder.getSafOrignDT());
            }else{
                throw new UnsupportedOperationException("SAFData operation not supported without SAFOrigDT");
            }
        }

        // Transaction
        Element trans = et.subElement(version1, "Transaction");
        trans.append(transaction);
        return et.toString(envelope);
    }

    private Transaction mapResponse(String rawResponse, IPaymentMethod paymentMethod) throws ApiException {
        Transaction result = new Transaction();

        Element root = ElementTree.parse(rawResponse).get("PosResponse");
        ArrayList<String> acceptedCodes = new ArrayList<String>();
        acceptedCodes.add("00");
        acceptedCodes.add("0");
        acceptedCodes.add("85");
        acceptedCodes.add("10");
        acceptedCodes.add("02");
        acceptedCodes.add("2");

        // check gateway responses
        String gatewayRspCode = normalizeResponse(root.getString("GatewayRspCode"));
        String gatewayRspText = root.getString("GatewayRspMsg");
        cardType = root.getString("CardType");

        if (!acceptedCodes.contains(gatewayRspCode)) {
            throw new GatewayException(
                    String.format("Unexpected Gateway Response: %s - %s", gatewayRspCode, gatewayRspText),
                    gatewayRspCode,
                    gatewayRspText
            );
        } else {
            result.setAuthorizedAmount(root.getDecimal("AuthAmt"));
            result.setAvailableBalance(root.getDecimal("AvailableBalance"));
            result.setAvsResponseCode(root.getString("AVSRsltCode"));
            result.setAvsResponseMessage(root.getString("AVSRsltText"));
            result.setBalanceAmount(root.getDecimal("BalanceAmt"));
            result.setCardType(root.getString("CardType"));
            result.setCardLast4(root.getString("TokenPANLast4"));
            result.setCavvResponseCode(root.getString("CAVVResultCode"));
            result.setCommercialIndicator(root.getString("CPCInd"));
            result.setCvnResponseCode(root.getString("CVVRsltCode"));
            result.setCvnResponseMessage(root.getString("CVVRsltText"));
            result.setEmvIssuerResponse(root.getString("EMVIssuerResp"));
            result.setPointsBalanceAmount(root.getDecimal("PointsBalanceAmt"));
            result.setRecurringDataCode(root.getString("RecurringDataCode"));
            result.setReferenceNumber(root.getString("RefNbr"));
            result.setCardBrandTransactionId(root.getString("CardBrandTxnId"));

            String responseCode = normalizeResponse(root.getString("RspCode"));
            String responseText = root.getString("RspText", "RspMessage");
            result.setResponseCode(responseCode != null ? responseCode : gatewayRspCode);
            result.setResponseMessage(responseText != null ? responseText : gatewayRspText);
            result.setTransactionDescriptor(root.getString("TxnDescriptor"));
            result.setResponseDate(root.getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"), "RspDT"));
            result.setHostResponseDate(root.getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"), "HostRspDT"));

            if (paymentMethod != null) {
                TransactionReference reference = new TransactionReference();
                reference.setPaymentMethodType(paymentMethod.getPaymentMethodType());
                reference.setTransactionId(root.getString("GatewayTxnId"));
                reference.setClientTransactionId(root.getString("ClientTxnId"));
                reference.setAuthCode(root.getString("AuthCode"));
                result.setTransactionReference(reference);
            }

            // gift card create data
            if (root.has("CardData")) {
                GiftCard giftCard = new GiftCard();
                giftCard.setNumber(root.getString("CardNbr"));
                giftCard.setAlias(root.getString("Alias"));
                giftCard.setPin(root.getString("PIN"));

                result.setGiftCard(giftCard);
            }

            // token data
            if (root.has("TokenData")) {
                result.setToken(root.getString("TokenValue"));
            }

            // batch information
            if (root.has("BatchId")) {
                BatchSummary summary = new BatchSummary();
                summary.setBatchId(root.getInt("BatchId"));
                summary.setTransactionCount(root.getInt("TxnCnt"));
                summary.setTotalAmount(root.getDecimal("TotalAmt"));
                summary.setSequenceNumber(root.getString("BatchSeqNbr"));
                result.setBatchSummary(summary);
            }

            // debit mac
            if (root.has("DebitMac")) {
                DebitMac debitMac = new DebitMac();
                debitMac.setTransactionCode(root.getString("TransactionCode"));
                debitMac.setTransmissionNumber(root.getString("TransmissionNumber"));
                debitMac.setBankResponseCode(root.getString("BankResponseCode"));
                debitMac.setMacKey(root.getString("MacKey"));
                debitMac.setPinKey(root.getString("PinKey"));
                debitMac.setFieldKey(root.getString("FieldKey"));
                debitMac.setTraceNumber(root.getString("TraceNumber"));
                debitMac.setMessageAuthenticationCode(root.getString("MessageAuthenticationCode"));
                result.setDebitMac(debitMac);

                // add the track data for debit interact
                result.getTransactionReference().setOriginalPaymentMethod(paymentMethod);
            }

            //duplicate transaction
            if (root.has("AdditionalDuplicateData")) {
                AdditionalDuplicateData additionalDuplicateData = new AdditionalDuplicateData();
                additionalDuplicateData.setOriginalGatewayTxnId(root.getString("OriginalGatewayTxnId"));
                additionalDuplicateData.setOriginalRspDT(root.getDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"), "OriginalRspDT").toString());
                additionalDuplicateData.setOriginalClientTxnId(root.getString("OriginalClientTxnId"));
                additionalDuplicateData.setOriginalAuthCode(root.getString("OriginalAuthCode"));
                additionalDuplicateData.setOriginalRefNbr(root.getString("OriginalRefNbr"));
                additionalDuplicateData.setOriginalAuthAmt(root.getDecimal("OriginalAuthAmt"));
                additionalDuplicateData.setOriginalCardType(root.getString("OriginalCardType"));
                additionalDuplicateData.setOriginalCardNbrLast4(root.getString("OriginalCardNbrLast4"));

                // add the duplicate data
                result.setAdditionalDuplicateData(additionalDuplicateData);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <TResult> TResult mapReportResponse(String rawResponse, ReportType reportType, Class<TResult> clazz) throws ApiException {
        Element response = ElementTree.parse(rawResponse).get("PosResponse");
        ArrayList<String> acceptedCodes = new ArrayList<String>();
        acceptedCodes.add("00");
        acceptedCodes.add("0");

        // check gateway responses
        String gatewayRspCode = normalizeResponse(response.getString("GatewayRspCode"));
        String gatewayRspText = response.getString("GatewayRspMsg");

        if (!acceptedCodes.contains(gatewayRspCode)) {
            throw new GatewayException(
                    String.format("Unexpected Gateway Response: %s - %s", gatewayRspCode, gatewayRspText),
                    gatewayRspCode,
                    gatewayRspText
            );
        }

        Element doc = ElementTree.parse(rawResponse).get(mapReportType(reportType));

        try {
            TResult rvalue = clazz.newInstance();
            if (reportType.equals(ReportType.FindTransactions) || reportType.equals(ReportType.Activity) || reportType.equals(ReportType.TransactionDetail)) {
                // Activity
                if (rvalue instanceof ActivityReport) {
                    ActivityReport list = new ActivityReport();
                    for (Element detail : doc.getAll("Details")) {
                        list.add(hydrateTransactionSummary(detail));
                    }
                } else if (rvalue instanceof TransactionSummaryList) {
                    for (Element transaction : doc.getAll("Transactions")) {
                        ((TransactionSummaryList) rvalue).add(hydrateTransactionSummary(transaction));
                    }
                } else {
                    rvalue = (TResult) hydrateTransactionSummary(doc);
                }
            }
            return rvalue;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    private String normalizeResponse(String input) {
        if (input != null) {
            if (input.equals("0") || input.equals("85"))
                return "00";
        }
        return input;
    }

    private <T extends TransactionBuilder<Transaction>> String mapTransactionType(T builder) throws ApiException {
        TransactionModifier modifier = builder.getTransactionModifier();
        PaymentMethodType paymentMethodType = null;
        if (builder.getPaymentMethod() != null)
            paymentMethodType = builder.getPaymentMethod().getPaymentMethodType();

        switch (builder.getTransactionType()) {
            case BatchClose:
                return "BatchClose";
            case Decline:
                if (modifier.equals(TransactionModifier.ChipDecline))
                    return "ChipCardDecline";
                else if (modifier.equals(TransactionModifier.FraudDecline))
                    return "OverrideFraudDecline";
                throw new UnsupportedTransactionException();
            case Verify:
                if (modifier.equals(TransactionModifier.EncryptedMobile))
                    throw new UnsupportedTransactionException("Transaction not supported for this payment method.");
                return "CreditAccountVerify";
            case Capture:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "CreditAddToBatch";
                    else if (paymentMethodType.equals(PaymentMethodType.Debit))
                        return "DebitAddToBatch";
                    throw new UnsupportedTransactionException("Transaction not supported for this payment method.");
                }
            case Auth:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit)) {
                        if (modifier.equals(TransactionModifier.Additional))
                            return "CreditAdditionalAuth";
                        else if (modifier.equals(TransactionModifier.Incremental))
                            return "CreditIncrementalAuth";
                        else if (modifier.equals(TransactionModifier.Offline))
                            return "CreditOfflineAuth";
                        else if (modifier.equals(TransactionModifier.Recurring))
                            return "RecurringBillingAuth";
//                        else if (modifier.equals(TransactionModifier.EncryptedMobile))
//                            throw new UnsupportedTransactionException("Transaction not supported for this payment method.");
                        return "CreditAuth";
                    } else if (paymentMethodType.equals(PaymentMethodType.Recurring))
                        return "RecurringBillingAuth";
                    else if (paymentMethodType.equals(PaymentMethodType.Debit))
                        return "DebitAuth";
                    throw new UnsupportedTransactionException("Transaction not supported for this payment method.");
                }
            case Sale:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit)) {
                        if (modifier.equals(TransactionModifier.Offline))
                            return "CreditOfflineSale";
                        else if (modifier.equals(TransactionModifier.Recurring))
                            return "RecurringBilling";
                        else return "CreditSale";
                    } else if (paymentMethodType.equals(PaymentMethodType.Recurring)) {
                        if (((RecurringPaymentMethod) builder.getPaymentMethod()).getPaymentType().equals("ACH"))
                            return "CheckSale";
                        return "RecurringBilling";
                    } else if (paymentMethodType.equals(PaymentMethodType.Debit))
                        return "DebitSale";
                    else if (paymentMethodType.equals(PaymentMethodType.Cash))
                        return "CashSale";
                    else if (paymentMethodType.equals(PaymentMethodType.ACH))
                        return "CheckSale";
                    else if (paymentMethodType.equals(PaymentMethodType.EBT)) {
                        if (modifier.equals(TransactionModifier.CashBack))
                            return "EBTCashBackPurchase";
                        else if (modifier.equals(TransactionModifier.Voucher))
                            return "EBTVoucherPurchase";
                        else return "EBTFSPurchase";
                    } else if (paymentMethodType.equals(PaymentMethodType.Gift))
                        return "GiftCardSale";
                    throw new UnsupportedTransactionException();
                }
            case Refund:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "CreditReturn";
                    else if (paymentMethodType.equals(PaymentMethodType.Debit)) {
                        if (builder.getPaymentMethod() instanceof TransactionReference)
                            throw new UnsupportedTransactionException();
                        return "DebitReturn";
                    } else if (paymentMethodType.equals(PaymentMethodType.Cash))
                        return "CashReturn";
                    else if (paymentMethodType.equals(PaymentMethodType.EBT)) {
                        if (builder.getPaymentMethod() instanceof TransactionReference)
                            throw new UnsupportedTransactionException();
                        return "EBTFSReturn";
                    }
                    throw new UnsupportedTransactionException();
                }
            case Reversal:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "CreditReversal";
                    else if (paymentMethodType.equals(PaymentMethodType.Debit)) {
//                    I don't know why this is here, but it doesn't seem to be valid removing for now
//                    if(builder.getPaymentMethod() instanceof TransactionReference)
//                        throw new UnsupportedTransactionException();
                        return "DebitReversal";
                    } else if (paymentMethodType.equals(PaymentMethodType.Gift))
                        return "GiftCardReversal";
                    else if (paymentMethodType.equals(PaymentMethodType.EBT))
                        return "EBTFSReversal";
                    throw new UnsupportedTransactionException();
                }
            case Edit:
                if (modifier.equals(TransactionModifier.LevelII) || modifier.equals(TransactionModifier.Level_III))
                    return "CreditCPCEdit";
                else return "CreditTxnEdit";
            case Void:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "CreditVoid";
                    else if (paymentMethodType.equals(PaymentMethodType.ACH))
                        return "CheckVoid";
                    else if (paymentMethodType.equals(PaymentMethodType.Gift))
                        return "GiftCardVoid";
                    throw new UnsupportedTransactionException();
                }
            case AddValue:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "PrePaidAddValue";
                    else if (paymentMethodType.equals(PaymentMethodType.Debit))
                        return "DebitAddValue";
                    else if (paymentMethodType.equals(PaymentMethodType.Gift))
                        return "GiftCardAddValue";
                    throw new UnsupportedTransactionException();
                }
            case Balance:
                if (paymentMethodType != null) {
                    if (paymentMethodType.equals(PaymentMethodType.EBT))
                        return "EBTBalanceInquiry";
                    else if (paymentMethodType.equals(PaymentMethodType.Gift))
                        return "GiftCardBalance";
                    else if (paymentMethodType.equals(PaymentMethodType.Credit))
                        return "PrePaidBalanceInquiry";
                    throw new UnsupportedTransactionException();
                }
            case BenefitWithdrawal:
                return "EBTCashBenefitWithdrawal";
            case Activate:
                return "GiftCardActivate";
            case Alias:
                return "GiftCardAlias";
            case Deactivate:
                return "GiftCardDeactivate";
            case Replace:
                return "GiftCardReplace";
            case Reward:
                return "GiftCardReward";
            case Increment:
                return "CreditIncrementalAuth";
            case Tokenize:
                return "Tokenize";
            case TokenUpdate:
            case TokenDelete:
                return "ManageTokens";
            default:
                throw new UnsupportedTransactionException();
        }

    }

    private String mapReportType(ReportType type) throws UnsupportedTransactionException {
        switch (type) {
            case TransactionDetail:
                return "ReportTxnDetail";
            case Activity:
            case FindTransactions:
                return "FindTransactions";
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private String getToken(IPaymentMethod paymentMethod) {
        if (paymentMethod instanceof ITokenizable) {
            String tokenValue = ((ITokenizable) paymentMethod).getToken();
            if (tokenValue != null && !tokenValue.equals(""))
                return tokenValue;
            return null;
        }
        return null;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return sdf.format(date);
    }

    private TransactionSummary hydrateTransactionSummary(Element root) {
        TransactionSummary summary = new TransactionSummary();
        summary.setAccountDataSource(root.getString("AcctDataSrc"));
        summary.setAmount(root.getDecimal("Amt"));
        summary.setAuthCode(root.getString("AuthCode"));
        summary.setAuthorizedAmount(root.getDecimal("AuthAmt"));
        summary.setBatchCloseDate(root.getDateTime("BatchCloseDT"));
        summary.setBatchSequenceNumber(root.getString("BatchSeqNbr"));
        summary.setCaptureAmount(root.getDecimal("CaptureAmtInfo"));
        summary.setCardSwiped(root.getString("CardSwiped"));
        summary.setCardType(root.getString("CardType"));
        summary.setCavvResponseCode(root.getString("CAVVResultCode"));
        summary.setClerkId(root.getString("ClerkID"));
        summary.setClientTransactionId(root.getString("ClientTxnId"));
        summary.setCompanyName(root.getString("Company"));
        summary.setConvenienceAmount(root.getDecimal("ConvenienceAmtInfo"));
        summary.setCustomerFirstName(root.getString("CustomerFirstname"));
        summary.setCustomerId(root.getString("CustomerID"));
        summary.setCustomerLastName(root.getString("CustomerLastname"));
        summary.setDebtRepaymentIndicator(root.getString("DebtRepaymentIndicator") == "1");
        summary.setDescription(root.getString("Description"));
        summary.setEmvChipCondition(root.getString("EMVChipCondition"));
        summary.setEmvIssuerResponse(root.getString("EMVIssuerResp"));
        summary.setFraudRuleInfo(root.getString("FraudInfoRule"));
        summary.setFullyCaptured(root.getString("FullyCapturedInd") == "1");
        summary.setGatewayResponseCode(normalizeResponse(root.getString("GatewayRspCode")));
        summary.setGatewayResponseMessage(root.getString("GatewayRspMsg"));
        summary.setGiftCurrency(root.getString("GiftCurrency"));
        summary.setGratuityAmount(root.getDecimal("GratuityAmtInfo"));
        summary.setHasEmvTags(root.getString("HasEMVTag") == "1");
        summary.setHasEcomPaymentData(root.getString("HasEComPaymentData") == "1");
        summary.setInvoiceNumber(root.getString("InvoiceNbr"));
        summary.setIssuerResponseCode(root.getString("IssuerRspCode", "RspCode"));
        summary.setIssuerResponseMessage(root.getString("IssuerRspText", "RspText"));
        summary.setIssuerTransactionId(root.getString("IssTxnId"));
        summary.setMaskedAlias(root.getString("GiftMaskedAlias"));
        summary.setMaskedCardNumber(root.getString("MaskedCardNbr"));
        summary.setOneTimePayment(root.getString("OneTime") == "1");
        summary.setOriginalTransactionId(root.getString("OriginalGatewayTxnId"));
        summary.setPaymentMethodKey(root.getString("PaymentMethodKey"));
        summary.setPaymentType(root.getString("PaymentType"));
        summary.setPoNumber(root.getString("CardHolderPONbr"));
        summary.setRecurringDataCode(root.getString("RecurringDataCode"));
        summary.setReferenceNumber(root.getString("RefNbr"));
        summary.setResponseDate(root.getDateTime("RspDT"));
        summary.setScheduleId(root.getString("ScheduleID"));
        summary.setServiceName(root.getString("ServiceName"));
        summary.setSettlementAmount(root.getDecimal("SettlementAmt"));
        summary.setShippingAmount(root.getDecimal("ShippingAmtInfo"));
        summary.setStatus(root.getString("Status", "TxnStatus"));
        summary.setSurchargeAmount(root.getDecimal("SurchargeAmtInfo"));
        summary.setTaxType(root.getString("TaxType"));
        summary.setTokenPanLastFour(root.getString("TokenPANLast4"));
        summary.setTransactionDate(root.getDateTime("TxnUtcDT", "ReqUtcDT"));
        summary.setTransactionDescriptor(root.getString("TxnDescriptor"));
        summary.setTransactionId(root.getString("GatewayTxnId"));
        summary.setTransactionStatus(root.getString("TxnStatus"));
        summary.setUniqueDeviceId(root.getString("UniqueDeviceId"));
        summary.setUsername(root.getString("UserName"));
        summary.setHasLevelIII(root.getString("HasLevelIII"));

        // lodging data
        if (root.has("LodgingData")) {
            LodgingData lodgingData = new LodgingData();

            String advancedDepositType = root.getString("AdvancedDepositType");
            ReverseStringEnumMap<AdvancedDepositType> map = new ReverseStringEnumMap<AdvancedDepositType>(AdvancedDepositType.class);
            lodgingData.setAdvancedDepositType(map.get(advancedDepositType));
            lodgingData.setLodgingDataEdit(root.getString("LodgingDataEdit"));
            summary.setLodgingData(lodgingData);
        }

        // check data
        if (root.has("CheckData")) {
            CheckData checkData = new CheckData();
            checkData.setAccountInfo(root.getString("AccountInfo"));
            checkData.setCheckAction(root.getString("CheckAction"));
            checkData.setCheckType(root.getString("CheckType"));
            checkData.setConsumerInfo(root.getString("ConsumerInfo"));
            checkData.setDataEntryMode(root.getString("DataEntryMode"));
            checkData.setSecCode(root.getString("SECCode"));
            summary.setCheckData(checkData);
        }

        // alt payment data
        if (root.has("AltPaymentData")) {
            AltPaymentData altPaymentData = new AltPaymentData();
            altPaymentData.setBuyerEmailAddress(root.getString("BuyerEmailAddress"));
            altPaymentData.setStateDate(root.getDate("StatusDT"));
            altPaymentData.setStatus(root.getString("Status"));
            altPaymentData.setStatusMessage(root.getString("StatusMsg"));
            summary.setAltPaymentData(altPaymentData);
        }

        // card holder data
        if (root.has("CardHolderData")) {
            summary.setCardHolderFirstName(root.getString("CardHolderFirstName"));
            summary.setCardHolderLastName(root.getString("CardHolderLastName"));
            Address address = new Address();
            address.setStreetAddress1(root.getString("CardHolderAddr"));
            address.setCity(root.getString("CardHolderCity"));
            address.setState(root.getString("CardHolderState"));
            address.setPostalCode(root.getString("CardHolderZip"));
            summary.setBillingAddress(address);
        }

        return summary;
    }

    // Get the SDK release version
    private String getReleaseVersion() {
        String version = "";
        try {
            Document pomXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("pom.xml"));
            org.w3c.dom.Element pomRoot = (org.w3c.dom.Element) pomXml.getElementsByTagName("project").item(0);
            version = pomRoot.getElementsByTagName("version").item(0).getTextContent();
        } catch (Exception ex) {
            System.out.println("JAVA SDK version could not be extracted from pom.xml file.");
        }
        return version;
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new ApiException("Portico does not support KeepAlive.");
    }

    protected String getPosReqDT() {
        // Override format/parse methods to handle differences in `X` and `Z` format identifiers
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ") {
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
                StringBuffer rfcFormat = super.format(date, toAppendTo, pos);
                return rfcFormat.insert(rfcFormat.length() - 2, ":");
            }

            @Override
            public Date parse(String text, ParsePosition pos) {
                if (text.length() > 3) {
                    text = text.substring(0, text.length() - 3) + text.substring(text.length() - 2);
                }
                return super.parse(text, pos);
            }
        };

        return dateFormat.format(new Date()).replace("::", ":");
    }

    private int getSecure3DVersion(Secure3dVersion version) {
        if (version == null) {
            return 1;
        }
        switch (version) {
            case TWO:
                return 2;
            case ONE:
            case ANY:
            default:
                return 1;
        }
    }

    private boolean isAppleOrGooglePay(PaymentDataSourceType paymentDataSource) {
        return paymentDataSource == PaymentDataSourceType.APPLEPAY
                || paymentDataSource == PaymentDataSourceType.APPLEPAYAPP
                || paymentDataSource == PaymentDataSourceType.APPLEPAYWEB
                || paymentDataSource == PaymentDataSourceType.GOOGLEPAYAPP
                || paymentDataSource == PaymentDataSourceType.GOOGLEPAYWEB;
    }

    private void buildLineItems(ElementTree et, Element data, boolean isVisa, List<CommercialLineItem> items) {
        if (items == null || items.size() == 0) {
            return;
        }

        Element lineItems = et.subElement(data, "LineItems");
        for (CommercialLineItem item : items) {
            Element lineItem = et.subElement(lineItems, "LineItemDetail");
            et.subElement(lineItem, "ItemDescription", item.getDescription());
            et.subElement(lineItem, "ProductCode", item.getProductCode());
            et.subElement(lineItem, "Quantity", item.getQuantity());
            et.subElement(lineItem, "ItemTotalAmt", item.getTotalAmount());
            et.subElement(lineItem, "UnitOfMeasure", item.getUnitOfMeasure());

            if (!isVisa) {
                continue;
            }

            // The schema says this field should exist, but it's not currently allowed.
            et.subElement(lineItem, "ItemCommodityCode ", item.getCommodityCode());
            et.subElement(lineItem, "UnitCost", item.getUnitCost());
            et.subElement(lineItem, "VATTaxAmt", item.getTaxAmount());
            et.subElement(lineItem, "DiscountAmt", item.getDiscountDetails().getDiscountAmount());
        }
    }
}
