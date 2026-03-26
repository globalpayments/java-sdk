package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

import static org.junit.Assert.*;

    public class VapsCombined3DESTokenizationTests {
        private CreditCardData card;
        private CreditTrackData track;
        private AcceptorConfig acceptorConfig;
        private NetworkGatewayConfig config;

        public VapsCombined3DESTokenizationTests() throws ApiException {
            Address address = new Address();
            address.setName("My STORE");
            address.setStreetAddress1("1 MY STREET");
            address.setCity("MYTOWN");
            address.setPostalCode("90210");
            address.setState("KY");
            address.setCountry("USA");

            acceptorConfig = new AcceptorConfig();
            acceptorConfig.setAddress(address);

            // data code values
            acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
            acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
            acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
            acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

            // hardware software config values
            acceptorConfig.setHardwareLevel("34");
            acceptorConfig.setSoftwareLevel("21205710");

            // pos configuration values
            acceptorConfig.setSupportsPartialApproval(true);
            acceptorConfig.setSupportsShutOffAmount(true);
            acceptorConfig.setSupportsReturnBalance(true);
            acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
            acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
            acceptorConfig.setSupportsEmvPin(true);
            acceptorConfig.setSupportWexAvailableProducts(true);
            acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
            acceptorConfig.setVisaFleet2(false);

            acceptorConfig.setServiceType(ServiceType.GPN_API);
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
            acceptorConfig.setTokenizationType(TokenizationType.MerchantTokenization);
            acceptorConfig.setMerchantId("00009121977");
            // gateway config
            config = new NetworkGatewayConfig();
            config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
            config.setPrimaryPort(15031);
            config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
            config.setSecondaryPort(15031);
            config.setCompanyId("0044");
            config.setTerminalId("0007999999911");
            config.setAcceptorConfig(acceptorConfig);
            config.setEnableLogging(true);
            config.setStanProvider(StanGenerator.getInstance());
            config.setBatchProvider(BatchProvider.getInstance());

            ServicesContainer.configureService(config);

        }

        @Test
        public void test_file_action_combined_MC() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            card = new CreditCardData();
            card.setExpMonth(10);
            card.setExpYear(2025);
            card.setTokenizationData("5506740000004316");
            card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                    "F000019990E00003"));
            Transaction response = card.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        }

        @Test
        public void test_Visa_file_action_Combined() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            card = new CreditCardData();
            card.setExpMonth(10);
            card.setExpYear(2025);
            card.setTokenizationData("4012002000060016");
            card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("49AB0D7DF39F4EAA3ADEB107CCCC03D0",
                    "F000019990E00003"));
            Transaction response = card.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        }

        @Test
        public void test_Discover_file_action_combined() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            card = new CreditCardData();
            card.setExpMonth(10);
            card.setExpYear(2025);
            card.setTokenizationData("6011000990156527");
            //mc data
            card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                    "F000019990E00003"));
            Transaction response = card.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        }

        @Test
        public void test_Amex_file_action_Combined() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            card = new CreditCardData();
            card.setExpMonth(10);
            card.setExpYear(2025);
            card.setTokenizationData("372700699251018");
            //mc data
            card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                    "F000019990E00003"));
            Transaction response = card.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        }

        @Test
        public void test_file_action_combined_MC_swipe() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            track = new CreditTrackData();
            track.setExpiry("2512");
            track.setTrackNumber(TrackNumber.TrackOne);
            track.setTokenizationData("5473500000000014");
            track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A15B6FB3D21191BA5",
                    "F000019990E00003"));

            Transaction response = track.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        }

        @Test
        public void test_file_action_combined_MC_trackTwo() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            track = new CreditTrackData();
            track.setExpiry("2512");
            track.setTrackNumber(TrackNumber.TrackTwo);
            track.setTokenizationData("5473500000000014");
            track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                    "F000019990E00003"));
            Transaction response = track.fileAction()
                    .execute();
            assertNotNull(response);
            assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        }

        //Negative scenarios - Operation type - DeTokenize
        @Test
        public void test_file_action_combined_MC_incorrect_operation_type() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            track = new CreditTrackData();
            track.setExpiry("2512");
            track.setTrackNumber(TrackNumber.TrackTwo);
            track.setTokenizationData("5473500000000014");
            track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                    "F000019990E00003"));
            Transaction response = track.fileAction()
                    .execute();
            assertNotNull(response);
            //encryption error
            assertTrue(("952").matches(response.getResponseCode()));

        }

        //Missing tokenization data - Negative scenerio
        @Test
        public void test_file_action_combined_MC_() throws ApiException {
            acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
            acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
            acceptorConfig.setOperationType(OperationType.Decrypt);

            track = new CreditTrackData();
            track.setExpiry("2512");
            track.setTrackNumber(TrackNumber.TrackTwo);
            track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                    "F000019990E00003"));
            Transaction response = track.fileAction()
                    .execute();
            assertNotNull(response);
            assertTrue(("904").matches(response.getResponseCode()));

        }
    }
