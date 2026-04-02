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

        if(builder.getInstallmentId() !=null && builder.getEntity() == null) {
            return (GpApiRequest)
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Get)
                            .setEndpoint(GpApiRequest.INSTALLMENT_ENDPOINT + "/" + builder.getInstallmentId());
        } else if (builder.getInstallmentId() == null && builder.getEntity() == null) {
            throw new ApiException("Installment id is mandatory and cannot be null");
        }
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
                .set("expiry_year", installment.getCreditCardData().getExpYear() != null ? StringUtils.padLeft(installment.getCreditCardData().getExpYear(), 4, '0').substring(2, 4) : null)
                .set("brand", installment.getCreditCardData().getCardType());

        JsonDoc paymentMethodData = new JsonDoc()
                .set("entry_mode", installment.getEntryMode())
                .set("card", cardData);
        requestData.set("payment_method", paymentMethodData);

        // Add Visa installment specific fields
        if (installment.getFundingMode() != null) {
            requestData.set("funding_mode", installment.getFundingMode());
        }

        if (installment.getInstallmentTerms() != null) {
            JsonDoc termsData = new JsonDoc();
            if (installment.getInstallmentTerms().getMaxTimeUnitNumber() != null) {
                termsData.set("max_time_unit_number", installment.getInstallmentTerms().getMaxTimeUnitNumber());
            }
            if (installment.getInstallmentTerms().getMaxAmount() != null) {
                termsData.set("max_amount", installment.getInstallmentTerms().getMaxAmount());
            }
            if (!termsData.isEmpty()) {
                requestData.set("terms", termsData);
            }
        }

        if (installment.getEligiblePlans() != null) {
            requestData.set("eligible_plans", installment.getEligiblePlans());
        }


        return requestData;
    }
}
