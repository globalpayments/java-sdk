package com.global.api.network.entities.nts;

import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;

public class NtsRequestsToBalanceRequest implements INtsRequestMessage {

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        String userData = ntsObjectParam.getNtsUserData();

        if (builder instanceof ManagementBuilder) {
            ManagementBuilder manageBuilder = (ManagementBuilder) builder;
            NtsUtils.log("BATCH NUMBER", manageBuilder.getBatchNumber());
            request.addRange(manageBuilder.getBatchNumber(), 2);

            NtsUtils.log("TRANSACTION COUNT", manageBuilder.getTransactionCount());
            request.addRange(manageBuilder.getTransactionCount(), 3);

            NtsUtils.log("TOTAL SALES", StringUtils.toNumeric(manageBuilder.getTotalSales()));
            request.addRange(StringUtils.toNumeric(manageBuilder.getTotalSales()), 9);

            NtsUtils.log("TOTAL RETURNS", StringUtils.toNumeric(manageBuilder.getTotalReturns()));
            request.addRange(StringUtils.toNumeric(manageBuilder.getTotalReturns()), 9);

            if (!StringUtils.isNullOrEmpty(userData) && userData.length() > 0) {
                NtsUtils.log("USER DATA LENGTH", StringUtils.padLeft(userData.length(), 3, ' '));
                request.addRange(userData.length(), 3);

                NtsUtils.log("USER DATA", userData);
                request.addRange(StringUtils.padRight(userData, userData.length(), ' '), userData.length());
            } else {
                NtsUtils.log("USER DATA LENGTH", "000");
                request.addRange(0, 3);
            }
        }
        return request;
    }
}
