package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.entities.NtsPDLData;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;

public class NtsPDLRequest implements INtsRequestMessage {


    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        TransactionType transactionType = ntsObjectParam.getNtsBuilder().getTransactionType();
        if (builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authorizationBuilder = (AuthorizationBuilder) builder;
            NtsPDLData pdlData = authorizationBuilder.getNtsPDLData();

            // PARAMETER TYPE
            request.addRange(pdlData.getParameterType().getValue(), 2);
            NtsUtils.log("PARAMETER TYPE", pdlData.getParameterType().getValue());

            //  TABLE-ID
            request.addRange(pdlData.getTableId().getValue(), 2);
            NtsUtils.log("TABLE-ID", pdlData.getTableId().getValue());

            if (transactionType.equals(TransactionType.EmvPdl)) {
                //  EMV PDL CARD TYPE
                request.addRange(pdlData.getEmvPDLCardType().getValue(), 2);
                NtsUtils.log("EMV PDL CARD TYPE", pdlData.getEmvPDLCardType().getValue());

                // EMV PDL PARAMETER VERSION or EMV PDL TABLE VERSION
                request.addRange(pdlData.getParameterVersion(), 3);
                NtsUtils.log("PARAMETER VERSION or TABLE VERSION", pdlData.getParameterVersion());

                if (pdlData.getEmvPdlConfigurationName() != null) {
                    // EMV PDL CONFIGURATION NAME
                    request.addRange(pdlData.getEmvPdlConfigurationName(), 40);
                    NtsUtils.log("EMV PDL CONFIGURATION NAME", pdlData.getEmvPdlConfigurationName());
                }
            } else {
                // PARAMETER VERSION or TABLE VERSION
                request.addRange(pdlData.getParameterVersion(), 3);
                NtsUtils.log("PARAMETER VERSION or TABLE VERSION", pdlData.getParameterVersion());
            }
            // BLOCK SEQUENCE NUMBER
            request.addRange(pdlData.getBlockSequenceNumber(), 2);
            NtsUtils.log("BLOCK SEQUENCE NUMBER", pdlData.getBlockSequenceNumber());
        }
        return request;
    }
}
