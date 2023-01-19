package com.global.api.entities.transactionApi.entities;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.BaseBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.transactionApi.TransactionApiRequest;
import com.global.api.entities.transactionApi.enums.TransactionAPIEndPoints;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.CountryUtils;

import java.util.Arrays;
import java.util.Optional;

public interface TransactionApiUtils {
    String[] SUPPORTED_COUNTRIES = {"840", "124", "036", "554", "826"};

    static <T extends BaseBuilder<Transaction>> TransactionAPIEndPoints getEndpointURL(T builder) {
        TransactionType type = null;
        PaymentMethodType paymentMethodType = null;
        TransactionReference reference = null;
        if (builder instanceof AuthorizationBuilder) {
            type = ((AuthorizationBuilder) builder).getTransactionType();
            paymentMethodType = ((AuthorizationBuilder) builder).getPaymentMethod().getPaymentMethodType();
        } else if (builder instanceof ManagementBuilder) {
            type = ((ManagementBuilder) builder).getTransactionType();
            paymentMethodType = ((ManagementBuilder) builder).getPaymentMethod().getPaymentMethodType();
            if(((ManagementBuilder) builder).getPaymentMethod() instanceof TransactionReference) {
                IPaymentMethod paymentMethod = ((ManagementBuilder) builder).getPaymentMethod();
                reference = (TransactionReference) paymentMethod;
            }
        }

        if (paymentMethodType == PaymentMethodType.Credit) {
            if (type == TransactionType.Sale) {
                return TransactionAPIEndPoints.CreditSale;
            } else if (type == TransactionType.Auth) {
                return TransactionAPIEndPoints.CreditAuth;
            } else if (type == TransactionType.Refund && reference == null) {
                return TransactionAPIEndPoints.CreditReturn;
            } else if (type == TransactionType.Refund ) {
                if(reference.getTransactionId() != null)
                    return TransactionAPIEndPoints.RefundCreditSaleWithTransactionId;
                else
                    return TransactionAPIEndPoints.RefundCreditSaleWithReferenceId;
            }else if(type==TransactionType.Edit && reference!=null){
                if(reference.getTransactionId()!=null)
                    return TransactionAPIEndPoints.CreditSaleWithTransactionId;
                else
                    return TransactionAPIEndPoints.CreditSaleWithReferenceId;
            } else if(type==TransactionType.Void && reference != null) {
                if (reference.getOriginalTransactionType() == null) {
                    throw new UnsupportedOperationException("Original Transaction Type Required for Void");
                } else if(reference.getTransactionId() != null && reference.getClientTransactionId() != null){
                    throw new UnsupportedOperationException("Transaction ID or Reference ID is Required for Void");
                }
                if (reference.getTransactionId() != null) {
                    if (reference.getOriginalTransactionType() == TransactionType.Sale)
                        return TransactionAPIEndPoints.CreditVoidWithTransactionId;
                    else if (reference.getOriginalTransactionType() == TransactionType.Refund)
                        return TransactionAPIEndPoints.VoidCreditReturnWithTransactionID;
                } else {
                    if (reference.getOriginalTransactionType() == TransactionType.Sale) {
                        return TransactionAPIEndPoints.CreditVoidWithReferenceID;
                    } else if (reference.getOriginalTransactionType() == TransactionType.Refund) {
                        return TransactionAPIEndPoints.VoidCreditReturnWithReferenceID;
                    }
                }
            }else if(type==TransactionType.Fetch && reference!=null){
                if (reference.getOriginalTransactionType() == null) {
                    throw new UnsupportedOperationException("Original Transaction Type Required for Fetch");
                }
                if (reference.getTransactionId() != null) {
                    if (reference.getOriginalTransactionType() == TransactionType.Sale)
                        return TransactionAPIEndPoints.CreditSaleWithTransactionIdGet;
                    else if (reference.getOriginalTransactionType() == TransactionType.Refund)
                        return TransactionAPIEndPoints.CreditReturnWithTransactionIdGet;
                }else{
                    if (reference.getOriginalTransactionType() == TransactionType.Sale) {
                        return TransactionAPIEndPoints.CreditSaleWithReferenceIdGet;
                    } else if (reference.getOriginalTransactionType() == TransactionType.Refund) {
                        return TransactionAPIEndPoints.CreditReturnWithReferenceIdGet;
                    }
                }
            }

        } else if(paymentMethodType ==  PaymentMethodType.ACH) {
            if (type == TransactionType.Sale) {
                return TransactionAPIEndPoints.CheckSales;

            } else if (type == TransactionType.Refund && reference == null) {
                return TransactionAPIEndPoints.CheckRefund;
            } else if (type == TransactionType.Refund) {
                if(reference.getTransactionId() != null)
                    return TransactionAPIEndPoints.RefundChecksaleWithTransactionId;
                else
                    return TransactionAPIEndPoints.RefundCheckSaleWithReferenceId;
            }
            else if(type==TransactionType.Fetch && reference!=null){
                if (reference.getOriginalTransactionType() == null) {
                    throw new UnsupportedOperationException("Original Transaction Type Required for Fetch");
                }
                if (reference.getTransactionId() != null) {
                    if (reference.getOriginalTransactionType() == TransactionType.Sale)
                        return TransactionAPIEndPoints.ACHSaleWithTransactionId;
                    else if (reference.getOriginalTransactionType() == TransactionType.Refund)
                        return TransactionAPIEndPoints.ACHRefundWithTransactionId;
                }else{
                    if (reference.getOriginalTransactionType() == TransactionType.Sale) {
                        return TransactionAPIEndPoints.ACHSaleWithReferenceId;
                    } else if (reference.getOriginalTransactionType() == TransactionType.Refund) {
                        return TransactionAPIEndPoints.ACHRefundWithReferenceId;
                    }
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    public static <T extends BaseBuilder<Transaction>> String getCountryCode(T builder) {
        Customer customerInfo = null;
        String country = null;
        if (builder instanceof AuthorizationBuilder) {
            customerInfo = ((AuthorizationBuilder) builder).getCustomer();
            country = ((AuthorizationBuilder) builder).getCountry();
        } else if (builder instanceof ManagementBuilder) {
            customerInfo = ((ManagementBuilder) builder).getCustomer();
            country = ((ManagementBuilder) builder).getCountry();
        }
        String countryCode;
        if (customerInfo != null) {
            countryCode = CountryUtils.getNumericCodeByCountry(customerInfo.getAddress().getCountry());
        } else if (country != null) {
            countryCode = CountryUtils.getNumericCodeByCountry(country);
        } else {
            throw new UnsupportedOperationException("Country is mandatory field.");
        }

        Optional<String> validCountry = Arrays.stream(SUPPORTED_COUNTRIES).filter(item -> item.equals(countryCode)).findAny();
        if (validCountry.isPresent()) {
            return validCountry.get();
        } else {
            throw new UnsupportedOperationException("Country code is not supported.");
        }
    }
}
