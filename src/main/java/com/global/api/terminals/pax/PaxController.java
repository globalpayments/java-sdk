package com.global.api.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.UnsupportedConnectionModeException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.pax.interfaces.PaxHttpInterface;
import com.global.api.terminals.pax.interfaces.PaxTcpInterface;
import com.global.api.terminals.pax.responses.CreditResponse;
import com.global.api.terminals.pax.responses.DebitResponse;
import com.global.api.terminals.pax.responses.EbtResponse;
import com.global.api.terminals.pax.responses.GiftResponse;
import com.global.api.terminals.pax.subgroups.*;
import com.global.api.utils.StringUtils;

import java.util.ArrayList;

public class PaxController extends DeviceController {
    private IDeviceInterface device;

    private IMessageSentInterface onMessageSent;
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public ConnectionModes getConnectionMode() {
        if(settings != null)
            return settings.getConnectionMode();
        return ConnectionModes.SERIAL;
    }

    public IDeviceInterface configureInterface() {
        if(device == null)
            device = new PaxInterface(this);
        return device;
    }

    public PaxController(ITerminalConfiguration settings) throws ConfigurationException {
        super(settings);

        switch (settings.getConnectionMode()) {
            case TCP_IP:
                _interface = new PaxTcpInterface(settings);
                break;
            case HTTP:
                _interface = new PaxHttpInterface(settings);
                break;
            case SERIAL:
            case SSL_TCP:
                throw new UnsupportedConnectionModeException();
        }

        _interface.setMessageSentHandler(new IMessageSentInterface() {
            public void messageSent(String message) {
                if (onMessageSent != null)
                    onMessageSent.messageSent(message);
            }
        });
    }

    //<editor-fold desc="OVERRIDES">
    public TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException {
        Integer requestId = builder.getRequestId();
        if(requestId == null && requestIdProvider != null) {
            requestId = requestIdProvider.getRequestId();
        }
        // create sub groups
        AmountRequest amounts = new AmountRequest();
        AccountRequest account = new AccountRequest();
        AvsRequest avs = new AvsRequest();
        TraceRequest trace = new TraceRequest();
        trace.setReferenceNumber(requestId.toString());
        CashierSubGroup cashier = new CashierSubGroup();
        CommercialRequest commercial = new CommercialRequest();
        EcomSubGroup ecom = new EcomSubGroup();
        ExtDataSubGroup extData = new ExtDataSubGroup();

        // amounts sub group
        amounts.setTransactionAmount(StringUtils.toNumeric(builder.getAmount()));
        amounts.setCashBackAmount(StringUtils.toNumeric(builder.getCashBackAmount()));
        amounts.setTipAmount(StringUtils.toNumeric(builder.getGratuity()));
        amounts.setTaxAmount(StringUtils.toNumeric(builder.getTaxAmount()));

        // account sub group
        if (builder.getPaymentMethod() != null) {
            if (builder.getPaymentMethod() instanceof CreditCardData) {
                CreditCardData card = (CreditCardData)builder.getPaymentMethod();
                if (StringUtils.isNullOrEmpty(card.getToken())) {
                    account.setAccountNumber(card.getNumber());
                    account.setExpd(card.getShortExpiry());
                    if (builder.getTransactionType() != TransactionType.Verify && builder.getTransactionType() != TransactionType.Refund)
                        account.setCvvCode(card.getCvn());
                }
                else extData.set(PaxExtData.TOKEN, card.getToken());
            }
            else if (builder.getPaymentMethod() instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference)builder.getPaymentMethod();
                if (!StringUtils.isNullOrEmpty(reference.getAuthCode()))
                    trace.setAuthCode(reference.getAuthCode());
                if (!StringUtils.isNullOrEmpty(reference.getTransactionId()))
                    extData.set(PaxExtData.HOST_REFERENCE_NUMBER, reference.getTransactionId());
            }
            else if (builder.getPaymentMethod() instanceof GiftCard) {
                GiftCard card = (GiftCard)builder.getPaymentMethod();
                account.setAccountNumber(card.getNumber());
            }
        }
        if (builder.isAllowDuplicates()) account.setDupOverrideFlag("1");

        // Avs Sub Group
        if (builder.getAddress() != null) {
            avs.setZipCode(builder.getAddress().getPostalCode());
            avs.setAddress(builder.getAddress().getStreetAddress1());
        }

        // Trace Sub Group
        trace.setInvoiceNumber(builder.getInvoiceNumber());

        // Commercial Group
        commercial.setCustomerCode(builder.getCustomerCode());
        commercial.setPoNumber(builder.getPoNumber());
        commercial.setTaxExempt(builder.getTaxExempt());
        commercial.setTaxExemptId(builder.getTaxExemptId());

        // Additional Info sub group
        if (builder.isRequestMultiUseToken())
            extData.set(PaxExtData.TOKEN_REQUEST, "1");

        if (builder.isSignatureCapture())
            extData.set(PaxExtData.SIGNATURE_CAPTURE, "1");

        PaxTxnType transType = mapTransactionType(builder.getTransactionType(), builder.isRequestMultiUseToken());
        switch (builder.getPaymentMethodType()) {
            case Credit:
                return doCredit(transType, amounts, account, trace, avs, cashier, commercial, ecom, extData);
            case Debit:
                return doDebit(transType, amounts, account, trace, cashier, extData);
            case Gift:
                PaxMsgId messageId = builder.getCurrency() == CurrencyType.Currency ? PaxMsgId.T06_DO_GIFT : PaxMsgId.T08_DO_LOYALTY;
                return doGift(messageId, transType, amounts, account, trace, cashier, extData);
            case EBT:
                if(builder.getCurrency() != null)
                    account.setEbtType(builder.getCurrency().getValue().substring(0, 1));
                return doEbt(transType, amounts, account, trace, cashier);
            default:
                throw new UnsupportedTransactionException();
        }
    }

    public TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException {
        Integer requestId = builder.getRequestId();
        if(requestId == null && requestIdProvider != null) {
            requestId = requestIdProvider.getRequestId();
        }
        AmountRequest amounts = new AmountRequest();
        AccountRequest account = new AccountRequest();
        TraceRequest trace = new TraceRequest();
        trace.setReferenceNumber(requestId.toString());
        ExtDataSubGroup extData = new ExtDataSubGroup();

        // amounts
        if(builder.getAmount() != null) {
            amounts.setTransactionAmount(StringUtils.toNumeric(builder.getAmount()));
        }

        if(builder.getPaymentMethod() != null) {
            if(builder.getPaymentMethod() instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference)builder.getPaymentMethod();
                if(!StringUtils.isNullOrEmpty(reference.getTransactionId()))
                    extData.set(PaxExtData.HOST_REFERENCE_NUMBER, builder.getTransactionId());
            }
            else if (builder.getPaymentMethod() instanceof GiftCard) {
                GiftCard card = (GiftCard)builder.getPaymentMethod();
                account.setAccountNumber(card.getNumber());
            }
        }

        PaxTxnType transType = mapTransactionType(builder.getTransactionType());
        switch(builder.getPaymentMethodType()) {
            case Credit:
                return doCredit(transType, amounts, account, trace, new AvsRequest(), new CashierSubGroup(), new CommercialRequest(), new EcomSubGroup(), extData);
            case Gift:
                PaxMsgId messageId = builder.getCurrency() == CurrencyType.Currency ? PaxMsgId.T06_DO_GIFT : PaxMsgId.T08_DO_LOYALTY;
                return doGift(messageId, transType, amounts, account, trace, new CashierSubGroup(), extData);
            case EBT:
                return doEbt(transType, amounts, account, trace, new CashierSubGroup());
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private PaxTxnType mapTransactionType(TransactionType type) throws UnsupportedTransactionException {
        return mapTransactionType(type, false);
    }
    private PaxTxnType mapTransactionType(TransactionType type, boolean requestToken) throws UnsupportedTransactionException {
        switch (type) {
            case AddValue:
                return PaxTxnType.ADD;
            case Auth:
                return PaxTxnType.AUTH;
            case Balance:
                return PaxTxnType.BALANCE;
            case Capture:
                return PaxTxnType.POSTAUTH;
            case Refund:
                return PaxTxnType.RETURN;
            case Sale:
                return PaxTxnType.SALE_REDEEM;
            case Verify:
                return requestToken ? PaxTxnType.TOKENIZE : PaxTxnType.VERIFY;
            case Void:
                return PaxTxnType.VOID;
            case BenefitWithdrawal:
                return PaxTxnType.WITHDRAWAL;
            case Reversal:
                return PaxTxnType.REVERSAL;
            default:
                throw new UnsupportedTransactionException();
        }
    }
    //</editor-fold>

    //<editor-fold desc="TRANSACTION COMMANDS">
    private byte[] doTransaction(PaxMsgId messageId, PaxTxnType transactionType, IRequestSubGroup... subGroups) throws ApiException {
        ArrayList<Object> commands = new ArrayList<Object>();
        commands.add(transactionType);
        commands.add(ControlCodes.FS);
        if(subGroups.length > 0){
            commands.add(subGroups[0]);
            for(int i = 1; i < subGroups.length; i++){
                commands.add(ControlCodes.FS);
                commands.add(subGroups[i]);
            }
        }

        DeviceMessage message = TerminalUtilities.buildRequest(messageId, commands.toArray());
        return _interface.send(message);
    }

    public CreditResponse doCredit(PaxTxnType transactionType, AmountRequest amounts, AccountRequest accounts, TraceRequest trace, AvsRequest avs, CashierSubGroup cashier, CommercialRequest commercial, EcomSubGroup ecom, ExtDataSubGroup extData) throws ApiException {
        byte[] response = doTransaction(PaxMsgId.T00_DO_CREDIT, transactionType, amounts, accounts, trace, avs, cashier, commercial, ecom, extData);
        return new CreditResponse(response);
    }

    public GiftResponse doGift(PaxMsgId messageId, PaxTxnType transactionType, AmountRequest amounts, AccountRequest accounts, TraceRequest trace, CashierSubGroup cashier, ExtDataSubGroup extData) throws ApiException {
        byte[] response = doTransaction(messageId, transactionType, amounts, accounts, trace, cashier,extData);
        return new GiftResponse(response);
    }

    public DebitResponse doDebit(PaxTxnType transactionType, AmountRequest amounts, AccountRequest accounts, TraceRequest trace, CashierSubGroup cashier, ExtDataSubGroup extData) throws ApiException {
        byte[] response = doTransaction(PaxMsgId.T02_DO_DEBIT, transactionType, amounts, accounts, trace, cashier, extData);
        return new DebitResponse(response);
    }

    public EbtResponse doEbt(PaxTxnType transactionType, AmountRequest amounts, AccountRequest accounts, TraceRequest trace, CashierSubGroup cashier) throws ApiException {
        byte[] response = doTransaction(PaxMsgId.T04_DO_EBT, transactionType, amounts, accounts, trace, cashier, new ExtDataSubGroup());
        return new EbtResponse(response);
    }
    //</editor-fold>
}
