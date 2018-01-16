package com.global.api.entities.tableservice;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerListResponse extends TableServiceResponse {
    private List<String> servers;

    public String[] getServers() {
        return servers.toArray(new String[servers.size()]);
    }
    public void setServers(String[] servers) {
        this.servers = Arrays.asList(servers);
    }

    public ServerListResponse(String json) throws ApiException {
        this(json, "default");
    }
    public ServerListResponse(String json, String configName) throws ApiException {
        super(json, configName);
        expectedAction = "getServerList";
    }

    protected void mapResponse(JsonDoc response) throws ApiException {
        super.mapResponse(response);

        // populate servers
        String serverList = response.getString("serverList");
        if(!StringUtils.isNullOrEmpty(serverList)) {
            servers = new ArrayList<String>();
            for(String server: serverList.split(","))
                servers.add(server);
        }
    }
}
