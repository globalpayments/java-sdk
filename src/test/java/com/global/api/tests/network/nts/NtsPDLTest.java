package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.nts.NtsPDLResponse;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsPDLData;
import com.global.api.network.entities.mpdl.*;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.TerminalOutputCapability;
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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsPDLTest {

    // gateway config
    NetworkGatewayConfig config;
    NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class

    public NtsPDLTest() throws ConfigurationException {
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
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ParameterDataLoad);
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
    public void test_Magnum_PDL_001() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.EndOfTable, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        System.out.println(table10.getTable().toString());

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        // check response
        assertEquals("00", response.getResponseCode());


    }

    @Test //working 30
    public void test_Magnum_PDL_002() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getCustomerDiscretionaryTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table30);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table30Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table30Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 30 data: " + table30Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable30> table = IMPDLTable.parseData(table30Data.toString(), PDLTableID.Table30);
        System.out.println(table.getTable().toString());
    }

    @Test //working 40
    public void test_Magnum_PDL_003() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getCardDataTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table40);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table40Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table40Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 40 data: " + table40Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable40> table = IMPDLTable.parseData(table40Data.toString(), PDLTableID.Table40);
        System.out.println(table.getTable().toString());
    }

    @Test //working 50
    public void test_Magnum_PDL_004() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());


        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getBinRangeTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table50);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table50Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table50Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 50 data: " + table50Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable50> table = IMPDLTable.parseData(table50Data.toString(), PDLTableID.Table50);
        System.out.println(table.getTable().toString());
    }

    @Test //working 60
    public void test_Magnum_PDL_005() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getProductDataTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table60);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table60Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table60Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 60 data: " + table60Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable60> table = IMPDLTable.parseData(table60Data.toString(), PDLTableID.Table60);
        System.out.println(table.getTable().toString());
        printData(table.getTable());
    }

    void printData(MPDLTable60 table){
        table.getProducts().stream().forEach(product -> {
            System.out.println(product.getReceiptDescription()+" : "+ product.getConexxusCode()+" : ");
            printCardCodes(product.getCards());
        });
    }

    void printCardCodes(List<MPDLTable60.Cards> cards){
        cards.stream().forEach(card -> System.out.println( card.getHostCardType() +" : "+ card.getProductCode()));
    }

    @Test //working 70
    public void test_Magnum_PDL_006() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getMessageTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table70);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        Integer blockSequenceNumber = 1;
        StringBuilder table70Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table70Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 70 data: " + table70Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable70> table = IMPDLTable.parseData(table70Data.toString(), PDLTableID.Table70);
        System.out.println(table.getTable().toString());
    }

    @Test //working 80
    public void test_Magnum_PDL_007() throws ApiException {
        // PDL configurations.
        NtsPDLData ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        ntsPDLData.setParameterVersion("   ");
        ntsPDLData.setTableId(PDLTableID.Table10);
        ntsPDLData.setBlockSequenceNumber("00");

        Transaction response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // PDL configurations for customer data.
        NtsPDLResponse pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        ntsPDLData = new NtsPDLData();
        ntsPDLData.setParameterType(PDLParameterType.RequestMagnumPdl);
        MPDLTable<MPDLTable10> table10 = pdlResponse.getTable();
        ntsPDLData.setParameterVersion(table10.getTable().getResponseCodeTableVersion());
        ntsPDLData.setTableId(PDLTableID.Table80);

        PDLEndOfTableFlag endOfData = PDLEndOfTableFlag.NotEndOfTable;
        int blockSequenceNumber = 1;
        StringBuilder table80Data = new StringBuilder();
        while (endOfData.equals(PDLEndOfTableFlag.NotEndOfTable)) {
            ntsPDLData.setBlockSequenceNumber(StringUtils.padLeft(String.valueOf(blockSequenceNumber++), 2, '0'));

            response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                    .withPDLData(ntsPDLData)
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                    .execute();
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
            pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
            endOfData = pdlResponse.getEndOfTableFlag();
            table80Data.append(pdlResponse.getTableDataBlockData());
        }

        System.out.println("Table 80 data: " + table80Data);

        System.out.println("End block");

        // End of data receive
        ntsPDLData.setBlockSequenceNumber("00");
        ntsPDLData.setParameterType(PDLParameterType.MagnumPDLConfirm);

        response = NetworkService.fetchPDL(TransactionType.MagnumPDL)
                .withPDLData(ntsPDLData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        pdlResponse = (NtsPDLResponse) response.getNtsResponse().getNtsResponseMessage();
        assertEquals(PDLEndOfTableFlag.DownloadConfirmation, pdlResponse.getEndOfTableFlag());

        MPDLTable<MPDLTable80> table = IMPDLTable.parseData(table80Data.toString(), PDLTableID.Table80);
        System.out.println(table.getTable().toString());
    }

}
