package com.global.api.gateways;

import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiRequest;

public interface IAccessTokenProvider {
    GpApiRequest signIn(String appId, String appKey, int secondsToExpire, IntervalToExpire intervalToExpire, String[] permissions) throws GatewayException;
    GpApiRequest signOut() throws GatewayException;
}
