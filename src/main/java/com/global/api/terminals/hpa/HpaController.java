package com.global.api.terminals.hpa;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.hpa.builders.HpaAdminBuilder;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.hpa.interfaces.HpaTcpInterface;
import com.global.api.terminals.hpa.responses.SipBaseResponse;
import com.global.api.terminals.hpa.responses.SipDeviceResponse;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class HpaController extends DeviceController implements IDisposable {
    public MessageFormat getFormat() {
        if(settings != null) {
            return getConnectionModes() == ConnectionModes.TCP_IP ? MessageFormat.HPA : MessageFormat.Visa2nd;
        }
        return MessageFormat.Visa2nd;
    }

    public HpaController(ITerminalConfiguration settings) throws ConfigurationException {
        super(settings);
    }

    @Override
    protected void generateInterface() throws ConfigurationException {
        _device = new HpaInterface(this);
        if (Objects.requireNonNull(settings.getConnectionMode()) == ConnectionModes.TCP_IP) {
            _interface = new HpaTcpInterface(settings);
        } else {
            throw new ConfigurationException("Specified connection method not supported for HeartSIP.");
        }
    }

    private <T extends SipBaseResponse> T sendMessage(Class<T> clazz, String message, String... messageIds) throws ApiException {
        return sendMessage(clazz, message, false, true, messageIds);
    }

    private <T extends SipBaseResponse> T sendMessage(Class<T> clazz, String message, boolean keepAlive, boolean awaitResponse, String... messageIds) throws ApiException {
        IDeviceMessage deviceMessage = TerminalUtilities.buildRequest(message, getFormat());
        deviceMessage.setKeepAlive(keepAlive);
        deviceMessage.setAwaitResponse(awaitResponse);

        byte[] response = _interface.send(deviceMessage);
        if(awaitResponse) {
            try {
                Constructor<T> instance = clazz.getConstructor(byte[].class, String[].class);
                return instance.newInstance(response, messageIds);
            }
            catch(Exception e) {
                throw new ApiException("Failed to convert message to requested type.", e);
            }
        }
        else return null;
    }

    <T extends SipBaseResponse> T sendAdminMessage(Class<T> clazz, HpaAdminBuilder builder) throws ApiException {
        int requestId = 1004;
        if(requestIdProvider != null) {
            requestId = requestIdProvider.getRequestId();
        }
        builder.set("RequestId", requestId);
        return sendMessage(clazz, builder.buildMessage(), builder.isKeepAlive(), builder.isAwaitResponse(), builder.getMessageIds());
    }

    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String transactionType = mapTransactionType(builder.getTransactionType());
        Integer requestId = builder.getRequestId();
        if(requestId == null && requestIdProvider != null) {
            requestId = requestIdProvider.getRequestId();
        }

        Element request = et.element("SIP");
        et.subElement(request, "Version").text("1.0");
        et.subElement(request, "ECRId").text("1004");
        et.subElement(request, "Request").text(transactionType);
        et.subElement(request, "RequestId", requestId);
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
        Integer requestId = builder.getRequestId();
        if(requestId == null && requestIdProvider != null) {
            requestId = requestIdProvider.getRequestId();
        }

        Element request = et.element("SIP");
        et.subElement(request, "Version").text("1.0");
        et.subElement(request, "ECRId").text("1004");
        et.subElement(request, "Request").text(mapTransactionType(builder.getTransactionType()));
        et.subElement(request, "TransactionId", builder.getTransactionId());
        et.subElement(request, "RequestId" , requestId);
        
        if (builder.getGratuity() != null)
            et.subElement(request, "TipAmount").text(StringUtils.toNumeric(builder.getGratuity()));

        return sendMessage(SipDeviceResponse.class, et.toString(request), transactionType);
    }

    @Override
    public ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException("This transaction is not currently supported for this payment type.");
    }

    private String mapTransactionType(TransactionType type) throws UnsupportedTransactionException {
        switch (type) {
            case Sale:
                return HpaMsgId.CREDIT_SALE.getValue();
            case Verify:
                return HpaMsgId.CARD_VERIFY.getValue();
            case Refund:
                return HpaMsgId.CREDIT_REFUND.getValue();
            case Void:
                return HpaMsgId.CREDIT_VOID.getValue();
            case Balance:
                return HpaMsgId.BALANCE.getValue();
            case AddValue:
                return HpaMsgId.ADD_VALUE.getValue();
            case Auth:
                return HpaMsgId.CREDIT_AUTH.getValue();
            case Edit:
                return HpaMsgId.TIP_ADJUST.getValue();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    public void dispose() {
        _device.dispose();
        _interface.disconnect();
    }
}
