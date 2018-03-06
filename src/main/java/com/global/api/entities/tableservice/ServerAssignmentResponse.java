package com.global.api.entities.tableservice;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ServerAssignmentResponse extends TableServiceResponse {
    private ShiftAssignments assignments;

    public ShiftAssignments getAssignments() {
        return assignments;
    }

    public ServerAssignmentResponse(String json) throws ApiException {
        this(json, "default");
    }
    public ServerAssignmentResponse(String json, String configName) throws ApiException {
        super(json, configName);
        expectedAction = "getServerAssignment";
    }

    protected void mapResponse(JsonDoc response) throws ApiException {
        super.mapResponse(response);

        assignments = new ShiftAssignments();

        // if we have a row then it's an array
        if(response.has("row")) {
            for(JsonDoc row: response.getEnumerator("row"))
                addAssignment(row);
        }
        else addAssignment(response);
    }

    private void addAssignment(JsonDoc assignment) {
        String server = assignment.getString("server");
        String tables = assignment.getString("tables");

        if(!StringUtils.isNullOrEmpty(tables)) {
            Set<Integer> ids = new HashSet<Integer>();
            for(String table: tables.split(","))
                ids.add(Integer.parseInt(table));

            assignments.put(server, ids.toArray(new Integer[ids.size()]));
        }
    }
}
