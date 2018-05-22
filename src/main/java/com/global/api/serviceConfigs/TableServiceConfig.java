package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.gateways.TableServiceConnector;

public class TableServiceConfig extends Configuration {
    private TableServiceProviders tableServiceProvider;

    public TableServiceProviders getTableServiceProvider() {
        return tableServiceProvider;
    }
    public void setTableServiceProvider(TableServiceProviders tableServiceProvider) {
        this.tableServiceProvider = tableServiceProvider;
    }

    public void configureContainer(ConfiguredServices services) {
        if(tableServiceProvider.equals(TableServiceProviders.FreshTxt)) {
            TableServiceConnector conn = new TableServiceConnector();
            conn.setServiceUrl("https://www.freshtxt.com/api31/");
            conn.setTimeout(timeout);

            services.setTableServiceConnector(conn);
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if(tableServiceProvider == null)
            throw new ConfigurationException("A table service provider must be specified.");
    }
}
