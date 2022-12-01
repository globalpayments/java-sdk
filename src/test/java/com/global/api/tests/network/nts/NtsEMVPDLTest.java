package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.nts.NtsEMVPDLResponse;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsPDLData;
import com.global.api.network.entities.emvpdl.*;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.network.enums.nts.EmvPDLCardType;
import com.global.api.network.enums.nts.PDLEndOfTableFlag;
import com.global.api.network.enums.nts.PDLParameterType;
import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.utils.StringUtils;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsEMVPDLTest {
    // gateway config
    NetworkGatewayConfig config;
    NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private EMVPDLTable10V2 table10V2;

    public NtsEMVPDLTest() throws ConfigurationException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("510");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.EmvParameterDataLoad);

        ntsRequestMessageHeader.setPriorMessageResponseTime(1);
        ntsRequestMessageHeader.setPriorMessageConnectTime(999);
        ntsRequestMessageHeader.setPriorMessageCode("01");


        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        // gateway config
        config = new NetworkGatewayConfig(Target.NTS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setAcceptorConfig(acceptorConfig);

        // NTS Related configurations
        config.setBinTerminalId(" ");
        config.setBinTerminalType(" ");
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);
        config.setTerminalId("21");
        config.setUnitNumber("00066654534");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

    }

    @Test //working 10
    public void test_Emv_PDL_001() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.None);

        Transaction response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        NtsEMVPDLResponse pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());


        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working 30
    public void test_Emv_PDL_002() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.None);

        Transaction response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        NtsEMVPDLResponse pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());

        // Table 30
        ntsPDLData.setTableId(PDLTableID.Table30);
        EMVPDLTable<EMVPDLTable10> table = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table.getTable().getEmvPdlTableId30Version());
        ntsPDLData.setBlockSequenceNumber("01");
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        String table30Data = pdlResponse.getEmvPdlTableDataBlockData();

        // Completion
        ntsPDLData.setTableId(PDLTableID.Table30);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable30> table30 = IEMVPDLTable.parseData(table30Data, PDLTableID.Table30);
        System.out.println(table30.getTable().toString());

    }

    @Test //working 40
    public void test_Emv_PDL_003() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.None);

        Transaction response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        NtsEMVPDLResponse pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());

        // Table 40
        ntsPDLData.setTableId(PDLTableID.Table40);
        EMVPDLTable<EMVPDLTable10> table10 = pdlResponse.getTable();
        Optional<EMVPDLCardTypesTable> table = table10.getTable().getTableVersionByCardType(EmvPDLCardType.Visa);
        String table40Version = table.get().getEmvPdlTableId40Version();
        ntsPDLData.setParameterVersion(table40Version);
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Visa);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table40Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));


            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table40Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }

        System.out.println("Table 40 data: " + table40Data);

        System.out.println("End block");


        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable40> table40 = IEMVPDLTable.parseData(table40Data.toString(), PDLTableID.Table40);
        System.out.println(table40.getTable().toString());

    }

    @Test //working 60
    public void test_Emv_PDL_005() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.None);

        Transaction response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        NtsEMVPDLResponse pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());

        // Table 40
        ntsPDLData.setTableId(PDLTableID.Table60);
        EMVPDLTable<EMVPDLTable10> table10 = pdlResponse.getTable();
        Optional<EMVPDLCardTypesTable> table = table10.getTable().getTableVersionByCardType(EmvPDLCardType.Mastercard);
        String table60Version = table.get().getEmvPdlTableId60Version();
        ntsPDLData.setParameterVersion(table60Version);
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Mastercard);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table60Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));


            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();

            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table60Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 60 data: " + table60Data);

        System.out.println("End block");


        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable60> table60 = IEMVPDLTable.parseData(table60Data.toString(), PDLTableID.Table60);
        System.out.println(table60.getTable().toString());
    }

    @Test //working 10 version 2
    public void test_Emv_PDL_006() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("002");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.None);
        ntsPDLData.setEmvPdlConfigurationName(String.format("%40s", " "));

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table10Data = new StringBuilder();
        NtsEMVPDLResponse pdlResponse = null;
        Transaction response = null;
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());

            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table10Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }
        assertNotNull(pdlResponse);
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 10 data: " + table10Data);

        System.out.println("End block");
        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        // check response
        assertEquals("00", response.getResponseCode());

        EMVPDLTable<EMVPDLTable10V2> table = IEMVPDLTable.parseData(table10Data.toString(), PDLTableID.Table10);
        table10V2 = table.getTable();
        //System.out.println(table.toString());
    }

    @Test //working 30 version 2
    public void test_Emv_PDL_007() throws ApiException {
        test_Emv_PDL_006();

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("002");
        ntsPDLData.setBlockSequenceNumber("01");

        // Table 30
        ntsPDLData.setTableId(PDLTableID.Table30);
        String table30Version = table10V2.getTableVersionsFlags().get(0).getEmvPdlTableId30Version();
        ntsPDLData.setParameterVersion(table30Version);
        String configurationName = table10V2.getTableVersionsFlags().get(0).getEmvPdlConfigurationName();
        ntsPDLData.setEmvPdlConfigurationName(configurationName);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Visa);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table30Data = new StringBuilder();
        Transaction response = null;
        NtsEMVPDLResponse pdlResponse = null;
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            assertNotNull(pdlResponse);
            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            String emvPdlTableDataBlockData= pdlResponse.getEmvPdlTableDataBlockData();
            assertNotNull(emvPdlTableDataBlockData);
            table30Data.append(emvPdlTableDataBlockData);

        }
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 30 data: " + table30Data);

        System.out.println("End block");

        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());


        EMVPDLTable<EMVPDLTable30> table30 = IEMVPDLTable.parseData(table30Data.toString(), PDLTableID.Table30);
        System.out.println(table30.getTable().toString());
    }

    @Test //working 40 version 2
    public void test_Emv_PDL_008() throws ApiException {
        test_Emv_PDL_006();

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("002");
        ntsPDLData.setBlockSequenceNumber("01");

        // Table 40
        ntsPDLData.setTableId(PDLTableID.Table40);
        String table40Version = table10V2.getTableVersionsFlags().get(0).getEmvPdlCardTypes().get(0).getEmvPdlTableId40Version();
        ntsPDLData.setParameterVersion(table40Version);
        String configurationName = table10V2.getTableVersionsFlags().get(0).getEmvPdlConfigurationName();
        ntsPDLData.setEmvPdlConfigurationName(configurationName);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Visa);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table40Data = new StringBuilder();
        Transaction response = null;
        NtsEMVPDLResponse pdlResponse = null;
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();

            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table40Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 40 data: " + table40Data);

        System.out.println("End block");

        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable40> table40 = IEMVPDLTable.parseData(table40Data.toString(), PDLTableID.Table40);
        System.out.println(table40.getTable().toString());
    }

    @Test //working 50 version 2
    public void test_Emv_PDL_009() throws ApiException {
        test_Emv_PDL_006();

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("002");
        ntsPDLData.setBlockSequenceNumber("01");

        // Table 50
        ntsPDLData.setTableId(PDLTableID.Table50);
        String table50Version = table10V2.getTableVersionsFlags().get(0).getEmvPdlCardTypes().get(1).getEmvPdlTableId50Version();
        ntsPDLData.setParameterVersion(table50Version);
        String configurationName = table10V2.getTableVersionsFlags().get(0).getEmvPdlConfigurationName();
        ntsPDLData.setEmvPdlConfigurationName(configurationName);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Visa);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table50Data = new StringBuilder();
        Transaction response = null;
        NtsEMVPDLResponse pdlResponse = null;
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();

            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table50Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 50 data: " + table50Data);

        System.out.println("End block");

        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable50> table50 = IEMVPDLTable.parseData(table50Data.toString(), PDLTableID.Table50);
        System.out.println(table50.getTable().toString());
    }

    @Test //working 60 version 2
    public void test_Emv_PDL_010() throws ApiException {
        test_Emv_PDL_006();

        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestEMVPDL);
        ntsPDLData.setParameterVersion("002");
        ntsPDLData.setBlockSequenceNumber("01");

        // Table 60
        ntsPDLData.setTableId(PDLTableID.Table60);
        String table60Version = table10V2.getTableVersionsFlags().get(0).getEmvPdlCardTypes().get(1).getEmvPdlTableId60Version();
        ntsPDLData.setParameterVersion(table60Version);
        String configurationName = table10V2.getTableVersionsFlags().get(0).getEmvPdlConfigurationName();
        ntsPDLData.setEmvPdlConfigurationName(configurationName);
        ntsPDLData.setEmvPDLCardType(EmvPDLCardType.Visa);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table60Data = new StringBuilder();
        Transaction response = null;
        NtsEMVPDLResponse pdlResponse = null;
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            assertNotNull(pdlResponse);
            endOfData = pdlResponse.getEmvPdlEndOfTableFlag();
            table60Data.append(pdlResponse.getEmvPdlTableDataBlockData());
        }
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEmvPdlEndOfTableFlag());
        System.out.println("Table 60 data: " + table60Data);

        System.out.println("End block");

        // Completion
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.EMVPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.EmvPdl)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsEMVPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEmvPdlEndOfTableFlag());

        EMVPDLTable<EMVPDLTable60> table60 = IEMVPDLTable.parseData(table60Data.toString(), PDLTableID.Table60);
        System.out.println(table60.getTable().toString());
    }


}
