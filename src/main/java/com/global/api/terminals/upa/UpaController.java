package com.global.api.terminals.upa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.*;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.abstractions.IUPAMessage;
import com.global.api.terminals.enums.TerminalReportType;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.terminals.upa.interfaces.UpaMicInterface;
import com.global.api.terminals.upa.interfaces.UpaTcpInterface;
import com.global.api.terminals.upa.responses.BatchReportResponse;
import com.global.api.terminals.upa.responses.UpaTransactionResponse;
import com.global.api.terminals.upa.subgroups.RequestLodgingFields;
import com.global.api.terminals.upa.subgroups.RequestParamFields;
import com.global.api.terminals.upa.subgroups.RequestProcessingIndicatorsFields;
import com.global.api.terminals.upa.subgroups.RequestTransactionFields;
import com.global.api.utils.JsonDoc;

import java.nio.charset.StandardCharsets;

public class UpaController extends DeviceController {
    private IDeviceInterface _device;
    private IUPAMessage _upaInterface;
    private IMessageSentInterface onMessageSent;
    private IMessageSentInterface onMessageReceived;

    private IDeviceMessage paramMsg;

    void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    void setOnMessageReceivedHandler(IMessageSentInterface onMessageReceived){
        this.onMessageReceived = onMessageReceived;
    }
    public UpaController(ConnectionConfig settings) throws ConfigurationException {
        super(settings);

        if(_device == null) {
            _device = new UpaInterface(this);
        }

        this.requestIdProvider = settings.getRequestIdProvider();

        if (settings.getConnectionMode() == ConnectionModes.TCP_IP) {
            _interface = new UpaTcpInterface(settings);
        } else if (settings.getConnectionMode() == ConnectionModes.MEET_IN_THE_CLOUD) {
            _interface = new UpaMicInterface(settings);
        } else {
            throw new ConfigurationException("Unsupported connection mode.");
        }
        _upaInterface = (IUPAMessage) _interface;

        _interface.setMessageSentHandler(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });

        _upaInterface.setMessageReceivedHandler(new IMessageReceivedInterface() {
            @Override
            public void messageReceived(byte[] message) {
                if(onMessageReceived != null){
                    onMessageReceived.messageSent(new String(message, StandardCharsets.UTF_8));
                }
            }
        });
    }

    public Integer getRequestId() {
        return this.requestIdProvider.getRequestId();
    }

    @Override
    public IDeviceInterface configureInterface() {
        if(_device == null)
            _device = new UpaInterface(this);
        return _device;
    }

    @Override
    public byte[] send(IDeviceMessage message) throws ApiException {
        return _interface.send(message);
    }

    private UpaTransactionResponse doTransaction(
        UpaMessageId messageId,
        Integer requestId,
        RequestParamFields paramFields,
        RequestTransactionFields transactionFields,
        RequestProcessingIndicatorsFields processingIndicators,
        RequestLodgingFields lodgingFields
    ) throws ApiException {
        JsonDoc body = new JsonDoc();

        if (paramFields.getElementsJson() != null) {
            body.set("params", paramFields.getElementsJson());
        }

        if (transactionFields.getElementsJson() != null) {
            body.set("transaction", transactionFields.getElementsJson());
        }

        if (processingIndicators != null && messageId == UpaMessageId.StartCardTransaction) {
            body.set("processingIndicators", processingIndicators.getElementsJson());
        }

        if (lodgingFields != null && lodgingFields.getElementsJson() != null){
            body.set("lodging",lodgingFields.getElementsJson());
        }

        String requestIdAsString = requestId != null ? requestId.toString() : getRequestId().toString();

        JsonDoc jsonDoc = prepareForBuildMessage(messageId, requestIdAsString, body);

        DeviceMessage message = TerminalUtilities.buildMessage(jsonDoc);

        byte[] resp;
      
        try {
            resp = send(message);
        } catch (ApiException e) {
            if (e.getMessage().equals("Terminal did not respond in the given timeout.")) {
                _device.cancel();                
            }
            throw new ApiException(e.getMessage());
        }

        JsonDoc responseObj = JsonDoc.parse(
                new String(resp, StandardCharsets.UTF_8)
        );

        return new UpaTransactionResponse(responseObj);
    }

    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        UpaMessageId messageId = mapTransactionType(builder.getTransactionType());

        Integer requestId = builder.getRequestId();

        RequestParamFields requestParamFields = new RequestParamFields();
        requestParamFields.setParams(builder);

        RequestTransactionFields requestTransactionFields = new RequestTransactionFields();
        requestTransactionFields.setParams(builder);

        RequestProcessingIndicatorsFields processingIndicators = new RequestProcessingIndicatorsFields();
        processingIndicators.setParams(builder);

        RequestLodgingFields requestLodgingFields = new RequestLodgingFields();
        requestLodgingFields.setParams(builder);

        return doTransaction(messageId, requestId, requestParamFields, requestTransactionFields, processingIndicators,requestLodgingFields);
    }

    private JsonDoc prepareForBuildMessage(UpaMessageId messageType, String requestId, JsonDoc body) {
        JsonDoc data = new JsonDoc();
        JsonDoc json = new JsonDoc();

        data.set("EcrId", "13");
        data.set("requestId", requestId);
        data.set("command", messageType.toString());

        if (body != null) {
            data.set("data", body);
        }

        json.set("data", data);
        json.set("message", "MSG");

        return json;
    }

    private UpaMessageId mapTransactionType(TransactionType type) throws UnsupportedTransactionException {
        switch (type) {
            case Auth:
                return UpaMessageId.PreAuth;
            case Sale:
                return UpaMessageId.Sale;
            case Void:
                return UpaMessageId.Void;
            case Refund:
                return UpaMessageId.Refund;
            case Edit:
                return UpaMessageId.TipAdjust;
            case Verify:
                return UpaMessageId.CardVerify;
            case Reversal:
                return UpaMessageId.Reversal;
            case Balance:
                return UpaMessageId.BalanceInquiry;
            case Capture:
                return UpaMessageId.AuthCompletion;
            case Activate:
                return UpaMessageId.StartCardTransaction;
            case DeleteOpenTab:
                return UpaMessageId.DeletePreAuth;
            default:
                throw new UnsupportedTransactionException("Selected gateway does not support this transaction type");            
        }
    }

    @Override
    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        UpaMessageId messageId = mapTransactionType(builder.getTransactionType());

        Integer requestId = builder.getRequestId();

        RequestParamFields requestParamFields = new RequestParamFields();
        requestParamFields.setParams(builder);

        RequestTransactionFields requestTransactionFields = new RequestTransactionFields();
        requestTransactionFields.setParams(builder);

        return doTransaction(messageId, requestId, requestParamFields, requestTransactionFields, null,null);
    }

    @Override
    public ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException {
        IDeviceMessage message = buildReportParams(builder);

        byte[] resp;

        try {
            resp = send(message);
        } catch (ApiException e) {
            if (e.getMessage().equals("Terminal did not respond in the given timeout.")) {
                _device.cancel();
            }
            throw new ApiException(e.getMessage());
        }

        JsonDoc responseObj = JsonDoc.parse(
                new String(resp, StandardCharsets.UTF_8)
        );
        switch (builder.getTerminalReportType()) {
            case GetBatchReport:
                return new BatchReportResponse(responseObj);
            default:
                throw new GatewayException("Unknown report type!");
        }
    }

    private IDeviceMessage buildReportParams(TerminalReportBuilder builder) throws UnsupportedTransactionException {
        String requestId = builder.getTerminalSearchBuilder().getReferenceNumber();
        if (requestId == null) {
            requestId = getRequestId().toString();
        }
        JsonDoc doc = new JsonDoc();
        doc.set("message", "MSG");

        JsonDoc baseRequest = doc.subElement("data");
        baseRequest.set("command", MapReportType(builder.getTerminalReportType()));
        baseRequest.set("EcrId", _device.getEcrId());
        baseRequest.set("requestId", requestId);
        JsonDoc dataParams = new JsonDoc();
        switch (builder.getTerminalReportType()) {
            case GetBatchReport:
                if (builder.getTerminalSearchBuilder().getBatch() > 0) {
                    dataParams.set("batch", builder.getTerminalSearchBuilder().getBatch());
                }
                break;
            default:
                break;
        }


        if (!dataParams.getKeys().isEmpty()) {
            baseRequest.set("params", dataParams);
        }

        return TerminalUtilities.buildMessage(doc);
    }

    private String MapReportType(TerminalReportType type) throws UnsupportedTransactionException {
        switch (type) {
            case GetBatchReport:
                return UpaMessageId.GetBatchReport.toString();
            default:
                throw new UnsupportedTransactionException("Unsupported report type");
        }
    }
}
