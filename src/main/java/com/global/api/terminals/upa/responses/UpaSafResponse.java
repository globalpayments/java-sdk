package com.global.api.terminals.upa.responses;

import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.SummaryType;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.terminals.SummaryResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaSafType;
import com.global.api.utils.JsonDoc;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpaSafResponse implements ISAFResponse {

    private Map<SummaryType, SummaryResponse> approved;
    private Map<SummaryType, SummaryResponse> pending;
    private Map<SummaryType, SummaryResponse> declined;
    private String deviceResponseCode;
    private String deviceResponseText;
    private String status;
    @Getter
    private String transactionType;
    @Getter
    private Integer multipleMessage;
    private static final String DATA = "data";
    private static final String CMD_RESULT = "cmdResult";
    private static final String RESULT = "result";
    private static final String SUCCESS = "success";
    private static final String ZERO = "00";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String MULTIPLE_MESSAGE = "multipleMessage";
    private static final String RESPONSE = "response";
    private static final String SAF_DETAILS = "SafDetails";
    private static final String SAF_TYPE = "SafType";
    private static final String SAF_COUNT = "SafCount";
    private static final String SAF_TOTAL = "SafTotal";
    private static final String SAF_RECORDS = "SafRecords";
    private static final String TOTAL_AMOUNT = "totalAmount";
    private static final String TRANSACTION_TIME = "transactionTime";
    private static final String TRANSACTION_TYPE = "transactionType";
    private static final String MASKED_PAN = "maskedPan";
    private static final String CARD_TYPE = "cardType";
    private static final String CARD_ACQUISITION = "cardAcquisition";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String RESPONSE_TEXT = "responseText";
    private static final String REFERENCE_NO = "referenceNumber";
    private static final String BASE_AMOUNT = "baseAmount";
    private static final String TAX_AMOUNT = "taxAmount";
    private static final String TIP_AMOUNT = "tipAmount";
    private static final String REQUEST_AMOUNT = "requestAmount";
    private static final String INVOICE_NUMBER = "invoiceNbr";
    private static final String CLERK_ID = "clerkId";
    private static final String DELETE_SAF = "DeleteSAF";
    private static final String SURCHARGE = "surcharge";
    private static final String TRANSACTION_NUMBER = "tranNo";
    private static final String SAF_REFERENCE_NO = "safReferenceNumber";

    public UpaSafResponse(JsonDoc responseObj) {
        JsonDoc responseData = responseObj.get(DATA);

        if (responseData != null) {
            JsonDoc cmdResult = responseData.get(CMD_RESULT);

            if (cmdResult != null) {
                status = cmdResult.getString(RESULT);
                deviceResponseCode = status.equalsIgnoreCase(SUCCESS) ? ZERO : cmdResult.getString(ERROR_CODE);
                deviceResponseText = cmdResult.getString(ERROR_MESSAGE);
            }

            transactionType = responseData.getString(RESPONSE);

            JsonDoc innerData = responseData.get(DATA);

            if (innerData != null) {
                if (transactionType.equals(DELETE_SAF)){
                    parseDeleteSAF(innerData);
                }
                if (innerData.getInt(MULTIPLE_MESSAGE) != null) {
                    multipleMessage = innerData.getInt(MULTIPLE_MESSAGE);
                }
                List<JsonDoc> safDetails = innerData.getEnumerator(SAF_DETAILS);
                if (safDetails != null) {


                    safDetails.forEach(safDetail -> {
                        SummaryResponse summaryResponse= new SummaryResponse();

                        if (safDetail.getString(SAF_TYPE) != null) {
                            summaryResponse.summaryType = mapSafType(safDetail.getString(SAF_TYPE));
                        }
                        if (safDetail.getInt(SAF_COUNT) != null) {
                            summaryResponse.count = safDetail.getInt(SAF_COUNT);
                        }
                        if (safDetail.getDecimal(SAF_TOTAL) != null) {
                            summaryResponse.totalAmount = safDetail.getDecimal(SAF_TOTAL);
                        }

                        List<JsonDoc> safRecords = safDetail.getEnumerator(SAF_RECORDS);
                        if (safRecords != null) {
                            safRecords.forEach(safRecord -> {
                                TransactionSummary transactionSummary = new TransactionSummary();
                                transactionSummary.setApprovalCode(safRecord.getString("approvalCode"));
                                transactionSummary.setMaskedCardNumber(safRecord.getString(MASKED_PAN));
                                transactionSummary.setPinVerified(safRecord.getString("PinVerified"));
                                transactionSummary.setGatewayResponseCode(safRecord.getString(RESPONSE_CODE));
                                transactionSummary.setAvailableBalance(safRecord.getDecimal("availableBalance"));
                                transactionSummary.setGatewayResponseMessage(safRecord.getString(RESPONSE_TEXT));
                                transactionSummary.setAppName(safRecord.getString("appName"));
                                transactionSummary.setTranNo(safRecord.getString("tranNo"));
                                transactionSummary.setSafReferenceNumber(safRecord.getString("safReferenceNumber"));

                                try {
                                    transactionSummary.setTransactionDate(safRecord.getDateTime("transactionTime"));
                                } catch (GatewayException ignored) { }

                                if (safRecord.getDecimal(TIP_AMOUNT) != null)
                                    transactionSummary.setGratuityAmount(safRecord.getDecimal(TIP_AMOUNT));

                                if (safRecord.getDecimal(BASE_AMOUNT) != null)
                                    transactionSummary.setBaseAmount(safRecord.getDecimal(BASE_AMOUNT));

                                if (safRecord.getDecimal("baseDue") != null)
                                    transactionSummary.setAmountDue(safRecord.getDecimal("baseDue"));

                                if (safRecord.getDecimal(REQUEST_AMOUNT) != null) {
                                    transactionSummary.setRequestAmount(safRecord.getDecimal(REQUEST_AMOUNT));

                                    // re-uses "totalAmount" device response property, but this
                                    // might help call attention to the transaction being a partial
                                    // authorization
                                    transactionSummary.setAuthorizedAmount(safRecord.getDecimal("totalAmount"));
                                }

                                if (safRecord.getString("expiryDate") != null)
                                    transactionSummary.setExpiryDate(safRecord.getString("expiryDate"));

                                if (safRecord.getString("referenceNumber") != null)
                                    transactionSummary.setTransactionId(safRecord.getString("referenceNumber"));

                                if (safRecord.getString("maskedPan") != null)
                                    transactionSummary.setMaskedCardNumber(safRecord.getString("maskedPan"));

                                if (safRecord.getString("invoiceNbr") != null)
                                    transactionSummary.setInvoiceNumber(safRecord.getString("invoiceNbr"));

                                if (safRecord.getDecimal("totalAmount") != null)
                                    transactionSummary.setTotalAmount(safRecord.getString("totalAmount"));

                                summaryResponse.transactions.add(transactionSummary);
                            });
                        }

                        if(summaryResponse.summaryType.equals(SummaryType.Declined)){
                            if (declined == null) {
                                declined = new HashMap<>();
                            }
                            declined.put(summaryResponse.summaryType, summaryResponse);
                        }else if(summaryResponse.summaryType.equals(SummaryType.Pending)){
                            if (pending == null) {
                                pending = new HashMap<>();
                            }
                            pending.put(summaryResponse.summaryType, summaryResponse);
                        }
                        else {
                            if (approved == null) {
                                approved = new HashMap<>();
                            }
                            approved.put(summaryResponse.summaryType, summaryResponse);
                        }
                    });


                }

            }
        }
    }

    @Override
    public Integer getTotalCount() {
        return null;
    }

    @Override
    public BigDecimal getTotalAmount() {
        return null;
    }

    @Override
    public Map<SummaryType, SummaryResponse> getApproved() {
        return approved;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public Map<SummaryType, SummaryResponse> getDeclined() {
        return declined;
    }

    @Override
    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    @Override
    public String getDeviceResponseText() {
        return deviceResponseText;
    }

    @Override
    public Map<SummaryType, SummaryResponse> getPending() {
        return pending;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setCommand(String command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        return null;
    }

    public void setDeviceResponseCode(String deviceResponseCode) {
        throw new UnsupportedOperationException();
    }

    public void setDeviceResponseText(String deviceResponseText) {
        throw new UnsupportedOperationException();
    }

    public void setStatus(String status) {
        throw new UnsupportedOperationException();
    }

    public void setVersion(String version) {
        throw new UnsupportedOperationException();
    }

    private SummaryType mapSafType(String safDetails) {
        if (safDetails.equals(UpaSafType.APPROVED.getValue())) {
            return SummaryType.Approved;
        } else if (safDetails.equals(UpaSafType.PENDING.getValue())) {
            return SummaryType.Pending;
        } else if (safDetails.equals(UpaSafType.FAILED.getValue())) {
            return SummaryType.Declined;
        }
        return null;
    }

    public static void parseDeleteSAF(JsonDoc innerData){
        TransactionSummary transactionSummary = new TransactionSummary();
        if (innerData.getDecimal(TIP_AMOUNT) != null) {
            transactionSummary.setGratuityAmount(innerData.getDecimal(TIP_AMOUNT));
        }
        if (innerData.getDecimal(TAX_AMOUNT) != null) {
            transactionSummary.setTaxAmount(innerData.getDecimal(TAX_AMOUNT));
        }
        if (innerData.getDecimal(SURCHARGE) != null) {
            transactionSummary.setSurchargeAmount(innerData.getDecimal(SURCHARGE));
        }
        transactionSummary.setInvoiceNumber(innerData.getString(INVOICE_NUMBER));
        transactionSummary.setClerkId(innerData.getString(CLERK_ID));
        transactionSummary.setTransactionType(innerData.getString(TRANSACTION_TYPE));
        transactionSummary.setTotalAmount(innerData.getString(TOTAL_AMOUNT));
        transactionSummary.setMaskedCardNumber(innerData.getString(MASKED_PAN));
        transactionSummary.setReferenceNumber(innerData.getString(REFERENCE_NO));
        transactionSummary.setSafReferenceNumber(innerData.getString(SAF_REFERENCE_NO));
        transactionSummary.setGatewayResponseMessage(innerData.getString(RESPONSE_TEXT));
        transactionSummary.setCardType(innerData.getString(CARD_TYPE));
        transactionSummary.setTransactionId(innerData.getString(TRANSACTION_NUMBER));
        transactionSummary.setCardAcquisition(innerData.getString(CARD_ACQUISITION));
        transactionSummary.setTransactionTime(innerData.getString(TRANSACTION_TIME));
        if (innerData.getDecimal(BASE_AMOUNT) != null) {
            transactionSummary.setBaseAmount(innerData.getDecimal(BASE_AMOUNT));
        }
        transactionSummary.setGatewayResponseCode(RESPONSE_CODE);
    }

}
