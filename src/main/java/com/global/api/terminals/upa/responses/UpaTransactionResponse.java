package com.global.api.terminals.upa.responses;

import com.global.api.entities.UpaConfigContent;
import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.enums.CardType;
import com.global.api.entities.enums.TerminalConfigType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.EmvUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class UpaTransactionResponse extends UpaResponseHandler {
    public UpaTransactionResponse(JsonDoc responseData) throws ApiException {
        parseResponse(responseData);

        JsonDoc response = isGpApiResponse(responseData) ? responseData.get("response") : responseData.get("data");
        setTransactionType(responseData.getString("response"));

        JsonDoc data = response.get("data");
        if (data != null) {
            if (Optional.ofNullable(getCommand()).isPresent()) {
                try {
                    UpaMessageId messageId = UpaMessageId.valueOf(getCommand());
                    switch (messageId) {
                        case GetAppInfo:
                            hydrateGetAppInfoData(data);
                            break;
                        case Scan:
                            setScanData(String.valueOf(responseData.getValue("scanData")));
                            break;
                        case ExecuteUDDataFile:
                            setDataString(String.valueOf(responseData.getValue("dataString")));
                            break;
                        case GetConfigContents:
                            hydrateGetConfigData(data);
                            break;
                        case GetDebugInfo:
                            //TODO map response
                            break;
                        default:
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    throw new MessageException("Unknown command: " + getCommand(), e);
                }
            }

            if (Objects.equals(getTransactionType(), UpaMessageId.GetAppInfo.name())) {
                hydrateGetAppInfoData(data);
            }

            JsonDoc host = data.get("host");
            if (host != null) {
                hydrateHostData(host);
            }

            JsonDoc payment = data.get("payment");
            if (payment != null) { // is null on decline response
                hydratePaymentData(payment);
            }

            JsonDoc transaction = data.get("transaction");
            if (transaction != null) {
                hydrateTransactionData(transaction);
            }

            JsonDoc emv = data.get("emv");
            if (emv != null) {
                hydrateEmvData(emv);
            }

            JsonDoc pan = data.get("PAN");
            if (pan != null) {
                unmaskedCardNumber = pan.getString("clearPAN");
            }

            JsonDoc dcc = data.get(DCC);
            if (dcc != null) {
                hydrateDccData(dcc);
            }

            //Added Fallback for startCardTransaction
            if (data.getInt(FALLBACK) != null) {
                fallback = data.getInt(FALLBACK);
            }

            //Added Expiry for startCardTransaction
            if (data.getString(EXPIRY_DATE) != null) {
                expiryDate = data.getString(EXPIRY_DATE);
            }

            if (data.getDecimal(SERVICE_CODE) != null) {
                serviceCode = data.getDecimal(SERVICE_CODE);
            }

            // Merchant Id
            merchantId = data.getString("merchantId");
        }
    }

    protected void hydrateHostData(JsonDoc host) {
        setAmountDue(host.getDecimal("balanceDue"));
        setApprovalCode(host.getString("approvalCode"));
        setAvsResponseCode(host.getString("AvsResultCode"));
        setAvsResponseText(host.getString("AvsResultText"));
        setBalanceAmount(host.getDecimal("availableBalance"));
        setCardBrandTransId(host.getString("cardBrandTransId"));
        setResponseCode(host.getString("responseCode"));
        setResponseText(host.getString("responseText"));
        setMerchantFee(host.getDecimal("surcharge"));
        setTerminalRefNumber(host.getString("tranNo"));
        setToken(host.getString("tokenValue"));
        setTransactionId(host.getString("referenceNumber"));
        setTransactionAmount(host.getDecimal("totalAmount"));
        setBaseAmount(host.getDecimal("baseAmount"));
        setTipAmount(host.getDecimal("tipAmount"));
        setIssuerResponseCode(host.getString("IssuerResp"));
        setIsoResponseCode(host.getString("IsoRespCode"));
        setBankResponseCode(host.getString("BankRespCode"));

        if (getTransactionAmount() == null) {
            BigDecimal amount = host.getDecimal("amount");
            if (amount != null) {
                setTransactionAmount(amount);
            }
        }
        if (host.getString(RESPONSE_ID) != null) {
            responsesId = host.getString(RESPONSE_ID);
        }
        setResponseDateTime(host.getString(RESPONSE_DATE_TIME));
        if (host.getInt(GATEWAY_RESPONSE_CODE) != null) {
            gatewayResponsCode = host.getInt(GATEWAY_RESPONSE_CODE);
        }
        gatewayResponseMessage = host.getString(GATEWAY_RESPONSE_MESSAGE);
        if (host.getDecimal(AUTHORIZED_AMOUNT) != null) {
            authorizeAmount = host.getDecimal(AUTHORIZED_AMOUNT);
        }
        transactionDescriptor = host.getString(TRANSACTION_DESCRIPTOR);
        if (host.getString(RECURRING_DATA_CODE) != null) {
            setRecurringDataCode(host.getString(RECURRING_DATA_CODE));
        }
        setCvvResponseCode(host.getString(CVV_RESPONSE_CODE));
        setCvvResponseText(host.getString(CVV_RESPONSE_TEXT));
        if (host.getString(CAVV_RESULT_CODE) != null) {
            setCavvResultCode(host.getString(CAVV_RESULT_CODE));
        }

        if (host.getString(TRACE_NUMBER) != null) {
            setTraceNumber(host.getString(TRACE_NUMBER));
        }
        if (host.getString(TOKEN_RESPONSE_CODE) != null) {
            tokenResponsCode = host.getString(TOKEN_RESPONSE_CODE);
        }
        setTokenResponseMessage(host.getString(TOKEN_RESPONSE_MESSAGE));
        setCustomHash(host.getString(CUSTOM_HASH));
    }

    protected void hydratePaymentData(JsonDoc payment) {
        setCardHolderName(payment.getString("cardHolderName"));

        if (payment.getString("cardType") != null) {
            switch (payment.getString("cardType").toUpperCase(Locale.ENGLISH)) {
                case "VISA":
                    cardType = CardType.VISA;
                    break;
                case "MASTERCARD":
                    cardType = CardType.MC;
                    break;
                case "DISCOVER":
                    cardType = CardType.DISC;
                    break;
                case "AMERICAN EXPRESS":
                    cardType = CardType.AMEX;
                    break;
                default:
                    break;
            }
        }

        setEntryMethod(payment.getString("cardAcquisition"));
        setMaskedCardNumber(payment.getString("maskedPan"));
        setPaymentType(payment.getString("cardGroup"));
        setInvoiceNumber(payment.getString("invoiceNbr"));
        setPinVerified(payment.getString("PinVerified"));
        setAccountType(payment.getString("AccountType"));
        setTransactionType(payment.getString("transactionType"));
        setSequenceNo(payment.getString("PosSequenceNbr"));
        setApplicationName(payment.getString("appName"));

    }

    protected void hydrateTransactionData(JsonDoc transaction) {
        if (transaction.getDecimal("totalAmount") != null) {
            setTransactionAmount(transaction.getDecimal("totalAmount"));
        }
        if (transaction.getDecimal("tipAmount") != null) {
            setTipAmount(transaction.getDecimal("tipAmount"));
        }
    }

    protected void hydrateEmvData(JsonDoc emv) {
        setApplicationCryptogram(emv.getString("9F26"));
        if (emv.getString("9F27") != null) {
            switch (emv.getString("9F27")) {
                case "0":
                    setApplicationCryptogramType(ApplicationCryptogramType.AAC);
                    break;
                case "40":
                    setApplicationCryptogramType(ApplicationCryptogramType.TC);
                    break;
                case "80":
                    setApplicationCryptogramType(ApplicationCryptogramType.ARQC);
                    break;
                default:
                    break;
            }
        }

        setApplicationId(emv.getString("9F06"));
        setApplicationLabel(emv.getString("50"));
        setApplicationPreferredName(emv.getString("9F12"));
        applicationIdentifier = emv.getString("4F");
        //Upa is already parsing the Cardholder name from 5F20 so below is just a check to see if it was not pass
        //then take it from 5F20.  Otherwise, if 5F20 is not passed cardholder name will be blank with this check.
        if (StringUtils.isNullOrEmpty(getCardHolderName()) && emv.has("5F20")) {
            setCardHolderName(emv.getString("5F20"));
        }
        if (emv.getString("5F2A") != null) {
            transactionCurrencyCode = emv.getString("5F2A");
        }
        if (emv.getString("5F2D") != null) {
            String value = emv.getString("5F2D");
            cardHolderLanguage = EmvUtils.mapCardHolderLanguage(value);
        }
        if (emv.getString("5F34") != null) {
            sequenceNo = emv.getString("5F34");
        }
        applicationInterchangeProfile = emv.getString("82");
        dedicatedFileName = emv.getString("84");
        authorizedResponse = emv.getString("8A");
        if (emv.getString("95") != null) {
            terminalVerificationResult = emv.getString("95");
        }
        if (emv.getString("99") != null) {
            transactionPin = emv.getString("99");
        }
        transactionDate = emv.getString("9A");
        if (emv.getString("9B") != null) {
            transactionStatusInfo = emv.getString("9B");
        }
        if (emv.getString("9C") != null) {
            emvTransactionType = emv.getString("9C");
        }
        if (emv.getString("9F02") != null) {
            amountAuthorized = emv.getString("9F02");
        }
        if (emv.getString("9F03") != null) {
            otherAmount = emv.getString("9F03");
        }
        if (emv.getString("9F08") != null) {
            applicationVersionNumber = emv.getString("9F08");
        }
        issuerActionCode = emv.getString("9F0D");
        iacDenial = emv.getString("9F0E");
        iacOnline = emv.getString("9F0F");
        if (emv.getString("9F10") != null) {
            issuerApplicationData = emv.getString("9F10");
        }
        if (emv.getString("9F1A") != null) {
            countryCode = emv.getString("9F1A");
        }
        serialNo = emv.getString("9F1E");
        terminalCapabilities = emv.getString("9F33");
        cvmResult = emv.getString("9F34");
        if (emv.getString("9F35") != null) {
            terminalType = emv.getString("9F35");
        }
        applicationTransactionCounter = emv.getString("9F36");
        unpredictableNumber = emv.getString("9F37");
        additionalTerminalCapabilities = emv.getString("9F40");
        if (emv.getString("9F41") != null) {
            transactionSequenceCounter = emv.getString("9F41");
        }
        tacDefault = emv.getString(TAC_DEFAULT);
        tacDenial = emv.getString(TAC_DENIAL);
        tacOnline = emv.getString(TAC_ONLINE);
    }

    protected void hydrateDccData(JsonDoc dcc) {
        if (dcc.getDecimal(EXCHANGE_RATE) != null) {
            exchangeRate = dcc.getDecimal(EXCHANGE_RATE);
        }
        if (dcc.getDecimal(MARK_UP) != null) {
            markUp = dcc.getDecimal(MARK_UP);
        }
        transactionCurrency = dcc.getString(TRANSACTION_CURRENCY);
        if (dcc.getDecimal(TRANSACTION_AMOUNT) != null) {
            setTransactionAmount(dcc.getDecimal(TRANSACTION_AMOUNT));
        }
    }

    protected void hydrateGetAppInfoData(JsonDoc data) {
        setDeviceSerialNum(data.getString("deviceSerialNum", null));
        setAppVersion(data.getString("appVersion", null));
        setOsVersion(data.getString("OsVersion", null));
        setEmvSdkVersion(data.getString("EmvSdkVersion", null));
        setCtlsSdkVersion(data.getString("CTLSSdkVersion", null));
    }

    private void hydrateGetConfigData(JsonDoc data) {
        UpaConfigContent configContent = new UpaConfigContent();
        configContent.setConfigType(TerminalConfigType.getByValue(data.getString("configType")));
        configContent.setFileContent(data.getString("fileContents"));
        configContent.setLength(data.getInt("length"));

        setConfigContent(configContent);
    }
}