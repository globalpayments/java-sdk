package com.global.api.tests.tableservice;

import com.global.api.entities.enums.TableServiceProviders;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.tableservice.*;
import com.global.api.serviceConfigs.TableServiceConfig;
import com.global.api.services.TableService;
import com.global.api.utils.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableServiceTests {
    private TableService _service;

    public TableServiceTests() throws ApiException {
        TableServiceConfig config = new TableServiceConfig();
        config.setTableServiceProvider(TableServiceProviders.FreshTxt);

        _service = new TableService(config);
    }

    @Before
    public void login() throws ApiException {
        LoginResponse response = _service.login("globa10", "glob8859");
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_001_assignCheck() throws ApiException {
        Ticket response = _service.assignCheck(1, 1);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002_openOrder() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse orderOpened = assignedCheck.openOrder();
        assertNotNull(orderOpened);
        assertEquals("00", orderOpened.getResponseCode());
    }

    @Test
    public void test_003_bumpStatus() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse statusResponse = assignedCheck.bumpStatus(_service.getBumpStatuses()[0]);
        assertNotNull(statusResponse);
        assertEquals("00", statusResponse.getResponseCode());
        assertEquals(assignedCheck.getBumpStatusId(), 2);
    }

    @Test(expected = MessageException.class)
    public void test_004_bumpStatusWithBadStatus() throws ApiException {
        Ticket.fromId(1, 1).bumpStatus("badStatus");
    }

    @Test
    public void test_005_settleCheck() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse response = assignedCheck.settleCheck();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_006_settleCheckWithStatus() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse response = assignedCheck.settleCheck(_service.getBumpStatuses()[1]);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(assignedCheck.getBumpStatusId(), 3);
    }

    @Test(expected = MessageException.class)
    public void test_007_settleCheckWithBadStatus() throws ApiException {
        Ticket.fromId(1, 1).settleCheck("badStatus");
    }

    @Test
    public void test_008_clearTable() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse response = assignedCheck.clearTable();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_009_queryTableStatus() throws ApiException {
        TableServiceResponse response = _service.queryTableStatus(1);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_010_queryCheckStatus() throws ApiException {
        TableServiceResponse response = _service.queryCheckStatus(1);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_011_editTable() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);
        assignedCheck.setPartyName("Party Of One");
        assignedCheck.setPartyNumber(1);
        assignedCheck.setSection("Lonely Section");

        TableServiceResponse response = assignedCheck.update();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_012_transfer() throws ApiException {
        Ticket assignedCheck = Ticket.fromId(1, 1);

        TableServiceResponse response = assignedCheck.transfer(2);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_013_getServerList() throws ApiException {
        ServerListResponse response = _service.getServerList();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getServers());
    }

    @Test
    public void test_014_updateServerList() throws ApiException {
        String[] servers = new String[] {
                "Russ", "Shane", "Mark", "Salina"
        };

        ServerListResponse response = _service.updateServerList(servers);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getServers());
        assertEquals(4, response.getServers().length);
        assertEquals("Russ,Shane,Mark,Salina", StringUtils.join(",", response.getServers()));
    }

    @Test
    public void test_015_assignShifts() throws ApiException {
        ShiftAssignments assignments = new ShiftAssignments();
        assignments.put("Russell", new Integer[] { 1, 2, 3, 4 });
        assignments.put("Shane", new Integer[] { 200, 201, 202, 203 });
        assignments.put("Mark", new Integer[] { 304, 305, 306 });
        assignments.put("Salina", new Integer[] { 409, 408, 409 });

        ServerAssignmentResponse response = _service.assignShift(assignments);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // check return values
        assertNotNull(response.getAssignments());
        assertTrue(response.getAssignments().containsKey("Russell"));
        for (int table : response.getAssignments().get("Russell")) {
            assertTrue(Arrays.asList(1, 2, 3, 4).contains(table));
        }

        assertTrue(response.getAssignments().containsKey("Shane"));
        for (int table: response.getAssignments().get("Shane")) {
            assertTrue(Arrays.asList( 200, 201, 202, 203 ).contains(table));
        }

        assertTrue(response.getAssignments().containsKey("Mark"));
        for (int table: response.getAssignments().get("Mark")) {
            assertTrue(Arrays.asList( 304, 305, 306 ).contains(table));
        }

        assertTrue(response.getAssignments().containsKey("Salina"));
        for (int table: response.getAssignments().get("Salina")) {
            assertTrue(Arrays.asList( 409, 408, 409 ).contains(table));
        }
    }

    @Test
    public void test_016_getAllServerAssignments() throws ApiException {
        ServerAssignmentResponse response = _service.getServerAssignments();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_017_getServerAssignmentsByServer() throws ApiException {
        ServerAssignmentResponse response = _service.getServerAssignments("Russell");
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_018_getTableAssignmentsByTable() throws ApiException {
        ServerAssignmentResponse response = _service.getServerAssignments(1);
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
