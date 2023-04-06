package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.ResubmitBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.network.entities.NTSUserData;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.nts.*;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GatewayConnectorConfig;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.*;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NtsConnector extends GatewayConnectorConfig {

    private NtsMessageCode messageCode;
    private int timeout;
    private IRequestEncoder requestEncoder;
    @Override
    public int getTimeout() {
        if(super.getTimeout()==30000)
            this.timeout=0;
        else
            this.timeout=super.getTimeout();
        return timeout;
    }

    private static TransactionReference getReferencesObject(TransactionBuilder builder, NtsResponse ntsResponse, NTSCardTypes cardTypes) {
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionType transactionType = builder.getTransactionType();
        AuthorizationBuilder authBuilder;
        Map<UserDataTag, String> userData = new HashMap<>();
        TransactionReference reference = NtsUtils.prepareTransactionReference(ntsResponse);
        if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit) &&
                (transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale))) {
            if (cardTypes.equals(NTSCardTypes.MastercardFleet) || cardTypes.equals(NTSCardTypes.VisaFleet) || cardTypes.equals(NTSCardTypes.Mastercard) || cardTypes.equals(NTSCardTypes.Visa) || cardTypes.equals(NTSCardTypes.AmericanExpress) || cardTypes.equals(NTSCardTypes.Discover) || cardTypes.equals(NTSCardTypes.PayPal) || cardTypes.equals(NTSCardTypes.MastercardPurchasing)) {
                reference = NtsUtils.prepareTransactionReference(ntsResponse);
                userData = reference.getBankcardData();
                if (userData != null) {
                    reference.setSystemTraceAuditNumber(userData.getOrDefault(UserDataTag.Stan, ""));
                    reference.setPartialApproval(userData.getOrDefault(UserDataTag.PartiallyApproved, "N").equals("Y"));
                    reference.setOriginalApprovedAmount(StringUtils.toAmount(userData.get(UserDataTag.ApprovedAmount)));
                }
                // authorization builder
                if (builder instanceof AuthorizationBuilder) {
                    authBuilder = (AuthorizationBuilder) builder;
                    reference.setOriginalAmount(authBuilder.getAmount());
                    reference.setOriginalPaymentMethod(authBuilder.getPaymentMethod());
                    reference.setPaymentMethodType(authBuilder.getPaymentMethod().getPaymentMethodType());
                }
            }

            if (builder instanceof AuthorizationBuilder) {
                if (cardTypes.equals(NTSCardTypes.WexFleet)) {
                    if (transactionType.equals(TransactionType.Auth)) {
                        NtsAuthCreditResponseMapper ntsAuthCreditResponseMapper = (NtsAuthCreditResponseMapper) ntsResponse.getNtsResponseMessage();
                        String hostResponseArea = ntsAuthCreditResponseMapper.getCreditMapper().getHostResponseArea();
                        if (!StringUtils.isNullOrEmpty(hostResponseArea) && userData != null) {
                            StringParser responseParser = new StringParser(hostResponseArea);
                            String amount = responseParser.readString(7);
                            userData.put(UserDataTag.ApprovedAmount, amount);
                            reference.setOriginalApprovedAmount(new BigDecimal(amount));
                            if (builder.getTagData() != null) {
                                userData.put(UserDataTag.AvailableProducts, responseParser.readString(49));
                                userData.put(UserDataTag.EmvDataLength, responseParser.readString(4));
                                userData.put(UserDataTag.EvmData, responseParser.readRemaining());
                            }
                        }
                    } else if (transactionType.equals(TransactionType.Sale)) {
                        NtsSaleCreditResponseMapper ntsSaleCreditResponseMapper = (NtsSaleCreditResponseMapper) ntsResponse.getNtsResponseMessage();
                        String hostResponseArea = ntsSaleCreditResponseMapper.getCreditMapper().getHostResponseArea();
                        if (!StringUtils.isNullOrEmpty(hostResponseArea) && userData != null) {
                            StringParser responseParser = new StringParser(hostResponseArea);
                            String amount = responseParser.readString(7);
                            userData.put(UserDataTag.ApprovedAmount, amount);
                            reference.setOriginalApprovedAmount(new BigDecimal(amount));
                            userData.put(UserDataTag.ReceiptText, responseParser.readRemaining());
                            if (builder.getTagData() != null) {
                                userData.put(UserDataTag.EmvDataLength, responseParser.readString(4));
                                userData.put(UserDataTag.EvmData, responseParser.readRemaining());
                            }
                        }
                    }
                } else if (cardTypes.equals(NTSCardTypes.FleetWide) || cardTypes.equals(NTSCardTypes.FuelmanFleet)) {
                    if (transactionType.equals(TransactionType.Auth) && userData != null) {
                        NtsAuthCreditResponseMapper ntsAuthCreditResponseMapper = (NtsAuthCreditResponseMapper) ntsResponse.getNtsResponseMessage();
                        String hostResponseArea = ntsAuthCreditResponseMapper.getCreditMapper().getHostResponseArea();
                        StringParser responseParser = new StringParser(hostResponseArea);
                        String amount = responseParser.readString(7);
                        userData.put(UserDataTag.ApprovedAmount, amount);
                        reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount,2));
                        userData.put(UserDataTag.ReceiptText, responseParser.readRemaining());
                    } else if (transactionType.equals(TransactionType.Sale) && userData != null) {
                        NtsSaleCreditResponseMapper ntsSaleCreditResponseMapper = (NtsSaleCreditResponseMapper) ntsResponse.getNtsResponseMessage();
                        String hostResponseArea = ntsSaleCreditResponseMapper.getCreditMapper().getHostResponseArea();
                        StringParser responseParser = new StringParser(hostResponseArea);
                        String amount = responseParser.readString(7);
                        userData.put(UserDataTag.ApprovedAmount, amount);
                        reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount,2));
                        userData.put(UserDataTag.ReceiptText, responseParser.readRemaining());
                    }
                }
            }
            authBuilder = (AuthorizationBuilder) builder;
            reference.setOriginalAmount(authBuilder.getAmount());
            reference.setOriginalPaymentMethod(authBuilder.getPaymentMethod());
            reference.setPaymentMethodType(authBuilder.getPaymentMethod().getPaymentMethodType());
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit)
                && transactionType.equals(TransactionType.AddValue)) {
            reference.setOriginalTransactionCode(TransactionCode.Load);
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Gift)
                && ntsResponse.getNtsResponseMessage() instanceof NtsAuthCreditResponseMapper) {
            NtsAuthCreditResponseMapper ntsAuthCreditResponseMapper = (NtsAuthCreditResponseMapper) ntsResponse.getNtsResponseMessage();
            String hostResponseArea = ntsAuthCreditResponseMapper.getCreditMapper().getHostResponseArea();
            StringParser responseParser = new StringParser(hostResponseArea);
            reference.setOriginalTransactionTypeIndicator(ReverseStringEnumMap.parse(responseParser.readString(8).trim(), TransactionTypeIndicator.class));
            reference.setSystemTraceAuditNumber(responseParser.readString(6));
            userData.put(UserDataTag.RemainingBalance, responseParser.readString(6));
        } else if(paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)
                && transactionType != TransactionType.DataCollect
                && transactionType != TransactionType.Capture){
            NtsDebitResponse ntsDebitResponse = (NtsDebitResponse) ntsResponse.getNtsResponseMessage();
            reference.setOriginalTransactionCode(ntsDebitResponse.getTransactionCode());
            reference.setOriginalApprovedAmount(StringUtils.toAmount(String.valueOf(ntsDebitResponse.getAmount())));
        } else if(paymentMethod.getPaymentMethodType().equals(PaymentMethodType.EBT)
                && transactionType != TransactionType.DataCollect
                && transactionType != TransactionType.Capture){
            NtsEbtResponse ntsEbtResponse = (NtsEbtResponse) ntsResponse.getNtsResponseMessage();
            reference.setOriginalTransactionCode(ntsEbtResponse.getTransactionCode());
        }
        if (builder instanceof AuthorizationBuilder) {
            authBuilder = (AuthorizationBuilder) builder;
            reference.setOriginalAmount(authBuilder.getAmount());
            reference.setOriginalPaymentMethod(authBuilder.getPaymentMethod());
            reference.setPaymentMethodType(authBuilder.getPaymentMethod().getPaymentMethodType());
            reference.setOriginalTransactionDate(authBuilder.getNtsRequestMessageHeader().getTransactionDate());
            reference.setOriginalTransactionTime(authBuilder.getNtsRequestMessageHeader().getTransactionTime());
        }
        reference.setBankcardData(userData);
        return reference;
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        messageCode = builder.getNtsRequestMessageHeader().getNtsMessageCode();
        if(builder.getTimestamp()!=null)
        builder.getNtsTag16().setTimeStamp(NtsUtils.getDateObject(builder.getTimestamp()));
        setTimeToHeader(builder);
        //message body
        MessageWriter request = new MessageWriter();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        NTSCardTypes cardType = NtsUtils.mapCardType(paymentMethod);
        String userData = setUserData(builder, paymentMethod, cardType);

        // Request parameters.
        NtsObjectParam ntsObjectParam = new NtsObjectParam();
        ntsObjectParam.setNtsBuilder(builder);
        ntsObjectParam.setNtsRequest(request);
        ntsObjectParam.setNtsAcceptorConfig(acceptorConfig);
        ntsObjectParam.setNtsUserData(userData);
        ntsObjectParam.setNtsEnableLogging(isEnableLogging());
        ntsObjectParam.setNtsBatchProvider(batchProvider);
        ntsObjectParam.setNtsCardType(cardType);
        ntsObjectParam.setBinTerminalId(binTerminalId);
        ntsObjectParam.setBinTerminalType(binTerminalType);
        ntsObjectParam.setInputCapabilityCode(inputCapabilityCode);
        ntsObjectParam.setSoftwareVersion(softwareVersion);
        ntsObjectParam.setLogicProcessFlag(logicProcessFlag);
        ntsObjectParam.setTerminalType(terminalType);
        ntsObjectParam.setUnitNumber(unitNumber);
        ntsObjectParam.setTerminalId(terminalId);
        ntsObjectParam.setCompanyId(companyId);
        ntsObjectParam.setTimeout(getTimeout());

        //Preparing the request
        request = NtsRequestObjectFactory.getNtsRequestObject(ntsObjectParam);
        Transaction transaction=sendRequest(request, builder);
        transaction.setMessageInformation(ntsObjectParam.getNtsBuilder().getNtsRequestMessageHeader().getPriorMessageInformation());
        return transaction;
    }
    private String setUserData(TransactionBuilder<Transaction> builder, IPaymentMethod paymentMethod, NTSCardTypes cardType) throws ApiException {
        String userData = "";
        if (cardType != null && isUserDataPresent(builder, paymentMethod, cardType)) {
            messageCode = builder.getNtsRequestMessageHeader().getNtsMessageCode();
            if (isNonBankCard(cardType)
                    || isDataCollectForNonFleetBankCard(cardType, builder.getTransactionType())) {
                userData = NTSUserData.getNonBankCardUserData(builder, cardType, messageCode, acceptorConfig);
            } else {
                userData = NTSUserData.getBankCardUserData(builder, paymentMethod, cardType, messageCode, acceptorConfig);
            }
        } else if (builder.getTransactionType() == TransactionType.BatchClose) {
            userData = NTSUserData.getRequestToBalanceUserData(builder);
        }
        return userData;
    }
    public Transaction resubmitTransaction(ResubmitBuilder builder) throws ApiException {
        String transactionToken = builder.getTransactionToken();
        Transaction result = null;
        if(transactionToken != null) {
            byte[] decodeRequest = this.decodeRequest(builder.getTransactionToken());
            IDeviceMessage buildMessage = new DeviceMessage(decodeRequest);
            NtsUtils.log("-----------------------------------------------------------------------------------");
            NtsUtils.log("Tokenization Request", buildMessage.toString());
            byte[] responseBuffer = send(buildMessage);
            result = mapResponse(responseBuffer, builder);
        }
        return result;
    }
    private String encodeRequest(MessageWriter request) {
        int encodeCount = 0;
        while(encodeCount++ < 3) {
            String encodedRequest = doEncoding(request);
            if(TerminalUtilities.checkLRC(encodedRequest)) {
                return encodedRequest;
            }
        }
        return null;
    }
    private String doEncoding(MessageWriter request) {
        // base64 encode the message buffer
        int messageLength = request.getMessageRequest().length() + 2;
        MessageWriter req = new MessageWriter();
        req.add(messageLength, 2);
        req.add(request.getMessageRequest().toString());

        byte[] encoded = Base64.encodeBase64(req.toArray());
        String encodedString = new String(encoded);

        // encrypt it
        if(requestEncoder == null) {
            if(isEnableLogging()) {
                System.out.println(String.format("[TOKEN TRACE]: %s %s", companyId, terminalId));
            }
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }
        String token = requestEncoder.encode(encodedString);

        // build final token
        MessageWriter mw = new MessageWriter();
        mw.add(ControlCodes.STX);
        mw.addRange(token.getBytes());
        mw.add(ControlCodes.ETX);

        // generate the CRC
        mw.add(TerminalUtilities.calculateLRC(mw.toArray()));
        return new String(mw.toArray());
    }
    private byte[] decodeRequest(String encodedStr) {
        if(requestEncoder == null) {
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }

        byte[] encodedBuffer = encodedStr.getBytes();
        MessageReader mr = new MessageReader(encodedBuffer);

        String valueToDecrypt = encodedStr;
        if(mr.peek() == ControlCodes.STX.getByte()) {
            mr.readCode(); // pop the STX off
            valueToDecrypt = mr.readToCode(ControlCodes.ETX);

            byte lrc = mr.readByte();
            if(lrc != TerminalUtilities.calculateLRC(encodedBuffer)) {
                // invalid token
            }
        }

        String requestStr = requestEncoder.decode(valueToDecrypt);
        byte[] decoded = Base64.decodeBase64(requestStr);

        mr = new MessageReader(decoded);
        String mti = mr.readString(4);
        byte[] buffer = mr.readBytes(decoded.length);
        return decoded;
    }

    private <T extends TransactionBuilder<Transaction>> Transaction sendRequest(MessageWriter messageData, T builder) throws ApiException {
        NtsUtils.log("--------------------- FINAL REQUEST ---------------------");
        NtsUtils.log("Request length:", String.valueOf(messageData.getMessageRequest().length()));

        try {
            int messageLength = messageData.getMessageRequest().length() + 2;
            MessageWriter req = new MessageWriter();
            req.add(messageLength, 2);
            req.add(messageData.getMessageRequest().toString());

            IDeviceMessage buildMessage = new DeviceMessage(req.toArray());
            NtsUtils.log("Request", buildMessage.toString());
            byte[] responseBuffer = send(buildMessage);
            Transaction response =mapResponse(responseBuffer, builder);
            String transactionToken = encodeRequest(messageData);
            if(transactionToken != null) {
                response.setTransactionToken(transactionToken);
            }

            return response;

        } catch (GatewayException exc) {
            exc.setHost(currentHost.getValue());
            throw exc;
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }

    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(byte[] buffer, T builder) throws ApiException {
        Transaction result = new Transaction();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        MessageReader mr = new MessageReader(buffer);
        NTSCardTypes cardType = NtsUtils.mapCardType(paymentMethod);
        StringParser sp = new StringParser(buffer);
        NtsUtils.log("--------------------- RESPONSE ---------------------");
        NtsUtils.log("Response", sp.getBuffer());
        NtsResponse ntsResponse = NtsResponseObjectFactory.getNtsResponseObject(mr.readBytes((int) mr.getLength()), builder);

        if (Boolean.FALSE.equals(isAllowedResponseCode(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode()))) {
            throw new GatewayException(
                    String.format("Unexpected response from gateway: %s %s", ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue(),
                            ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().toString()),
                    ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue(),
                    ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().name());
        } else {
            NtsResponseMessageHeader ntsResponseMessageHeader = ntsResponse.getNtsResponseMessageHeader();
            result.setResponseCode(ntsResponseMessageHeader.getNtsNetworkMessageHeader().getResponseCode().getValue());
            result.setNtsResponse(ntsResponse);
            result.setPendingRequestIndicator(ntsResponseMessageHeader.getPendingRequestIndicator());
            if (paymentMethod != null) {
                result.setTransactionReference(getReferencesObject(builder, ntsResponse, cardType));
            }
        }
        //Batch Summary
        if (builder.getTransactionType().equals(TransactionType.BatchClose)){
            ManagementBuilder manageBuilder = (ManagementBuilder) builder;
            NtsRequestToBalanceData data = manageBuilder.getNtsRequestsToBalanceData();
            BatchSummary summary = new BatchSummary();
            summary.setBatchId(manageBuilder.getBatchNumber());
            summary.setResponseCode(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue());
            summary.setSequenceNumber(String.valueOf(data.getDaySequenceNumber()));
            summary.setTransactionCount(manageBuilder.getTransactionCount());
            summary.setSaleAmount(manageBuilder.getTotalSales());
            summary.setReturnAmount(manageBuilder.getTotalReturns());
            result.setBatchSummary(summary);
        }
            return result;
    }

    private Boolean isAllowedResponseCode(NtsHostResponseCode code) {
        return code == NtsHostResponseCode.Success
                || code == NtsHostResponseCode.PartiallyApproved
                || code == NtsHostResponseCode.Denial
                || code == NtsHostResponseCode.VelocityReferral
                || code == NtsHostResponseCode.AvsReferralForFullyOrPartially;
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        //message header section
        messageCode = builder.getNtsRequestMessageHeader().getNtsMessageCode();
        if(builder.getTimestamp()!=null)
            builder.getNtsTag16().setTimeStamp(NtsUtils.getDateObject(builder.getTimestamp()));
        setTimeToHeader(builder);
        //message body
        MessageWriter request = new MessageWriter();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        NTSCardTypes cardType = NtsUtils.mapCardType(paymentMethod);
        String userData = setUserData(builder, paymentMethod, cardType);

        // Request parameters.
        NtsObjectParam ntsObjectParam = new NtsObjectParam();
        ntsObjectParam.setNtsBuilder(builder);
        ntsObjectParam.setNtsRequest(request);
        ntsObjectParam.setNtsAcceptorConfig(acceptorConfig);
        ntsObjectParam.setNtsUserData(userData);
        ntsObjectParam.setNtsEnableLogging(isEnableLogging());
        ntsObjectParam.setNtsBatchProvider(batchProvider);
        ntsObjectParam.setNtsCardType(cardType);
        ntsObjectParam.setBinTerminalId(binTerminalId);
        ntsObjectParam.setBinTerminalType(binTerminalType);
        ntsObjectParam.setInputCapabilityCode(inputCapabilityCode);
        ntsObjectParam.setSoftwareVersion(softwareVersion);
        ntsObjectParam.setLogicProcessFlag(logicProcessFlag);
        ntsObjectParam.setTerminalType(terminalType);
        ntsObjectParam.setUnitNumber(unitNumber);
        ntsObjectParam.setTerminalId(terminalId);
        ntsObjectParam.setCompanyId(companyId);
        ntsObjectParam.setTimeout(getTimeout());


        request = NtsRequestObjectFactory.getNtsRequestObject(ntsObjectParam);
        Transaction transaction=sendRequest(request, builder);
        transaction.setMessageInformation(ntsObjectParam.getNtsBuilder().getNtsRequestMessageHeader().getPriorMessageInformation());
        return transaction;
    }

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

    /**
     * Check the given card is BankCard type or not.
     *
     * @param cardType
     * @return True: if card type is non bank card.
     */
    private boolean isNonBankCard(NTSCardTypes cardType) {
        return (cardType.equals(NTSCardTypes.StoredValueOrHeartlandGiftCard)
                || cardType.equals(NTSCardTypes.WexFleet)
                || cardType.equals(NTSCardTypes.VoyagerFleet)
                || cardType.equals(NTSCardTypes.FleetOne)
                || cardType.equals(NTSCardTypes.FuelmanFleet)
                || cardType.equals(NTSCardTypes.FleetWide));
    }

    /**
     * Check the given card is non-fleet BankCard and DataCollect request.
     *
     * @param cardType
     * @param transactionType
     * @return True: if card type is non-fleet BankCard and DataCollect.
     */
    private boolean isDataCollectForNonFleetBankCard(NTSCardTypes cardType, TransactionType transactionType) {
        return (
                    transactionType == TransactionType.DataCollect
                    || transactionType == TransactionType.Capture
                )
                && (
                        cardType == NTSCardTypes.Mastercard
                        || cardType == NTSCardTypes.Visa
                        || cardType == NTSCardTypes.AmericanExpress
                        || cardType == NTSCardTypes.Discover
                        || cardType == NTSCardTypes.StoredValueOrHeartlandGiftCard
                        || cardType == NTSCardTypes.PinDebit
                        || cardType == NTSCardTypes.MastercardPurchasing
        );
    }

    private boolean isUserDataPresent(TransactionBuilder<Transaction> builder, IPaymentMethod paymentMethod, NTSCardTypes cardType) {
        TransactionType transactionType = builder.getTransactionType();
        if (messageCode.equals(NtsMessageCode.PinDebit) && transactionType == TransactionType.DataCollect)
            return true;
        else if (messageCode.equals(NtsMessageCode.PinDebit) || messageCode.equals(NtsMessageCode.Mail)
                || messageCode.equals(NtsMessageCode.UtilityMessage))
            return false;
        else if (transactionType.equals(TransactionType.Reversal)
                && !cardType.equals(NTSCardTypes.WexFleet)
                && !cardType.equals(NTSCardTypes.StoredValueOrHeartlandGiftCard))
            return false;
        else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.EBT))
            return false;
        else
            return true;
    }

    private void setTimeToHeader(TransactionBuilder builder){
        String timeStamp=null;
        if(builder instanceof AuthorizationBuilder){
            timeStamp=((AuthorizationBuilder) builder).getTimestamp();
        }else if(builder instanceof  ManagementBuilder){
            timeStamp=((ManagementBuilder) builder).getTimestamp();
        }
        if(timeStamp !=null){
            Date date=NtsUtils.getDateObject(timeStamp);
            String transactionDate = new SimpleDateFormat("MMdd").format(date);
            String transactionTime = new SimpleDateFormat("HHmmss").format(date);
            builder.getNtsRequestMessageHeader().setTransactionDate(transactionDate);
            builder.getNtsRequestMessageHeader().setTransactionTime(transactionTime);
        }
    }
}
