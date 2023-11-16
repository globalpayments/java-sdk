package com.global.api.terminals.genius.responses;

import com.global.api.entities.IntegratedCircuitCard;
import com.global.api.entities.enums.CardType;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.*;

@Setter
@Getter
public class MitcResponse extends TerminalResponse {
    private Integer gatewayResponseCode;
    private String gatewayResponseMessage;
    private String invoiceNumber;
    private String transactionId;
    private String responseDateTime;
    private BigDecimal amount;
    private BigDecimal batchAmount;
    private String batchNumber;
    private BigDecimal gratuityAmount;
    private String status;
    private String approvalCode;
    private String avsResultCode;
    private String cvvResultCode;
    private String avsResultText;
    private String cashbackAmount;
    private BigDecimal authorizedAmount;
    private String amountOther;
    private Integer tokenResponseCode;
    private String tokenResponseMsg;
    private String token;
    private Integer traceNumber;
    private String availableBalance;
    private IntegratedCircuitCard icc;
    private String currencyCode;
    private String tenderType;
    private String entryMethod;
    private String clientTransactionId;
    private String creditSaleId;
    private String debitSaleId;
    private String processorResponse;
    private String creditReturnId;
    private String paymentType;
    private String maskedCardNumber;
    private String cardHolderName;
    private String expMonth;
    private String expYear;
    private String type;
    private String postalCode;
    private String customerId;
    private int statusCode;
    private String deviceResponseCode;
    private String deviceResponseText;

    public MitcResponse(int responseCode, String responseMessage, String responseData ) throws Exception {
        try {
            mapStatusCode(responseCode,responseMessage);
            JsonDoc responseObj = JsonDoc.parse(responseData);
            HashMapper(responseObj.dict);

            //Set ICC Values if present in response
            if (responseObj.has("transactions")) {
                List<JsonDoc> transactions = responseObj.getEnumerator("transactions");
                JsonDoc transaction = (JsonDoc) transactions.get(0);
                JsonDoc attribute = transaction.get("credit_attributes") == null ? transaction.get("debit_attributes") : transaction.get("credit_attributes");
                if(attribute.has("emv")){
                    JsonDoc emv=attribute.get("emv");
                    if(emv.has("icc")){
                        JsonDoc iccValues=emv.get("icc");
                        setICCValues(iccValues);
                    }
                }
            }

        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    private void mapStatusCode(Integer statusCode, String statusMessage){
        switch (statusCode.toString()) {
            case "200":
            case "201":
            case "473":
                responseCode = "00";
                responseText = "success";
                break;
            case "470":
            case "472":
                responseCode = "05";
                responseText = "declined";
                break;
            case "471":
            case "474":
                responseCode = "10";
                responseText = "partial approval";
                break;
            case "400":
            case "401":
            case "402":
            case "403":
            case "404":
            case "409":
            case "429":
            case "500":
            case "503":
                responseCode = "ER";
//                try {
//                    deviceResponseText =
//                }
            default:
                break;
        }

        gatewayResponseCode = statusCode;
        gatewayResponseMessage = statusMessage;
    }

    private void setICCValues(JsonDoc iccValues){
        this.icc = IntegratedCircuitCard.builder()
                .dedicated_file_name(iccValues.getString("dedicated_file_name"))
                .application_label(iccValues.getString("application_label"))
                .application_expiry_date(iccValues.getString("application_expiry_date"))
                .application_effective_date(iccValues.getString("application_effective_date"))
                .application_interchange_profile(iccValues.getString("application_interchange_profile"))
                .application_version_number(iccValues.getString("application_version_number"))
                .application_transaction_counter(iccValues.getString("application_transaction_counter"))
                .application_usage_control(iccValues.getString("application_usage_control"))
                .application_preferred_name(iccValues.getString("application_preferred_name"))
                .application_display_name(iccValues.getString("application_display_name"))
                .application_pan_sequence_number(iccValues.getString("application_pan_sequence_number"))
                .application_cryptogram(iccValues.getString("application_cryptogram"))
                .application_cryptogram_type(iccValues.getString("application_cryptogram_type"))
                .cardholder_verification_method_results(iccValues.getString("cardholder_verification_method_results"))
                .issuer_application_data(iccValues.getString("issuer_application_data"))
                .terminal_verification_results(iccValues.getString("terminal_verification_results"))
                .unpredictable_number(iccValues.getString("unpredictable_number"))
                .pos_entry_mode(iccValues.getString("pos_entry_mode"))
                .terminal_type(iccValues.getString("terminal_type"))
                .ifd_serial_number(iccValues.getString("ifd_serial_number"))
                .terminal_country_code(iccValues.getString("terminal_country_code"))
                .terminal_identification(iccValues.getString("terminal_identification"))
                .tac_default(iccValues.getString("tac_default"))
                .tac_denial(iccValues.getString("tac_denial"))
                .tac_online(iccValues.getString("tac_online"))
                .transaction_type(iccValues.getString("transaction_type"))
                .transaction_currency_code(iccValues.getString("transaction_currency_code"))
                .transaction_status_information(iccValues.getString("transaction_status_information"))
                .cryptogram_information_data(iccValues.getString("cryptogram_information_data"))
                .pin_statement(iccValues.getString("pin_statement"))
                .cvm_method(iccValues.getString("cvm_method"))
                .iac_default(iccValues.getString("iac_default"))
                .iac_denial(iccValues.getString("iac_denial"))
                .iac_online(iccValues.getString("iac_online"))
                .authorization_response_code(iccValues.getString("authorization_response_code"))
                .build();
        this.icc=icc;
    }

    private void HashMapper(HashMap<String, Object> lhm1)  {
        for (Map.Entry<String, Object> entry : lhm1.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                assignValues((String) value,key);
            } else if (value instanceof ArrayList) {
                ((ArrayList<?>) value).get(0);
                JsonDoc doc = (JsonDoc) ((ArrayList<?>) value).get(0);
                Map<String, Object> subMap = (Map<String, Object>)doc.dict;
                HashMapper((HashMap<String, Object>) subMap);
            } else if (value instanceof JsonDoc) {
            JsonDoc doc = (JsonDoc) value;
            Map<String, Object> subMap = (Map<String, Object>)doc.dict;
            HashMapper((HashMap<String, Object>) subMap);
            } else {
                throw new IllegalArgumentException(String.valueOf(value));
            }
        }
    }

    private void assignValues(String value, String key){
        if (key.equals("invoice_number")) {
            this.invoiceNumber=value;
        }
        if (key.equals("amount")) {
            this.amount=new BigDecimal(value);
        }
        if (key.equals("currency_code")) {
            this.currencyCode=value;
        }
        if (key.equals("gratuity_amount")) {
            this.tipAmount=new BigDecimal(value);
        }
        if (key.equals("tender_type")) {
            this.tenderType=value;
        }
        if (key.equals("entry_type")) {
            this.entryMethod=value;
        }
        if (key.equals("id")) {
            this.transactionId=value;
        }
        if (key.equals("reference_id")) {
            this.clientTransactionId=value;
        }
        if (key.equals("transaction_datetime")) {
            this.responseDateTime=value;
        }
        if (key.equals("approval_code")) {
            this.approvalCode=value;
        }
        if (key.equals("avs_response")) {
            this.avsResultCode=value;
        }
        if (key.equals("avs_response_description")) {
            this.avsResultText=value;
        }
        if (key.equals("cardsecurity_response")) {
            this.cvvResultCode=value;
        }
        if (key.equals("cashback_amount")) {
            this.cashbackAmount=value;
        }
        if (key.equals("type")) {
            this.type=value;
        }
        if (key.equals("masked_card_number")) {
            this.maskedCardNumber=value;
        }
        if (key.equals("cardholder_name")) {
            this.cardHolderName=value;
        }
        if (key.equals("expiry_month")) {
            this.expMonth=value;
        }
        if (key.equals("expiry_year")) {
            this.expYear=value;
        }
        if (key.equals("token")) {
            this.token=value;
        }
        if (key.equals("type")) {
            this.cardType= CardType.getValue(value.toLowerCase());
        }
        if (key.equals("balance")) {
            this.availableBalance=value;
        }
        if (key.equals("postal_code")) {
            this.postalCode=value;
        }
        if (key.equals("rfmiq")) {
            this.customerId= value;
        }
        if (key.equals("debit_trace_number")) {
            this.traceNumber=new Integer(value);
        }
        if (key.equals("tokenization_error_code")) {
            this.tokenResponseCode= new Integer(value);
        }
        if (key.equals("tokenization_error_message")) {
            this.tokenResponseMsg= value;
        }
        if (key.equals("amount_authorized")) {
            this.authorizedAmount=new BigDecimal(value);
        }
    }
}
