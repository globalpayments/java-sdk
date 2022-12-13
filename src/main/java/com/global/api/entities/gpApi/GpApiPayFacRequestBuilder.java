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
                    if (builder.getUserPersonalData() == null) {
                        throw new IllegalArgumentException("Merchant data is mandatory!");
                    }

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

            default:
                break;
        }

        return null;
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
                .set("description", _builder.getDescription())
                .set("type", merchantData.getType().toString())
                .set("addresses", setAddressList())
                .set("payment_processing_statistics", setPaymentStatistics());

        var tier =
                new JsonDoc()
                        .set("reference", merchantData.getTier());

        data
                .set("tier", tier)
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
                var address = new HashMap<String, Object>();
                address.put("line_1", person.getAddress().getStreetAddress1());
                address.put("line_2", person.getAddress().getStreetAddress2());
                address.put("line_3", person.getAddress().getStreetAddress3());
                address.put("city", person.getAddress().getCity());
                address.put("state", person.getAddress().getState());
                address.put("postal_code", person.getAddress().getPostalCode());
                address.put("country", person.getAddress().getCountryCode());

                item.put("address", address);
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
                bank.put("code", bankAccountData.getRoutingNumber());   // @TODO confirmantion from GP-API team
            }

            bank.put("international_code", "");                         // @TODO confirmantion from GP-API team

            if (bankAccountData.getBankAddress() != null) {
                var address = new HashMap<>();

                address.put("line_1", bankAccountData.getBankAddress().getStreetAddress1());
                address.put("line_2", bankAccountData.getBankAddress().getStreetAddress2());
                address.put("line_3", bankAccountData.getBankAddress().getStreetAddress3());
                address.put("city", bankAccountData.getBankAddress().getCity());
                address.put("postal_code", bankAccountData.getBankAddress().getPostalCode());
                address.put("state", bankAccountData.getBankAddress().getState());
                address.put("country", CountryUtils.getCountryCodeByCountry(bankAccountData.getBankAddress().getCountryCode()));

                bank.put("address", address);
            }

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
            var item = new HashMap<String, Object>();
            item.put("quantity", product.getQuantity());
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

            item.put("functions", new String[] { address.getKey() });
            item.put("line_1", dataAddress.getStreetAddress1());
            item.put("line_2", dataAddress.getStreetAddress2());
            item.put("city", dataAddress.getCity());
            item.put("postal_code", dataAddress.getPostalCode());
            item.put("state", dataAddress.getState());
            item.put("country", CountryUtils.getCountryCodeByCountry(dataAddress.getCountryCode()));

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
