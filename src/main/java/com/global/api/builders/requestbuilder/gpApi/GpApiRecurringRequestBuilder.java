package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.Customer;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.Request;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.utils.JsonDoc;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpApiRecurringRequestBuilder implements IRequestBuilder {

    private final Map<String, String> maskedData = new HashMap<>();
    private RecurringBuilder builder;

    @Override
    public GpApiRequest buildRequest(Object builder, GpApiConnector gateway) throws ApiException {
        this.builder = (RecurringBuilder) builder;
        JsonDoc requestDataPayer = new JsonDoc();
        Map<String, Object> requestData = new HashMap<>();
        String merchantUrl = "";
        if(gateway.getGpApiConfig().getMerchantId() != null) {
            merchantUrl = "/merchants/"+ gateway.getGpApiConfig().getMerchantId();
        }
        Request.HttpMethod verb;
        String endpoint;
        switch (((RecurringBuilder<?>) builder).getTransactionType()) {
            case Create:
                endpoint = merchantUrl + GpApiRequest.PAYERS_ENDPOINT;
                verb = GpApiRequest.HttpMethod.Post;
                if (((RecurringBuilder<?>) builder).getEntity() instanceof Customer) {
                    requestDataPayer = preparePayerRequest();
                }
                break;
            case Edit:
                endpoint = merchantUrl + GpApiRequest.PAYERS_ENDPOINT + "/";
                if (this.builder.getEntity().getId() != null) {
                    endpoint = endpoint + this.builder.getEntity().getId();
                }
                verb = GpApiRequest.HttpMethod.Patch;
                if (((RecurringBuilder<?>) builder).getEntity() instanceof Customer) {
                    requestDataPayer = preparePayerRequest();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + ((RecurringBuilder<?>) builder).getTransactionType());
        }
        return (GpApiRequest) new GpApiRequest()
                .setVerb(verb)
                .setEndpoint(endpoint)
                .setRequestBody(requestDataPayer.toString())
                .setMaskedData(maskedData);
    }


    private JsonDoc preparePayerRequest() {
        Customer customer = (Customer) builder.getEntity();
        var data = new JsonDoc()
                .set("first_name", customer.getFirstName())
                .set("last_name", customer.getLastName())
                .set("reference", customer.getKey());
        if (customer.getPaymentMethods() != null && !customer.getPaymentMethods().isEmpty()) {
            ArrayList<HashMap<String, Object>> paymentsToAdd = new ArrayList<>();
            for (RecurringPaymentMethod paymentMethod : customer.getPaymentMethods()) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("id", paymentMethod.getId());
                item.put("default", customer.getPaymentMethods().get(0).getId().equals(paymentMethod.getId()) ? "YES" : "NO");
                paymentsToAdd.add(item);
            }
            data.set("payment_methods", paymentsToAdd);
        }
        return data;
    }


}

