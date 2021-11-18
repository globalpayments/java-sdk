package com.global.api.terminals.upa;

import java.nio.charset.StandardCharsets;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.terminals.upa.interfaces.UpaTcpInterface;
import com.global.api.terminals.upa.responses.UpaTransactionResponse;
import com.global.api.terminals.upa.subgroups.RequestParamFields;
import com.global.api.terminals.upa.subgroups.RequestTransactionFields;
import com.global.api.utils.JsonDoc;

public class UpaController extends DeviceController {
    private IDeviceInterface _device;
    private byte[] resp;

    public UpaController(ITerminalConfiguration settings) throws ConfigurationException {
        super(settings);

        if(_device == null) {
            _device = new UpaInterface(this);
        }

        this.requestIdProvider = settings.getRequestIdProvider();

        if (settings.getConnectionMode() == ConnectionModes.TCP_IP) {
            _interface = new UpaTcpInterface(settings);
        } else {
            throw new ConfigurationException("Unsupported connection mode.");
        }
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
        RequestParamFields paramFields,
        RequestTransactionFields transactionFields
    ) throws ApiException {
        JsonDoc body = new JsonDoc();

        if (paramFields.getElementsJson() != null) {
            body.set("params", paramFields.getElementsJson());
        }

        if (transactionFields.getElementsJson() != null) {
            body.set("transaction", transactionFields.getElementsJson());
        }

        DeviceMessage message = TerminalUtilities.buildMessage(
            messageId,
            getRequestId().toString(),
            body
        );

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

        return new UpaTransactionResponse(responseObj.get("data").get("data"));
    }

    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        UpaMessageId messageId = mapTransactionType(builder.getTransactionType());      

        RequestParamFields requestParamFields = new RequestParamFields();
        requestParamFields.setParams(builder);

        RequestTransactionFields requestTransactionFields = new RequestTransactionFields();
        requestTransactionFields.setParams(builder);

        return doTransaction(messageId, requestParamFields, requestTransactionFields);
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
            default:
                throw new UnsupportedTransactionException("Selected gateway does not support this transaction type");            
        }
    }

    @Override
    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        UpaMessageId messageId = mapTransactionType(builder.getTransactionType());     

        RequestParamFields requestParamFields = new RequestParamFields();
        requestParamFields.setParams(builder);

        RequestTransactionFields requestTransactionFields = new RequestTransactionFields();
        requestTransactionFields.setParams(builder);

        return doTransaction(messageId, requestParamFields, requestTransactionFields);
    }
}
