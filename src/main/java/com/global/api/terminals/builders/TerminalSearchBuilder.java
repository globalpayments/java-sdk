package com.global.api.terminals.builders;


import com.global.api.entities.enums.PaxTxnType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.diamond.enums.DiamondCloudSearchCriteria;
import com.global.api.entities.enums.CardType;
import com.global.api.terminals.upa.Entities.Enums.UpaSearchCriteria;
import com.global.api.entities.enums.PaxSearchCriteriaType;
import com.global.api.entities.exceptions.MessageException;
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
    @Getter @Setter
    private String authCode;
    @Getter @Setter
    private int batch;
    @Getter @Setter
    private String ecrId;
    @Getter @Setter
    private String reportOutput;

    public TerminalSearchBuilder(TerminalReportBuilder terminalReportBuilder){
        this.terminalReportBuilder = terminalReportBuilder;
    }
    public TerminalSearchBuilder and(PaxSearchCriteriaType criteria, Object value) throws NoSuchFieldException, IllegalAccessException, MessageException {
        setProperty(criteria.getValue(), value);
        return this;
    }
    public <T> TerminalSearchBuilder and(UpaSearchCriteria criteria, T value) throws IllegalAccessException, MessageException {
        setProperty(criteria.toString(), value);
        return this;
    }

    public <T> TerminalSearchBuilder and(DiamondCloudSearchCriteria criteria, T value) throws IllegalAccessException, MessageException {
        setProperty(criteria.toString(), value);
        return this;
    }

    public ITerminalReport execute() throws ApiException {
        return this.execute("default");
    }

    public ITerminalReport execute(String configName) throws ApiException {
        return terminalReportBuilder.execute(configName);
    }

    private <T> void setProperty(String propertyName, T value) throws IllegalAccessException {
        // TODO this is different that dotnet version
        if (propertyName == null) {
            return;
        }
        Field[] fields = this.getClass().getDeclaredFields();
        Field actualField = null;
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase(propertyName)) {
                actualField = field;
                break;
            }
        }
        if (actualField == null) {
            return;
        }
        if (actualField.getType() == String.class) {
            setToString(actualField, value);
        } else if (actualField.getType() == int.class) {
            setToInt(actualField, value);
        } else if (actualField.getType() == Integer.class) {
            setToInteger(actualField, value);
        }
    }

    private void setToString(Field actualField, Object value) throws IllegalAccessException {
        if (value == null) {
            actualField.set(this, null);
            return;
        }
        actualField.set(this, String.valueOf(value));
    }

    private void setToInt(Field actualField, Object value) throws IllegalAccessException {
        if (value == null) {
            actualField.set(this, 0);
            return;
        }
        actualField.set(this, Integer.valueOf(String.valueOf(value.toString())));
    }

    private void setToInteger(Field actualField, Object value) throws IllegalAccessException {
        if (value == null) {
            actualField.set(this, null);
            return;
        }
        actualField.set(this, Integer.valueOf(String.valueOf(value.toString())));
    }
}
