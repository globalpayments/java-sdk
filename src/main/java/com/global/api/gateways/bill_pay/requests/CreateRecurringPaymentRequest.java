package com.global.api.gateways.bill_pay.requests;

import com.global.api.entities.Customer;
import com.global.api.entities.Schedule;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.enums.ScheduleFrequency;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateRecurringPaymentRequest extends BillPayRequestBase {
    protected static final String SOAPENV_BODY = "soapenv:Body";
    protected static final String BIL_CREATE_RECURRING_PAYMENT = "bil:CreateRecurringPayment";
    protected static final String BIL_REQUEST = "bil:request";
    protected static final String BDMS_BILLS = "bdms:Bills";
    protected static final String BDMS_BILL_TYPE = "bdms:BillType";
    protected static final String BDMS_ID1 = "bdms:ID1";
    protected static final String BDMS_ID2 = "bdms:ID2";
    protected static final String BDMS_ID3 = "bdms:ID3";
    protected static final String BDMS_ID4 = "bdms:ID4";
    protected static final String BDMS_OBLIGOR_ADDRESS = "bdms:ObligorAddress";
    protected static final String BDMS_OBLIGOR_CITY = "bdms:ObligorCity";
    protected static final String BDMS_OBLIGOR_COUNTRY = "bdms:ObligorCountry";
    protected static final String BDMS_OBLIGOR_EMAIL_ADDRESS = "bdms:ObligorEmailAddress";
    protected static final String BDMS_OBLIGOR_FIRST_NAME = "bdms:ObligorFirstName";
    protected static final String BDMS_OBLIGOR_LAST_NAME = "bdms:ObligorLastName";
    protected static final String BDMS_OBLIGOR_MIDDLE_NAME = "bdms:ObligorMiddleName";
    protected static final String BDMS_OBLIGOR_PHONE_NUMBER = "bdms:ObligorPhoneNumber";
    protected static final String BDMS_OBLIGOR_POSTAL_CODE = "bdms:ObligorPostalCode";
    protected static final String BDMS_OBLIGOR_STATE = "bdms:ObligorState";
    protected static final String BDMS_RECURRING_PAYMENT_BILL = "bdms:RecurringPaymentBill";
    protected static final String BDMS_INITIAL_PAYMENT_METHOD = "bdms:InitialPaymentMethod";
    protected static final String BDMS_INSTANCE_PAYMENT_AMOUNT = "bdms:InstancePaymentAmount";
    protected static final String BDMS_NUMBER_OF_INSTANCE = "bdms:NumberOfInstance";
    protected static final String BDMS_ORDER_ID = "bdms:OrderID";
    protected static final String BDMS_ORIGINAL_LAST_PRIMARY_CONVENIENCE_FEE_AMOUNT = "bdms:OriginalLastPrimaryConvFeeAmount";
    protected static final String BDMS_ORIGINAL_PRIMARY_CONVENIENCE_FEE_AMOUNT = "bdms:OriginalPrimaryConvFeeAmount";
    protected static final String BDMS_PRIMARY_ACCOUNT_TOKEN = "bdms:PrimaryAccountToken";
    protected static final String BDMS_RECURRING_PAYMENT_AUTHORIZATION_TYPE = "bdms:RecurringPaymentAuthorizationType";
    protected static final String BDMS_SCHEDULE_TYPE = "bdms:ScheduleType";
    protected static final String BDMS_SECONDARY_ACCOUNT_TOKEN = "bdms:SecondaryAccountToken";
    protected static final String BDMS_SIGNATURE_IMAGE = "bdms:SignatureImage";
    protected static final String BDMS_END_DAY = "bdms:EndDay";
    protected static final String BDMS_END_MONTH = "bdms:EndMonth";
    protected static final String BDMS_END_YEAR = "bdms:EndYear";
    protected static final String BDMS_FIRST_INSTANCE_DAY = "bdms:FirstInstanceDay";
    protected static final String BDMS_FIRST_INSTANCE_MONTH = "bdms:FirstInstanceMonth";
    protected static final String BDMS_FIRST_INSTANCE_YEAR = "bdms:FirstInstanceYear";
    protected static final String BDMS_SECOND_INSTANCE_DAY = "bdms:SecondInstanceDay";
    protected static final String BDMS_SECOND_INSTANCE_MONTH = "bdms:SecondInstanceMonth";
    protected static final String BDMS_SECOND_INSTANCE_YEAR = "bdms:SecondInstanceYear";
    protected static final String BDMS_PAYOR_BUSINESS_NAME = "bdms:PayorBusinessName";
    protected static final String BDMS_PAYOR_EMAIL = "bdms:PayorEmailAddress";
    protected static final String BDMS_PAYOR_FIRST_NAME = "bdms:PayorFirstName";
    protected static final String BDMS_PAYOR_LAST_NAME = "bdms:PayorLastName";
    protected static final String BDMS_PAYOR_MIDDLE_NAME = "bdms:PayorMiddleName";
    protected static final String BDMS_PAYOR_PHONE_NUMBER = "bdms:PayorPhoneNumber";
    protected static final String BDMS_PAYOR_PHONE_NUMBER_REGION_CODE = "bdms:PayorPhoneNumberRegionCode";
    protected static final String BDMS_PAYOR_ADDRESS = "bdms:PayorAddress";
    protected static final String BDMS_PAYOR_CITY = "bdms:PayorCity";
    protected static final String BDMS_PAYOR_COUNTRY = "bdms:PayorCountry";
    protected static final String BDMS_PAYOR_POSTAL_CODE = "bdms:PayorPostalCode";

    protected static final String SECOND_INSTANCE_DATE_EXCEPTION = "Second Instance Date is required for the semi-monthly schedule.";
    protected static final String PRIMARY_ACCOUNT_TOKEN_REQUIRED_EXCEPTION = "Primary token is required to perform recurring transaction.";
    protected static final String SCHEDULE_TYPE_REQUIRED_EXCEPTION = "Schedule Type is required to perform recurring transaction.";

    public CreateRecurringPaymentRequest(ElementTree et) {
        super(et);
    }

    public String build(Element envelope, Credentials credentials, Schedule schedule) {
        Element body = et.subElement(envelope, SOAPENV_BODY);
        Element methodElement = et.subElement(body, BIL_CREATE_RECURRING_PAYMENT);
        Element requestElement = et.subElement(methodElement, BIL_REQUEST);

        buildCredentials(requestElement, credentials);

        Element billElement = et.subElement(requestElement, BDMS_BILLS);
        buildBillTransactions(billElement, schedule.getBills());

        buildEndDate(requestElement, schedule.getEndDate());

        buildFirstInstanceDate(requestElement, schedule.getStartDate());

        et.subElement(requestElement, BDMS_INITIAL_PAYMENT_METHOD, schedule.getInitialPaymentMethod());
        et.subElement(requestElement, BDMS_INSTANCE_PAYMENT_AMOUNT, schedule.getAmount());
        et.subElement(requestElement, BDMS_NUMBER_OF_INSTANCE, schedule.getNumberOfPayments());
        et.subElement(requestElement, BDMS_ORDER_ID, schedule.getId());

        if (!StringUtils.isNullOrEmpty(String.valueOf(schedule.getLastPrimaryConvenienceAmount())))
            et.subElement(requestElement, BDMS_ORIGINAL_LAST_PRIMARY_CONVENIENCE_FEE_AMOUNT, schedule.getLastPrimaryConvenienceAmount());

        if (!StringUtils.isNullOrEmpty(String.valueOf(schedule.getPrimaryConvenienceAmount())))
            et.subElement(requestElement, BDMS_ORIGINAL_PRIMARY_CONVENIENCE_FEE_AMOUNT, schedule.getPrimaryConvenienceAmount());

        if (schedule.getCustomer() != null) {
            buildPayOrData(requestElement, schedule.getCustomer());
        }

        if (schedule.getToken() != null) {
            et.subElement(requestElement, BDMS_PRIMARY_ACCOUNT_TOKEN, schedule.getToken());
        } else {
            throw new UnsupportedOperationException(PRIMARY_ACCOUNT_TOKEN_REQUIRED_EXCEPTION);
        }

        et.subElement(requestElement, BDMS_RECURRING_PAYMENT_AUTHORIZATION_TYPE, schedule.getRecurringAuthorizationType());

        if (schedule.getFrequency() != null) {
            et.subElement(requestElement, BDMS_SCHEDULE_TYPE, schedule.getFrequency());
            if (schedule.getFrequency().equals(ScheduleFrequency.SemiMonthly)) {
                if (schedule.getSecondInstanceDate() != null)
                    buildSecondInstanceDate(requestElement, schedule.getSecondInstanceDate());
                else
                    throw new UnsupportedOperationException(SECOND_INSTANCE_DATE_EXCEPTION);
            }
        } else {
            throw new UnsupportedOperationException(SCHEDULE_TYPE_REQUIRED_EXCEPTION);
        }

        if (!StringUtils.isNullOrEmpty(schedule.getSecondaryToken())) {
            et.subElement(requestElement, BDMS_SECONDARY_ACCOUNT_TOKEN, schedule.getSecondaryToken());
        }

        if (!StringUtils.isNullOrEmpty(schedule.getSignatureImageInBase64())) {
            et.subElement(requestElement, BDMS_SIGNATURE_IMAGE, schedule.getSignatureImageInBase64());
        }

        return et.toString(envelope);
    }

    protected void buildBillTransactions(Element parent, List<Bill> bills) {
        for (Bill bill : bills) {
            Element billTransaction = et.subElement(parent, CreateRecurringPaymentRequest.BDMS_RECURRING_PAYMENT_BILL);
            et.subElement(billTransaction, BDMS_BILL_TYPE, bill.getBillType());
            et.subElement(billTransaction, BDMS_ID1, bill.getIdentifier1());
            et.subElement(billTransaction, BDMS_ID2, bill.getIdentifier2());
            et.subElement(billTransaction, BDMS_ID3, bill.getIdentifier3());
            et.subElement(billTransaction, BDMS_ID4, bill.getIdentifier4());
            if (bill.getCustomer() != null){
                if (bill.getCustomer().getAddress() != null) {
                    et.subElement(billTransaction, BDMS_OBLIGOR_ADDRESS, bill.getCustomer().getAddress().getStreetAddress1());
                    et.subElement(billTransaction, BDMS_OBLIGOR_CITY, bill.getCustomer().getAddress().getCity());
                    et.subElement(billTransaction, BDMS_OBLIGOR_COUNTRY, bill.getCustomer().getAddress().getCountry());
                    et.subElement(billTransaction, BDMS_OBLIGOR_POSTAL_CODE, bill.getCustomer().getAddress().getPostalCode());
                    et.subElement(billTransaction, BDMS_OBLIGOR_STATE, bill.getCustomer().getAddress().getState());
                }
                et.subElement(billTransaction,BDMS_OBLIGOR_EMAIL_ADDRESS,bill.getCustomer().getEmail());
                et.subElement(billTransaction,BDMS_OBLIGOR_FIRST_NAME,bill.getCustomer().getFirstName());
                et.subElement(billTransaction,BDMS_OBLIGOR_LAST_NAME,bill.getCustomer().getLastName());
                et.subElement(billTransaction,BDMS_OBLIGOR_MIDDLE_NAME,bill.getCustomer().getMiddleName());
                if (bill.getCustomer().getPhone() != null) {
                    et.subElement(billTransaction, BDMS_OBLIGOR_PHONE_NUMBER, bill.getCustomer().getPhone().getNumber());
                }
            }
        }
    }

    protected void buildPayOrData(Element parent, Customer customer) {
        et.subElement(parent, BDMS_PAYOR_BUSINESS_NAME, customer.getCompany());
        et.subElement(parent, BDMS_PAYOR_EMAIL, customer.getEmail());
        et.subElement(parent, BDMS_PAYOR_FIRST_NAME, customer.getFirstName());
        et.subElement(parent, BDMS_PAYOR_LAST_NAME, customer.getLastName());
        et.subElement(parent, BDMS_PAYOR_MIDDLE_NAME, customer.getMiddleName());
        if (customer.getPhone() != null) {
            et.subElement(parent, BDMS_PAYOR_PHONE_NUMBER, customer.getPhone().getNumber());
            et.subElement(parent, BDMS_PAYOR_PHONE_NUMBER_REGION_CODE, customer.getPhone().getCountryCode());
        }
        if (customer.getAddress() != null) {
            et.subElement(parent, BDMS_PAYOR_ADDRESS, customer.getAddress().getStreetAddress1());
            et.subElement(parent, BDMS_PAYOR_CITY, customer.getAddress().getCity());
            et.subElement(parent, BDMS_PAYOR_COUNTRY, customer.getAddress().getCountry());
            et.subElement(parent, BDMS_PAYOR_POSTAL_CODE, customer.getAddress().getPostalCode());
        }
    }

    protected void buildEndDate(Element parent, Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        et.subElement(parent, BDMS_END_DAY, StringUtils.padLeft(calendar.get(Calendar.DAY_OF_MONTH), 2, '0'));
        int indexedMonth = calendar.get(Calendar.MONTH);
        int endMonth = indexedMonth + 1;
        et.subElement(parent, BDMS_END_MONTH, StringUtils.padLeft(endMonth, 2, '0'));
        et.subElement(parent, BDMS_END_YEAR, calendar.get(Calendar.YEAR));

    }

    protected void buildFirstInstanceDate(Element parent, Date firstInstanceDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(firstInstanceDate);
        et.subElement(parent, BDMS_FIRST_INSTANCE_DAY, StringUtils.padLeft(calendar.get(Calendar.DAY_OF_MONTH), 2, '0'));
        int indexedMonth = calendar.get(Calendar.MONTH);
        int endMonth = indexedMonth + 1;
        et.subElement(parent, BDMS_FIRST_INSTANCE_MONTH, StringUtils.padLeft(endMonth, 2, '0'));
        et.subElement(parent, BDMS_FIRST_INSTANCE_YEAR, calendar.get(Calendar.YEAR));

    }

    protected void buildSecondInstanceDate(Element parent, Date secondInstanceDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(secondInstanceDate);
        et.subElement(parent, BDMS_SECOND_INSTANCE_DAY, StringUtils.padLeft(calendar.get(Calendar.DAY_OF_MONTH), 2, '0'));
        int indexedMonth = calendar.get(Calendar.MONTH);
        int endMonth = indexedMonth + 1;
        et.subElement(parent, BDMS_SECOND_INSTANCE_MONTH, StringUtils.padLeft(endMonth, 2, '0'));
        et.subElement(parent, BDMS_SECOND_INSTANCE_YEAR, calendar.get(Calendar.YEAR));

    }

}
