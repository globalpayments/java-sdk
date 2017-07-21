package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.InquiryType;

public interface IBalanceable {
    AuthorizationBuilder balanceInquiry();
    AuthorizationBuilder balanceInquiry(InquiryType inquiry);
}
