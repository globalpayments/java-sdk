package com.global.api.terminals.diamond;

import com.global.api.builders.validations.RequestBuilderValidations;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.diamond.entities.DiamondCloudRequest;
import com.global.api.terminals.diamond.interfaces.DiamondHttpInterface;
import com.global.api.terminals.diamond.interfaces.DiamondInterface;
import com.global.api.terminals.diamond.responses.DiamondCloudResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiamondController extends DeviceController {

    private static final Map<String, String> ENDPOINT_EXCEPTIONS = new HashMap<>();

    static {
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.CAPTURE_EU, Region.EU.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.CANCEL_AUTH, Region.EU.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.INCREASE_AUTH, Region.EU.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.RECONCILIATION, Region.EU.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.CAPTURE, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.EBT_FOOD, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.EBT_BALANCE, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.EBT_RETURN, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.GIFT_RELOAD, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.GIFT_BALANCE, Region.US.toString());
        ENDPOINT_EXCEPTIONS.put(DiamondCloudRequest.GIFT_REDEEM, Region.US.toString());
    }

    private IDeviceInterface device;
    private IMessageSentInterface onMessageSent;
    private final DiamondCloudConfig _settings;

    public DiamondController(DiamondCloudConfig settings) throws ConfigurationException {
        super(settings);
        _settings = settings;

        if (device == null) {
            device = new DiamondInterface(this);
        }

        this.requestIdProvider = settings.getRequestIdProvider();
        this.logManagementProvider = settings.getLogManagementProvider();

        if (settings.getConnectionMode() == ConnectionModes.DIAMOND_CLOUD) {
            _interface = new DiamondHttpInterface(settings);
        } else {
            throw new ConfigurationException("Unsupported connection mode.");
        }

        _interface.setMessageSentHandler(new IMessageSentInterface() {
            public void messageSent(String message) {
                if (onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    @Override
    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        IDeviceMessage request = buildProcessTransaction(builder);
        return doTransaction(request);
    }

    @Override
    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        IDeviceMessage request = buildManageTransaction(builder);
        return doTransaction(request);
    }

    @Override
    public ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException {

        new RequestBuilderValidations(builder.setupValidationsReport())
                .Validate(builder, builder.getTerminalReportType());

        JsonDoc request = new JsonDoc();
        switch (builder.getTerminalReportType()) {
            case LocalDetailReport:
                request.set("endpoint", "/" + _settings.getPosID() + "/details/" + builder.getTerminalSearchBuilder().getReferenceNumber());

                JsonDoc param = new JsonDoc();
                param.set("POS_ID", _settings.getPosID())
                        .set("cloud_id", builder.getTerminalSearchBuilder().getReferenceNumber());

                request.set("queryParams", param);

                request.set("verb", GpApiRequest.HttpMethod.Get.getValue());

                break;
            default:
                throw new GatewayException("Report type not defined!");
        }

        String requestAsString = request.toString();
        byte[] payload = requestAsString.getBytes(StandardCharsets.UTF_8);

        DeviceMessage requestToDo = new DeviceMessage(payload);

        return doTransaction(requestToDo);
    }

    public IDeviceInterface configureInterface() {
        if (device == null)
            device = new DiamondInterface(this);
        return device;
    }

    private DiamondCloudResponse doTransaction(IDeviceMessage request) throws ApiException {
        byte[] response = _interface.send(request);

        if (response == null) {
            return null;
        }

        String jsonObject = new String(response, StandardCharsets.UTF_8);

        return new DiamondCloudResponse(jsonObject);
    }

    private IDeviceMessage buildProcessTransaction(TerminalAuthBuilder builder) throws GatewayException {
        JsonDoc body = new JsonDoc();
        JsonDoc request = new JsonDoc();
        JsonDoc param = new JsonDoc();
        String endpoint = "";
        param.set("POS_ID", _settings.getPosID());
        switch (builder.getTransactionType()) {
            case Sale:
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                switch (builder.getPaymentMethodType()) {
                    case EBT:
                        endpoint = DiamondCloudRequest.EBT_FOOD;
                        body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                        break;

                    case Gift:
                        endpoint = DiamondCloudRequest.GIFT_REDEEM;
                        body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                        break;

                    default:
                        endpoint = DiamondCloudRequest.SALE;
                        body.set("amount", StringUtils.toNumeric(builder.getAmount()))
                                .set("tip_amount", StringUtils.toNumeric(builder.getGratuity()))
                                .set("cash_back", StringUtils.toNumeric(builder.getCashBackAmount()));
                        break;
                }
                break;
            case Refund:
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                if (builder.getPaymentMethodType() == PaymentMethodType.EBT) {
                    endpoint = DiamondCloudRequest.EBT_RETURN;
                    body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                } else {
                    endpoint = DiamondCloudRequest.SALE_RETURN;
                    body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                }
                break;
            case Auth:
                endpoint = DiamondCloudRequest.AUTH;
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                break;
            case Balance:
                if (_settings.getRegion() != Region.EU.toString()) {
                    request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                    if (builder.getPaymentMethodType() == PaymentMethodType.EBT) {
                        endpoint = DiamondCloudRequest.EBT_BALANCE;
                    }
                    if (builder.getPaymentMethodType() == PaymentMethodType.Gift) {
                        endpoint = DiamondCloudRequest.GIFT_BALANCE;
                    }
                } else {
                    throw new GatewayException("Transaction type " + TransactionType.Balance + " for payment type not supported " +
                            "in " + _settings.getRegion());
                }
                break;
            case BatchClose:
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                endpoint = DiamondCloudRequest.RECONCILIATION;
                break;
            case AddValue:
                if (builder.getPaymentMethodType() == PaymentMethodType.Gift) {
                    request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                    endpoint = DiamondCloudRequest.GIFT_RELOAD;
                    body.set("amount", StringUtils.toNumeric(builder.getAmount()));
                }
                break;
            default:
                throw new GatewayException("Transaction type " + builder.getTransactionType() + " not supported!");
        }

        if (!validateEndpoint(endpoint)) {
            throw new GatewayException("This feature is not supported in " + _settings.getRegion() + " region!");
        }

        request.set("endpoint", "/" + _settings.getPosID() + endpoint)
                .set("body", body)
                .set("queryParams", param);

        byte[] payload = request.toString().getBytes(StandardCharsets.UTF_8);

        return new DeviceMessage(payload);
    }

    private IDeviceMessage buildManageTransaction(TerminalManageBuilder builder) throws GatewayException {
        JsonDoc body = new JsonDoc();
        JsonDoc request = new JsonDoc();
        JsonDoc param = new JsonDoc();
        String endpoint = "";
        param.set("POS_ID", _settings.getPosID());
        switch (builder.getTransactionType()) {
            case Void:
                endpoint = DiamondCloudRequest.VOID;
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                body.set("transaction_id", builder.getTransactionId());
                break;
            case Edit:
                endpoint = DiamondCloudRequest.TIP_ADJUST;
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                body.set("tip_amount", StringUtils.toNumeric(builder.getGratuity()))
                        .set("amount", StringUtils.toNumeric(builder.getAmount()))
                        .set("transaction_id", builder.getTransactionId());
                break;
            case Capture:
                endpoint = DiamondCloudRequest.CAPTURE;
                if (_settings.getRegion() == Region.EU.toString()) {
                    endpoint = DiamondCloudRequest.CAPTURE_EU;
                }
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                body.set("tip_amount", StringUtils.toNumeric(builder.getGratuity()))
                        .set("amount", StringUtils.toNumeric(builder.getAmount()))
                        .set("transaction_id", builder.getTransactionId());
                break;
            case Delete:
                if (builder.getTransactionModifier() == TransactionModifier.DeletePreAuth) {
                    endpoint = DiamondCloudRequest.CANCEL_AUTH;
                    request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                    body.set("transaction_id", builder.getTransactionId());
                }
                break;
            case Auth:
                if (builder.getTransactionModifier() == TransactionModifier.Incremental) {
                    endpoint = DiamondCloudRequest.INCREASE_AUTH;
                    request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                    body.set("amount", StringUtils.toNumeric(builder.getAmount()))
                            .set("transaction_id", builder.getTransactionId());
                }
                break;
            case Refund:
                endpoint = DiamondCloudRequest.SALE_RETURN;
                request.set("verb", GpApiRequest.HttpMethod.Post.getValue());
                body.set("transaction_id", builder.getTransactionId());
                break;
            default:
                throw new GatewayException("Transaction type " + builder.getTransactionType() + " not supported!");

        }

        if (!validateEndpoint(endpoint)) {
            throw new GatewayException("This feature is not supported in " + _settings.getRegion() + " region!");
        }
        request.set("endpoint", "/" + _settings.getPosID() + endpoint)
                .set("body", body)
                .set("queryParams", param);

        byte[] payload = request.toString().getBytes(StandardCharsets.UTF_8);

        return new DeviceMessage(payload);
    }

    private boolean validateEndpoint(String endpoint) throws GatewayException {
        if (StringUtils.isNullOrEmpty(endpoint)) {
            throw new GatewayException("Payment type not supported!");
        }

        if (ENDPOINT_EXCEPTIONS.containsKey(endpoint)) {
            return Objects.equals(ENDPOINT_EXCEPTIONS.get(endpoint), _settings.getRegion());
        }

        return true;
    }

}
