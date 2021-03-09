package com.global.api.gateways.bill_pay;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.billing.ConvenienceFeeResponse;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.billing.LoadSecurePayResponse;
import com.global.api.entities.enums.BillingLoadType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.bill_pay.requests.ClearLoadedBillsRequest;
import com.global.api.gateways.bill_pay.requests.CommitPreloadedBillsRequest;
import com.global.api.gateways.bill_pay.requests.GetConvenienceFeeRequest;
import com.global.api.gateways.bill_pay.requests.LoadSecurePayRequest;
import com.global.api.gateways.bill_pay.requests.PreloadBillsRequest;
import com.global.api.gateways.bill_pay.responses.BillingRequestResponse;
import com.global.api.gateways.bill_pay.responses.ConvenienceFeeRequestResponse;
import com.global.api.gateways.bill_pay.responses.PreloadBillsResponse;
import com.global.api.gateways.bill_pay.responses.SecurePayResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class BillingRequest extends GatewayRequestBase {
    public BillingRequest(Credentials credentials, String serviceUrl, int timeout) {
        this.credentials = credentials;
        this.serviceUrl = serviceUrl;
        this.timeout = timeout;
    }

    public BillingResponse execute(BillingBuilder builder) throws ApiException {
        switch (builder.getTransactionType())
        {
            case Activate:
                return commitPreloadBills();
            case Create:
                if (builder.getBillingLoadType().equals(BillingLoadType.BILLS)) {
                    return preloadBills(builder);
                }

                if (builder.getBillingLoadType().equals(BillingLoadType.SECURE_PAYMENT)) {
                    return loadSecurePay(builder);
                }

                throw new UnsupportedTransactionException();
            case Fetch:
                return getConvenienceFee(builder);
            case Delete:
                return clearLoadedBills();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private ConvenienceFeeResponse getConvenienceFee(BillingBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "GetConvenienceFee");
        String request = new GetConvenienceFeeRequest(et)
            .build(envelope, builder, credentials);

        String response = doTransaction(request);
        ConvenienceFeeResponse result = new ConvenienceFeeRequestResponse()
            .withResponseTagName("GetConvenienceFeeResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to retrieve the payment fee", result.getResponseCode(), result.getResponseMessage());
    }

    private BillingResponse preloadBills(BillingBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "PreloadBills");
        String request = new PreloadBillsRequest(et)
            .build(envelope, builder, credentials);

        String response = doTransaction(request);
        BillingResponse result = new PreloadBillsResponse()
            .withResponseTagName("PreloadBillsResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to load the hosted bills", result.getResponseCode(), result.getResponseMessage());
    }

    private BillingResponse commitPreloadBills() throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "CommitPreloadedBills");
        String request = new CommitPreloadedBillsRequest(et)
            .build(envelope, credentials);

        String response = doTransaction(request);
        BillingResponse result = new BillingRequestResponse()
            .withResponseTagName("CommitPreloadedBillsResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to commit the preloaded bills", result.getResponseCode(), result.getResponseMessage());
    }

    private BillingResponse clearLoadedBills() throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "ClearLoadedBills");
        String request = new ClearLoadedBillsRequest(et)
            .build(envelope, credentials);

        String response = doTransaction(request);

        return new BillingRequestResponse()
            .withResponseTagName("ClearLoadedBillsResponse")
            .withResponse(response)
            .map();
    }

    private LoadSecurePayResponse loadSecurePay(BillingBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "LoadSecurePayDataExtended");
        String request = new LoadSecurePayRequest(et)
            .build(envelope, builder, credentials);

        String response = doTransaction(request);
        LoadSecurePayResponse result = new SecurePayResponse()
            .withResponseTagName("LoadSecurePayDataExtendedResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return result;
        }

        throw new GatewayException("An error occurred attempting to load the hosted bill", result.getResponseCode(), result.getResponseMessage());
    }
}
