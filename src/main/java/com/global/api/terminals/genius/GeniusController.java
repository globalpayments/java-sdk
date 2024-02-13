package com.global.api.terminals.genius;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.genius.builders.MitcManageBuilder;
import com.global.api.terminals.genius.enums.MitcRequestType;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.genius.enums.GeniusEndpoints;
import com.global.api.terminals.genius.interfaces.MitcGateway;
import com.global.api.terminals.genius.request.GeniusMitcRequest;
import com.global.api.terminals.genius.responses.MitcResponse;
import com.global.api.terminals.pax.responses.LocalDetailReportResponse;
import com.global.api.utils.AmountUtils;
import com.global.api.utils.JsonDoc;
import lombok.Getter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GeniusController extends DeviceController {

    private IDeviceInterface _device;
    @Getter
    private MitcGateway gateway;

    public GeniusController(ITerminalConfiguration settings) throws ConfigurationException {
        super(settings);

        _device = new GeniusInterface(this);

        if (settings.getConnectionMode() == ConnectionModes.MEET_IN_THE_CLOUD) {
            gateway = new MitcGateway(settings);
        } else {
            throw new ConfigurationException("Unsupported connection mode.");
        }
    }

    @Override
    public IDeviceInterface configureInterface() throws ConfigurationException {
        if (_device == null)
            _device = new GeniusInterface(this);
        return _device;
    }

    public MitcResponse send(
            String message,
            MitcRequestType requestType,
            String targetId
    ) throws GatewayException, NoSuchAlgorithmException, InvalidKeyException {
        String endpoint = "";
        GeniusMitcRequest.HttpMethod verb = null;
        switch (requestType) {
            case CARD_PRESENT_SALE:
                endpoint = GeniusEndpoints.CARD_PRESENT_SALE.getValue();
                verb = GeniusEndpoints.CARD_PRESENT_SALE.getMethod();
                break;
            case CARD_PRESENT_REFUND:
                endpoint = GeniusEndpoints.CARD_PRESENT_REFUND.getValue();
                verb = GeniusEndpoints.CARD_PRESENT_REFUND.getMethod();
                break;
            case REPORT_SALE_CLIENT_ID:
                endpoint = GeniusEndpoints.REPORT_SALE_CLIENT_ID.getValue();
                verb = GeniusEndpoints.REPORT_SALE_CLIENT_ID.getMethod();
                break;
            case REPORT_REFUND_CLIENT_ID:
                endpoint = GeniusEndpoints.REPORT_REFUND_CLIENT_ID.getValue();
                verb = GeniusEndpoints.REPORT_REFUND_CLIENT_ID.getMethod();
                break;
            case REFUND_BY_CLIENT_ID:
                endpoint = "/creditsales/reference_id/" + targetId + "/creditreturns"; // had to remove the leading "/transactions/" from underlying endpoint
                verb = GeniusEndpoints.REFUND_BY_CLIENT_ID.getMethod();
                break;
            case VOID_CREDIT_SALE:
                endpoint = GeniusEndpoints.VOID_CREDIT_SALE.getValue();
                endpoint = endpoint.replace("%s", targetId);
                verb = GeniusEndpoints.VOID_CREDIT_SALE.getMethod();
                break;
            case VOID_DEBIT_SALE:
                endpoint = GeniusEndpoints.VOID_DEBIT_SALE.getValue();
                endpoint = endpoint.replace("%s", targetId);
                verb = GeniusEndpoints.VOID_DEBIT_SALE.getMethod();
                break;
            case VOID_REFUND:
                endpoint = GeniusEndpoints.VOID_REFUND.getValue();
                endpoint = endpoint.replace("%s", targetId);
                verb = GeniusEndpoints.VOID_REFUND.getMethod();
                break;
        }
        return this.gateway.doTransaction(verb, endpoint, message, requestType);
    }

    @Override
    public MitcResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        JsonDoc healthcareAmount = new JsonDoc();

        if (builder.getAutoSubstantiation() != null) {
            AutoSubstantiation autoSub = builder.getAutoSubstantiation();
            healthcareAmount
                    .set("copay_amount", AmountUtils.transitFormat(autoSub.getCopaySubTotal()))
                    .set("clinical_amount", AmountUtils.transitFormat(autoSub.getClinicSubTotal()))
                    .set("dental_amount", AmountUtils.transitFormat(autoSub.getDentalSubTotal()))
                    .set("prescription_amount", AmountUtils.transitFormat(autoSub.getPrescriptionSubTotal()))
                    .set("vision_amount", AmountUtils.transitFormat(autoSub.getVisionSubTotal()))
                    .set("healthcare_total_amount", AmountUtils.transitFormat(autoSub.getTotalHelthcareAmount()));
        }
        JsonDoc purchaseOrder = new JsonDoc();

        if (builder.getAddress() != null && builder.getAddress().getPostalCode() != null)
            purchaseOrder.set("destination_postal_code", builder.getAddress().getPostalCode());

        if (builder.getAddress() != null && builder.getAddress().getPostalCode() != null)
            purchaseOrder.set("destination_postal_code", builder.getAddress().getPostalCode());

        purchaseOrder
                .set("po_number", builder.getPoNumber())
                .set("tax_amount", AmountUtils.transitFormat(builder.getTaxAmount()));

        JsonDoc payment = new JsonDoc();
        payment
                .set("amount", AmountUtils.transitFormat(builder.getAmount()))
                .set("currency_code", "840")
                .set("invoice_number", builder.getInvoiceNumber());

        if (builder.getTransactionType() == TransactionType.Sale) {
            payment
                    .set("gratuity_eligible_amount", AmountUtils.transitFormat(builder.getGratuity()))
                    .set("healthcare_amounts", healthcareAmount);
//                    .set("purchase_order", getPurchaseOrderData(builder));
        }

        JsonDoc receipt = new JsonDoc();

        if (builder.getClerkNumber() != null) {
            receipt.set("clerk_id", builder.getClerkNumber());
        } else {
            receipt.set("clerk_id", "NA");
        }

        JsonDoc processingIndicator = new JsonDoc();
        processingIndicator
                .set("allow_duplicate", builder.isAllowDuplicates());
        if (builder.getTransactionType() == TransactionType.Sale) {
            processingIndicator
                    .set("create_token", builder.isRequestMultiUseToken())
                    .set("partial_approval", builder.isAllowPartialAuth());
        }

        JsonDoc terminal = new JsonDoc().set("terminal_id", gateway.getConfig().getGeniusMitcConfig().getTerminalId());

        JsonDoc transaction = new JsonDoc();
        if (gateway.getConfig().getGeniusMitcConfig().isAllowKeyEntry()) {
            transaction
                    .set("keyed_entry_mode", "allowed");
        }

        transaction
                .set("country_code", "840")
                .set("language", "en-US");

        if (!processingIndicator.getKeys().isEmpty())
            transaction.set("processing_indicators", processingIndicator);

        if (builder.isRequestMultiUseToken()) {
            if (builder.getStoredCredentialInitiator() == StoredCredentialInitiator.CardHolder) {
                transaction.set("create_token_reason", "unscheduled_customer_initiated_transaction");
            } else {
                transaction.set("create_token_reason", "unscheduled_merchant_initiated_transaction");
            }
        }

        transaction.set("terminal", terminal);

        JsonDoc request = new JsonDoc();
        // Generating the reference ID.
        request.set("reference_id", builder.getClientTransactionId());

        request.set("payment", payment);

        request.set("receipt", receipt);

        request.set("transaction", transaction);

        MitcRequestType requestType = null;

        if (builder.getTransactionType() == TransactionType.Sale) {
            requestType = MitcRequestType.CARD_PRESENT_SALE;
        } else if (builder.getTransactionType() == TransactionType.Refund) {
            requestType = MitcRequestType.CARD_PRESENT_REFUND;
        }

        try {
            return send(request.toString(), requestType, null);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        return null;
    }

    @Override
    public LocalDetailReportResponse processLocalDetailReport(TerminalReportBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public TerminalResponse processReport(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        MitcRequestType requestType;

        GeniusEndpoints endpoint = null;
        if (transactionType == TransactionType.Sale) {
            if (transactionIdType == TransactionIdType.CLIENT_TRANSACTION_ID) {
                requestType = MitcRequestType.REPORT_SALE_CLIENT_ID;
                endpoint = GeniusEndpoints.REPORT_SALE_CLIENT_ID;
            }
            else {
                requestType = MitcRequestType.REPORT_SALE_GATEWAY_ID;
            }
        } else if (transactionType == TransactionType.Refund) {
            if (transactionIdType == TransactionIdType.CLIENT_TRANSACTION_ID) {
                requestType = MitcRequestType.REPORT_REFUND_CLIENT_ID;
                endpoint = GeniusEndpoints.REPORT_REFUND_CLIENT_ID;
            }
            else {
                requestType = MitcRequestType.REPORT_REFUND_GATEWAY_ID;
            }
        } else {
            throw new ApiException("Target transaction type must be either a sale or refund");
        }

        GeniusMitcRequest request = new GeniusMitcRequest();
        request.setEndpoint(String.format(endpoint.getValue(), transactionId));
        request.setVerb(endpoint.getMethod());

        return this.gateway.doTransaction(request.getVerb(),request.getEndpoint(),null, requestType);
    }
    public TerminalResponse manageTransaction(MitcManageBuilder builder) throws ApiException, NoSuchAlgorithmException, InvalidKeyException  {
        JsonDoc request = new JsonDoc();
        MitcRequestType requestType = null;
        String targetId = builder.getClientTransactionId();

        /* VoidCredit, VoidCreditRefund && RefundSale Request*/
        if((builder.followOnTransactionType == TransactionType.Void || builder.followOnTransactionType== TransactionType.Refund)
                && (builder.getPaymentMethodType() != PaymentMethodType.Debit)){

            JsonDoc payment = new JsonDoc();
            if(AmountUtils.transitFormat(builder.getAmount())!=null){
                payment.set("amount", AmountUtils.transitFormat(builder.getAmount()));
            }
            request.set("payment", payment);

            /* RefundSale Request*/
            if(builder.originalTransType==TransactionType.Sale && builder.followOnTransactionType == TransactionType.Refund){
                payment.set("invoice_number", builder.getInvoiceNumber());
                JsonDoc customer = new JsonDoc();
                if(builder.getCustomer()!=null) {
                    customer.set("id", builder.getCustomer().getId());
                    customer.set("title", builder.getCustomer().getTitle());
                    customer.set("first_name", builder.getCustomer().getFirstName());
                    customer.set("middle_name", builder.getCustomer().getMiddleName());
                    customer.set("last_name", builder.getCustomer().getLastName());
                    customer.set("business_name", builder.getCustomer().getCompany());
                    customer.set("email", builder.getCustomer().getEmail());
                    customer.set("phone", builder.getCustomer().getHomePhone());
                    customer.set("note", builder.getCustomer().getNote());
                    JsonDoc address = new JsonDoc();
                    address.set("line1", builder.getCustomer().getAddress().getStreetAddress1());
                    address.set("line2", builder.getCustomer().getAddress().getStreetAddress2());
                    address.set("city", builder.getCustomer().getAddress().getCity());
                    address.set("state", builder.getCustomer().getAddress().getState());
                    address.set("country", builder.getCustomer().getAddress().getCountry());
                    address.set("postal_code", builder.getCustomer().getAddress().getPostalCode());
                    customer.set("billing_address", address);
                }
                request.set("customer", customer);
            }
        }


        JsonDoc transaction = new JsonDoc();
        JsonDoc processingIndicators = new JsonDoc();

        /* VoidDebit Request*/
        if(builder.originalTransType==TransactionType.Sale && builder.getPaymentMethodType() == PaymentMethodType.Debit
                && builder.followOnTransactionType==TransactionType.Void){
            transaction.set("message_authentication_code", builder.getMessageAuthCode());
            transaction.set("reason_code", builder.getReasonCode());
            transaction.set("tracking_id", builder.getTrackingId());

            JsonDoc receipt = new JsonDoc();
            receipt.set("signature_image", builder.getSignatureImage());
            receipt.set("signature_format", builder.getSignatureFormat());
            receipt.set("signature_line", builder.getSignatureLine());
            request.set("receipt", receipt);
        }

        processingIndicators.set("generate_receipt", builder.receipt);

        /* RefundSale Request*/
        if(builder.originalTransType==TransactionType.Sale && builder.followOnTransactionType == TransactionType.Refund){
            transaction.set("soft_descriptor", builder.getSoftDescriptor());
            if(builder.isAllowDuplicates()){
                processingIndicators.set("allow_duplicate", builder.isAllowDuplicates());
            }
        }
        transaction.set("processing_indicators", processingIndicators);
        request.set("transaction", transaction);

        if (builder.followOnTransactionType == TransactionType.Void) {
            if (builder.originalTransType == TransactionType.Refund) {
                requestType = MitcRequestType.VOID_REFUND;
            } else {
                if (builder.getPaymentMethodType() == PaymentMethodType.Credit) {
                    requestType = MitcRequestType.VOID_CREDIT_SALE;
                } else {
                    requestType = MitcRequestType.VOID_DEBIT_SALE;
                }
            }
        } else if (builder.followOnTransactionType == TransactionType.Refund) {
            requestType = MitcRequestType.REFUND_BY_CLIENT_ID;
        }

        return send(request.toString(), requestType, targetId);
    }
}
