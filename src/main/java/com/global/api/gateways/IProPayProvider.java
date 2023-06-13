package com.global.api.gateways;

import com.global.api.builders.ProPayBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;

public interface IProPayProvider {
    Transaction processProPay(ProPayBuilder builder) throws ApiException;
}
