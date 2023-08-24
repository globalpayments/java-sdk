package com.global.api.network.elements;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE123_TotalType;
import com.global.api.network.enums.DE123_TransactionType;
import com.global.api.utils.ReverseStringEnumMap;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class DE123_ReconciliationTotals_nws implements IDataElement<DE123_ReconciliationTotals_nws> {
        private String entryFormat = "00";
        private List<DE123_ReconciliationTotal> totals;

        public String getEntryFormat() {
            return entryFormat;
        }
        public void setEntryFormat(String entryFormat) {
            this.entryFormat = entryFormat;
        }
        public int getEntryCount() {
            return totals.size();
        }
        public List<DE123_ReconciliationTotal> getTotals() {
            return totals;
        }
        public void setTotals(List<DE123_ReconciliationTotal> totals) {
            this.totals = totals;
        }

        public DE123_ReconciliationTotals_nws() {
            totals = new LinkedList<>();
        }

        public void setTotalCredits(BigDecimal totalAmount) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(0);
            total.setTransactionType(DE123_TransactionType.DebitLessReversals);
            total.setTotalAmount(totalAmount);
            totals.add(total);
        }
        public void setTotalCredits(int transactionCount, BigDecimal totalAmount, String cardType) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(transactionCount);
            total.setTransactionType(DE123_TransactionType.DebitLessReversals);
            total.setTotalAmount(totalAmount);
            total.setCardType(cardType);
            totals.add(total);
        }
        public void setTotalReturns(int transactionCount, BigDecimal totalAmount, String cardType,PaymentMethodType paymentMethodType) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(transactionCount);
            total.setTransactionType(DE123_TransactionType.CreditLessReversals);
            total.setTotalAmount(totalAmount);
            total.setCardType(cardType);
            total.setRefund(true);
            total.setPaymentMethodType(paymentMethodType);
            totals.add(total);
        }
        public void setTotalVoid(int transactionCount, BigDecimal totalAmount, String cardType, PaymentMethodType paymentMethodType) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(transactionCount);
            total.setTransactionType(DE123_TransactionType.AllVoids_Voids);
            total.setTotalAmount(totalAmount);
            total.setCardType(cardType);
            total.setPaymentMethodType(paymentMethodType);
            totals.add(total);
        }

        public void setTotalDebits(int transactionCount, BigDecimal totalAmount, String cardType) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(transactionCount);
            total.setTransactionType(DE123_TransactionType.CreditLessReversals);
            total.setTotalAmount(totalAmount);
            total.setCardType(cardType);
            totals.add(total);
        }
        public void setTotalDebits(int transactionCount, BigDecimal totalAmount) {
            DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
            total.setTransactionCount(transactionCount);
            total.setTransactionType(DE123_TransactionType.CreditLessReversals);
            total.setTotalAmount(totalAmount);
            totals.add(total);
        }

        public DE123_ReconciliationTotals_nws fromByteArray(byte[] buffer) {
            StringParser sp = new StringParser(buffer);

            entryFormat = sp.readString(2);
            int entryCount = sp.readInt(2);

            for(int i = 0; i < entryCount; i++) {
                DE123_ReconciliationTotal total = new DE123_ReconciliationTotal();
                total.setTransactionType(ReverseStringEnumMap.parse(sp.readString(3), DE123_TransactionType.class));
                total.setTotalType(ReverseStringEnumMap.parse(sp.readString(3), DE123_TotalType.class));
                total.setCardType(StringUtils.trimEnd(sp.readString(4)));
                total.setTransactionCount(Integer.parseInt(sp.readToChar('\\')));
                total.setTotalAmount(StringUtils.toAmount(sp.readToChar('\\')));

                totals.add(total);
            }

            return this;
        }

        public byte[] toByteArray() {
            String rvalue = entryFormat
                    .concat(StringUtils.padLeft(getEntryCount(), 2, '0'));

            for(DE123_ReconciliationTotal total: totals) {
                rvalue = rvalue.concat(total.getTransactionType().getValue())
                        .concat(DE123_TotalType.NotSpecific.getValue())
                        .concat(StringUtils.padRight(total.getCardType(), 4, ' '))
                        .concat(total.getTransactionCount() + "\\")
                        .concat(StringUtils.toNumeric(total.getTotalAmount()) + "\\");
            }
            return rvalue.getBytes();
        }

        public String toString() {
            return new String(toByteArray());
        }
    }

