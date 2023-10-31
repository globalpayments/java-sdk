package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.User;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.gpApi.entities.FundsAccountDetails;
import com.global.api.entities.gpApi.entities.UserAccount;
import com.global.api.entities.reporting.*;
import com.global.api.paymentMethods.AccountFunds;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.GpApiService;
import com.global.api.services.PayFacService;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiMerchantAccountsTest extends BaseGpApiTest {
    private List<MerchantAccountSummary> accounts;
    private PayFacService payFacService;
    private static GpApiConfig config;

    private static final Date startDate = DateUtils.addDays(new Date(), -365);
    private static final Date endDate = DateUtils.addDays(new Date(), -3);

    public GpApiMerchantAccountsTest() throws ApiException {
        config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @Before
    public void testInitialize() throws ApiException {
        payFacService = new PayFacService();

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        accounts = response.getResults().size() > 0 ? response.getResults() : null;
    }

    @Test
    public void FindAccountsInfo() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.ACTIVE, rs.getStatus());
        }
    }

    @Test
    public void FindAccountsInfo_SearchByStatusActive() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.ACTIVE, rs.getStatus());
        }
    }

    @Test
    public void FindAccountsInfo_SearchByStatusInactive() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.INACTIVE)
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.INACTIVE, rs.getStatus());
        }
    }

    @Test
    public void FindAccountsInfo_SearchByName() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.AccountName, "Sandbox FMA")
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.ACTIVE, rs.getStatus());
        }
    }

    @Test
    public void FindAccountsInfo_SearchById() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.ResourceId, "FMA_a07e67cdfdc641c4a5fe77a7f9f96cdd")
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.ACTIVE, rs.getStatus());
        }
    }

    @Test
    public void FindAccountsInfo_WithoutParameters() throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .execute();

        assertNotNull(response);

        for (MerchantAccountSummary rs : response.getResults()) {
            assertEquals(MerchantAccountStatus.ACTIVE.toString(), rs.getStatus().toString());
        }
    }

    @Test
    public void AccountDetails() throws ApiException {
        String accountId = accounts.size() > 0 ? accounts.get(0).getId() : null;

        MerchantAccountSummary response =
                ReportingService
                        .accountDetail(accountId)
                        .execute();

        assertEquals(accountId, response.getId());
    }

    @Test
    public void AccountDetails_RandomId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .accountDetail(UUID.randomUUID().toString())
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - Retrieve information about this transaction is not supported", ex.getMessage());
            assertEquals("INVALID_TRANSACTION_ACTION", ex.getResponseCode());
            assertEquals("40042", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AccountDetails_NullId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .accountDetail(null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Account details does not exist for null", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Ignore //can be tested only in Production
    @Test
    public void EditAccountInformation() throws ApiException {
        Address billingAddress = new Address();

        billingAddress.setStreetAddress1("123 Merchant Street");
        billingAddress.setStreetAddress2("Suite 2");
        billingAddress.setStreetAddress3("foyer");
        billingAddress.setCity("Beverly Hills");
        billingAddress.setState("CA");
        billingAddress.setPostalCode("90210");
        billingAddress.setCountryCode("US");

        CreditCardData creditCardInformation = cardInformation();

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchants.getResults().get(0).getId())
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        MerchantAccountSummary accountSummary = null;
        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == MerchantAccountType.FUND_MANAGEMENT) {
                accountSummary = el;
            }
        }

        boolean exceptionCaught = false;
        try {
            //It can only be tested in production environment
            payFacService
                    .editAccount()
                    .withAccountNumber(accountSummary.getId())
                    .withUserReference(merchant.getUserReference())
                    .withAddress(billingAddress, null)
                    .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 500 - Miscellaneous error", ex.getMessage());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50134", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AccountAddressLookup() throws ApiException {
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);

        Address address =
                new Address()
                        .setPostalCode("CB6 1AS")
                        .setStreetAddress1("2649")
                        .setStreetAddress2("Primrose");

        MerchantAccountSummary response =
                ReportingService
                        .accountDetail(accessTokenInfo.getMerchantManagementAccountID())
                        .withPaging(1, 10)
                        .where(SearchCriteria.Address, address)
                        .execute();

        assertTrue(response.getAddresses().size() > 0);
        assertEquals(address.getPostalCode(), response.getAddresses().get(0).getPostalCode());
    }

    @Test
    public void AccountAddressLookup_WithoutAddressLine1() throws ApiException {
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);

        Address address = new Address();
        address.setPostalCode("CB6 1AS");
        address.setStreetAddress2("Primrose");

        MerchantAccountSummary response =
                ReportingService
                        .accountDetail(accessTokenInfo.getMerchantManagementAccountID())
                        .withPaging(1, 10)
                        .where(SearchCriteria.Address, address)
                        .execute();

        assertTrue(response.getAddresses().size() > 0);
        assertEquals(address.getPostalCode(), response.getAddresses().get(0).getPostalCode());
    }

    @Test
    public void AccountAddressLookup_WithoutAddressLine2() throws ApiException {
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);

        Address address = new Address();
        address.setPostalCode("CB6 1AS");
        address.setStreetAddress1("2649");

        MerchantAccountSummary response =
                ReportingService
                        .accountDetail(accessTokenInfo.getMerchantManagementAccountID())
                        .withPaging(1, 10)
                        .where(SearchCriteria.Address, address)
                        .execute();

        assertTrue(response.getAddresses().size() > 0);
        assertEquals(address.getPostalCode(), response.getAddresses().get(0).getPostalCode());
    }

    @Test
    public void EditAccountInformation_WithoutCardDetails() throws ApiException {
        Address billingAddress = new Address();

        billingAddress.setStreetAddress1("123 Merchant Street");
        billingAddress.setStreetAddress2("Suite 2");
        billingAddress.setStreetAddress3("foyer");
        billingAddress.setCity("Beverly Hills");
        billingAddress.setState("CA");
        billingAddress.setPostalCode("90210");
        billingAddress.setCountryCode("US");

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchants.getResults().get(0).getId())
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        MerchantAccountSummary accountSummary = null;

        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == MerchantAccountType.FUND_MANAGEMENT) {
                accountSummary = el;
            }
        }

        assertNotNull(accountSummary);

        boolean exceptionCaught = false;
        try {
            payFacService
                    .editAccount()
                    .withAccountNumber(accountSummary.getId())
                    .withUserReference(merchant.getUserReference())
                    .withAddress(billingAddress, null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payer.payment_method.name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditAccountInformation_WithoutAddress() throws ApiException {
        CreditCardData creditCardInformation = cardInformation();

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchants.getResults().get(0).getId())
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        MerchantAccountSummary accountSummary = null;

        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == MerchantAccountType.FUND_MANAGEMENT) {
                accountSummary = el;
            }
        }

        assertNotNull(accountSummary);

        boolean exceptionCaught = false;
        try {
            payFacService
                    .editAccount()
                    .withAccountNumber(accountSummary.getId())
                    .withUserReference(merchant.getUserReference())
                    .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payer.billing_address.line_1", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditAccountInformation_WithoutCardHolderName() throws ApiException {
        CreditCardData creditCardInformation = cardInformation();
        creditCardInformation.setCardHolderName(null);

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchants.getResults().get(0).getId())
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        MerchantAccountSummary accountSummary = null;
        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == MerchantAccountType.FUND_MANAGEMENT) {
                accountSummary = el;
            }
        }

        assertNotNull(accountSummary);

        boolean exceptionCaught = false;
        try {
            payFacService
                    .editAccount()
                    .withAccountNumber(accountSummary.getId())
                    .withUserReference(merchant.getUserReference())
                    .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields payer.payment_method.name", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditAccountInformation_WithoutAccountId() throws ApiException {
        Address billingAddress = new Address();

        billingAddress.setStreetAddress1("123 Merchant Street");
        billingAddress.setStreetAddress2("Suite 2");
        billingAddress.setStreetAddress3("foyer");
        billingAddress.setCity("Beverly Hills");
        billingAddress.setState("CA");
        billingAddress.setPostalCode("90210");
        billingAddress.setCountryCode("US");

        CreditCardData creditCardInformation = cardInformation();

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        User merchant = User.fromId(merchants.getResults().get(0).getId(), UserType.MERCHANT);

        boolean exceptionCaught = false;
        try {
            new PayFacService()
                    .editAccount()
                    .withAddress(billingAddress, AddressType.Billing)
                    .withUserReference(merchant.getUserReference())
                    .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("accountNumber cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditAccountInformation_WithoutUserRef() throws ApiException {
        Address billingAddress =
                new Address()
                        .setStreetAddress1("123 Merchant Street")
                        .setStreetAddress2("Suite 2")
                        .setStreetAddress3("foyer")
                        .setCity("Beverly Hills")
                        .setState("CA")
                        .setPostalCode("90210")
                        .setCountryCode("US");

        CreditCardData creditCardInformation = cardInformation();

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getResults().size() > 0);

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchants.getResults().get(0).getId())
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        MerchantAccountSummary accountSummary = null;
        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == MerchantAccountType.FUND_MANAGEMENT) {
                accountSummary = el;
            }
        }

        boolean exceptionCaught = false;
        try {
            new PayFacService()
                    .editAccount()
                    .withAccountNumber(accountSummary.getId())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withCreditCardData(creditCardInformation, PaymentMethodFunction.PRIMARY_PAYOUT)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - Retrieve information about this transaction is not supported", ex.getMessage());
            assertEquals("INVALID_TRANSACTION_ACTION", ex.getResponseCode());
            assertEquals("40042", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AccountAddressLookup_MissingPostalCode() throws ApiException {
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);

        Address address =
                new Address()
                        .setStreetAddress1("2649")
                        .setStreetAddress2("Primrose");

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .accountDetail(accessTokenInfo.getMerchantManagementAccountID())
                    .withPaging(1, 10)
                    .where(SearchCriteria.Address, address)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields:postal_code.", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40251", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AccountAddressLookup_MissingAddressLine() throws GatewayException {
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);
        Address address =
                new Address()
                        .setPostalCode("CB6 1AS");

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .accountDetail(accessTokenInfo.getMerchantManagementAccountID())
                    .withPaging(1, 10)
                    .where(SearchCriteria.Address, address)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields:line_1 or line_2.", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40251", ex.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    //region Transfer and Split
    @Test
    public void TransferFunds() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setAccountName(accountSender.getName());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        Transaction transfer = funds
                .transfer(new BigDecimal("10"))
                .withClientTransactionId("")
                .withDescription(description)
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("10"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage().toUpperCase()); // TODO why it is CAPTURED in dotnet?
        assertEquals(SUCCESS, transfer.getResponseCode().toUpperCase());
    }

    @Test
    public void TransferFunds_OnlyMandatoryFields() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());

        Transaction transfer = funds
                .transfer(new BigDecimal("10"))
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("10"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage().toUpperCase());
        assertEquals(SUCCESS, transfer.getResponseCode().toUpperCase());
    }

    @Test
    public void TransferFunds_WithIdempotency() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());

        String idempotencyKey = UUID.randomUUID().toString();
        Transaction transfer = funds
                .transfer(new BigDecimal("10"))
                .withIdempotencyKey(idempotencyKey)
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("10"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage().toUpperCase());
        assertEquals(SUCCESS, transfer.getResponseCode().toUpperCase());

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("10"))
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transfer.getTransactionId() + ", status=SUCCESS", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithoutSenderAccountName() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        Transaction transfer = funds
                .transfer(new BigDecimal("0.01"))
                .withClientTransactionId("")
                .withDescription(description)
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("0.01"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage().toUpperCase());
        assertEquals(SUCCESS, transfer.getResponseCode().toUpperCase());
    }

    @Test
    public void TransferFunds_WithoutUsableBalanceMode() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setAccountName(accountSender.getName());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        Transaction transfer = funds
                .transfer(new BigDecimal("0.01"))
                .withClientTransactionId("")
                .withDescription(description)
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("0.01"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage());
        assertEquals(SUCCESS, transfer.getResponseCode());
    }
    //endregion

    //region Transfer error scenarios

    @Test
    public void TransferFunds_WithoutSenderAccountId() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountName(accountSender.getName());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        Transaction transfer = funds
                .transfer(new BigDecimal("0.01"))
                .withClientTransactionId("")
                .withDescription(description)
                .execute();

        assertNotNull(transfer.getTransactionId());
        assertEquals(new BigDecimal("0.01"), transfer.getBalanceAmount());
        assertEquals(SUCCESS, transfer.getResponseMessage().toUpperCase());
        assertEquals(SUCCESS, transfer.getResponseCode().toUpperCase());
    }

    @Test
    public void TransferFunds_WithoutRecipientAccountId() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setAccountName(accountSender.getName());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .withClientTransactionId("")
                    .withDescription(description)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields recipient_account_id", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithoutSenderAccountIdAndAccountName() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .withClientTransactionId("")
                    .withDescription(description)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following conditionally mandatory fields account_id, account_name.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40007", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithoutMerchantId() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setAccountName(accountSender.getName());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        String description = UUID.randomUUID().toString().replace(".", "").substring(0, 11);

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .withClientTransactionId("")
                    .withDescription(description)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 403 - Access token and merchant info do not match", ex.getMessage());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("40003", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithoutAmount() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following fields amount", ex.getMessage());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithRandomSenderAccountId() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantSummary merchantRecipient = null;
        for (MerchantSummary el : merchants) {
            if (!el.getId().equals(merchantSender.getId())) {
                merchantRecipient = el;
                break;
            }
        }

        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);
        MerchantAccountSummary accountRecipient = GetAccountByType(merchantRecipient.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(UUID.randomUUID().toString());
        funds.setAccountName(accountSender.getName());
        funds.setRecipientAccountId(accountRecipient.getId());
        funds.setMerchantId(merchantSender.getId());
        funds.setUsableBalanceMode(UsableBalanceMode.AVAILABLE_AND_PENDING_BALANCE);

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertTrue(ex.getMessage().contains("400 - Merchant configuration does not exist for the following combination: merchant_management_account_id -"));
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithRandomRecipientAccountId() throws ApiException {
        List<MerchantSummary> merchants = GetMerchants();

        MerchantSummary merchantSender = merchants.get(0);
        MerchantAccountSummary accountSender = GetAccountByType(merchantSender.getId(), MerchantAccountType.FUND_MANAGEMENT);

        AccountFunds funds = new AccountFunds();
        funds.setAccountId(accountSender.getId());
        funds.setRecipientAccountId(UUID.randomUUID().toString());
        funds.setMerchantId(merchantSender.getId());

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Transfers may only be initiated between accounts under the same partner program", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40041", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void TransferFunds_WithRandomMerchantId() throws ApiException {
        AccountFunds funds = new AccountFunds();
        funds.setAccountId(UUID.randomUUID().toString());
        funds.setRecipientAccountId(UUID.randomUUID().toString());
        funds.setMerchantId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            funds
                    .transfer(new BigDecimal("0.01"))
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - Retrieve information about this transaction is not supported", ex.getMessage());
            assertEquals("INVALID_TRANSACTION_ACTION", ex.getResponseCode());
            assertEquals("40042", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AddFunds() throws ApiException {
        String amount = "10";
        String currency = "USD";
        String accountId = "FMA_a78b841dfbd14803b3a31e4e0c514c72";
        String merchantId = "MER_5096d6b88b0b49019c870392bd98ddac";
        User merchant = User.fromId(merchantId, UserType.MERCHANT);

        User response = merchant
                .addFunds()
                .withAmount(amount)
                .withAccountNumber(accountId)
                .withPaymentMethodName(PaymentMethodName.BankTransfer)
                .withPaymentMethodType(PaymentMethodType.Credit)
                .withCurrency(currency)
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        FundsAccountDetails fundsAccountDetails = response.getFundsAccountDetails();
        assertNotNull(fundsAccountDetails);
        assertEquals(FundsStatus.CAPTURED.toString(), fundsAccountDetails.getStatus());
        assertEquals(amount, fundsAccountDetails.getAmount());
        assertEquals(currency, fundsAccountDetails.getCurrency());
        assertEquals("CREDIT", fundsAccountDetails.getPaymentMethodType());
        assertEquals("BANK_TRANSFER", fundsAccountDetails.getPaymentMethodName());
        UserAccount userAccount = fundsAccountDetails.getAccount();
        assertNotNull(userAccount);
        assertEquals(accountId, userAccount.getId());
    }

    @Test
    public void AddFunds_OnlyMandatory() throws ApiException {
        String amount = "10";
        String accountId = "FMA_a78b841dfbd14803b3a31e4e0c514c72";
        String merchantId = "MER_5096d6b88b0b49019c870392bd98ddac";
        User merchant = User.fromId(merchantId, UserType.MERCHANT);

        User response = merchant
                .addFunds()
                .withAmount(amount)
                .withAccountNumber(accountId)
                .withPaymentMethodType(PaymentMethodType.Credit)
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        FundsAccountDetails fundsAccountDetails = response.getFundsAccountDetails();
        assertNotNull(fundsAccountDetails);
        assertEquals(FundsStatus.CAPTURED.toString(), fundsAccountDetails.getStatus());
        assertEquals(amount, fundsAccountDetails.getAmount());
        assertEquals("CREDIT", fundsAccountDetails.getPaymentMethodType());
        assertEquals("BANK_TRANSFER", fundsAccountDetails.getPaymentMethodName());
        UserAccount userAccount = fundsAccountDetails.getAccount();
        assertNotNull(userAccount);
        assertEquals(accountId, userAccount.getId());
    }

    @Test
    public void AddFunds_InsufficientFunds() throws ApiException {

        String amount = "10";
        String accountId = "FMA_a78b841dfbd14803b3a31e4e0c514c72";
        String merchantId = "MER_5096d6b88b0b49019c870392bd98ddac";
        User merchant = User.fromId(merchantId, UserType.MERCHANT);

        User response = merchant
                .addFunds()
                .withAmount(amount)
                .withAccountNumber(accountId)
                .execute();

        assertNotNull(response);
        assertEquals("DECLINED", response.getResponseCode());
        FundsAccountDetails fundsAccountDetails = response.getFundsAccountDetails();
        assertNotNull(fundsAccountDetails);
        assertEquals(FundsStatus.DECLINE.toString(), fundsAccountDetails.getStatus());
        assertEquals(amount, fundsAccountDetails.getAmount());
        assertEquals("DEBIT", fundsAccountDetails.getPaymentMethodType());
        assertEquals("BANK_TRANSFER", fundsAccountDetails.getPaymentMethodName());
        UserAccount userAccount = fundsAccountDetails.getAccount();
        assertNotNull(userAccount);
        assertEquals(accountId, userAccount.getId());
    }

    @Test
    public void AddFunds_WithoutAmount() throws ApiException {
        String currency = "USD";
        String accountId = "FMA_a78b841dfbd14803b3a31e4e0c514c72";
        String merchantId = "MER_5096d6b88b0b49019c870392bd98ddac";
        User merchant = User.fromId(merchantId, UserType.MERCHANT);

        boolean errorFound = false;
        try {
            User response = merchant
                    .addFunds()
                    .withAccountNumber(accountId)
                    .withPaymentMethodName(PaymentMethodName.BankTransfer)
                    .withPaymentMethodType(PaymentMethodType.Credit)
                    .withCurrency(currency)
                    .execute();
        } catch (BuilderException exception) {
            errorFound = true;
            assertEquals("amount cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void AddFunds_WithoutAccountNumber() throws ApiException {
        String amount = "10";
        String currency = "USD";
        String merchantId = "MER_5096d6b88b0b49019c870392bd98ddac";
        User merchant = User.fromId(merchantId, UserType.MERCHANT);

        boolean errorFound = false;
        try {
            User response = merchant
                    .addFunds()
                    .withAmount(amount)
                    .withPaymentMethodName(PaymentMethodName.BankTransfer)
                    .withPaymentMethodType(PaymentMethodType.Credit)
                    .withCurrency(currency)
                    .execute();
        } catch (BuilderException exception) {
            errorFound = true;
            assertEquals("accountNumber cannot be null for this transaction type.", exception.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void AddFunds_WithoutUserRef() throws ApiException {
        String amount = "10";
        String currency = "USD";
        String accountId = "FMA_a78b841dfbd14803b3a31e4e0c514c72";

        boolean errorFound = false;

        try {
            User response = new User()
                    .addFunds()
                    .withAmount(amount)
                    .withAccountNumber(accountId)
                    .withPaymentMethodName(PaymentMethodName.BankTransfer)
                    .withPaymentMethodType(PaymentMethodType.Credit)
                    .withCurrency(currency)
                    .execute();
            response.addFunds();
        } catch (NullPointerException exception) {
            errorFound = true;
        } finally {
            assertTrue(errorFound);
        }
    }
    //endregion

    private CreditCardData cardInformation() {
        CreditCardData creditCardData = new CreditCardData();

        creditCardData.setNumber("4263970000005262");
        creditCardData.setExpMonth(expMonth);
        creditCardData.setExpYear(expYear);
        creditCardData.setCvn("123");
        creditCardData.setCardPresent(true);
        creditCardData.setCardHolderName("Jason Mason");

        return creditCardData;
    }

    private List<MerchantSummary> GetMerchants() throws ApiException {
        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        return merchants.getResults();
    }

    private MerchantAccountSummary GetAccountByType(String merchantSenderId, MerchantAccountType merchantAccountType) throws ApiException {
        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchantSenderId)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        for (MerchantAccountSummary el : response.getResults()) {
            if (el.getType() == merchantAccountType) {
                return el;
            }
        }

        return null;
    }

}