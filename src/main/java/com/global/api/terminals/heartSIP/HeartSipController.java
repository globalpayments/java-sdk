package com.global.api.terminals.heartSIP;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDisposable;
import com.global.api.terminals.heartSIP.responses.SipBaseResponse;
import com.global.api.terminals.heartSIP.responses.SipDeviceResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.heartSIP.interfaces.HeartSipTcpInterface;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

import java.lang.reflect.Constructor;

public class HeartSipController extends DeviceController implements IDisposable {
    IDeviceInterface _device;

    private IMessageSentInterface onMessageSent;
    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public MessageFormat getFormat() {
        if(settings != null)
            return getConnectionModes() == ConnectionModes.TCP_IP ? MessageFormat.HeartSIP : MessageFormat.Visa2nd;
        return MessageFormat.Visa2nd;
    }

    public IDeviceInterface configureInterface() throws ConfigurationException {
        if(_device == null)
            _device = new HeartSipInterface(this);
        return _device;
    }

    public HeartSipController(ITerminalConfiguration settings) throws ConfigurationException {
        super(settings);
        switch(settings.getConnectionMode()) {
            case TCP_IP:
                _interface = new HeartSipTcpInterface(settings);
                break;
            default:
                throw new ConfigurationException("Specified connection method not supported for HeartSIP.");
        }

        _interface.setMessageSentHandler(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    public <T extends SipBaseResponse> T sendMessage(Class<T> clazz, String message, String... messageIds) throws ApiException {
        byte[] response = _interface.send(TerminalUtilities.buildRequest(message, getFormat()));
        try {
            Constructor<T> instance = clazz.getConstructor(byte[].class, String[].class);
            return instance.newInstance(response, messageIds);
        }
        catch(Exception e) {
            throw new ApiException("Failed to convert message to requested type.", e);
        }
    }

    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String transactionType = mapTransactionType(builder.getTransactionType());

        Element request = et.element("SIP");
        et.subElement(request, "Version").text("1.0");
        et.subElement(request, "ECRId").text("1004");
        et.subElement(request, "Request").text(transactionType);
        et.subElement(request, "RequestId", builder.getReferenceNumber());
        et.subElement(request, "CardGroup", builder.getPaymentMethodType());
        et.subElement(request, "ConfirmAmount").text("0");
        et.subElement(request, "BaseAmount").text(StringUtils.toNumeric(builder.getAmount()));
        if (builder.getGratuity() != null)
            et.subElement(request, "TipAmount").text(StringUtils.toNumeric(builder.getGratuity()));
        else et.subElement(request, "TipAmount").text("0");

        // EBT amount
        if(builder.getPaymentMethodType() == PaymentMethodType.EBT)
            et.subElement(request, "EBTAmount").text(StringUtils.toNumeric(builder.getAmount()));

        // total
        et.subElement(request, "TotalAmount").text(StringUtils.toNumeric(builder.getAmount()));

        return sendMessage(SipDeviceResponse.class, et.toString(request), transactionType);
    }

    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String transactionType = mapTransactionType(builder.getTransactionType());

        Element request = et.element("SIP");
        et.subElement(request, "Version").text("1.0");
        et.subElement(request, "ECRId").text("1004");
        et.subElement(request, "Request").text(mapTransactionType(builder.getTransactionType()));
        et.subElement(request, "TransactionId", builder.getTransactionId());

        if (builder.getGratuity() != null)
            et.subElement(request, "TipAmount").text(StringUtils.toNumeric(builder.getGratuity()));

        return sendMessage(SipDeviceResponse.class, et.toString(request), transactionType);
    }

    private String mapTransactionType(TransactionType type) throws UnsupportedTransactionException {
        switch (type) {
            case Sale:
                return HsipMsgId.CREDIT_SALE.getValue();
            case Verify:
                return HsipMsgId.CARD_VERIFY.getValue();
            case Refund:
                return HsipMsgId.CREDIT_REFUND.getValue();
            case Void:
                return HsipMsgId.CREDIT_VOID.getValue();
            case Balance:
                return HsipMsgId.BALANCE.getValue();
            case AddValue:
                return HsipMsgId.ADD_VALUE.getValue();
            case Auth:
                return HsipMsgId.CREDIT_AUTH.getValue();
            case Edit:
                return HsipMsgId.TIP_ADJUST.getValue();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    public void dispose() {
        _device.dispose();
        _interface.disconnect();
    }
}
