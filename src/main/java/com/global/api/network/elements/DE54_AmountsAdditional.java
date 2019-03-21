package com.global.api.network.elements;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE3_AccountType;
import com.global.api.network.enums.DE54_AmountTypeCode;
import com.global.api.network.enums.Iso4217_CurrencyCode;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.StringParser;

import java.math.BigDecimal;
import java.util.HashMap;

public class DE54_AmountsAdditional implements IDataElement<DE54_AmountsAdditional> {
    private HashMap<DE3_AccountType, HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount>> amountMap;

    public DE54_AmountsAdditional() {
        amountMap = new HashMap<DE3_AccountType, HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount>>();
    }

    public DE54_AdditionalAmount get(DE3_AccountType accountType, DE54_AmountTypeCode amountType) {
        if(amountMap.containsKey(accountType)) {
            HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount> amounts = amountMap.get(accountType);
            if(amounts.containsKey(amountType)) {
                return amounts.get(amountType);
            }
            return null;
        }
        return null;
    }

    public BigDecimal getAmount(DE3_AccountType accountType, DE54_AmountTypeCode amountType) {
        DE54_AdditionalAmount entity = get(accountType, amountType);
        if(entity != null) {
            return entity.getAmount();
        }
        return null;
    }

    public void put(DE54_AmountTypeCode amountType, DE3_AccountType accountType, Iso4217_CurrencyCode currencyCode, BigDecimal amount) throws ApiException {
        DE54_AdditionalAmount entry = new DE54_AdditionalAmount();
        entry.setAccountType(accountType);
        entry.setAmountType(amountType);
        entry.setCurrencyCode(currencyCode);
        entry.setAmount(amount);
        put(entry);
    }
    public void put(DE54_AdditionalAmount entry) throws ApiException {
        if(amountMap.size() < 6) {
            if(!amountMap.containsKey(entry.getAccountType())) {
                amountMap.put(entry.getAccountType(), new HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount>());
            }
            amountMap.get(entry.getAccountType()).put(entry.getAmountType(), entry);
        }
        else {
            throw new BuilderException("You may only specify 6 additional amountMap.");
        }
    }

    public int size() {
        return amountMap.size();
    }

    public DE54_AmountsAdditional fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        byte[] entryBuffer = sp.readBytes(20);
        while(entryBuffer.length > 0) {
            DE54_AdditionalAmount entry = new DE54_AdditionalAmount().fromByteArray(entryBuffer);
            if(!amountMap.containsKey(entry.getAccountType())) {
                amountMap.put(entry.getAccountType(), new HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount>());
            }
            amountMap.get(entry.getAccountType()).put(entry.getAmountType(), entry);

            entryBuffer = sp.readBytes(20);
        }

        return this;
    }

    public byte[] toByteArray() {
        MessageWriter mw = new MessageWriter();

        for(HashMap<DE54_AmountTypeCode, DE54_AdditionalAmount> amounts: amountMap.values()) {
            for(DE54_AdditionalAmount amount: amounts.values()) {
                mw.addRange(amount.toByteArray());
            }
        }

        return mw.toArray();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
