package com.global.api.terminals.builders;

import com.global.api.entities.enums.CardType;
import com.global.api.entities.enums.PaxSearchCriteriaType;
import com.global.api.entities.enums.PaxTxnType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.pax.responses.LocalDetailReportResponse;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;


public class TerminalSearchBuilder {

    @Getter @Setter
    private PaxTxnType transactionType;
    @Getter @Setter
    private String edcType;
    @Getter @Setter
    private CardType cardType;
    @Getter @Setter
    private Integer recordNumber;
    @Getter @Setter
    private String transactionNumber;
    @Getter @Setter
    private String authNumber;
    @Getter @Setter
    private String ecrReferenceNumber;
    @Getter @Setter
    private String referenceNumber;
    private TerminalReportBuilder terminalReportBuilder;
    @Getter @Setter
    private String terminalReferenceNumber;
    @Getter @Setter
    private String merchantId;
    @Getter @Setter
    private String merchantName;

    public TerminalSearchBuilder(TerminalReportBuilder terminalReportBuilder){
        this.terminalReportBuilder = terminalReportBuilder;
    }
    public TerminalSearchBuilder and(PaxSearchCriteriaType criteria, Object value) throws NoSuchFieldException, IllegalAccessException, MessageException {
        setProperty(criteria.getValue(), value);
        return this;
    }
    public LocalDetailReportResponse execute(String configName) throws ApiException {
        return terminalReportBuilder.execute(configName);
    }

    private <T> void setProperty(String propertyName, T value) throws MessageException {
        try {
            Field prop = getClass().getDeclaredField(propertyName);
            prop.setAccessible(true);
            if (prop != null) {
                if (prop.getType() == value.getClass()) {
                    prop.set(this, value);
                } else if (prop.getType().getName().equals("java.lang.Integer")) {
                    if (value != null) {
                        prop.set(this, Integer.parseInt(value.toString()));
                    }
                } else {
                    prop.set(this, value);
                }
            }
        }catch(Exception e){
            throw new MessageException("Invalid type provided for "+ propertyName +" field");
        }

    }

}
