package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.PaxSearchCriteriaType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.diamond.enums.DiamondCloudSearchCriteria;
import com.global.api.terminals.enums.TerminalReportType;
import com.global.api.terminals.upa.Entities.Enums.UpaSearchCriteria;
import com.global.api.builders.validations.Validations;
import lombok.Getter;
import lombok.Setter;

public class TerminalReportBuilder{

    private TerminalSearchBuilder terminalSearchBuilder;
    @Getter @Setter
    private TerminalReportType terminalReportType;

    public TerminalSearchBuilder getTerminalSearchBuilder(){
        if(terminalSearchBuilder == null){
            terminalSearchBuilder = new TerminalSearchBuilder(this);
        }
        return terminalSearchBuilder;
    }

    public TerminalReportBuilder() {
        this.terminalSearchBuilder = new TerminalSearchBuilder(this);
    }

    public TerminalReportBuilder(TerminalReportType terminalReportType) {
        this.terminalReportType = terminalReportType;
    }


    public TerminalSearchBuilder withPaxReportSearchCriteria(PaxSearchCriteriaType criteria,Object value)  {

        try {
            return terminalSearchBuilder.and(criteria,value);

        } catch (NoSuchFieldException | IllegalAccessException | MessageException e) {
            throw new RuntimeException(e);
        }
    }

    public ITerminalReport execute() throws ApiException {
        return this.execute("default");
    }

    public ITerminalReport execute(String configName) throws ApiException {
        getTerminalSearchBuilder();
        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.processReport(this);
    }

    public <T> TerminalSearchBuilder where(DiamondCloudSearchCriteria criteria, T value) throws IllegalAccessException, MessageException {
        return getTerminalSearchBuilder().and(criteria, value);
    }

    public <T> TerminalSearchBuilder where(UpaSearchCriteria criteria, T value) throws IllegalAccessException, MessageException {
        return getTerminalSearchBuilder().and(criteria, value);
    }

    public Validations setupValidationsReport() {
        Validations validations = new Validations();
        validations.of(TerminalReportType.LocalDetailReport).check("terminalSearchBuilder").propertyOf(String.class, "referenceNumber").isNotNull();
        return validations;
    }
}