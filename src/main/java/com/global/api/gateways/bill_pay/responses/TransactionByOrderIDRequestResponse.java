package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.billing.AuthorizationRecord;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.Element;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionByOrderIDRequestResponse extends BillPayResponseBase<TransactionSummary> {
    protected static final String RESPONSE_CODE = "a:ResponseCode";
    protected static final String SINGLE_ZERO = "0";
    protected static final String DOUBLE_ZERO = "00";
    protected static final String GATEWAY_EXCEPTION_WITH_MESSAGE = "Unexpected Gateway Response: %s - %s";
    protected static final String TRANSACTION = "a:Transaction";
    protected static final String AMOUNT = "a:Amount";
    protected static final String FEE_AMOUNT = "a:FeeAmount";
    protected static final String MERCHANT_INVOICE_NUMBER = "a.MerchantInvoiceNumber";
    protected static final String MERCHANT_PO_NUMBER = "a.MerchantPONumber";
    protected static final String MERCHANT_TRANSACTION_DESCRIPTION = "a.MerchantTransactionDescription";
    protected static final String MERCHANT_TRANSACTION_ID = "a.MerchantTransactionID";
    protected static final String TRANSACTION_DATE = "a.TransactionDate";
    protected static final String APPLICATION= "a.Application";
    protected static final String BILL_TRANSACTIONS = "a:BillTransactions";
    protected static final String NET_AMOUNT = "a.NetAmount";
    protected static final String NET_FEE_AMOUNT = "a.NetFeeAmount";
    protected static final String AUTHORIZATIONS = "a:Authorizations";
    protected static final String PAYOR_ADDRESS = "a:PayorAddress";
    protected static final String PAYOR_CITY = "a:PayorCity";
    protected static final String PAYOR_COUNTRY = "a:PayorCountry";
    protected static final String PAYOR_POSTAL_CODE = "a:PayorPostalCode";
    protected static final String PAYOR_STATE = "a:PayorState";
    protected static final String PAYOR_BUSINESS_NAME = "a:PayorBusinessName";
    protected static final String PAYOR_EMAIL_ADDRESS = "a:PayorEmailAddress";
    protected static final String PAYOR_FIRST_NAME = "a:PayorFirstName";
    protected static final String PAYOR_LAST_NAME = "a:PayorLastName";
    protected static final String PAYOR_MIDDLE_NAME = "a:PayorMiddleName";
    protected static final String PAYOR_PHONE_NUMBER = "a:PayorPhoneNumber";
    protected static final String BILL_TRANSACTION_RECORD = "a:BillTransactionRecord";
    protected static final String BILL_TYPE = "a:BillType";
    protected static final String ID1 = "a:ID1";
    protected static final String ID2 = "a:ID2";
    protected static final String ID3 = "a:ID3";
    protected static final String ID4 = "a:ID4";
    protected static final String AMOUNT_TO_APPLY_BILL = "a:AmountToApplyToBill";
    protected static final String OBLIGOR_ADDRESS = "a:ObligorAddress";
    protected static final String OBLIGOR_CITY = "a:ObligorCity";
    protected static final String OBLIGOR_COUNTRY = "a:ObligorCountry";
    protected static final String OBLIGOR_POSTAL_CODE = "a:ObligorPostalCode";
    protected static final String OBLIGOR_STATE = "a:ObligorState";
    protected static final String OBLIGOR_EMAIL_ADDRESS = "a:ObligorEmailAddress";
    protected static final String OBLIGOR_FIRST_NAME = "a:ObligorFirstName";
    protected static final String OBLIGOR_LAST_NAME = "a:ObligorLastName";
    protected static final String OBLIGOR_MIDDLE_NAME = "a:ObligorMiddleName";
    protected static final String OBLIGOR_PHONE_NUMBER = "a:ObligorPhoneNumber";
    protected static final String AUTHORIZATION_RECORD = "a:AuthorizationRecord";
    protected static final String ADD_TO_BATCH_REF_NUMBER = "a:AddToBatchReferenceNumber";
    protected static final String AUTH_CODE = "a:AuthCode";
    protected static final String AUTH_TYPE = "a:AuthorizationType";
    protected static final String AVS_RESULT_CODE = "a:AvsResultCode";
    protected static final String AVS_RESULT_TEXT = "a:AvsResultText";
    protected static final String CARD_ENTRY_METHOD = "a:CardEntryMethod";
    protected static final String CVV_RESULT_CODE = "a:CvvResultCode";
    protected static final String CVV_RESULT_TEXT = "a:CvvResultText";
    protected static final String EMV_APP_CRYPTOGRAM = "a:EmvApplicationCryptogram";
    protected static final String EMV_APP_CRYPTOGRAM_TYPE = "a:EmvApplicationCryptogramType";
    protected static final String EMV_APP_ID = "a:EmvApplicationID";
    protected static final String EMV_APP_NAME = "a:EmvApplicationName";
    protected static final String EMV_CARDHOLDER_VERIFICATION_METHOD = "a:EmvCardholderVerificationMethod";
    protected static final String EMV_ISSUER_RESPONSE = "a:EmvIssuerResponse";
    protected static final String EMV_SIGNATURE_REQUIRED = "a:EmvSignatureRequired";
    protected static final String GATEWAY = "a:Gateway";
    protected static final String GATEWAY_BATCH_ID = "a:GatewayBatchID";
    protected static final String GATEWAY_DESCRIPTION = "a:GatewayDescription";
    protected static final String MASKED_ACCOUNT_NUMBER = "a:MaskedAccountNumber";
    protected static final String MASKED_ROUTING_NUMBER = "a:MaskedRoutingNumber";
    protected static final String PAYMENT_METHOD = "a:PaymentMethod";
    protected static final String REF_AUTH_ID = "a:ReferenceAuthorizationID";
    protected static final String REF_NUMBER = "a:ReferenceNumber";
    protected static final String ROUTING_NUMBER = "a:RoutingNumber";
    protected static final String AUTH_ID = "a.AuthorizationID";
    protected static final String ORIGINAL_AUTH_ID = "a:OriginalAuthorizationID";
    protected static final String TRANSACTION_TYPE = "a:TransactionType";
    protected static final String TRANSACTION_ID = "a:TransactionID";
    @SneakyThrows
    @Override
    public TransactionSummary map() {
        List<String> acceptedCodes = Arrays.asList(SINGLE_ZERO, DOUBLE_ZERO);
        String responseCode = response.getString(RESPONSE_CODE);
        String responseMessage = getFirstResponseMessage(response);

        if (!acceptedCodes.contains(responseCode)) {
            throw new GatewayException(String.format(GATEWAY_EXCEPTION_WITH_MESSAGE, responseCode, responseMessage),
                        responseCode,
                        responseMessage);
        }
        TransactionSummary summary = new TransactionSummary();
        summary.setGatewayResponseCode(responseCode);
        summary.setGatewayResponseMessage(responseMessage);

        Element transaction = response.get(TRANSACTION);
        if (transaction.getDecimal(AMOUNT) != null) {
            summary.setAmount(transaction.getDecimal(AMOUNT));
        }
        if (transaction.getDecimal(FEE_AMOUNT) != null) {
            summary.setFeeAmount(transaction.getDecimal(FEE_AMOUNT));
        }
        summary.setInvoiceNumber(transaction.getString(MERCHANT_INVOICE_NUMBER));
        summary.setPoNumber(transaction.getString(MERCHANT_PO_NUMBER));
        summary.setTransactionDescriptor(transaction.getString(MERCHANT_TRANSACTION_DESCRIPTION));
        summary.setMerchantId(transaction.getString(MERCHANT_TRANSACTION_ID));
        summary.setPayOrData(populatePayOrData(transaction));
        summary.setTransactionDate(transaction.getDateTime(TRANSACTION_DATE));
        summary.setAppName(transaction.getString(APPLICATION));
        Element billTransaction = transaction.get(BILL_TRANSACTIONS);
        summary.setBillTransactions(populateBillTransactionFromElement(billTransaction));
        summary.setTransactionType(transaction.getString(TRANSACTION_TYPE));
        if (transaction.getDecimal(TRANSACTION_ID) != null){
            summary.setTransactionId(String.valueOf(transaction.getDecimal(TRANSACTION_ID)));
        }
        if (transaction.getDecimal(NET_AMOUNT) != null) {
            summary.setNetAmount(transaction.getDecimal(NET_AMOUNT));
        }
        if (transaction.getDecimal(NET_FEE_AMOUNT) != null) {
            summary.setNetFeeAmount(transaction.getDecimal(NET_FEE_AMOUNT));
        }
        Element authorizationsElement = transaction.get(AUTHORIZATIONS);
        summary.setAuthorizationRecord(populateAuthorizationRecordsFromElement(authorizationsElement));
        return summary;
    }

    private Customer populatePayOrData(Element transactionElement) {
        Customer payOrData = new Customer();
        Address address = new Address();
        address.setStreetAddress1(transactionElement.getString(PAYOR_ADDRESS));
        address.setCity(transactionElement.getString(PAYOR_CITY));
        address.setCountry(transactionElement.getString(PAYOR_COUNTRY));
        address.setPostalCode(transactionElement.getString(PAYOR_POSTAL_CODE));
        address.setState(transactionElement.getString(PAYOR_STATE));
        payOrData.setAddress(address);

        payOrData.setCompany(transactionElement.getString(PAYOR_BUSINESS_NAME));
        payOrData.setEmail(transactionElement.getString(PAYOR_EMAIL_ADDRESS));
        payOrData.setFirstName(transactionElement.getString(PAYOR_FIRST_NAME));
        payOrData.setLastName(transactionElement.getString(PAYOR_LAST_NAME));
        payOrData.setMiddleName(transactionElement.getString(PAYOR_MIDDLE_NAME));
        payOrData.setWorkPhone(transactionElement.getString(PAYOR_PHONE_NUMBER));
        return payOrData;
    }

    private List<Bill> populateBillTransactionFromElement(Element billTrasactionElement) {
        if (billTrasactionElement.getElement().getChildNodes().getLength() > 0) {
            List<Bill> billTransactionsList = new ArrayList<>();
            for (Element bill :
                    billTrasactionElement.getAll(BILL_TRANSACTION_RECORD)) {
                Bill newBill = new Bill();
                newBill.setBillType(bill.getString(BILL_TYPE));
                newBill.setIdentifier1(bill.getString(ID1));
                newBill.setIdentifier2(bill.getString(ID2));
                newBill.setIdentifier3(bill.getString(ID3));
                newBill.setIdentifier4(bill.getString(ID4));
                if (bill.getDecimal(AMOUNT_TO_APPLY_BILL) != null) {
                    newBill.setAmount(bill.getDecimal(AMOUNT_TO_APPLY_BILL));
                }
                Customer customerData = new Customer();
                Address address = new Address();

                address.setStreetAddress1(bill.getString(OBLIGOR_ADDRESS));
                address.setCity(bill.getString(OBLIGOR_CITY));
                address.setCountry(bill.getString(OBLIGOR_COUNTRY));
                address.setPostalCode(bill.getString(OBLIGOR_POSTAL_CODE));
                address.setState(bill.getString(OBLIGOR_STATE));
                customerData.setAddress(address);

                customerData.setEmail(bill.getString(OBLIGOR_EMAIL_ADDRESS));
                customerData.setFirstName(bill.getString(OBLIGOR_FIRST_NAME));
                customerData.setLastName(bill.getString(OBLIGOR_LAST_NAME));
                customerData.setMiddleName(bill.getString(OBLIGOR_MIDDLE_NAME));
                customerData.setWorkPhone(bill.getString(OBLIGOR_PHONE_NUMBER));
                newBill.setCustomer(customerData);
                billTransactionsList.add(newBill);

            }
            return billTransactionsList;
        }
        return null;
    }

    private List<AuthorizationRecord> populateAuthorizationRecordsFromElement(Element authorizationsElement) {
        if (authorizationsElement.getElement().getChildNodes().getLength() > 0) {
            List<AuthorizationRecord> authorizationRecordList = new ArrayList<>();
            for (Element record :
                    authorizationsElement.getAll(AUTHORIZATION_RECORD)){
                AuthorizationRecord authorizationRecord = new AuthorizationRecord();
                authorizationRecord.setAddToBatchReferenceNumber(record.getString(ADD_TO_BATCH_REF_NUMBER));
                if (record.getDecimal(AMOUNT) != null){
                    authorizationRecord.setAmount(record.getDecimal(AMOUNT));
                }
                authorizationRecord.setAuthCode(record.getString(AUTH_CODE));
                authorizationRecord.setAuthorizationType(record.getString(AUTH_TYPE));
                authorizationRecord.setAvsResultCode(record.getString(AVS_RESULT_CODE));
                authorizationRecord.setAvsResultText(record.getString(AVS_RESULT_TEXT));
                authorizationRecord.setCardEntryMethod(record.getString(CARD_ENTRY_METHOD));
                authorizationRecord.setCvvResultCode(record.getString(CVV_RESULT_CODE));
                authorizationRecord.setCvvResultText(record.getString(CVV_RESULT_TEXT));
                authorizationRecord.setEmvApplicationCryptogram(record.getString(EMV_APP_CRYPTOGRAM));
                authorizationRecord.setEmvApplicationCryptogramType(record.getString(EMV_APP_CRYPTOGRAM_TYPE));
                authorizationRecord.setEmvApplicationID(record.getString(EMV_APP_ID));
                authorizationRecord.setEmvApplicationName(record.getString(EMV_APP_NAME));
                authorizationRecord.setEmvCardholderVerificationMethod(record.getString(EMV_CARDHOLDER_VERIFICATION_METHOD));
                authorizationRecord.setEmvIssuerResponse(record.getString(EMV_ISSUER_RESPONSE));
                authorizationRecord.setEmvSignatureRequired(record.getString(EMV_SIGNATURE_REQUIRED));
                authorizationRecord.setGateway(record.getString(GATEWAY));
                authorizationRecord.setGatewayBatchID(record.getString(GATEWAY_BATCH_ID));
                authorizationRecord.setGatewayDescription(record.getString(GATEWAY_DESCRIPTION));
                authorizationRecord.setMaskedAccountNumber(record.getString(MASKED_ACCOUNT_NUMBER));
                authorizationRecord.setMaskedRoutingNumber(record.getString(MASKED_ROUTING_NUMBER));
                authorizationRecord.setPaymentMethod(record.getString(PAYMENT_METHOD));
                if (record.getInt(REF_AUTH_ID) != null){
                    authorizationRecord.setReferenceAuthorizationID(record.getInt(REF_AUTH_ID));
                }
                authorizationRecord.setReferenceNumber(record.getString(REF_NUMBER));
                authorizationRecord.setRoutingNumber(record.getString(ROUTING_NUMBER));
                if (record.getDecimal(NET_AMOUNT) != null){
                    authorizationRecord.setNetAmount(record.getDecimal(NET_AMOUNT));
                }
                if (record.getInt(AUTH_ID) != null){
                    authorizationRecord.setAuthorizationID(record.getInt(AUTH_ID));
                }
                if (record.getInt(ORIGINAL_AUTH_ID) != null){
                    authorizationRecord.setOriginalAuthorizationID(record.getInt(ORIGINAL_AUTH_ID));
                }
                authorizationRecordList.add(authorizationRecord);
            }
            return authorizationRecordList;
        }
        return null;
    }
}
