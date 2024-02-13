package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.PaxSearchCriteriaType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.pax.responses.LocalDetailReportResponse;

public class TerminalReportBuilder{

    private PaxSearchCriteriaType searchCriteriaType;
    private TerminalSearchBuilder terminalSearchBuilder;

    public TerminalSearchBuilder getTerminalSearchBuilder(){
        if(terminalSearchBuilder == null){
            terminalSearchBuilder = new TerminalSearchBuilder(this);
        }
        return terminalSearchBuilder;
    }

    public TerminalReportBuilder() {
        this.terminalSearchBuilder = new TerminalSearchBuilder(this);
    }

    public TerminalSearchBuilder withPaxReportSearchCriteria(PaxSearchCriteriaType criteria,Object value)  {

        try {
            return terminalSearchBuilder.and(criteria,value);

        } catch (NoSuchFieldException | IllegalAccessException | MessageException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDetailReportResponse execute(String configName) throws ApiException {

        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.processLocalDetailReport(this);
    }
}
