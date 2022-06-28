package com.global.api.network.entities.gnap;

import com.global.api.entities.enums.TransactionType;
import com.global.api.network.enums.gnap.*;
import com.global.api.utils.StringUtils;
import lombok.*;


@Builder
@Setter
@Getter
public class OptionalData {
    private TerminalType terminalType;
    private IntegratedHardwareList integratedHardwareList;
    private PinPadCommunication pinPadComm;
    private IntegratedPinpadVersionType integratedPinpadVersionType;
    private PaymentSolutionProviderCode paymentSolutionProviderCode;
    private Base24TransactionModifier base24TransactionModifier;
    private String pinPadSerialNumber;
    private String pOSVARCode;
    private String employeeID;
    private String integratedPinPadVersion;
    private String pOSVersionNO;
    private String paymentSolutionVersionNO;
    private String cAPKKeyVersion;
    private String tLSCiphers;
    boolean telephoneAuthTransaction;
    private String terminalSoftwareVersion;
    private PinPadModel pinPadModel;
    private String publicKeyFileVersion;
    private String terminalOSVersion;
    private String pinPadOSVersion;
    private String terminalSerialNumber;
    private String pinPadSoftwareVersion;

    public String getOptionalData(TransactionType transactionType,TransactionCode code, CardBrand cardBrand, CardType cardType) {
        if(code!=null){
            telephoneAuthTransaction=code.equals(TransactionCode.TelephoneAuthPurchase);
        }
        boolean isPartialData = ((employeeID != null) || transactionType.equals(TransactionType.Void))||telephoneAuthTransaction;

        StringBuilder str = new StringBuilder();
        str.append("*");
        str.append(terminalType.getValue());
        if(terminalType.equals(TerminalType.IntegratedSolutions)) {
            str.append(integratedHardwareList.getValue());
            str.append(pinPadComm.getValue());
            str.append(integratedPinpadVersionType.getValue());
            str.append(paymentSolutionProviderCode.getValue());
            str.append(getBase24TransactionModifier(cardBrand, cardType));
            str.append(employeeID==null?String.format("%05d", 0):employeeID);
            if (!isPartialData){
                str.append(StringUtils.padLeft(integratedPinPadVersion.replace(".",""), 6, '0'));
                str.append(StringUtils.padRight(pinPadSerialNumber,10,' '));
                str.append(pOSVARCode);
                str.append(StringUtils.padRight(pOSVersionNO,9,' '));
                str.append(StringUtils.padRight(paymentSolutionVersionNO,9,' '));
                str.append(StringUtils.padRight(cAPKKeyVersion,4,' '));
                str.append(StringUtils.padRight(tLSCiphers, 10, ' '));
            }
        }else if(terminalType.equals(TerminalType.StandAloneOrSemiIntegratedSolutions)){
            str.append(integratedHardwareList.getValue());
            str.append(terminalSoftwareVersion== null ? String.format("%5s", " ") : terminalSoftwareVersion);
            str.append(getBase24TransactionModifier(cardBrand, cardType));
            str.append(employeeID==null?String.format("%05d", 0):employeeID);
            if (!isPartialData) {
                str.append(pinPadModel.getValue());
                str.append(terminalSerialNumber);
                str.append(publicKeyFileVersion == null ? String.format("%2s", " ") : publicKeyFileVersion);
                str.append(terminalOSVersion == null ? String.format("%5s", " ") : terminalOSVersion);
                str.append(pinPadOSVersion);
                str.append(pinPadSerialNumber);
                str.append(pinPadSoftwareVersion);
            }

        }
        str.append(StringUtils.padRight("",80-str.length(),' '));
        return str.toString();
    }

    public String getBase24TransactionModifier(CardBrand cardBrand,CardType cardType ){
        if(cardType.equals(CardType.Credit) && (cardBrand.equals(CardBrand.AmericanExpress) || cardBrand.equals(CardBrand.JCB)))
            return Base24TransactionModifier.VoidTransactions.getValue();
        else if(telephoneAuthTransaction)
            return Base24TransactionModifier.TelephoneAuthorizedPurchases.getValue();
        else
            return Base24TransactionModifier.OtherTransaction.getValue();
    }
}
