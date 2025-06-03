package com.global.api.builders.requestbuilder.gpApi;

import com.global.api.builders.InstallmentBuilder;
import com.global.api.entities.IRequestBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.Installment;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

public class GpApiInstallmentRequestBuilder implements IRequestBuilder<InstallmentBuilder> {

    private static InstallmentBuilder _builder;

    /**
     * Build request for installment
     * @param builder
     * @param gateway
     * @return GpApiRequest
     * @throws ApiException
     */
    @Override
    public GpApiRequest buildRequest(InstallmentBuilder builder, GpApiConnector gateway) throws ApiException {
        _builder = builder;
        JsonDoc requestData = prepareInstallmentRequest(_builder, gateway);

        return (GpApiRequest)
                new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint(GpApiRequest.INSTALLMENT_ENDPOINT)
                        .setRequestBody(requestData.toString());
    }

    private static JsonDoc prepareInstallmentRequest(InstallmentBuilder builder, GpApiConnector gateway) {
        Installment installment = (Installment) builder.getEntity();
        JsonDoc requestData = new JsonDoc()
                .set("account_name", gateway.getGpApiConfig().getAccessTokenInfo().getTransactionProcessingAccountName())
                .set("amount", StringUtils.toNumeric(installment.getAmount()))
                .set("channel", gateway.getGpApiConfig().getChannel())
                .set("currency", installment.getCurrency())
                .set("country", gateway.getGpApiConfig().getCountry())
                .set("reference", installment.getReference())
                .set("program", installment.getProgram());


        JsonDoc cardData = new JsonDoc()
                .set("number", installment.getCreditCardData().getNumber())
                .set("expiry_month", installment.getCreditCardData().getExpMonth() != null ? StringUtils.padLeft(installment.getCreditCardData().getExpMonth(), 2, '0') : null)
                .set("expiry_year", installment.getCreditCardData().getExpYear() != null ? StringUtils.padLeft(installment.getCreditCardData().getExpYear(), 4, '0').substring(2, 4) : null);

        JsonDoc paymentMethodData = new JsonDoc()
                .set("entry_mode", installment.getEntryMode())
                .set("card", cardData);
        requestData.set("payment_method", paymentMethodData);
        return requestData;
    }
}
