package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.NonNull;

public interface INtsRequestMessage {
    Integer MESSAGE_TYPE = 9;
    Integer COMPANY_ID = 45; // Default company ID for P66

    static MessageWriter prepareHeader(@NonNull NtsObjectParam params) {
        TransactionBuilder builder = params.getNtsBuilder();
        MessageWriter headerRequest = new MessageWriter();
        NTSCardTypes cardType = params.getNtsCardType();

        NtsRequestMessageHeader ntsRequestMessageHeader = builder.getNtsRequestMessageHeader();
        String strSpace = "";

        NtsUtils.log("--------------------- REQUEST HEADER ---------------------");
        // Message Type
        NtsUtils.log("message type", String.valueOf(MESSAGE_TYPE));
        headerRequest.addRange(MESSAGE_TYPE, 1);
        // Company Number
        String companyId = params.getCompanyId() != null ? params.getCompanyId() : String.valueOf(COMPANY_ID);
        NtsUtils.log("company number", String.valueOf(companyId));
        headerRequest.addRange(companyId, 3);
        // Binary TerminalId
        NtsUtils.log("binary terminal id", params.getBinTerminalId());
        headerRequest.addRange(String.format("%1s", params.getBinTerminalId()), 1);

        // Binary Terminal Type
        NtsUtils.log("binary terminal type", params.getBinTerminalType());
        headerRequest.addRange(String.format("%1s", params.getBinTerminalType()), 1);
        // Host Response Code
        NtsUtils.log("Host Response Code", String.format("%2s", strSpace));
        headerRequest.addRange(String.format("%2s", strSpace), 2);
        // Timeout Value
        if (cardType != null) {
            NtsUtils.log("Timeout Value", cardType.getTimeOut().toString());
            headerRequest.addRange(cardType.getTimeOut(), 3);
        } else {
            NtsUtils.log("Timeout Value", String.valueOf(15));
            headerRequest.addRange(String.valueOf(15), 3);
        }

        // Filler
        NtsUtils.log("Filler", String.format("%1s", strSpace));
        headerRequest.addRange(String.format("%1s", strSpace), 1);
        //Input Capability Code
        NtsUtils.log("Input Capability Code", params.getInputCapabilityCode());
        headerRequest.addRange(String.valueOf(params.getInputCapabilityCode().getValue()), 1);
        // Filler
        NtsUtils.log("Filler", String.format("%1s", strSpace));
        headerRequest.addRange(String.format("%1s", strSpace), 1);
        // Terminal Destination TagPurchase_CashBack
        NtsUtils.log("Terminal Destination Tag", ntsRequestMessageHeader.getTerminalDestinationTag());
        headerRequest.addRange(ntsRequestMessageHeader.getTerminalDestinationTag(), 3);
        // Software Version
        NtsUtils.log("Software Version", params.getSoftwareVersion());
        headerRequest.addRange(params.getSoftwareVersion(), 2);
        // Pin Indicator
        NtsUtils.log("Pin Indicator", ntsRequestMessageHeader.getPinIndicator());
        headerRequest.addRange(ntsRequestMessageHeader.getPinIndicator().getValue(), 1);
        // Logic Process Flag or Store_And_Forward_Indicator
        NtsUtils.log("Logic Process Flag or Store_And_Forward_Indicator", params.getLogicProcessFlag());
        headerRequest.addRange(params.getLogicProcessFlag().getValue(), 1);
        // Message Code
        NtsUtils.log("Message Code", ntsRequestMessageHeader.getNtsMessageCode());
        headerRequest.addRange(ntsRequestMessageHeader.getNtsMessageCode().getValue(), 2);
        // Terminal Type
        NtsUtils.log("Terminal Type", params.getTerminalType());
        headerRequest.addRange(params.getTerminalType().getValue(), 2);
        // Unit Number
        NtsUtils.log("Unit Number", String.valueOf(params.getUnitNumber()));
        headerRequest.addRange(params.getUnitNumber(), 11);
        // Terminal Id
        NtsUtils.log("Terminal Id", String.valueOf(params.getTerminalId()));
        headerRequest.addRange(params.getTerminalId(), 2);


        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionReference transactionReference = null;
        if (paymentMethod instanceof TransactionReference) {
            transactionReference = (TransactionReference) paymentMethod;
        }
        if (builder instanceof AuthorizationBuilder) {
            // Transaction Date
            NtsUtils.log("Transaction Date", String.valueOf(ntsRequestMessageHeader.getTransactionDate()));
            headerRequest.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);

            // Transaction Time
            NtsUtils.log("Transaction Time", String.valueOf(ntsRequestMessageHeader.getTransactionTime()));
            headerRequest.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
        } else if (builder instanceof ManagementBuilder) {
            ManagementBuilder manageBuilder = (ManagementBuilder) builder;
            if (transactionReference != null &&
                    (manageBuilder.getTransactionType() == TransactionType.Reversal
                    || manageBuilder.getTransactionType() == TransactionType.Refund
                    || manageBuilder.getTransactionType() == TransactionType.Void
                    || manageBuilder.getTransactionType() == TransactionType.PreAuthCompletion)
                    ) {
                // Transaction Date
                NtsUtils.log("Transaction Date", transactionReference.getOriginalTransactionDate());
                headerRequest.addRange(transactionReference.getOriginalTransactionDate(), 4);

                // Transaction Time
                NtsUtils.log("Transaction Time", transactionReference.getOriginalTransactionTime());
                headerRequest.addRange(transactionReference.getOriginalTransactionTime(), 6);
            } else if (manageBuilder.getTransactionType() == TransactionType.BatchClose
                        ||manageBuilder.getTransactionType() == TransactionType.Capture
                        || manageBuilder.getTransactionType() == TransactionType.DataCollect) {
                // Transaction Date
                NtsUtils.log("Transaction Date", String.valueOf(ntsRequestMessageHeader.getTransactionDate()));
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);

                // Transaction Time
                NtsUtils.log("Transaction Time", String.valueOf(ntsRequestMessageHeader.getTransactionTime()));
                headerRequest.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
            }
        }


        // Prior Message Response Time
        NtsUtils.log("Prior Message Response Time", String.valueOf(ntsRequestMessageHeader.getPriorMessageResponseTime()));
        headerRequest.addRange(StringUtils.padLeft(ntsRequestMessageHeader.getPriorMessageResponseTime(), 3, '0'), 3);
        // Prior Message Connect Time
        NtsUtils.log("Prior Message Connect Time", String.valueOf(ntsRequestMessageHeader.getPriorMessageConnectTime()));
        headerRequest.addRange(ntsRequestMessageHeader.getPriorMessageConnectTime(), 3);
        // Prior Message Code
        NtsUtils.log("Prior Message Code", String.valueOf(ntsRequestMessageHeader.getPriorMessageCode()));
        headerRequest.addRange(ntsRequestMessageHeader.getPriorMessageCode(), 2);

        NtsUtils.log("Request header :", headerRequest.getMessageRequest().toString());

        return headerRequest;
    }

    MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException;

}
