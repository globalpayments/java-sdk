package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.AccountType;
import com.global.api.network.entities.gnap.*;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.enums.gnap.*;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.network.enums.gnap.CardType;
import com.global.api.network.enums.gnap.GnapFIDS;
import com.global.api.network.enums.gnap.LogMessages;
import com.global.api.network.enums.NetworkProcessingFlag;
import com.global.api.network.enums.gnap.TransactionCode;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.GatewayConnectorConfig;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.*;
import org.joda.time.DateTime;

import java.math.BigDecimal;

public class GnapConnector extends GatewayConnectorConfig {
    boolean isCreditCard;
    boolean isDebitCard;
    boolean isEmvDataPresent;
    final String fs = String.valueOf((char) 0x1C);
    final String rs = String.valueOf((char) 0x1E);
    private TransactionCode transCode=null;

    @Override
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    public NetworkProcessingFlag getProcessingFlag() {
        return processingFlag;
    }

    public void setProcessingFlag(NetworkProcessingFlag processingFlag) {
        this.processingFlag = processingFlag;
    }

    @Override
    public void setAcceptorConfig(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {

        MessageWriter mr = new MessageWriter();
        GnapRequestData reqData = builder.getGnapRequestData();
        GnapMessageHeader header = reqData.getGnapMessageHeader();
        GnapProdSubFids prodSubFids = reqData.getGnapProdSubFids();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        CardBrand cardBrand=null;
        OptionalData optionalData = builder.getGnapRequestData().getOptionalData();
        if(header!=null) {
            header.setCurrentDate(DateTime.now().toString("yyMMdd"));
            header.setCurrentTime(DateTime.now().toString("hhmmss"));
        }

        if (paymentMethod != null) {
            cardBrand=GnapUtils.getCardBrand(paymentMethod);
            isCreditCard = paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit) ||paymentMethod instanceof Credit;
            isDebitCard = paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit) ||paymentMethod instanceof Debit;
        }

        TransactionType transactionType = builder.getTransactionType();
        EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
        isEmvDataPresent = tagData != null;
        setTransactionCodeInHeader(header,builder);
        if(isCreditCard){
            reqData.setCardType(CardType.Credit);
        }else if(isDebitCard){
            reqData.setCardType(CardType.Debit);
        }

        GnapUtils.log("***************Request Header*****************", "*****");
        MessageWriter headerWriter = buildRequestHeader(header, acceptorConfig);
        GnapUtils.log("Final Request Header", new String(headerWriter.toArray()));
        MapGnapFids mapFids = new MapGnapFids(mr);
        mr.addRange(headerWriter.toArray());

        GnapUtils.log("***************Request Fids*****************", "*****");

        //FID_B("Transaction Amount")
            mapFids.addFid(GnapFIDS.FID_B, getTransactionAmount(builder));

        //FID_D("Account Type")
        if(cardBrand!=null) {
            if ((isDebitCard || cardBrand.equals(CardBrand.UnionPay)) && reqData.getAccountType() != null) {
                mapFids.addFid(GnapFIDS.FID_D, accountTypeMapping(reqData.getAccountType()));
            }
        }

        //FID_F("Approval Code")
        if(isEmvDataPresent && tagData.getApprovalCode()!=null){
                mapFids.addFid(GnapFIDS.FID_F, StringUtils.padRight(tagData.getApprovalCode(), 8, ' '));
        }else if (reqData.getApprovalCode() != null) {
            mapFids.addFid(GnapFIDS.FID_F, StringUtils.padRight(reqData.getApprovalCode(), 8, ' '));
        }

        //FID_P("Draft Capture Flag")
        if(cardBrand!=null) {
            mapFids.addFid(GnapFIDS.FID_P, getDraftCapture(cardBrand, transactionType));
        }
        //FID_Q("Echo Data")
        mapFids.addFid(GnapFIDS.FID_Q, reqData.getEchoData());

        //FID_R("Card Type")
        mapFids.addFid(GnapFIDS.FID_R, reqData.getCardType());

        //FID_S("Invoice Number")
        mapFids.addFid(GnapFIDS.FID_S, reqData.getInvoiceNumber());

        //FID_U("Language Code")
        if(paymentMethod instanceof Credit){
            mapFids.addFid(GnapFIDS.FID_U, reqData.getLanguageCode());
        }else if(paymentMethod instanceof Debit && tagData !=null){
            if(tagData.getLanguageCode()!=null) {
                mapFids.addFid(GnapFIDS.FID_U, tagData.getLanguageCode());
            }else{
                mapFids.addFid(GnapFIDS.FID_U, reqData.getLanguageCode());
            }
        }
        //FID_X("ISO Response Code")
        if(isEmvDataPresent && header.getMessageSubType().equals(MessageSubType.StoreAndForwardTransactions)){
            mapFids.addFid(GnapFIDS.FID_U, tagData.getIsoResponseCode());
        }


        //FID_a("Optional Data")
        if(cardBrand!=null && optionalData != null) {
            mapFids.addFid(GnapFIDS.FID_a, optionalData.getOptionalData(transactionType, header.getTransactionCode(), cardBrand, reqData.getCardType()));
        }

        //FID_b("PIN Block")
        if(paymentMethod instanceof IPinProtected) {
            String pinBlock = ((IPinProtected)paymentMethod).getPinBlock();
            if(!StringUtils.isNullOrEmpty(pinBlock)) {
                mapFids.addFid(GnapFIDS.FID_b, pinBlock);
            }
        }

        //FID_d("Retailer ID")
        mapFids.addFid(GnapFIDS.FID_d, reqData.getRetailerID());

        //FID_e("POS Condition Code")
        mapFids.addFid(GnapFIDS.FID_e, reqData.getPosConditionCode());

        //FID_h("Sequence Number")
        if (reqData.getSequenceNumber() != null) {
            mapFids.addFid(GnapFIDS.FID_h, reqData.getSequenceNumber().getValue());
        }

        //FID_p("Paypass Device Type Indicator")
        if(isEmvDataPresent) {
            mapFids.addFid(GnapFIDS.FID_p, tagData.getDeviceTypeIndicator());
        }

        //FID_q("Track 2 Data")
        if(paymentMethod !=null) {
            if (paymentMethod instanceof IEncryptable && acceptorConfig.isSupportsE2EEEncryption()) {
                EncryptionData encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
                if (encryptionData != null) {
                    mapFids.addFid(GnapFIDS.FID_q, encryptionData.getTrackNumber());
                }
            } else if (paymentMethod instanceof ITrackData) {
                ITrackData card = (ITrackData) paymentMethod;
                    mapFids.addFid(GnapFIDS.FID_q, card.getValue());
            } else if (paymentMethod instanceof ICardData) {
                ICardData card = (ICardData) paymentMethod;
                mapFids.addFid(GnapFIDS.FID_q, getManualTrackData(card.getNumber(), getShortExpiry(card)));
            }
        }

        //FID_t("Global Payments Pinpad serial Number")
        mapFids.addFid(GnapFIDS.FID_t, acceptorConfig.getPinPadSerialNumber());

        //FID_z("Union Pay Indicator")
        if(cardBrand!=null) {
            if (cardBrand.equals(CardBrand.UnionPay))
                mapFids.addFid(GnapFIDS.FID_z, UnionPayIndicator.UnionPayTransaction);
            else
                mapFids.addFid(GnapFIDS.FID_z, UnionPayIndicator.NonUnionPayTransaction);
        }
        //FID_4("Message Reason Codes for Merchant Initiated Transactions")
        mapFids.addFid(GnapFIDS.FID_4, reqData.getMerchantReasonCodes());

        //FID_6("Product Sub-FIDs")
        if (prodSubFids != null) {
            mapFids.addFid(GnapFIDS.FID_6, "");

            //subFID_B("CVV2/CVC2 Value")
            if (prodSubFids.getCvv2Value() != null) {
                mapFids.addSubFid(GnapSubFids.subFID_B, StringUtils.padRight(prodSubFids.getCvv2Value(), 4, ' '));
            }

            //subFID_E("POS Entry Mode")
            if(paymentMethod !=null) {
                if(paymentMethod instanceof ITrackData) {
                    ITrackData card = (ITrackData) paymentMethod;
                    String entryMode = card.getEntryMethod().getValue() + acceptorConfig.getPinCapability().getValue();
                    mapFids.addSubFid(GnapSubFids.subFID_E, entryMode);
                }else if(paymentMethod instanceof  ICardData) {
                    String entryMode = mapEntryMethod(EntryMethod.Manual) + acceptorConfig.getPinCapability().getValue();
                    mapFids.addSubFid(GnapSubFids.subFID_E, entryMode);
                }
            }

            //subFID_H("Card Verification Digits Presence indicator and Result")
            if (isCreditCard && (transactionType.equals(TransactionType.Sale) || transactionType.equals(TransactionType.Auth))
                    && prodSubFids.getCardPresenceIndicator() != null)
                mapFids.addSubFid(GnapSubFids.subFID_H, StringUtils.padRight(prodSubFids.getCardPresenceIndicator().getValue(), 2, ' '));


            //subFID_I("Transaction Currency Code (TCC)")
            if (isEmvDataPresent && tagData.getTransactionCurrencyCode()!=null)
                mapFids.addSubFid(GnapSubFids.subFID_I, tagData.getTransactionCurrencyCode().substring(1));


            if (isEmvDataPresent && !header.getTransactionCode().equals(TransactionCode.TelephoneAuthPurchase)){
                //subFID_O("EMV Request Data")
                mapFids.addSubFid(GnapSubFids.subFID_O, getEmvReqData(tagData));
                //subFID_P("EMV Additional Request Data")
                mapFids.addSubFid(GnapSubFids.subFID_P, getEmvAdditionalReqData(tagData));
            }

            //subFID_S("UnionPay Online PIN DUKPT KSN")
            mapFids.addSubFid(GnapSubFids.subFID_S, prodSubFids.getUnionPayOnlinePINDUKPTKSN());

            //subFID_T("DUKPT KSN")
            if(paymentMethod instanceof IEncryptable && acceptorConfig.isSupportsE2EEEncryption()){
                EncryptionData encryptionData= ((IEncryptable)paymentMethod).getEncryptionData();
                if(encryptionData != null) {
                    mapFids.addSubFid(GnapSubFids.subFID_T,encryptionData.getKsn());
                }
            }

            //subFID_X("Point of Service Data")
            if (prodSubFids.getPointOfServiceData() != null)
                mapFids.add(GnapSubFids.subFID_X, prodSubFids.getPointOfServiceData().getValue());

            //subFID_q("Paywave Form Factor Indicator")
            if (isEmvDataPresent)
                mapFids.addSubFid(GnapSubFids.subFID_q, tagData.getFormFactorIndicator());
        }

        mr.add(ControlCodes.ETX);
        IDeviceMessage buildMessage = new DeviceMessage(mr.toArray());
        GnapUtils.log("Final Request", buildMessage.toString());

        return sendRequest(mr, builder);
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {

        MessageWriter mr = new MessageWriter();
        GnapRequestData reqData = builder.getGnapRequestData();
        GnapMessageHeader header = reqData.getGnapMessageHeader();
        GnapProdSubFids prodSubFids = reqData.getGnapProdSubFids();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        CardBrand cardBrand=GnapUtils.getCardBrand(paymentMethod);
        TransactionReference reference = null;
        OptionalData optionalData = builder.getGnapRequestData().getOptionalData();
        boolean isInstanceOfReference = false;

        if(header!=null) {
            header.setCurrentDate(DateTime.now().toString("yyMMdd"));
            header.setCurrentTime(DateTime.now().toString("hhmmss"));
        }

        if (paymentMethod instanceof TransactionReference) {
            reference = (TransactionReference) paymentMethod;
            if(reference!=null) {
                paymentMethod = reference.getOriginalPaymentMethod();
            }
            isInstanceOfReference=true;
            cardBrand=GnapUtils.getCardBrand(paymentMethod);
            isCreditCard = paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit) ||paymentMethod instanceof Credit;
            isDebitCard = paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit) ||paymentMethod instanceof Debit;
        }

        setTransactionCodeInHeader(header,builder);
        TransactionType transactionType = builder.getTransactionType();
        EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
        isEmvDataPresent = tagData != null;

        if(isCreditCard){
            reqData.setCardType(CardType.Credit);
        }else if(isDebitCard){
            reqData.setCardType(CardType.Debit);
        }

        GnapUtils.log("***************Request Header*****************", "*****");
        MessageWriter headerWriter = buildRequestHeader(header, acceptorConfig);
        GnapUtils.log("Final Request Header", new String(headerWriter.toArray()));
        MapGnapFids mapFids = new MapGnapFids(mr);
        mr.addRange(headerWriter.toArray());

        GnapUtils.log("***************Request Fids*****************", "*****");
        if(!transactionType.equals(TransactionType.BatchClose)) {

            //FID_B("Transaction Amount")
            mapFids.addFid(GnapFIDS.FID_B, getTransactionAmount(builder));

            //FID_C("Original Transaction Amount")
            if(reference != null) {
                if (reference.getOriginalApprovedAmount() != reference.getOriginalAmount())
                    mapFids.addFid(GnapFIDS.FID_C, StringUtils.toNumeric(reference.getOriginalApprovedAmount()));
                else if (transactionType.equals(TransactionType.PreAuthCompletion))
                    mapFids.addFid(GnapFIDS.FID_C, StringUtils.toNumeric(reference.getOriginalAmount()));
            }
            //FID_D("Account Type")
            if((isDebitCard || cardBrand.equals(CardBrand.UnionPay)) && reqData.getAccountType()!=null) {
                mapFids.addFid(GnapFIDS.FID_D, accountTypeMapping(reqData.getAccountType()));
            }

            //FID_F("Approval Code")
            if(reference != null) {
                if (isInstanceOfReference && reference.getOriginalProcessingCode() != null && isApprovalCodeValid(cardBrand, transactionType)) {
                    mapFids.addFid(GnapFIDS.FID_F, StringUtils.padRight(reference.getOriginalProcessingCode(), 8, ' '));
                }
            }

            //FID_P("Draft Capture Flag")
            mapFids.addFid(GnapFIDS.FID_P,getDraftCapture(cardBrand,transactionType));

            //FID_Q("Echo Data")
            mapFids.addFid(GnapFIDS.FID_Q, reqData.getEchoData());

            //FID_R("Card Type")
            mapFids.addFid(GnapFIDS.FID_R, reqData.getCardType());

            //FID_S("Invoice Number")
            mapFids.addFid(GnapFIDS.FID_S, reqData.getInvoiceNumber());

            //FID_T("Original Invoice Number")
            if(reference != null) {
                if (isInstanceOfReference && reference.getOriginalInvoiceNumber() != null)
                    mapFids.addFid(GnapFIDS.FID_T, reference.getOriginalInvoiceNumber());
            }
            //FID_U("Language Code")
            if(paymentMethod instanceof Credit){
                mapFids.addFid(GnapFIDS.FID_U, reqData.getLanguageCode());
            }else if(paymentMethod instanceof Debit && tagData !=null){
                if(tagData.getLanguageCode()!=null) {
                    mapFids.addFid(GnapFIDS.FID_U, tagData.getLanguageCode());
                }else{
                    mapFids.addFid(GnapFIDS.FID_U, reqData.getLanguageCode());
                }
            }

        //FID_X("ISO Response Code")
        if (isEmvDataPresent && (transactionType.equals(TransactionType.PreAuthCompletion)||
                header.getMessageSubType().equals(MessageSubType.StoreAndForwardTransactions))){
            mapFids.addFid(GnapFIDS.FID_X, tagData.getIsoResponseCode());
        }

            //FID_a("Optional Data")
            if (optionalData != null)
                mapFids.addFid(GnapFIDS.FID_a, optionalData.getOptionalData(transactionType,header.getTransactionCode(),cardBrand, reqData.getCardType()));

            //FID_b("PIN Block")
            if(paymentMethod instanceof IPinProtected) {
                String pinBlock = ((IPinProtected)paymentMethod).getPinBlock();
                if(!StringUtils.isNullOrEmpty(pinBlock)) {
                    mapFids.addFid(GnapFIDS.FID_b, pinBlock);
                }
            }

            //FID_d("Retailer ID")
            mapFids.addFid(GnapFIDS.FID_d, reqData.getRetailerID());

            //FID_e("POS Condition Code")
            mapFids.addFid(GnapFIDS.FID_e, reqData.getPosConditionCode());

            //FID_h("Sequence Number")
            if (reqData.getSequenceNumber() != null) {
                mapFids.addFid(GnapFIDS.FID_h, reqData.getSequenceNumber().getValue());
            }

            //FID_j("Mastercard Banknet Reference Number, Visa Transaction Identifier or Discover Network Reference ID")
            if(reference != null) {
                if (isInstanceOfReference)
                    mapFids.addFid(GnapFIDS.FID_j, reference.getTransactionIdentifier());
            }
            //FID_m("Day Totals")
            if (reqData.getDayTotals() != null)
                mapFids.addFid(GnapFIDS.FID_m, reqData.getDayTotals().getValue(reqData.getSequenceNumber()));

            //FID_p("Paypass Device Type Indicator")
            if(isEmvDataPresent) {
                mapFids.addFid(GnapFIDS.FID_p, tagData.getDeviceTypeIndicator());
            }

            //FID_q("Track 2 Data")
            if (paymentMethod != null) {
                if(paymentMethod instanceof IEncryptable && acceptorConfig.isSupportsE2EEEncryption()){
                    EncryptionData encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
                    if (encryptionData != null) {
                        mapFids.addFid(GnapFIDS.FID_q, encryptionData.getTrackNumber());
                    }
                }else {
                    if (paymentMethod instanceof ITrackData) {
                        ITrackData card = (ITrackData) paymentMethod;
                        if (transactionType.equals(TransactionType.PreAuthCompletion) || transactionType.equals(TransactionType.Void)) {
                            if(builder.getAmount().compareTo(BigDecimal.valueOf(0))==0 && cardBrand.equals(CardBrand.UnionPay)){
                                mapFids.addFid(GnapFIDS.FID_q, card.getValue());
                            }else {
                                mapFids.addFid(GnapFIDS.FID_q, getManualTrackData(card.getPan(), card.getExpiry()));
                            }
                        } else {
                                mapFids.addFid(GnapFIDS.FID_q, card.getValue());
                        }
                    } else if (paymentMethod instanceof ICardData) {
                        ICardData card = (ICardData) paymentMethod;
                        mapFids.addFid(GnapFIDS.FID_q, getManualTrackData(card.getNumber(), getShortExpiry(card)));
                    }
                }
            }


        //FID_t("Global Payments Pinpad serial Number")
        mapFids.addFid(GnapFIDS.FID_t, acceptorConfig.getPinPadSerialNumber());

            //FID_z("Union Pay Indicator")
            if (cardBrand.equals(CardBrand.UnionPay))
                mapFids.addFid(GnapFIDS.FID_z,UnionPayIndicator.UnionPayTransaction);
            else
                mapFids.addFid(GnapFIDS.FID_z,UnionPayIndicator.NonUnionPayTransaction);

            //FID_4("Message Reason Codes for Merchant Initiated Transactions")
            mapFids.addFid(GnapFIDS.FID_4, reqData.getMerchantReasonCodes());

            //FID_5("Transaction Info")
            if(reference != null) {
                if (isInstanceOfReference)
                    mapFids.addFid(GnapFIDS.FID_5, reference.getOriginalTransactionInfo());
            }
            //FID_6("Product Sub-FIDs")
            if (prodSubFids != null) {
                mapFids.addFid(GnapFIDS.FID_6, "");

                //subFID_B("CVV2/CVC2 Value")
                if (prodSubFids.getCvv2Value() != null) {
                    mapFids.addSubFid(GnapSubFids.subFID_B, StringUtils.padRight(prodSubFids.getCvv2Value(), 4, ' '));
                }

                //subFID_E("POS Entry Mode")
                if (paymentMethod != null) {
                    if (paymentMethod instanceof ITrackData) {
                        ITrackData card = (ITrackData) paymentMethod;
                        String entryMode = card.getEntryMethod().getValue() + acceptorConfig.getPinCapability().getValue();
                        mapFids.addSubFid(GnapSubFids.subFID_E, entryMode);
                    } else {
                        String entryMode = mapEntryMethod(EntryMethod.Manual) + acceptorConfig.getPinCapability().getValue();
                        mapFids.addSubFid(GnapSubFids.subFID_E, entryMode);
                    }
                }

                //subFID_H("Card Verification Digits Presence indicator and Result")
                if (isCreditCard && (transactionType.equals(TransactionType.Sale) || transactionType.equals(TransactionType.Auth))
                        && prodSubFids.getCardPresenceIndicator() != null)
                    mapFids.addSubFid(GnapSubFids.subFID_H, StringUtils.padRight(prodSubFids.getCardPresenceIndicator().getValue(), 2, ' '));


                //subFID_I("Transaction Currency Code (TCC)")
                if (isEmvDataPresent)
                    mapFids.addSubFid(GnapSubFids.subFID_I, tagData.getTransactionCurrencyCode().substring(1));

                if (isEmvDataPresent) {
                        //subFID_O("EMV Request Data")
                        mapFids.addSubFid(GnapSubFids.subFID_O, getEmvReqData(tagData));
                        //subFID_P("EMV Additional Request Data")
                        mapFids.addSubFid(GnapSubFids.subFID_P, getEmvAdditionalReqData(tagData));
                }

                //subFID_S("UnionPay Online PIN DUKPT KSN")
                mapFids.addSubFid(GnapSubFids.subFID_S, prodSubFids.getUnionPayOnlinePINDUKPTKSN());

                //subFID_T("DUKPT KSN")
                if(paymentMethod instanceof IPinProtected && acceptorConfig.isSupportsE2EEEncryption()){
                    EncryptionData encryptionData= ((IEncryptable)paymentMethod).getEncryptionData();
                    if(encryptionData != null) {
                        mapFids.addSubFid(GnapSubFids.subFID_T,encryptionData.getKsn());
                    }
                }

            //subFID_X("Point of Service Data")
            if(prodSubFids.getPointOfServiceData() != null)
                mapFids.add(GnapSubFids.subFID_X, prodSubFids.getPointOfServiceData().getValue());

                //subFID_q("Paywave Form Factor Indicator")
                if (isEmvDataPresent)
                    mapFids.addSubFid(GnapSubFids.subFID_q, tagData.getFormFactorIndicator());
            }
        }else{
            //FID_U("Language Code")
            mapFids.addFid(GnapFIDS.FID_U, reqData.getLanguageCode());

            //FID_h("Sequence Number")
            if (reqData.getSequenceNumber() != null) {
                mapFids.addFid(GnapFIDS.FID_h, reqData.getSequenceNumber().getValue());
            }

            //FID_m("Day Totals")
            if (reqData.getDayTotals() != null)
                mapFids.addFid(GnapFIDS.FID_m, reqData.getDayTotals().getValue(reqData.getSequenceNumber()));

            //FID_t("Global Payments Pinpad serial Number")
            mapFids.addFid(GnapFIDS.FID_t, acceptorConfig.getPinPadSerialNumber());

        }

        mr.add(ControlCodes.ETX);
        IDeviceMessage buildMessage = new DeviceMessage(mr.toArray());
        GnapUtils.log("Final Request", buildMessage.toString());

        return sendRequest(mr, builder);
    }

    private <T extends TransactionBuilder<Transaction>> Transaction sendRequest(MessageWriter messageData, T builder) throws ApiException {

        try {
            int messageLength = messageData.length();
            GnapUtils.log("Message Length ", String.valueOf(messageLength));
            MessageWriter req = new MessageWriter();
            req.add(messageLength, 2);
            req.addRange(messageData.toArray());
            IDeviceMessage buildMessage = new DeviceMessage(req.toArray());
            GnapUtils.log("Final Request with header and data", buildMessage.toString());
            byte[] responseBuffer = send(buildMessage);
            return mapResponse(responseBuffer, builder);

        } catch (GatewayTimeoutException exc) {
            throw exc;
        } catch (GatewayException exc) {
            throw new GatewayException(exc.getMessage(), exc.getResponseCode(), exc.getResponseText());
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }

    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(byte[] buffer, T builder) throws Exception {

        Transaction result = new Transaction();
        String response = new String(buffer);
        GnapUtils.log("Response ", response);
        String header = response.substring(0, 48);
        String data = response.substring(48,response.length()-1);
        TransactionReference reference = new TransactionReference();

        GnapUtils.log("Response Header", header);
        GnapResponse gnapResponse = new GnapResponse();

        GnapMessageHeader gnapMessageHeader = buildResponseHeader(header);
        GnapResponseData gnapResponseData = buildResponseData(data, reference);

        gnapResponse.setGnapMessageHeader(gnapMessageHeader);
        gnapResponse.setGnapResponseData(gnapResponseData);
        result.setGnapResponse(gnapResponse);

        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        if(paymentMethod !=null) {
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference originalReference = (TransactionReference) builder.getPaymentMethod();
                reference.setOriginalPaymentMethod(originalReference.getOriginalPaymentMethod());
            } else {
                reference.setOriginalPaymentMethod(paymentMethod);
                reference.setPaymentMethodType(builder.getPaymentMethod().getPaymentMethodType());
            }
        }
        reference.setOriginalAmount(builder.getAmount());
        reference.setOriginalTransactionType(builder.getTransactionType());
        result.setResponseCode(gnapResponseData.getApprovalCode());
        result.setTransactionReference(reference);
        return result;
    }

    public static <T extends Enum<T> & IStringConstant> T readFidEnum(String value, Class<T> clazz) {
        return ReverseStringEnumMap.parse(value, clazz);
    }

    @Override
    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        return null;
    }

    @Override
    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        return null;
    }

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    @Override
    public boolean supportsOpenBanking() {
        return false;
    }

    private GnapResponseData buildResponseData(String sData, TransactionReference transactionReference) throws Exception {

        GnapResponseData gnapData = new GnapResponseData();
        GnapProdSubFids gnapProdSubFids = new GnapProdSubFids();

        String fidString = "";
        String subFidString = "";

        if (sData.contains(fs)) {
            fidString = sData.substring(sData.indexOf(fs)).substring(1);
        }

        if (sData.contains(rs)) {
            subFidString = sData.substring(sData.indexOf(rs)).substring(1);
        }
        String[] fidValues = fidString.split(fs);
        String[] subFidValues = subFidString.split(rs);

        GnapUtils.log("*************** Response FIDs *****************", "*****");

        for (String splt : fidValues) {
            if (!(splt.isEmpty())) {
                GnapFIDS gnapFIDS = readFidEnum(String.valueOf(splt.charAt(0)),GnapFIDS.class);
                String fid = splt.substring(1);

                GnapUtils.log("FID : " + gnapFIDS.getValue(),fid);

                switch (gnapFIDS) {

                    //FID_B("Transaction Amount") 
                    case FID_B:
                        gnapData.setTransactionAmount(new BigDecimal(fid));
                        transactionReference.setOriginalApprovedAmount(gnapData.getTransactionAmount());
                        break;

                    // FID_F("Approval Code")
                    case FID_F:
                        transactionReference.setOriginalProcessingCode(fid);
                        gnapData.setApprovalCode(fid);
                        break;

                    //FID_L("Balance Info")
                    case FID_L:
                        BalanceInfo balanceInfo=new BalanceInfo();
                        StringParser sp = new StringParser(fid);
                        balanceInfo.setAccountType1(sp.readString(2));
                        balanceInfo.setAmountType1(GnapConnector.readFidEnum(sp.readString(2),AmountType.class));
                        balanceInfo.setAmountSign1(sp.readString(1));
                        balanceInfo.setCurrencyCode1(sp.readString(3));
                        balanceInfo.setAmount1(new BigDecimal(sp.readString(12)));
                        if(fid.trim().length()>20) {
                            balanceInfo.setAccountType2(sp.readString(2));
                            balanceInfo.setAmountType2(GnapConnector.readFidEnum(sp.readString(2),AmountType.class));
                            balanceInfo.setAmountSign2(sp.readString(1));
                            balanceInfo.setCurrencyCode2(sp.readString(3));
                            balanceInfo.setAmount2(new BigDecimal(sp.readString(12)));
                        }
                        gnapData.setBalanceInfo(balanceInfo);
                        break;

                    // FID_M("PIN Encryption Key")
                    case FID_M:
                        gnapData.setPinEncryptionKey(fid);
                        break;

                    //FID_Q("Echo Data")
                    case FID_Q:
                        gnapData.setEchoData(fid);
                        break;

                    //FID_X("ISO Response Code")
                    case FID_X:
                        transactionReference.setIsoResponseCode(GnapConnector.readFidEnum(fid, ISOResponseCode.class));
                        gnapData.setIsoResponseCode(GnapConnector.readFidEnum(fid, ISOResponseCode.class));
                        if(transactionReference.getIsoResponseCode()!=null && transactionReference.getIsoResponseCode().equals(ISOResponseCode.PartialApproval)){
                            transactionReference.setPartialApproval(true);
                        }
                        break;

                    //FID_g("Response Message")
                    case FID_g:
                        gnapData.setResponseMessage(fid);
                        break;

                    // FID_h("Sequence Number")
                    case FID_h:
                        SequenceNumber sn = new SequenceNumber();
                        sn.setDayCounter(Integer.parseInt(fid.substring(0, 3)));
                        sn.setShiftCounter(Integer.parseInt(fid.substring(3, 6)));
                        sn.setBatchCounter(Integer.parseInt(fid.substring(6, 9)));
                        sn.setSequenceCounter(Integer.parseInt(fid.substring(9, 12)));
                        sn.setIndicator(Integer.parseInt(fid.substring(12)));
                        gnapData.setSequenceNumber(sn);
                        break;

                    //FID_j("Mastercard Banknet Reference Number, Visa Transaction Identifier or Discover Network Reference ID")
                    case FID_j:
                        transactionReference.setTransactionIdentifier(fid);
                        break;

                    //FID_m("Day Totals")
                    case FID_m:
                        GnapBatchTotal batchTotalRes = new GnapBatchTotal();
                        batchTotalRes.setTotalSalesTransaction(Integer.parseInt(fid.substring(6, 10)));
                        batchTotalRes.setSignTotalSale(GnapConnector.readFidEnum(fid.substring(10,11), BatchTotalSign.class));
                        batchTotalRes.setTotalSaleAmount(StringUtils.toString(fid.substring(11, 20), 2));

                        batchTotalRes.setTotalRefundTransaction(Integer.parseInt(fid.substring(20, 24)));
                        batchTotalRes.setSignTotalRefund(GnapConnector.readFidEnum(fid.substring(24,25), BatchTotalSign.class));
                        batchTotalRes.setTotalRefundAmount(StringUtils.toString(fid.substring(26, 34), 2));

                        batchTotalRes.setTotalAdjustmentTransaction(Integer.parseInt(fid.substring(34, 38)));
                        batchTotalRes.setSignTotalAdjustment(GnapConnector.readFidEnum(fid.substring(38,39), BatchTotalSign.class));
                        batchTotalRes.setTotalAdjustmentAmount(StringUtils.toString(fid.substring(39, 47), 2));

                        gnapData.setDayTotals(batchTotalRes);
                        break;

                    //FID_5("Transaction Info")
                    case FID_5:
                        transactionReference.setOriginalTransactionInfo(fid);
                        gnapData.setTransactionInfo(fid);
                        break;

                    //FID_6("Product Sub-FIDs")
                    case FID_6:
                        for (String sf : subFidValues) {
                            GnapSubFids gnapSubFIDS = readFidEnum(String.valueOf(sf.charAt(0)),GnapSubFids.class);
                            String subFid = sf.substring(1);
                            GnapUtils.log("  SUBFID : " + gnapSubFIDS.getValue(), subFid);
                            switch (gnapSubFIDS) {

                                //subFID_E("POS Entry Mode")
                                case subFID_E:
                                    transactionReference.setOriginalPosEntryMode(subFid);
                                    gnapProdSubFids.setPosEntryMode(subFid);
                                    break;

                                //subFID_H("Card Verification Digits Presence indicator and Result")
                                case subFID_H:
                                    gnapProdSubFids.setCardPresenceIndicator(GnapConnector.readFidEnum(subFid, CardIdentifierPresenceIndicator.class));
                                    break;

                                //subFID_I("Transaction Currency Code (TCC)")
                                case subFID_I:
                                    gnapProdSubFids.setTransactionCurrencyCode(subFid);
                                    break;

                                //subFID_Q("EMV Response Data")
                                case subFID_Q:
                                    gnapProdSubFids.setEmvResponseData(subFid);
                                    break;

                                //subFID_R("EMV Additional Response Data")
                                case subFID_R:
                                    gnapProdSubFids.setEmvAdditionalResponseData(subFid);
                                    break;

                                //subFID_W("CAVV/AAV Result Code")
                                case subFID_W:
                                    gnapProdSubFids.setCavvResultCode(subFid);
                                    break;

                            }
                        }
                        break;
                }
            }
        }
        gnapData.setGnapProdSubFids(gnapProdSubFids);
        return gnapData;
    }

    private GnapMessageHeader buildResponseHeader(String header) {
        GnapMessageHeader gnapMessageHeader = new GnapMessageHeader();

        StringParser sp = new StringParser(header);

        gnapMessageHeader.setDeviceType(sp.readString(2));
        gnapMessageHeader.setTransmissionNumber(sp.readString(2));
        gnapMessageHeader.setTerminalId(sp.readString(16));
        sp.readString(6); //Filler
        gnapMessageHeader.setCurrentDate(sp.readString(6));
        gnapMessageHeader.setCurrentTime(sp.readString(6));
        gnapMessageHeader.setMessageType(sp.readStringConstant(1, MessageType.class));
        gnapMessageHeader.setMessageSubType(sp.readStringConstant(1, MessageSubType.class));
        sp.readString(2);
        gnapMessageHeader.setTransactionCode(transCode);
        gnapMessageHeader.setProcessingFlag1(sp.readInt(1));
        gnapMessageHeader.setProcessingFlag2(sp.readInt(1));
        GnapUtils.log("Processing Flag", String.valueOf(gnapMessageHeader.getProcessingFlag2()));
        gnapMessageHeader.setProcessingFlag3(sp.readInt(1));
        gnapMessageHeader.setResponseCode(sp.readString(3));
        GnapUtils.log("Response Code", gnapMessageHeader.getResponseCode());

        return gnapMessageHeader;

    }

    private MessageWriter buildRequestHeader(GnapMessageHeader header, AcceptorConfig config) {
        MapGnapFids mg = new MapGnapFids();
        mg.add(config.getDeviceType());
        mg.add(StringUtils.padLeft(header.getTransmissionNumber(), 2, '0'));
        mg.add(StringUtils.padRight(terminalId, 16, ' '));
        mg.add(StringUtils.padLeft("", 6, ' '));
        mg.add(header.getCurrentDate());
        mg.add(header.getCurrentTime());
        mg.add(LogMessages.MessageType, getMessageType(header));
        mg.add(LogMessages.MessageSubType, header.getMessageSubType().getValue());
        mg.add(LogMessages.TransactionCode, header.getTransactionCode().getValue());
        transCode=header.getTransactionCode();
        mg.add(String.valueOf(header.getProcessingFlag1()));
        if (config.isEmvCapable())
         mg.add(LogMessages.ProcessingFlag2, ProcessingFlag2.EMVCapableDevice.getValue());
        else
            mg.add(LogMessages.ProcessingFlag2,ProcessingFlag2.NonEMVDevice.getValue());
        mg.add(String.valueOf(header.getProcessingFlag3()));
        mg.add(header.getResponseCode());
        return mg.getWriterObject();
    }

    private <T extends TransactionBuilder >String getTransactionAmount(T builder) {
        BigDecimal tranAmt = builder.getAmount();
        if (builder.getSurchargeAmount() != null)
            tranAmt = tranAmt.add(builder.getSurchargeAmount());
        else if (builder.getTaxAmount() != null)
            tranAmt = tranAmt.add(builder.getTaxAmount());
        else if (builder.getTipAmount() != null)
            tranAmt = tranAmt.add(builder.getTipAmount());
        else if (builder.getCashBackAmount() != null)
            tranAmt = tranAmt.add(builder.getCashBackAmount());

        return StringUtils.toNumeric(tranAmt);
    }

    private String getEmvReqData(EmvData tagData) {
        StringBuilder sb = new StringBuilder();
        sb.append("01");
        sb.append(tagData.getCryptogramInformationData());
        sb.append(tagData.getTerminalCountryCode().substring(1));
        sb.append(tagData.getEMVTransactionDate());
        sb.append(tagData.getApplicationCryptogram());
        sb.append(tagData.getApplicationInterchangeProfile());
        sb.append(tagData.getApplicationTransactionCounter());
        sb.append(tagData.getUnpredictableNumber());
        sb.append(tagData.getTerminalVerificationResults());
        sb.append(tagData.getEMVTransactionType());
        sb.append(tagData.getTransactionCurrencyCode().substring(1));
        sb.append(tagData.getCryptogramAmount());
        sb.append(tagData.getIssuerApplicationData());
        return sb.toString();
    }

    private String getEmvAdditionalReqData(EmvData tagData) {
        StringBuilder sb = new StringBuilder();
        sb.append("01");
        sb.append(tagData.getCardSequenceNumber());
        sb.append(tagData.getEMVTerminalType());
        sb.append(tagData.getCustomerVerificationResults());
        sb.append(tagData.getApplicationVersionNumber());
        sb.append(tagData.getDedicatedFileName());
        return sb.toString();
    }

    private  String getMessageType(GnapMessageHeader messageHeader)
    {
        if (messageHeader.getTransactionCode().equals(TransactionCode.EndOfBatch) || messageHeader.getTransactionCode().equals(TransactionCode.EndOfPeriod)){
           return MessageType.AdministrativeTransactions.getValue();
        }
        else {
            return MessageType.FinancialTransactions.getValue();
        }
    }

    private String getManualTrackData(String cardNumber,String expiry) {
        StringBuilder sb=new StringBuilder();
        sb.append("M");
        sb.append(cardNumber);
        sb.append("=");
        sb.append(expiry);
        sb.append("0?");
        return sb.toString();
    }

    private String accountTypeMapping(AccountType accountType) {
            if (accountType.equals(AccountType.Checking))
                return GnapAccountType.ChequingAccount.getValue();
            else if (accountType.equals(AccountType.Savings))
                return GnapAccountType.SavingsAccount.getValue();
            else
                return GnapAccountType.DefaultAccountInteracAndUP.getValue();
    }

    private <T extends TransactionBuilder<Transaction>> void setTransactionCodeInHeader(GnapMessageHeader header,T builder) {

        TransactionType transactionType = builder.getTransactionType();
        if(builder instanceof AuthorizationBuilder){
            if(transactionType.equals(TransactionType.Sale)){
                if(header.getTransactionCode()!=null && header.getTransactionCode().equals(TransactionCode.TelephoneAuthPurchase)) {
                    header.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
                } else {
                    header.setTransactionCode(TransactionCode.Purchase);
                }
            }else if(transactionType.equals(TransactionType.Auth)) {
                header.setTransactionCode(TransactionCode.PreAuthorization);
            } else if(transactionType.equals(TransactionType.Refund)){
                header.setTransactionCode(TransactionCode.Return);
            }
        }else if(builder instanceof ManagementBuilder){
            TransactionReference reference =(TransactionReference) builder.getPaymentMethod();
            if(transactionType.equals(TransactionType.BatchClose)) {
                if(((ManagementBuilder)builder).getBatchCloseType().equals(BatchCloseType.Forced)) {
                    header.setTransactionCode(TransactionCode.EndOfBatch);
                } else if(((ManagementBuilder)builder).getBatchCloseType().equals(BatchCloseType.EndOfShift)) {
                    header.setTransactionCode(TransactionCode.EndOfPeriod);
                }
            } else if( (header.getMessageSubType().equals(MessageSubType.StoreAndForwardTransactions)||header.getMessageSubType().equals(MessageSubType.TimeoutReversalStoreAndForwardTransaction))
                    && reference.getOriginalTransactionType().equals(TransactionType.Refund) && transactionType.equals(TransactionType.Void) ){
                header.setTransactionCode(TransactionCode.SAFReturnVoid);
            } else if((header.getMessageSubType().equals(MessageSubType.StoreAndForwardTransactions)||header.getMessageSubType().equals(MessageSubType.TimeoutReversalStoreAndForwardTransaction))
                    && reference.getOriginalTransactionType().equals(TransactionType.Sale)  && transactionType.equals(TransactionType.Void)) {
                header.setTransactionCode(TransactionCode.SAFPurchaseVoid);
            } else if((header.getMessageSubType().equals(MessageSubType.OnlineTransactions)||header.getMessageSubType().equals(MessageSubType.TimeoutReversalOnlineTransaction))
                    && reference.getOriginalTransactionType().equals(TransactionType.Refund)  && transactionType.equals(TransactionType.Void)) {
                header.setTransactionCode(TransactionCode.OnlineReturnVoid);
            }else if((header.getMessageSubType().equals(MessageSubType.OnlineTransactions)||header.getMessageSubType().equals(MessageSubType.TimeoutReversalOnlineTransaction))
                    && reference.getOriginalTransactionType().equals(TransactionType.Sale)  && transactionType.equals(TransactionType.Void)) {
                header.setTransactionCode(TransactionCode.OnlinePurchaseVoid);
            }else if(transactionType.equals(TransactionType.PreAuthCompletion)) {
                header.setTransactionCode(TransactionCode.PreAuthorizationCompletion);
            }
        }
    }

    private String getDraftCapture(CardBrand cardBrand,TransactionType transactionType){
        if(transactionType.equals(TransactionType.Auth) || cardBrand.equals(CardBrand.UnionPay))
            return DraftCaptureFlag.NonDraftCapture.getValue();
        else
            return DraftCaptureFlag.DraftCapture.getValue();
    }

    private String mapEntryMethod(EntryMethod entryMethod){
        if(entryMethod.equals(EntryMethod.Manual)) {
            return "01";
        }
        return null;
    }

    private String getShortExpiry(ICardData card) {
        if(card.getExpMonth() != null && card.getExpYear() != null) {
            return card.getExpYear().toString().substring(2, 4)+StringUtils.padLeft(card.getExpMonth().toString(), 2, '0');
        }
        return null;
    }

    private boolean isApprovalCodeValid(CardBrand cardBrand,TransactionType transactionType){
        if(cardBrand.equals(CardBrand.Interac) && (transactionType.equals(TransactionType.Void) || transactionType.equals(TransactionType.Refund))){
            return false;
        }
        return true;
    }

}
