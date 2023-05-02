package com.global.api.entities.gpApi;

import com.global.api.builders.PayFacBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Product;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.payFac.BankAccountData;
import com.global.api.entities.payFac.Person;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.utils.CountryUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpApiPayFacRequestBuilder {

    private static PayFacBuilder _builder;

    public static GpApiRequest buildRequest(PayFacBuilder builder, GpApiConnector gateway) {
        _builder = builder;

        var merchantUrl = !StringUtils.isNullOrEmpty(gateway.getGpApiConfig().getMerchantId()) ? "/merchants/" + gateway.getGpApiConfig().getMerchantId() : "";

        switch (builder.getTransactionType()) {

            case Create:
                if (builder.getTransactionModifier() == TransactionModifier.Merchant) {
                    var data = buildCreateMerchantRequest();

                    return
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Post)
                                    .setEndpoint(merchantUrl + "/merchants")
                                    .setRequestBody(data.toString());
                }
                break;

            case Edit:
                if (builder.getTransactionModifier() == TransactionModifier.Merchant) {
                    return
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Patch)
                                    .setEndpoint(merchantUrl + "/merchants/" + _builder.getUserReference().getUserId())
                                    .setRequestBody(buildEditMerchantRequest().toString());
                }
                break;

            case Fetch:
                if (builder.getTransactionModifier() == TransactionModifier.Merchant) {
                    return
                            new GpApiRequest()
                                    .setVerb(GpApiRequest.HttpMethod.Get)
                                    .setEndpoint(merchantUrl + "/merchants/" + _builder.getUserReference().getUserId());
                }
                break;

            case EditAccount:
                var dataRequest = new JsonDoc();
                HashMap<String, Object> paymentMethod = new HashMap<>();

                if (builder.getCreditCardInformation() != null) {
                    var card = new HashMap<String, Object>();
                    card.put("name", builder.getCreditCardInformation() != null ? builder.getCreditCardInformation().getCardHolderName() : null);
                    card.put("card", builder.getCreditCardInformation() instanceof CreditCardData ? mapCreditCardInfo(builder.getCreditCardInformation()) : null);

                    paymentMethod.put("payment_method", card);
                }

                if ((builder.getAddresses() != null) && (builder.getAddresses().containsKey(AddressType.Billing))){
                    paymentMethod.put("billing_address",
                            mapAddress((Address) builder.getAddresses().get(AddressType.Billing), "alpha2", null));
                }

                dataRequest.set("payer", paymentMethod);

                String endpoint = merchantUrl;
                if (builder.getUserReference() != null && !StringUtils.isNullOrEmpty(builder.getUserReference().getUserId())) {
                    endpoint = "/merchants/" + builder.getUserReference().getUserId();
                }

            return
                        new GpApiRequest()
                                .setVerb(GpApiRequest.HttpMethod.Patch)
                                .setEndpoint(endpoint + "/accounts/" + _builder.getAccountNumber())
                                .setRequestBody(dataRequest.toString());

            default:
                break;
        }

        return null;
    }

    private static HashMap<String, Object> mapAddress(Address address, String countryCodeType, String functionKey) {
        if(StringUtils.isNullOrEmpty(countryCodeType)) {
            countryCodeType = "alpha2";
        }

        var countryCode = "";

        switch (countryCodeType) {
            case "alpha2":
                countryCode = CountryUtils.getCountryCodeByCountry(address.getCountryCode());
                break;

            default:
                countryCode = address.getCountryCode();
                break;
        }

        HashMap item = new HashMap<>();

        if(address != null) {
            if (!StringUtils.isNullOrEmpty(functionKey))
                item.put("functions", new String[]{functionKey});
            if (!StringUtils.isNullOrEmpty(address.getStreetAddress1()))
                item.put("line_1", address.getStreetAddress1());
            if (!StringUtils.isNullOrEmpty(address.getStreetAddress2()))
                item.put("line_2", address.getStreetAddress2());
            if (!StringUtils.isNullOrEmpty(address.getStreetAddress3()))
                item.put("line_3", address.getStreetAddress3());
            if (!StringUtils.isNullOrEmpty(address.getCity()))
                item.put("city", address.getCity());
            if (!StringUtils.isNullOrEmpty(address.getPostalCode()))
                item.put("postal_code", address.getPostalCode());
            if (!StringUtils.isNullOrEmpty(address.getState()))
                item.put("state", address.getState());

            item.put("country", countryCode);
        }

        return item;
    }

    private static HashMap<String, Object> mapCreditCardInfo(CreditCardData value) {
        HashMap item = new HashMap<String, Object>();

        item.put("name", value.getCardHolderName());
        item.put("number", value.getNumber());
        item.put("expiry_month", value.getExpMonth() != null ? StringUtils.padLeft(value.getExpMonth(), 2, '0') : null);
        item.put("expiry_year", value.getExpYear() != null ? StringUtils.padLeft(value.getExpYear(), 4, '0').substring(2, 4) : null);
        item.put("cvv", value.getCvn());

        return item;
    }

    private static JsonDoc setMerchantInfo() {
        if (_builder.getUserPersonalData() == null) {
            return new JsonDoc();
        }

        var merchantData = _builder.getUserPersonalData();

        var data =
                new JsonDoc()
                    .set("name", merchantData.getUserName())
                    .set("legal_name", merchantData.getLegalName())
                    .set("dba", merchantData.getDBA())
                    .set("merchant_category_code", merchantData.getMerchantCategoryCode())
                    .set("website", merchantData.getWebsite())
                    .set("currency", merchantData.getCurrencyCode())
                    .set("tax_id_reference", merchantData.getTaxIdReference())
                    .set("notification_email", merchantData.getNotificationEmail())
                    .set("status", _builder.getUserReference() != null && _builder.getUserReference().getUserStatus() != null ? _builder.getUserReference().getUserStatus().toString() : null);

        var notifications =
                new JsonDoc()
                        .set("status_url", merchantData.getNotificationStatusUrl());

        if (notifications.getKeys() != null) {
            data.set("notifications", notifications);
        }

        return data;
    }

    private static JsonDoc buildCreateMerchantRequest() {
        var merchantData = _builder.getUserPersonalData();
        var data = setMerchantInfo();
        data
                .set("pricing_profile", merchantData.getTier())
                .set("description", _builder.getDescription())
                .set("type", merchantData.getType().toString())
                .set("addresses", setAddressList())
                .set("payment_processing_statistics", setPaymentStatistics());

        data
                .set("payment_methods", setPaymentMethod())
                .set("persons", setPersonList(null))
                .set("products", _builder.getProductData().size() > 0 ? setProductList(_builder.getProductData()) : null);

        return data;
    }

    private static JsonDoc setPaymentStatistics()
    {
        if (_builder.getPaymentStatistics() == null) {
            return null;
        }

        return
                new JsonDoc()
                        .set("total_monthly_sales_amount", StringUtils.toNumeric(_builder.getPaymentStatistics().getTotalMonthlySalesAmount()))
                        .set("average_ticket_sales_amount", StringUtils.toNumeric(_builder.getPaymentStatistics().getAverageTicketSalesAmount()))
                        .set("highest_ticket_sales_amount", StringUtils.toNumeric(_builder.getPaymentStatistics().getHighestTicketSalesAmount()));
    }

    private static ArrayList<HashMap<String, Object>> setPersonList(String type) {
        if (_builder.getPersonsData() == null || _builder.getPersonsData().size() == 0) {
            return null;
        }

        var personInfo = new ArrayList<HashMap<String, Object>>();
        for (Person person : (List<Person>) _builder.getPersonsData()) {
            var item = new HashMap<String, Object>();
            item.put("functions", new String[] { person.getFunctions().toString() });
            item.put("first_name", person.getFirstName());
            item.put("middle_name", person.getMiddleName());
            item.put("last_name", person.getLastName());
            item.put("email", person.getEmail());
            item.put("date_of_birth", person.getDateOfBirth() != null ? person.getDateOfBirth() : null);
            item.put("national_id_reference", person.getNationalIdReference());
            item.put("equity_percentage", person.getEquityPercentage());
            item.put("job_title", person.getJobTitle());

            if (person.getAddress() != null && type == null) {
                item.put("address", mapAddress(person.getAddress(), "alpha2", null));
            }

            if (person.getHomePhone() != null) {
                var contactPhone = new HashMap<String, Object>();
                contactPhone.put("country_code", person.getHomePhone().getCountryCode());
                contactPhone.put("subscriber_number", person.getHomePhone().getNumber());

                item.put("contact_phone", contactPhone);
            }

            if (person.getWorkPhone() != null) {
                var workPhone = new HashMap<String, Object>();
                workPhone.put("country_code", person.getWorkPhone().getCountryCode());
                workPhone.put("subscriber_number", person.getWorkPhone().getNumber());

                item.put("work_phone", workPhone);
            }

            personInfo.add(item);
        }

        return personInfo;
    }

    private static HashMap setBankTransferInfo(BankAccountData bankAccountData) {
        if (bankAccountData != null) {
            var data = new HashMap<>();
            data.put("account_holder_type", bankAccountData.getAccountOwnershipType());
            data.put("account_number", bankAccountData.getAccountNumber());
            data.put("account_type", bankAccountData.getAccountType());

            var bank = new HashMap();

            if (!StringUtils.isNullOrEmpty(bankAccountData.getBankName())) {
                bank.put("name", bankAccountData.getBankName());
            }

            if (!StringUtils.isNullOrEmpty(bankAccountData.getRoutingNumber())) {
                bank.put("code", bankAccountData.getRoutingNumber());   // @TODO confirmation from GP-API team
            }

            bank.put("international_code", "");                         // @TODO confirmation from GP-API team

            bank.put("address", (bankAccountData.getBankAddress() != null) ? mapAddress(bankAccountData.getBankAddress(), "alpha2", null) : null);

            data.put("bank", bank);

            return data;
        }

        return null;
    }

    private static HashMap setCreditCardInfo(CreditCardData creditCardInformation) {
        if(creditCardInformation!= null) {
            HashMap<Object, Object> ret = new HashMap<>();
            if (creditCardInformation.getCardHolderName() != null) {
                ret.put("name", creditCardInformation.getCardHolderName());
            }
            ret.put("number", creditCardInformation.getNumber());
            ret.put("expiry_month", creditCardInformation.getExpMonth());
            ret.put("expiry_year", creditCardInformation.getExpYear());

            return ret;
        }

        return null;
    }

    private static ArrayList<HashMap<String, Object>> setProductList(List<Product> productData) {
        var products = new ArrayList<HashMap<String, Object>>();

        for (var product : productData) {
            var deviceInfo = new HashMap<String, Object>();
            if (product.getProductId().contains("_CP-")) {
                deviceInfo.put("quantity", 1);
            }

            var item = new HashMap<String, Object>();
            item.put("device", deviceInfo.size() > 0 ? deviceInfo : null);
            item.put("id", product.getProductId());

            products.add(item);
        }

        return products;
    }

    private static ArrayList<HashMap<String, Object>> setPaymentMethod() {
        if(_builder.getPaymentMethodsFunctions() == null) {
            return null;
        }

        var paymentMethods = new ArrayList<HashMap<String, Object>>();
        var item1 = new HashMap<String, Object>();

        item1.put("functions", new String[] { _builder.getPaymentMethodsFunctions().get(_builder.getCreditCardInformation().getCardType()).toString() } );
        item1.put("card", setCreditCardInfo(_builder.getCreditCardInformation()));

        paymentMethods.add(item1);

        var item2 = new HashMap<String, Object>();

        item2.put("functions", new String[] { _builder.getPaymentMethodsFunctions().get(_builder.getBankAccountData().getAccountType()).toString() });
        item2.put("name", _builder.getBankAccountData().getAccountHolderName());
        item2.put("bank_transfer", setBankTransferInfo(_builder.getBankAccountData()));

        paymentMethods.add(item2);

        return paymentMethods;
    }

    private static ArrayList<HashMap<String, Object>> setAddressList() {
        if (_builder.getUserPersonalData() == null) {
            return null;
        }

        var merchantData = _builder.getUserPersonalData();
        var addressList = new HashMap<String, Object>();

        if (!StringUtils.isNullOrEmpty(merchantData.getUserAddress().getStreetAddress1())) {
            addressList.put(AddressType.Business.toString(), merchantData.getUserAddress());
        }

        if (!StringUtils.isNullOrEmpty(merchantData.getMailingAddress().getStreetAddress1())) {
            addressList.put(AddressType.Shipping.toString(), merchantData.getMailingAddress());
        }

        var addresses = new ArrayList<HashMap<String, Object>>();

        for (Map.Entry<String, Object> address : addressList.entrySet()) {
            var item = new HashMap<String, Object>();
            var dataAddress = ((Address) address.getValue());

            addresses.add(mapAddress(dataAddress, "alpha2", address.getKey()));

            addresses.add(item);
        }

        return addresses;
    }

    private static JsonDoc buildEditMerchantRequest() {
        var requestBody = setMerchantInfo();
        // requestBody.set("description", _builder.getDescription());
        requestBody.set("status_change_reason", _builder.getStatusChangeReason() != null ? _builder.getStatusChangeReason().toString() : null);
        requestBody.set("addresses", setAddressList());
        requestBody.set("persons", setPersonList("edit"));
        requestBody.set("payment_processing_statistics", setPaymentStatistics());
        requestBody.set("payment_methods", setPaymentMethod());

        return requestBody;
    }

}