package com.global.api.utils;

import com.global.api.network.enums.gnap.CardBrand;
import com.global.api.paymentMethods.*;

public class GnapUtils {

    private static boolean isEnableLogging = false;

    private GnapUtils(){

    }

    public static void log(String message, String value) {
        if (isEnableLogging) {
            System.out.println(message + " : " + value);
        }
    }

    public static void enableLogging(Boolean enableLogging)
    {
        isEnableLogging=enableLogging;
    }


    public static CardBrand getCardBrand(IPaymentMethod paymentMethod) {
        if (paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference) paymentMethod;
            if (transactionReference.getOriginalPaymentMethod() != null) {
                paymentMethod = transactionReference.getOriginalPaymentMethod();
            }
        }
        if(paymentMethod instanceof DebitTrackData) {
            return CardBrand.Interac;
        }
        else if (paymentMethod instanceof Credit){
            Credit card=(Credit) paymentMethod;
            String cardTypeValue=card.getCardType();
            if (cardTypeValue.equals("Amex")) {
                return CardBrand.AmericanExpress;
            } else if (cardTypeValue.equals("MC")) {
                return CardBrand.Mastercard;
            } else if (cardTypeValue.equals("Visa")) {
                return CardBrand.Visa;
            } else if (cardTypeValue.equals("Discover")) {
                return CardBrand.Discover;
            }else if(cardTypeValue.equals("Jcb")) {
                return CardBrand.JCB;
            }else if(cardTypeValue.equals("UnionPay")) {
                return CardBrand.UnionPay;
            }else{
                return CardBrand.Unknown;
            }
        }
        return null;
       }
}
