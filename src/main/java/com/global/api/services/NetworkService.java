package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.network.NetworkMessageHeader;

public class NetworkService {
    public static NetworkMessageHeader sendKeepAlive() throws ApiException {
        return sendKeepAlive("default");
    }
    
    public static NetworkMessageHeader sendKeepAlive(String configName) throws ApiException {
    	IPaymentGateway gateway = ServicesContainer.getInstance().getGateway(configName);
        return gateway.sendKeepAlive();
        
    }

}
