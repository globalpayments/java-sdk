package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.ResubmitBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.entities.NTSUserData;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.entities.nts.*;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.GatewayConnectorConfig;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.*;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class NtsConnector extends GatewayConnectorConfig {

    private NtsMessageCode messageCode;
    private int timeout;
    private static final int messageRequestLength = 2;
    private IRequestEncoder requestEncoder;
    private List<BatchSummary> batchSummaryList = new ArrayList<>();

    List<String> resubmitNonApprovedToken = new ArrayList<>();
    List<String> resubmitFormatErrorToken = new ArrayList<>();

    Transaction result1 = new Transaction();

    @Override
    public int getTimeout() {
        if (super.getTimeout() == 30000)
            this.timeout = 0;
        else
            this.timeout = super.getTimeout();
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
                            reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount, 2));
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
                            reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount, 2));
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
                        reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount, 2));
                        userData.put(UserDataTag.ReceiptText, responseParser.readRemaining());
                    } else if (transactionType.equals(TransactionType.Sale) && userData != null) {
                        NtsSaleCreditResponseMapper ntsSaleCreditResponseMapper = (NtsSaleCreditResponseMapper) ntsResponse.getNtsResponseMessage();
                        String hostResponseArea = ntsSaleCreditResponseMapper.getCreditMapper().getHostResponseArea();
                        StringParser responseParser = new StringParser(hostResponseArea);
                        String amount = responseParser.readString(7);
                        userData.put(UserDataTag.ApprovedAmount, amount);
                        reference.setOriginalApprovedAmount(StringUtils.getStringToAmount(amount, 2));
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
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)
                && transactionType != TransactionType.DataCollect
                && transactionType != TransactionType.Capture) {
            NtsDebitResponse ntsDebitResponse = (NtsDebitResponse) ntsResponse.getNtsResponseMessage();
            reference.setOriginalTransactionCode(ntsDebitResponse.getTransactionCode());
            reference.setOriginalApprovedAmount(StringUtils.toAmount(String.valueOf(ntsDebitResponse.getAmount())));
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.EBT)
                && transactionType != TransactionType.DataCollect
                && transactionType != TransactionType.Capture) {
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
        if (builder.getTimestamp() != null)
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
        Transaction transaction = sendRequest(request, builder);
        transaction.setMessageInformation(ntsObjectParam.getNtsBuilder().getNtsRequestMessageHeader().getPriorMessageInformation());
        if (builder.getTransactionType().equals(TransactionType.Sale)) {
            String token = generateDataCollectRequest(ntsObjectParam, transaction);
            transaction.setTransactionToken(token);
        }
        return transaction;

    }

    private String generateDataCollectRequest(NtsObjectParam ntsObjectParam, Transaction transaction) throws ApiException {

        TransactionBuilder ntsBuilder = ntsObjectParam.getNtsBuilder();
        NtsRequestMessageHeader ntsRequestMessageHeader = ntsBuilder.getNtsRequestMessageHeader();

        NtsMessageCode originalNtsMessageCode = ntsRequestMessageHeader.getNtsMessageCode();
        String originalNtsUserData = ntsObjectParam.getNtsUserData();
        IPaymentMethod originalPaymentMethod = ntsBuilder.getPaymentMethod();
        TransactionType originalTransactionType = ntsBuilder.getTransactionType();

        //payment method
        TransactionReference transactionReference = transaction.getTransactionReference();
        ntsBuilder.setPaymentMethod(transactionReference);

        //transaction type
        ntsBuilder.setTransactionType(TransactionType.DataCollect);

        //user data
        NTSCardTypes cardType = NtsUtils.mapCardType(transactionReference);
        String userData = setUserData(ntsBuilder, transactionReference, cardType);
        ntsObjectParam.setNtsUserData(userData);

        //set message code
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        MessageWriter request = generateResubmitDataCollectReq(ntsObjectParam);
        String token = encodeRequest(request);

        // reset datacollect and set original data
        ntsObjectParam.setNtsUserData(originalNtsUserData);
        ntsRequestMessageHeader.setNtsMessageCode(originalNtsMessageCode);
        ntsBuilder.setPaymentMethod(originalPaymentMethod);
        ntsBuilder.setTransactionType(originalTransactionType);


        return token;
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
        if (StringUtils.isNullOrEmpty(builder.getTransactionToken())) {
            throw new BuilderException("The transaction token cannot be null for resubmitted transactions.");
        }
        String transactionToken = builder.getTransactionToken();
        Transaction result = null;
        result1.setTransactionToken(transactionToken);
        byte[] decodeRequest = this.decodeRequest(transactionToken);
        MessageWriter request = new MessageWriter();
        String reqStr = new String(decodeRequest, StandardCharsets.UTF_8);
        int count = 21;
        String originalReq = reqStr.substring(messageRequestLength);
        String messageCode = originalReq.substring(21, 23);
        int hostRespCount = 6;
        switch (builder.getTransactionType()) {
            case BatchClose: {
                if (messageCode.equals(NtsMessageCode.RequestToBalacnce.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitRequestToBalance.getValue() + originalReq.substring(count + 2);
                } else if (messageCode.equals(NtsMessageCode.ForceRequestToBalance.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitForceRequestToBalance.getValue() + originalReq.substring(count + 2);
                }
                if (builder.isForceToHost()) {
                    if (messageCode.equals(NtsMessageCode.RequestToBalacnce.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.ForceRequestToBalance.getValue() + originalReq.substring(count + 2);
                    } else if (messageCode.equals(NtsMessageCode.RetransmitRequestToBalance.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.ForceRequestToBalance.getValue() + originalReq.substring(count + 2);
                    }
                }
            }
            break;
            case DataCollect:
            case Refund:
            case Sale: {
                if (messageCode.equals(NtsMessageCode.DataCollectOrSale.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitDataCollect.getValue() + originalReq.substring(count + 2);
                } else if (messageCode.equals(NtsMessageCode.CreditAdjustment.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitCreditAdjustment.getValue() + originalReq.substring(count + 2);
                } else if (messageCode.equals(NtsMessageCode.ForceCollectOrForceSale.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitForceCollect.getValue() + originalReq.substring(count + 2);
                } else if (messageCode.equals(NtsMessageCode.ForceCreditAdjustment.getValue())) {
                    originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitForceCreditAdjustment.getValue() + originalReq.substring(count + 2);
                }
                if (builder.isForceToHost()) {
                    if (messageCode.equals(NtsMessageCode.DataCollectOrSale.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.ForceCollectOrForceSale.getValue() + originalReq.substring(count + 2);
                    } else if (messageCode.equals(NtsMessageCode.CreditAdjustment.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.ForceCreditAdjustment.getValue() + originalReq.substring(count + 2);
                    } else if (messageCode.equals(NtsMessageCode.ReversalOrVoid.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.ForceReversalOrForceVoid.getValue() + originalReq.substring(count + 2);
                    }
                    if (messageCode.equals(NtsMessageCode.RetransmitDataCollect.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitForceCollect.getValue() + originalReq.substring(count + 2);
                    } else if (messageCode.equals(NtsMessageCode.RetransmitCreditAdjustment.getValue())) {
                        originalReq = originalReq.substring(0, count) + NtsMessageCode.RetransmitForceCreditAdjustment.getValue() + originalReq.substring(count + 2);
                    }
                }
                if (builder.getHostResponseCode().equals(NtsHostResponseCode.Code70TwiceInRow.getValue())){
                    originalReq = originalReq.substring(0, hostRespCount) + NtsHostResponseCode.Code70TwiceInRow.getValue() + originalReq.substring(hostRespCount + 2);
                }
            }
        }
        request.setMessageRequest(new StringBuilder(originalReq));
        result = sendRequest(request, builder);
        return result;
    }

    private String encodeRequest(MessageWriter request) {
        int encodeCount = 0;
        while (encodeCount++ < 3) {
            String encodedRequest = doEncoding(request);
            if (TerminalUtilities.checkLRC(encodedRequest)) {
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
        if (requestEncoder == null) {
            if (isEnableLogging()) {
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
        if (requestEncoder == null) {
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }

        byte[] encodedBuffer = encodedStr.getBytes();
        MessageReader mr = new MessageReader(encodedBuffer);

        String valueToDecrypt = encodedStr;
        if (mr.peek() == ControlCodes.STX.getByte()) {
            mr.readCode(); // pop the STX off
            valueToDecrypt = mr.readToCode(ControlCodes.ETX);

            byte lrc = mr.readByte();
            if (lrc != TerminalUtilities.calculateLRC(encodedBuffer)) {
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
            int messageLength = messageData.getMessageRequest().length() + messageRequestLength;
            MessageWriter req = new MessageWriter();
            req.add(messageLength, messageRequestLength);
            req.add(messageData.getMessageRequest().toString());

            IDeviceMessage buildMessage = new DeviceMessage(req.toArray());
            NtsUtils.log("Request", buildMessage.toString());
            byte[] responseBuffer = send(buildMessage);
            Transaction response = mapResponse(responseBuffer, builder, messageData);

            return response;

        } catch (GatewayException exc) {
            exc.setHost(currentHost.getValue());
            String transactionToken = checkResponse(exc.getResponseCode(), messageData, builder);

            if (transactionToken != null) {
                exc.setTransactionToken(transactionToken);
            }
            throw exc;
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }

    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(byte[] buffer, T builder, MessageWriter messageData) throws ApiException {
        Transaction result = new Transaction();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        MessageReader mr = new MessageReader(buffer);
        NTSCardTypes cardType = NtsUtils.mapCardType(paymentMethod);
        StringParser sp = new StringParser(buffer);
        NtsUtils.log("--------------------- RESPONSE ---------------------");
        NtsUtils.log("Response", sp.getBuffer());
        NtsResponse ntsResponse = NtsResponseObjectFactory.getNtsResponseObject(mr.readBytes((int) mr.getLength()), builder);
        NtsHostResponseCode hrc = ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode();

        if (builder instanceof ResubmitBuilder && hrc.equals(NtsHostResponseCode.HostSystemFailure)
                || hrc.equals(NtsHostResponseCode.TerminalTimeout)
                || hrc.equals(NtsHostResponseCode.TerminalTimeoutLostConnection)) {
            resubmitNonApprovedToken.add(result1.getTransactionToken());
            result.setNonApprovedDataCollectToken(resubmitNonApprovedToken);
        }else if(builder instanceof ResubmitBuilder && hrc.equals(NtsHostResponseCode.FormatError)){
            resubmitFormatErrorToken.add(result1.getTransactionToken());
            result.setFormatErrorDataCollectToken(resubmitFormatErrorToken);
        }

        if (Boolean.FALSE.equals(isAllowedResponseCode(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode()))
            && Boolean.FALSE.equals(isAllowedRCForRetransmit(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode(),builder))) {
            throw new GatewayException(
                    String.format("Unexpected response from gateway: %s %s", ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue(),
                            ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().toString()),
                    ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue(),
                    ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().name());
        } else {
            String responseCode = ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue();
            String transactionToken = checkResponse(responseCode, messageData, builder);

            NtsResponseMessageHeader ntsResponseMessageHeader = ntsResponse.getNtsResponseMessageHeader();
            result.setResponseCode(ntsResponseMessageHeader.getNtsNetworkMessageHeader().getResponseCode().getValue());
            result.setNtsResponse(ntsResponse);
            result.setPendingRequestIndicator(ntsResponseMessageHeader.getPendingRequestIndicator());
            if (paymentMethod != null) {
                result.setTransactionReference(getReferencesObject(builder, ntsResponse, cardType));
            }
            if (transactionToken != null) {
                result.setTransactionToken(transactionToken);
            }
        }
        //Batch Summary
        if (builder.getTransactionType().equals(TransactionType.BatchClose)) {
            BatchSummary summary = new BatchSummary();
            if (builder instanceof ManagementBuilder) {
                ManagementBuilder manageBuilder = (ManagementBuilder) builder;
                NtsRequestToBalanceData data = manageBuilder.getNtsRequestsToBalanceData();
                summary.setBatchId(manageBuilder.getBatchNumber());
                summary.setResponseCode(ntsResponse.getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode().getValue());
                summary.setSequenceNumber(String.valueOf(data.getDaySequenceNumber()));
                summary.setTransactionCount(manageBuilder.getTransactionCount());
                summary.setSaleAmount(manageBuilder.getTotalSales());
                summary.setReturnAmount(manageBuilder.getTotalReturns());
                summary.setTransactionToken(result.getTransactionToken());
                summary.setTotalAmount(manageBuilder.getTotalAmount());
                summary.setDebitAmount(manageBuilder.getTotalSales());
                summary.setCreditAmount(manageBuilder.getTotalReturns());
                batchSummaryList.add(summary);
                result.setBatchSummary(summary);
            } else if (builder instanceof ResubmitBuilder && (batchSummaryList != null)) {
                for (BatchSummary batchSummary : batchSummaryList) {
                    result.setBatchSummary(batchSummary);
                }
                batchSummaryList.clear();
            }
        }
        return result;
    }

    private <T extends TransactionBuilder<Transaction>> String checkResponse(String responseCode, MessageWriter messageData, T builder) {
        String encodedRequest = null;

        if (responseCode != null) {
            if (responseCode.equals("01")) {
                int count = 21;
                int hostRespCount = 6;
                // resend the batch close
                String originalReq = messageData.getMessageRequest().toString();

                if (responseCode.equals(NtsHostResponseCode.DenialRequestToBalance.getValue())) {
                    originalReq = originalReq.substring(0, hostRespCount) + NtsHostResponseCode.DenialRequestToBalance.getValue() + originalReq.substring(hostRespCount + 2);
                }
                messageData.setMessageRequest(new StringBuilder(originalReq));
            }
        }

        // Tokenize that which has not already.
        if (StringUtils.isNullOrEmpty(encodedRequest)) {
            // check for pan data and replace it with the truncated track
            encodedRequest = encodeRequest(messageData);
        }
        return encodedRequest;
    }

    private Boolean isAllowedResponseCode(NtsHostResponseCode code) {
        return code == NtsHostResponseCode.Success
                || code == NtsHostResponseCode.PartiallyApproved
                || code == NtsHostResponseCode.Denial
                || code == NtsHostResponseCode.VelocityReferral
                || code == NtsHostResponseCode.AvsReferralForFullyOrPartially
                || code == NtsHostResponseCode.DenialRequestToBalance
                || code == NtsHostResponseCode.InvalidPin;
    }

    private <T extends TransactionBuilder<Transaction>> Boolean isAllowedRCForRetransmit(NtsHostResponseCode responseCode, T builder) {
           return builder instanceof ResubmitBuilder  && responseCode == NtsHostResponseCode.HostSystemFailure
                    || responseCode == NtsHostResponseCode.TerminalTimeout
                    || responseCode == NtsHostResponseCode.TerminalTimeoutLostConnection
                    || responseCode == NtsHostResponseCode.FormatError;
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        //message header section
        messageCode = builder.getNtsRequestMessageHeader().getNtsMessageCode();
        if (builder.getTimestamp() != null)
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
        ntsObjectParam.setHostResponseCode(builder.getHostResponseCode());


        request = NtsRequestObjectFactory.getNtsRequestObject(ntsObjectParam);
        Transaction transaction = sendRequest(request, builder);
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

    private void setTimeToHeader(TransactionBuilder builder) {
        String timeStamp = null;
        if (builder instanceof AuthorizationBuilder) {
            timeStamp = ((AuthorizationBuilder) builder).getTimestamp();
        } else if (builder instanceof ManagementBuilder) {
            timeStamp = ((ManagementBuilder) builder).getTimestamp();
        }
        if (timeStamp != null) {
            Date date = NtsUtils.getDateObject(timeStamp);
            String transactionDate = new SimpleDateFormat("MMdd").format(date);
            String transactionTime = new SimpleDateFormat("HHmmss").format(date);
            builder.getNtsRequestMessageHeader().setTransactionDate(transactionDate);
            builder.getNtsRequestMessageHeader().setTransactionTime(transactionTime);
        }
    }

    private MessageWriter generateResubmitDataCollectReq(@NonNull NtsObjectParam ntsObjectParam) throws ApiException {
        MessageWriter request = null;

        PaymentMethodType paymentMethodType = ntsObjectParam.getNtsBuilder().getPaymentMethod() != null ?
                ntsObjectParam.getNtsBuilder().getPaymentMethod().getPaymentMethodType() : null;
        TransactionType transactionType = ntsObjectParam.getNtsBuilder().getTransactionType();

        // Setting the request header.
        request = prepareHeaderForDataCollect(ntsObjectParam);
        ntsObjectParam.setNtsRequest(request);
        if (paymentMethodType != null && isDataCollectTransaction(transactionType, paymentMethodType)) {
            request = prepareNtsDataCollectRequest(ntsObjectParam);
            return request;
        }
        return request;
    }

    static MessageWriter prepareHeaderForDataCollect(@NonNull NtsObjectParam params) {
        Integer MESSAGE_TYPE = 9;
        Integer COMPANY_ID = 45; // Default company ID for P66

        TransactionBuilder builder = params.getNtsBuilder();
        MessageWriter headerRequest = new MessageWriter();
        NTSCardTypes cardType = params.getNtsCardType();

        NtsRequestMessageHeader ntsRequestMessageHeader = builder.getNtsRequestMessageHeader();
        String strSpace = "";

        // Message Type
        headerRequest.addRange(MESSAGE_TYPE, 1);
        // Company Number
        String companyId = params.getCompanyId() != null ? params.getCompanyId() : String.valueOf(COMPANY_ID);
        headerRequest.addRange(companyId, 3);
        // Binary TerminalId
        headerRequest.addRange(String.format("%1s", params.getBinTerminalId()), 1);
        // Binary Terminal Type
        headerRequest.addRange(String.format("%1s", params.getBinTerminalType()), 1);
        // Host Response Code
        headerRequest.addRange(String.format("%2s", strSpace), 2);
        // Timeout Value
        if (cardType != null) {
            if (params.getTimeout() > 0) {
                headerRequest.addRange(params.getTimeout(), 3);
            } else {
                headerRequest.addRange(cardType.getTimeOut(), 3);
            }
        } else {
            headerRequest.addRange(String.valueOf(15), 3);
        }
        // Filler
        headerRequest.addRange(String.format("%1s", strSpace), 1);
        //Input Capability Code
        headerRequest.addRange(String.valueOf(params.getInputCapabilityCode().getValue()), 1);
        // Filler
        headerRequest.addRange(String.format("%1s", strSpace), 1);
        // Terminal Destination TagPurchase_CashBack
        headerRequest.addRange(ntsRequestMessageHeader.getTerminalDestinationTag(), 3);
        // Software Version
        headerRequest.addRange(params.getSoftwareVersion(), 2);
        // Pin Indicator
        headerRequest.addRange(ntsRequestMessageHeader.getPinIndicator().getValue(), 1);
        // Logic Process Flag or Store_And_Forward_Indicator
        headerRequest.addRange(params.getLogicProcessFlag().getValue(), 1);
        // Message Code
        headerRequest.addRange(ntsRequestMessageHeader.getNtsMessageCode().getValue(), 2);
        // Terminal Type
        headerRequest.addRange(params.getTerminalType().getValue(), 2);
        // Unit Number
        headerRequest.addRange(params.getUnitNumber(), 11);
        // Terminal Id
        headerRequest.addRange(params.getTerminalId(), 2);

        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionReference transactionReference = null;
        if (paymentMethod instanceof TransactionReference) {
            transactionReference = (TransactionReference) paymentMethod;
        }
        if (builder instanceof AuthorizationBuilder) {
            // Transaction Date
            headerRequest.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);
            // Transaction Time
            headerRequest.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
        } else if (builder instanceof ManagementBuilder) {
            ManagementBuilder manageBuilder = (ManagementBuilder) builder;
            if (paymentMethod != null && paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit)
                    && manageBuilder.getTransactionType() == TransactionType.Void) {
                // Transaction Date
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);
                // Transaction Time
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
            } else if (transactionReference != null &&
                    (manageBuilder.getTransactionType() == TransactionType.Reversal
                            || manageBuilder.getTransactionType() == TransactionType.Refund
                            || manageBuilder.getTransactionType() == TransactionType.Void
                            || manageBuilder.getTransactionType() == TransactionType.PreAuthCompletion)
            ) {
                // Transaction Date
                headerRequest.addRange(transactionReference.getOriginalTransactionDate(), 4);
                // Transaction Time
                headerRequest.addRange(transactionReference.getOriginalTransactionTime(), 6);
            } else if (manageBuilder.getTransactionType() == TransactionType.BatchClose
                    || manageBuilder.getTransactionType() == TransactionType.Capture
                    || manageBuilder.getTransactionType() == TransactionType.DataCollect) {
                // Transaction Date
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);

                // Transaction Time
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
            }
        }
        // Prior Message Response Time
        headerRequest.addRange(StringUtils.padLeft(ntsRequestMessageHeader.getPriorMessageInformation().getResponseTime(), 3, '0'), 3);
        // Prior Message Connect Time
        headerRequest.addRange(ntsRequestMessageHeader.getPriorMessageInformation().getConnectTime(), 3);
        // Prior Message Code
        headerRequest.addRange(ntsRequestMessageHeader.getPriorMessageInformation().getMessageReasonCode(), 2);
        return headerRequest;
    }

    private static MessageWriter prepareNtsDataCollectRequest(NtsObjectParam ntsObjectParam) throws BatchFullException {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        String userData = ntsObjectParam.getNtsUserData();
        NtsRequestMessageHeader ntsRequestMessageHeader = builder.getNtsRequestMessageHeader();

        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionReference transactionReference = null;
        if (paymentMethod instanceof TransactionReference) {
            transactionReference = (TransactionReference) paymentMethod;
            paymentMethod = transactionReference.getOriginalPaymentMethod();
        }

        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            if (trackData.getEntryMethod() != null) {
                NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                request.addRange(entryMethod.getValue(), 1);
            } else {
                request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
            }
        } else if (paymentMethod instanceof ICardData) {
            request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
        } else if (paymentMethod instanceof GiftCard) {
            GiftCard card = (GiftCard) paymentMethod;
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(card.getEntryMethod(), card.getTrackNumber(), ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            request.addRange(entryMethod.getValue(), 1);
        }
        // Card Type
        if (cardType != null) {
            request.addRange(cardType.getValue(), 2);
        }
        if (transactionReference != null) {

            IBatchProvider batchProvider = ntsObjectParam.getNtsBatchProvider();
            int batchNumber = builder.getBatchNumber();
            int sequenceNumber = 0;

            if (!StringUtils.isNullOrEmpty(transactionReference.getDebitAuthorizer())) {
                request.addRange(transactionReference.getDebitAuthorizer(), 2); // Response value from ebt or pin debit authorizer
            } else {
                request.addRange(DebitAuthorizerCode.NonPinDebitCard.getValue(), 2);
            }
            if (paymentMethod instanceof ICardData) {
                ICardData cardData = (ICardData) paymentMethod;
                String accNumber = cardData.getNumber();
                request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
                request.addRange(cardData.getShortExpiry(), 4);

            } else if (paymentMethod instanceof ITrackData) {
                ITrackData trackData = (ITrackData) paymentMethod;
                if (trackData != null && trackData.getPan() != null) {
                    // Account number
                    request.addRange(StringUtils.padRight(trackData.getPan(), 19, ' '), 19);

                    String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());
                    // Expiry date
                    request.addRange(StringUtils.padRight(expiryDate, 4, ' '), 4);
                } else {
                    request.addRange(StringUtils.padRight(trackData.getValue(), 40, ' '), 40);
                }
            } else if (paymentMethod instanceof GiftCard) {
                GiftCard gift = (GiftCard) paymentMethod;
                // Account number
                request.addRange(StringUtils.padRight(gift.getPan(), 19, ' '), 19);

                String expiryDate = NtsUtils.prepareExpDateWithoutTrack(gift.getExpiry());
                // Expiry date
                request.addRange(StringUtils.padRight(expiryDate, 4, ' '), 4);
            }
            request.addRange(transactionReference.getApprovalCode(), 6);
            request.addRange(transactionReference.getAuthorizer().getValue(), 1);

            BigDecimal approvedAmount = transactionReference.getOriginalApprovedAmount();
            if (approvedAmount == null) {
                approvedAmount = builder.getAmount();
            }
            request.addRange(StringUtils.toNumeric(approvedAmount, 7), 7);
            request.addRange(ntsRequestMessageHeader.getNtsMessageCode().getValue(), 2);
            request.addRange(transactionReference.getAuthCode(), 2);

            if (ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.RetransmitCreditAdjustment ||
                    ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.ForceCreditAdjustment ||
                    ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.RetransmitForceCreditAdjustment) {
                request.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);

                request.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
            } else if (ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.CreditAdjustment) {
                String transactionDate = DateTime.now(DateTimeZone.UTC).toString("MMdd");
                String transactionTime = DateTime.now(DateTimeZone.UTC).toString("HHmmss");

                request.addRange(transactionDate, 4);
                request.addRange(transactionTime, 6);
            } else {
                request.addRange(transactionReference.getOriginalTransactionDate(), 4);
                request.addRange(transactionReference.getOriginalTransactionTime(), 6);
            }
            if (batchNumber == 0 && batchProvider != null) {
                batchNumber = batchProvider.getBatchNumber();
            }
            //BatchNumber
            request.addRange(batchNumber, 2);

            if (!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
            }
            //Sequence Number
            request.addRange(StringUtils.padLeft(sequenceNumber, 3, '0'), 3);

            if (!StringUtils.isNullOrEmpty(userData)) {
                if (userData.length() != 99) {
                    // Extended user data flag
                    request.addRange("E", 1);
                    // User data length
                    request.addRange(userData.length(), 3);
                }
                request.addRange(userData, userData.length());
            }

        }
        return request;
    }

    private static boolean isDataCollectTransaction(TransactionType transactionType, PaymentMethodType paymentMethodType) {
        return (
                transactionType.equals(TransactionType.DataCollect)
                        || transactionType.equals(TransactionType.Capture)
        )
                &&
                (
                        Objects.equals(paymentMethodType, PaymentMethodType.Debit)
                                || Objects.equals(paymentMethodType, PaymentMethodType.Credit)
                                || Objects.equals(paymentMethodType, PaymentMethodType.Gift)
                                || Objects.equals(paymentMethodType, PaymentMethodType.EBT)
                );
    }
}
